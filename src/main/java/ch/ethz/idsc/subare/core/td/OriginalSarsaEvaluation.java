// code by jph and fluric
package ch.ethz.idsc.subare.core.td;

import ch.ethz.idsc.subare.core.DiscreteModel;
import ch.ethz.idsc.subare.core.util.PolicyBase;
import ch.ethz.idsc.subare.core.util.PolicyWrap;
import ch.ethz.idsc.subare.core.util.StateAction;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/* package */ class OriginalSarsaEvaluation extends AbstractSarsaEvaluation {
  public OriginalSarsaEvaluation(DiscreteModel discreteModel) {
    super(discreteModel);
  }

  @Override
  public Scalar crossEvaluate(Tensor state, PolicyBase policy1, PolicyBase policy2) {
    Tensor actions = Tensor.of(discreteModel.actions(state).stream(). //
        filter(action -> policy1.sac().isEncountered(StateAction.key(state, action))));
    if (actions.length() == 0)
      return RealScalar.ZERO;
    Tensor action = new PolicyWrap(policy1).next(state, actions);
    return policy2.qsaInterface().value(state, action);
  }
}
