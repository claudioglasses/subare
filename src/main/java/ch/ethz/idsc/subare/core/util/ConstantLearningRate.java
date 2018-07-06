// code by jph
package ch.ethz.idsc.subare.core.util;

import java.util.HashSet;
import java.util.Set;

import ch.ethz.idsc.subare.core.LearningRate;
import ch.ethz.idsc.subare.core.StepInterface;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** learning rate of alpha except in first update of state-action pair
 * for which the learning rate equals 1 in the case of warmStart. */
public class ConstantLearningRate implements LearningRate {
  /** @param alpha
   * @return constant learning rate with factor alpha */
  public static LearningRate of(Scalar alpha) {
    return new ConstantLearningRate(alpha, true);
  }

  /** @param alpha
   * @param warmStart whether to warmStart (alpha=1 if state-action pair not yet seen) or not
   * @return constant learning rate with factor alpha */
  public static LearningRate of(Scalar alpha, boolean warmStart) {
    return new ConstantLearningRate(alpha, warmStart);
  }

  /** @return constant learning rate with factor 1.0,
   * that means the updates have numeric precision */
  public static LearningRate one() {
    return of(RealScalar.of(1.0));
  }

  /** @return constant learning rate with exact factor 1,
   * that means the precision in the updates is preserved */
  public static LearningRate one_exact() {
    return of(RealScalar.ONE);
  }

  // ---
  private final Set<Tensor> visited = new HashSet<>();
  private final Scalar alpha;
  private final boolean warmStart;

  private ConstantLearningRate(Scalar alpha, boolean warmStart) {
    this.warmStart = warmStart;
    this.alpha = alpha;
  }

  @Override // from LearningRate
  public void digest(StepInterface stepInterface) {
    visited.add(StateAction.key(stepInterface));
  }

  @Override // from LearningRate
  public Scalar alpha(StepInterface stepInterface) {
    if (!warmStart)
      return alpha;
    return visited.contains(StateAction.key(stepInterface)) ? //
        alpha : RealScalar.ONE; // overcome initialization bias
  }

  @Override // from LearningRate
  public boolean encountered(Tensor state, Tensor action) {
    return visited.contains(StateAction.key(state, action));
  }
}
