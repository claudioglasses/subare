// code by jph
package ch.ethz.idsc.subare.ch05.wireloop;

import java.util.function.Function;

import ch.ethz.idsc.subare.core.PolicyInterface;
import ch.ethz.idsc.subare.core.td.Sarsa;
import ch.ethz.idsc.subare.core.td.SarsaType;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.EGreedyPolicy;
import ch.ethz.idsc.subare.core.util.ExploringStartsBatch;
import ch.ethz.idsc.subare.util.UserHome;
import ch.ethz.idsc.tensor.DecimalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Subdivide;
import ch.ethz.idsc.tensor.io.GifSequenceWriter;
import ch.ethz.idsc.tensor.io.ImageFormat;
import ch.ethz.idsc.tensor.sca.Round;

class SD_Wireloop {
  static Function<Scalar, Scalar> ROUND = Round.toMultipleOf(DecimalScalar.of(.01));

  static void handle(SarsaType type, int n) throws Exception {
    System.out.println(type);
    String name = "wire5";
    Wireloop wireloop = WireloopHelper.create(name, WireloopHelper::id_x);
    int EPISODES = 100;
    Tensor epsilon = Subdivide.of(.2, .01, EPISODES);
    DiscreteQsa qsa = DiscreteQsa.build(wireloop);
    System.out.println(qsa.size());
    GifSequenceWriter gsw = GifSequenceWriter.of(UserHome.file("Pictures/" + name + "_qsa_" + type + ".gif"), 100);
    for (int index = 0; index < EPISODES; ++index) {
      System.out.println(index + " " + epsilon.Get(index).map(ROUND));
      PolicyInterface policyInterface = EGreedyPolicy.bestEquiprobable(wireloop, qsa, epsilon.Get(index));
      Sarsa sarsa = type.supply(wireloop, qsa, RealScalar.of(.1), policyInterface);
      ExploringStartsBatch.apply(wireloop, sarsa, n, policyInterface);
      gsw.append(ImageFormat.of(WireloopHelper.render(wireloop, qsa)));
    }
    gsw.close();
  }

  public static void main(String[] args) throws Exception {
    handle(SarsaType.expected, 5);
  }
}
