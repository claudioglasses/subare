// code by fluric
package ch.ethz.idsc.subare.core.td;

import ch.ethz.idsc.subare.core.LearningRate;
import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.QsaInterface;
import ch.ethz.idsc.subare.core.StepInterface;
import ch.ethz.idsc.subare.core.util.FeatureMapper;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Max;

/** class is instantiated via {@link SarsaType} */
/* package */ class QLearningTrueOnlineSarsa extends TrueOnlineSarsa {
  QLearningTrueOnlineSarsa( //
      MonteCarloInterface monteCarloInterface, Scalar lambda, LearningRate learningRate, FeatureMapper featureMapper, Tensor w) {
    super(monteCarloInterface, lambda, learningRate, featureMapper, w);
  }

  @Override // from TrueOnlineSarsa
  protected Scalar evaluate(StepInterface stepInterface) {
    Tensor nextState = stepInterface.nextState();
    QsaInterface qsaInterface = qsaInterface();
    return monteCarloInterface.actions(nextState).stream() //
        .filter(action -> learningRate.encountered(nextState, action)) //
        .map(action -> qsaInterface.value(nextState, action)) //
        .reduce(Max::of) //
        .orElse(RealScalar.ZERO);
  }
}
