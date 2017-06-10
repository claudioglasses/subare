// code by jph
// inspired by Shangtong Zhang
package ch.ethz.idsc.subare.ch04.gambler;

import ch.ethz.idsc.subare.core.PolicyInterface;
import ch.ethz.idsc.subare.core.alg.ValueIteration;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.DiscreteUtils;
import ch.ethz.idsc.subare.core.util.DiscreteVs;
import ch.ethz.idsc.subare.core.util.GreedyPolicy;
import ch.ethz.idsc.subare.core.util.Policies;
import ch.ethz.idsc.subare.core.util.TensorValuesUtils;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.sca.N;

/** Shangtong Zhang states that using double precision in python
 * "due to tie and precision, can't reproduce the optimal policy in book"
 * 
 * Unlike stated in the book, there is not a unique optimal policy but many
 * using symbolic expressions we can reproduce the policy in book and
 * all other optimal actions
 * 
 * chapter 4, example 3 */
class VI_Gambler {
  public static void main(String[] args) {
    Gambler gambler = Gambler.createDefault();
    DiscreteQsa ref = GamblerHelper.getOptimalQsa(gambler);
    ValueIteration vi = new ValueIteration(gambler);
    vi.untilBelow(RealScalar.of(1e-20));
    final DiscreteVs vs = vi.vs();
    final DiscreteVs vr = DiscreteUtils.createVs(gambler, ref);
    Scalar diff = TensorValuesUtils.distance(vs, vr);
    System.out.println("error=" + N.of(diff));
    PolicyInterface policyInterface = GreedyPolicy.bestEquiprobable(gambler, vi.vs());
    Policies.print(policyInterface, gambler.states());
  }
}
