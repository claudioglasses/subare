// code by jph
package ch.ethz.idsc.subare.ch02.prison;

import ch.ethz.idsc.subare.ch02.Agent;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** Julian's idea
 * Prisoners Dilemma */
class Training {
  static final Tensor r2 = Tensors.matrixDouble(new double[][] { //
      { 1, 4 }, //
      { 0, 3 } });

  static Tensor train(Agent a1, Agent a2, int epochs) {
    Judger judger = new Judger(r2, a1, a2);
    for (int round = 0; round < epochs; ++round)
      judger.play();
    return judger.ranking();
  }
}