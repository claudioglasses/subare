// code by jph
// inspired by Shangtong Zhang
package ch.ethz.idsc.subare.core.alg;

import ch.ethz.idsc.subare.core.PolicyInterface;
import ch.ethz.idsc.subare.core.StandardModel;
import ch.ethz.idsc.subare.core.util.DiscreteVs;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;

// general bellman equation:
// v_pi(s) == Sum_a pi(a|s) * Sum_{s',r} p(s',r | s,a) * (r + gamma * v_pi(s'))
// bellman optimality equation:
// v_*(s) == max_a Sum_{s',r} p(s',r | s,a) * (r + gamma * v_*(s'))
public class IterativePolicyEvaluation {
  private final StandardModel standardModel;
  private final PolicyInterface policyInterface;
  private final Scalar gamma;
  private final DiscreteVs vs_new;
  private final DiscreteVs vs_old;
  private int iterations = 0;

  // ---
  /** iterative policy evaluation (4.5)
   * see box on p.81
   * 
   * parallel implementation
   * initial values are set to zeros
   * Jacobi style, i.e. updates take effect only in the next iteration
   * 
   * @param standardModel
   * @param policyInterface
   * @return */
  public IterativePolicyEvaluation( //
      StandardModel standardModel, PolicyInterface policyInterface, Scalar gamma) {
    this.standardModel = standardModel;
    this.policyInterface = policyInterface;
    this.gamma = gamma;
    vs_new = DiscreteVs.build(standardModel);
    vs_old = DiscreteVs.build(standardModel);
  }

  /** @param gamma
   * @param threshold
   * @return */
  public void until(Scalar threshold) {
    Scalar past = null;
    while (true) {
      step();
      Scalar delta = Norm._1.of(vs_new.values().subtract(vs_old.values()));
//      if (past != null && Scalars.lessThan(past, delta)) {
//        System.out.println("give up at " + past + " -> " + delta);
//        break;
//      }
      past = delta;
      if (Scalars.lessThan(delta, threshold))
        break;
    }
  }

  public void step() {
    vs_old.setAll(vs_new.values());
    Tensor gvalues = vs_new.values().multiply(gamma);
    vs_new.setAll(Tensor.of(standardModel.states().flatten(0) //
        .parallel() //
        .map(state -> jacobiAdd(state, gvalues))));
    ++iterations;
  }

  // helper function
  private Scalar jacobiAdd(Tensor state, Tensor gvalues) {
    return standardModel.actions(state).flatten(0) //
        .map(action -> policyInterface.policy(state, action).multiply(standardModel.qsa(state, action, gvalues))) //
        .reduce(Scalar::add).get();
  }

  public DiscreteVs vs() {
    return vs_new;
  }

  public int iterations() {
    return iterations;
  }
}
