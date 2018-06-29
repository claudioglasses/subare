// code by fluric
package ch.ethz.idsc.subare.core.td;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.FeatureMapper;
import ch.ethz.idsc.subare.core.util.StateActionMapper;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.red.Times;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Sign;

/** implementation of box "True Online Sarsa(lambda) for estimating w'x approx. q_pi or q_*
 * 
 * in Section 12.8, p.309 */
public class TrueOnlineSarsa {
  private final Random rand = new Random();
  private final MonteCarloInterface monteCarloInterface;
  private final Scalar alpha;
  private final Scalar gamma;
  private final FeatureMapper mapper;
  // ---
  private final Scalar gamma_lambda;
  private final Scalar alpha_gamma_lambda;
  private final int featureSize;
  // private final int dimState;
  // private final int dimAction;
  // ---
  private Scalar qOld = RealScalar.ZERO;
  private Scalar q;
  private Scalar q_prime;
  private Tensor x;
  private Tensor x_prime;
  private Tensor w;
  private Tensor z;
  private Scalar delta;

  /** @param monteCarloInterface
   * @param lambda in [0, 1]
   * @param alpha positive
   * @param gamma
   * @param mapper
   * @throws Exception if any parameter is outside valid range */
  public TrueOnlineSarsa(MonteCarloInterface monteCarloInterface, Scalar lambda, Scalar alpha, Scalar gamma, FeatureMapper mapper) {
    this.monteCarloInterface = monteCarloInterface;
    Clip.unit().requireInside(lambda);
    this.alpha = Sign.requirePositive(alpha);
    this.gamma = gamma;
    this.mapper = mapper;
    gamma_lambda = Times.of(gamma, lambda);
    alpha_gamma_lambda = Times.of(alpha, gamma, lambda);
    // dimState = mcInterface.states().get(0).length();
    // dimAction = mcInterface.actions(mcInterface.states().get(0)).get(0).length();
    featureSize = mapper.getFeatureSize();
    z = Array.zeros(featureSize);
    w = Array.zeros(featureSize); // remark: other initialization choices are possible
  }

  private void update(Scalar reward, Tensor s_prime, Tensor a_prime) {
    Tensor stateActionPair = StateActionMapper.getMap(s_prime, a_prime);
    x_prime = mapper.getFeature(stateActionPair);
    q = w.dot(x).Get();
    q_prime = w.dot(x_prime).Get();
    delta = reward.add(gamma.multiply(q_prime)).subtract(q);
    z = z.multiply(gamma_lambda).add(x.multiply(RealScalar.ONE.subtract(alpha_gamma_lambda.multiply(z.dot(x).Get()))));
    // ---
    Scalar q_qOld = q.subtract(qOld);
    Tensor scalez = z.multiply(alpha.multiply(delta.add(q_qOld)));
    Tensor scalex = x.multiply(alpha.multiply(q_qOld));
    w = w.add(scalez).subtract(scalex);
    qOld = q_prime;
    x = x_prime;
  }

  private Tensor getEGreedyAction(Tensor state, Scalar epsilon) {
    if (rand.nextFloat() > epsilon.number().doubleValue()) {
      return getGreedyAction(state);
    }
    int index = rand.nextInt(monteCarloInterface.actions(state).length());
    return monteCarloInterface.actions(state).get(index);
  }

  private Tensor getGreedyAction(Tensor state) {
    double max = Double.NEGATIVE_INFINITY;
    List<Tensor> bestActions = new ArrayList<>();
    for (Tensor action : monteCarloInterface.actions(state)) {
      Tensor stateActionPair = StateActionMapper.getMap(state, action);
      double current = mapper.getFeature(stateActionPair).dot(w).Get().number().doubleValue();
      if (current > max) {
        bestActions.add(action);
        max = current;
      }
    }
    int index = rand.nextInt(bestActions.size());
    return bestActions.get(index);
  }

  public void executeEpisode(Scalar epsilon) {
    // getting random index for startState
    int index = rand.nextInt(monteCarloInterface.startStates().length());
    Tensor state = monteCarloInterface.startStates().get(index);
    Tensor stateOld;
    Tensor action = getEGreedyAction(state, epsilon);
    Tensor actionOld;
    Scalar reward;
    // init every episode again
    Tensor stateActionPair = StateActionMapper.getMap(state, action);
    x = mapper.getFeature(stateActionPair);
    qOld = RealScalar.ZERO;
    z = Array.zeros(featureSize);
    // run through episode
    while (!monteCarloInterface.isTerminal(state)) {
      stateOld = state;
      actionOld = action;
      state = monteCarloInterface.move(stateOld, actionOld);
      reward = monteCarloInterface.reward(stateOld, actionOld, state);
      // System.out.println("from state " + stateOld + " to " + state + " with action " + actionOld + " reward: " + reward);
      action = getEGreedyAction(state, epsilon);
      update(reward, state, action);
    }
  }

  public void printValues() {
    System.out.println("Values for all state-action pairs:");
    for (Tensor state : monteCarloInterface.states()) {
      for (Tensor action : monteCarloInterface.actions(state)) {
        System.out.println(state + " -> " + action + " " + mapper.getFeature(Join.of(state, action)).dot(w));
      }
    }
  }

  /** Returns the Qsa according to the current feature weights.
   * Only use this function, when the state-action space is small enough. */
  public DiscreteQsa getQsa() {
    DiscreteQsa qsa = DiscreteQsa.build(monteCarloInterface);
    for (Tensor state : monteCarloInterface.states()) {
      for (Tensor action : monteCarloInterface.actions(state)) {
        Tensor stateActionPair = StateActionMapper.getMap(state, action);
        qsa.assign(state, action, mapper.getFeature(stateActionPair).dot(w).Get());
      }
    }
    return qsa;
  }

  public void printPolicy() {
    System.out.println("Greedy action to each state");
    for (Tensor state : monteCarloInterface.states()) {
      System.out.println(state + " -> " + getGreedyAction(state));
    }
  }

  public Tensor getW() {
    return w;
  }
}
