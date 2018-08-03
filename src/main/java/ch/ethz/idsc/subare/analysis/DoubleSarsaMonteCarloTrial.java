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
import ch.ethz.idsc.subare.core.td.DoubleSarsa;
import ch.ethz.idsc.subare.core.td.SarsaType;
import ch.ethz.idsc.subare.core.util.ConstantLearningRate;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.ExploringStarts;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

public class DoubleSarsaMonteCarloTrial implements MonteCarloTrial {
  private static final Scalar ALPHA = RealScalar.of(0.05);
  private static final Scalar EPSILON = RealScalar.of(0.1);
  private static final int DIGEST_DEPTH = 1; // 0 is equal to the MonteCarlo approach
  // ---
  private final MonteCarloInterface monteCarloInterface;
  private final DoubleSarsa doubleSarsa;
  private final Deque<StepInterface> deque = new ArrayDeque<>();

  public DoubleSarsaMonteCarloTrial(MonteCarloInterface monteCarloInterface, SarsaType sarsaType, LearningRate learningRate1_, LearningRate learningRate2_,
      DiscreteQsa qsa1_, DiscreteQsa qsa2_) {
    this.monteCarloInterface = monteCarloInterface;
    DiscreteQsa qsa1 = Objects.isNull(qsa1_) ? DiscreteQsa.build(monteCarloInterface) : qsa1_;
    DiscreteQsa qsa2 = Objects.isNull(qsa2_) ? DiscreteQsa.build(monteCarloInterface) : qsa2_;
    LearningRate learningRate1 = Objects.isNull(learningRate1_) ? ConstantLearningRate.of(ALPHA) : learningRate1_;
    LearningRate learningRate2 = Objects.isNull(learningRate2_) ? ConstantLearningRate.of(ALPHA) : learningRate2_;
    doubleSarsa = sarsaType.doubleSarsa(monteCarloInterface, //
        learningRate1, learningRate2, //
        qsa1, qsa2);
    doubleSarsa.setExplore(EPSILON);
  }

  public DoubleSarsaMonteCarloTrial(MonteCarloInterface monteCarloInterface, SarsaType sarsaType) {
    this(monteCarloInterface, sarsaType, null, null, null, null);
  }

  @Override // from MonteCarloTrial
  public void executeBatch() {
    Policy policy = doubleSarsa.getEGreedy();
    ExploringStarts.batch(monteCarloInterface, policy, DIGEST_DEPTH, doubleSarsa);
  }

  @Override // from MonteCarloTrial
  public DiscreteQsa qsa() {
    return doubleSarsa.qsa();
  }

  @Override // from MonteCarloTrial
  public void digest(StepInterface stepInterface) {
    deque.add(stepInterface);
    if (!monteCarloInterface.isTerminal(stepInterface.nextState())) {
      if (deque.size() == DIGEST_DEPTH) { // never true, if nstep == 0
        doubleSarsa.digest(deque);
        deque.poll();
      }
    } else {
      while (!deque.isEmpty()) {
        doubleSarsa.digest(deque);
        deque.poll();
      }
    }
  }

  @Override // from MonteCarloTrial
  public QsaInterface qsaInterface() {
    return qsa();
  }
}
