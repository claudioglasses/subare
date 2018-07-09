// code by fluric
package ch.ethz.idsc.subare.core.util;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.UnitVector;

/** requires keys of the form Join.of(state, action)
 * 
 * the implementation initializes the features as unit vectors */
public class ExactFeatureMapper implements FeatureMapper {
  private final Map<Tensor, Tensor> stateToFeature = new HashMap<>();
  private final int stateActionSize;
  private final int featureSize;

  public ExactFeatureMapper(MonteCarloInterface monteCarloInterface) {
    // count the number of possible state-action pairs first
    Scalar total = monteCarloInterface.states().stream() //
        .filter(v -> !monteCarloInterface.isTerminal(v)) //
        .map(v -> RealScalar.of(monteCarloInterface.actions(v).length())) //
        .reduce(Scalar::add).get();
    stateActionSize = total.number().intValue();
    featureSize = total.number().intValue(); // one-to-one mapping
    int index = -1;
    for (Tensor state : monteCarloInterface.states()) {
      for (Tensor action : monteCarloInterface.actions(state)) {
        if (monteCarloInterface.isTerminal(state))
          stateToFeature.put(StateAction.key(state, action), Array.zeros(stateActionSize));
        else
          stateToFeature.put(StateAction.key(state, action), UnitVector.of(stateActionSize, ++index));
      }
    }
  }

  @Override // from FeatureMapper
  public Tensor getFeature(Tensor key) {
    return stateToFeature.get(key);
  }

  @Override // from FeatureMapper
  public int getStateActionSize() {
    return stateActionSize;
  }

  @Override // from FeatureMapper
  public int getFeatureSize() {
    return featureSize;
  }
}
