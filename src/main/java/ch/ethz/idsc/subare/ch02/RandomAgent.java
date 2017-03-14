// code by jph
package ch.ethz.idsc.subare.ch02;

import ch.ethz.idsc.tensor.RealScalar;

/** the random agent picks any action equally likely
 * the policy is a constant vector of pi(a)=1/n */
public class RandomAgent extends Agent {
  final int n;

  public RandomAgent(int n) {
    this.n = n;
  }

  @Override
  public
  int takeAction() {
    return random.nextInt(n);
  }

  @Override
  void protected_feedReward(int a, RealScalar value) {
    // empty by design
  }

  @Override
  public String getDescription() {
    return "";
  }
}