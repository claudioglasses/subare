// code by fluric
package ch.ethz.idsc.subare.analysis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import ch.ethz.idsc.subare.core.LearningRate;
import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.Policy;
import ch.ethz.idsc.subare.core.QsaInterface;
import ch.ethz.idsc.subare.core.StepInterface;
import ch.ethz.idsc.subare.core.td.Sarsa;
import ch.ethz.idsc.subare.core.td.SarsaType;
import ch.ethz.idsc.subare.core.util.ConstantLearningRate;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.EGreedyPolicy;
import ch.ethz.idsc.subare.core.util.ExploringStarts;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.sca.Sign;

/** uses 1-step digest */
public class SarsaMonteCarloTrial implements MonteCarloTrial {
  private static final Scalar ALPHA = RealScalar.of(0.05);
  private static final Scalar EPSILON = RealScalar.of(0.1);
  // ---
  private final MonteCarloInterface monteCarloInterface;
  private final Sarsa sarsa;
  private final Deque<StepInterface> deque = new ArrayDeque<>();
  private final int digestDepth; // 0 is equal to the MonteCarlo approach

  public SarsaMonteCarloTrial(MonteCarloInterface monteCarloInterface, SarsaType sarsaType, LearningRate learningRate_, DiscreteQsa qsa_) {
    this(monteCarloInterface, sarsaType, learningRate_, qsa_, 1);
  }

  public SarsaMonteCarloTrial(MonteCarloInterface monteCarloInterface, SarsaType sarsaType, LearningRate learningRate_, DiscreteQsa qsa_, int digestDepth_) {
    this.monteCarloInterface = monteCarloInterface;
    DiscreteQsa qsa = Objects.isNull(qsa_) ? DiscreteQsa.build(monteCarloInterface) : qsa_;
    LearningRate learningRate = Objects.isNull(learningRate_) ? ConstantLearningRate.of(ALPHA) : learningRate_;
    sarsa = sarsaType.supply(monteCarloInterface, learningRate, qsa);
    sarsa.setExplore(EPSILON);
    digestDepth = digestDepth_;
    Sign.requirePositiveOrZero(RealScalar.of(digestDepth_));
  }

  public SarsaMonteCarloTrial(MonteCarloInterface monteCarloInterface, SarsaType sarsaType) {
    this(monteCarloInterface, sarsaType, null, null);
  }

  @Override // from MonteCarloTrial
  public void executeBatch() {
    Policy policy = EGreedyPolicy.bestEquiprobable(monteCarloInterface, sarsa.qsa(), EPSILON);
    ExploringStarts.batch(monteCarloInterface, policy, digestDepth, sarsa);
  }

  @Override // from MonteCarloTrial
  public DiscreteQsa qsa() {
    return sarsa.qsa();
  }

  @Override // from MonteCarloTrial
  public void digest(StepInterface stepInterface) {
    deque.add(stepInterface);
    if (!monteCarloInterface.isTerminal(stepInterface.nextState())) {
      if (deque.size() == digestDepth) { // never true, if nstep == 0
        sarsa.digest(deque);
        deque.poll();
      }
    } else {
      while (!deque.isEmpty()) {
        sarsa.digest(deque);
        deque.poll();
      }
    }
  }

  public int getDequeueSize() {
    return deque.size();
  }

  @Override // from MonteCarloTrial
  public QsaInterface qsaInterface() {
    return qsa();
  }
}
