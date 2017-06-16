// code by jph
package ch.ethz.idsc.subare.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.ethz.idsc.subare.core.DiscreteModel;
import ch.ethz.idsc.subare.core.Policy;
import ch.ethz.idsc.subare.util.Index;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** the term "equiprobable" appears in Exercise 4.1 */
public class EquiprobablePolicy implements Policy {
  private final DiscreteModel discreteModel;
  private final Map<Tensor, Scalar> map = new ConcurrentHashMap<>();

  public EquiprobablePolicy(DiscreteModel discreteModel) {
    this.discreteModel = discreteModel;
  }

  // TODO concurrent hash map may be unnecessary if policy would be synchronized -> test!
  @Override
  public Scalar probability(Tensor state, Tensor action) {
    if (!map.containsKey(state)) {
      Tensor actions = discreteModel.actions(state);
      Index actionIndex = Index.build(actions);
      if (!actionIndex.containsKey(action))
        throw new RuntimeException("action invalid " + action);
      int den = actions.length();
      if (den == 0)
        throw new RuntimeException();
      map.put(state, RationalScalar.of(1, den));
    }
    return map.get(state);
  }
}
