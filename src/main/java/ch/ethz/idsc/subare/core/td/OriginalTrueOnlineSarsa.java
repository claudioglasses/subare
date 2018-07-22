// code by fluric
package ch.ethz.idsc.subare.core.td;

import ch.ethz.idsc.subare.core.LearningRate;
import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.Policy;
import ch.ethz.idsc.subare.core.util.EGreedyPolicy;
import ch.ethz.idsc.subare.core.util.FeatureMapper;
import ch.ethz.idsc.subare.core.util.PolicyWrap;
import ch.ethz.idsc.subare.core.util.StateAction;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** class is instantiated via {@link SarsaType} */
/* package */ class OriginalTrueOnlineSarsa extends TrueOnlineSarsa {
  OriginalTrueOnlineSarsa( //
      MonteCarloInterface monteCarloInterface, Scalar lambda, LearningRate learningRate, FeatureMapper featureMapper, Tensor w) {
    super(monteCarloInterface, lambda, learningRate, featureMapper, w);
  }

  @Override // from AbstractSharedTrueOnlineSarsa
  Scalar evaluate(Tensor state, Tensor actions) {
    Policy policy = EGreedyPolicy.bestEquiprobable(monteCarloInterface, qsaInterface(), epsilon, state);
    Tensor nextAction = new PolicyWrap(policy).next(state, actions);
    Tensor nextX = featureMapper.getFeature(StateAction.key(state, nextAction));
    return w.dot(nextX).Get();
  }
}
