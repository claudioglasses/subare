// code by jph
// inspired by Shangtong Zhang
package ch.ethz.idsc.subare.ch06.windy;

import ch.ethz.idsc.subare.core.PolicyInterface;
import ch.ethz.idsc.subare.core.td.Sarsa;
import ch.ethz.idsc.subare.core.td.SarsaType;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.EGreedyPolicy;
import ch.ethz.idsc.subare.core.util.ExploringStarts;
import ch.ethz.idsc.subare.core.util.TensorValuesUtils;
import ch.ethz.idsc.subare.util.Digits;
import ch.ethz.idsc.subare.util.UserHome;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.io.GifSequenceWriter;
import ch.ethz.idsc.tensor.io.ImageFormat;

/** determines q(s,a) function for equiprobable "random" policy */
class Sarsa_Windygrid {
  static void handle(SarsaType type, int total) throws Exception {
    System.out.println(type);
    Windygrid windygrid = Windygrid.createFour();
    final DiscreteQsa ref = WindygridHelper.getOptimalQsa(windygrid);
    DiscreteQsa qsa = DiscreteQsa.build(windygrid);
    System.out.println(qsa.size());
    GifSequenceWriter gsw = GifSequenceWriter.of(UserHome.Pictures("windygrid_qsa_" + type + ".gif"), 100);
    for (int index = 0; index < total; ++index) {
      Scalar error = TensorValuesUtils.distance(qsa, ref);
      System.out.println(index + " " + error.map(Digits._1));
      PolicyInterface policyInterface = EGreedyPolicy.bestEquiprobable(windygrid, qsa, RealScalar.of(.1));
      Sarsa sarsa = type.supply(windygrid, qsa, RealScalar.of(.25), policyInterface);
      for (int count = 0; count < 10; ++count)
        ExploringStarts.batch(windygrid, policyInterface, sarsa);
      if (index % 2 == 0)
        gsw.append(ImageFormat.of(WindygridHelper.joinAll(windygrid, qsa, ref)));
    }
    gsw.close();
  }

  public static void main(String[] args) throws Exception {
    handle(SarsaType.qlearning, 20);
    // handle(SarsaType.expected, 20);
    // handle(SarsaType.original, 20);
  }
}
