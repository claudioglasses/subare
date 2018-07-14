// code by fluric
package ch.ethz.idsc.subare.core.td;

import java.util.Random;

import ch.ethz.idsc.subare.core.DiscreteQsaSupplier;
import ch.ethz.idsc.subare.core.LearningRate;
import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.StepInterface;
import ch.ethz.idsc.subare.core.adapter.StepAdapter;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.FeatureMapper;
import ch.ethz.idsc.subare.core.util.StateAction;
import ch.ethz.idsc.subare.util.RobustArgMax;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Times;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Clip;

/** implementation of box "True Online Sarsa(lambda) for estimating w'x approx. q_pi or q_*
 * 
 * in Section 12.8, p.309 */
public class TrueOnlineSarsa implements DiscreteQsaSupplier {
  private static final RobustArgMax ROBUST_ARG_MAX = new RobustArgMax(Chop._08);

  public static TrueOnlineSarsa of( //
      MonteCarloInterface monteCarloInterface, Scalar lambda, LearningRate learningRate, FeatureMapper featureMapper, Scalar init) {
    return new TrueOnlineSarsa(monteCarloInterface, lambda, learningRate, featureMapper, init);
  }

  public static TrueOnlineSarsa of( //
      MonteCarloInterface monteCarloInterface, Scalar lambda, LearningRate learningRate, FeatureMapper featureMapper) {
    return of(monteCarloInterface, lambda, learningRate, featureMapper, RealScalar.ZERO);
  }

  // ---
  private final Random random = new Random();
  private final MonteCarloInterface monteCarloInterface;
  private final Scalar gamma;
  private final FeatureMapper featureMapper;
  private final LearningRate learningRate;
  // ---
  private final Scalar gamma_lambda;
  private final int featureSize;
  // ---
  private Scalar qOld = RealScalar.ZERO;
  private Tensor x;
  /** weight vector w is a long-term memory, accumulating over the lifetime of the system */
  private Tensor w;
  /** eligibility trace z is a short-term memory, typically lasting less time than the length of an episode */
  private Tensor z;

  /** @param monteCarloInterface
   * @param lambda in [0, 1] Figure 12.14 in the book suggests that lambda in [0.8, 0.9]
   * tends to be a good choice
   * @param learningRate
   * @param featureMapper
   * @param init */
  private TrueOnlineSarsa(MonteCarloInterface monteCarloInterface, Scalar lambda, LearningRate learningRate, FeatureMapper featureMapper, Scalar init) {
    this.monteCarloInterface = monteCarloInterface;
    this.learningRate = learningRate;
    Clip.unit().requireInside(lambda);
    this.gamma = monteCarloInterface.gamma();
    this.featureMapper = featureMapper;
    gamma_lambda = Times.of(gamma, lambda);
    featureSize = featureMapper.featureSize();
    w = Tensors.vector(v -> init, featureSize);
  }

  /** With probability epsilon a random action is chosen. In the other case the best
   * (greedy) action is taken with equal probability when several best actions.
   * 
   * @param state
   * @param epsilon
   * @return epsilon greedy action */
  private Tensor getEGreedyAction(Tensor state, Scalar epsilon) {
    Tensor actions = monteCarloInterface.actions(state);
    if (random.nextFloat() > epsilon.number().doubleValue())
      actions = getGreedyActions(state);
    else
      z = Array.zeros(featureSize); // delete eligibility trace when random action taken
    int index = random.nextInt(actions.length());
    return actions.get(index);
  }

  /** Returns the best action according to the current state-action values. In case
   * of several best actions within a tolerance, all the best actions are returned.
   * 
   * @param state
   * @return */
  private Tensor getGreedyActions(Tensor state) {
    Tensor actions = Tensor.of(monteCarloInterface.actions(state).stream() //
        .filter(action -> learningRate.encountered(state, action)));
    if (actions.length() == 0) // if this state was not visited yet, choose randomly
      return monteCarloInterface.actions(state);
    Tensor vector = Tensor.of(actions.stream() //
        .map(action -> featureMapper.getFeature(StateAction.key(state, action)).dot(w).Get()));
    return Tensor.of(ROBUST_ARG_MAX.options(vector).mapToObj(actions::get));
  }

  /** @param epsilon greedy
   * @param state where episode starts
   * @param action taken from given state */
  public void executeEpisode(Scalar epsilon, Tensor state, Tensor action) {
    // init every episode again
    Clip.unit().requireInside(epsilon);
    Tensor stateActionPair = StateAction.key(state, action);
    x = featureMapper.getFeature(stateActionPair);
    qOld = RealScalar.ZERO;
    /** eligibility trace vector is initialized to zero at the beginning of the episode */
    z = Array.zeros(featureSize);
    // run through episode
    while (!monteCarloInterface.isTerminal(state)) {
      Tensor stateOld = state;
      Tensor actionOld = action;
      state = monteCarloInterface.move(stateOld, actionOld);
      Scalar reward = monteCarloInterface.reward(stateOld, actionOld, state);
      action = getEGreedyAction(state, epsilon);
      StepInterface stepInterface = new StepAdapter(stateOld, actionOld, reward, state);
      Scalar alpha = learningRate.alpha(stepInterface);
      learningRate.digest(stepInterface);
      update(reward, state, action, alpha);
    }
  }

  private void update(Scalar reward, Tensor s_next, Tensor a_next, Scalar alpha) {
    Tensor stateActionPair = StateAction.key(s_next, a_next);
    Scalar alpha_gamma_lambda = Times.of(alpha, gamma_lambda);
    Tensor x_prime = featureMapper.getFeature(stateActionPair);
    Scalar q = w.dot(x).Get();
    Scalar q_prime = w.dot(x_prime).Get();
    Scalar delta = reward.add(gamma.multiply(q_prime)).subtract(q);
    // eq (12.11)
    z = z.multiply(gamma_lambda) //
        .add(x.multiply(RealScalar.ONE.subtract(alpha_gamma_lambda.multiply(z.dot(x).Get()))));
    // ---
    Scalar q_qOld = q.subtract(qOld);
    Tensor scalez = z.multiply(alpha.multiply(delta.add(q_qOld)));
    Tensor scalex = x.multiply(alpha.multiply(q_qOld));
    w = w.add(scalez).subtract(scalex);
    qOld = q_prime;
    x = x_prime;
  }

  public void printValues() {
    System.out.println("Values for all state-action pairs:");
    for (Tensor state : monteCarloInterface.states())
      for (Tensor action : monteCarloInterface.actions(state)) {
        Tensor stateActionPair = StateAction.key(state, action);
        System.out.println(state + " -> " + action + " " + featureMapper.getFeature(stateActionPair).dot(w));
      }
  }

  /** Returns the Qsa according to the current feature weights.
   * Only use this function, when the state-action space is small enough. */
  @Override // from DiscreteQsaSupplier
  public DiscreteQsa qsa() {
    DiscreteQsa qsa = DiscreteQsa.build(monteCarloInterface);
    for (Tensor state : monteCarloInterface.states())
      for (Tensor action : monteCarloInterface.actions(state)) {
        Tensor stateActionPair = StateAction.key(state, action);
        qsa.assign(state, action, featureMapper.getFeature(stateActionPair).dot(w).Get());
      }
    return qsa;
  }

  public void printPolicy() {
    System.out.println("Greedy action to each state");
    for (Tensor state : monteCarloInterface.states())
      System.out.println(state + " -> " + getGreedyActions(state));
  }

  public Tensor getW() {
    return w.unmodifiable();
  }
}
