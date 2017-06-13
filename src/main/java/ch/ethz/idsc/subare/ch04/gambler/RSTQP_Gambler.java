// code by jph
package ch.ethz.idsc.subare.ch04.gambler;

import ch.ethz.idsc.subare.core.alg.ActionValueIteration;
import ch.ethz.idsc.subare.core.alg.Random1StepTabularQPlanning;
import ch.ethz.idsc.subare.core.util.ActionValueStatistics;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.StateActionCounter;
import ch.ethz.idsc.subare.core.util.TabularSteps;
import ch.ethz.idsc.subare.core.util.TensorValuesUtils;
import ch.ethz.idsc.subare.util.Digits;
import ch.ethz.idsc.subare.util.UserHome;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.GifSequenceWriter;
import ch.ethz.idsc.tensor.io.ImageFormat;

// R1STQP algorithm is not suited for gambler's dilemma
class RSTQP_Gambler {
  public static void main(String[] args) throws Exception {
    Gambler gambler = Gambler.createDefault();
    final DiscreteQsa ref = GamblerHelper.getOptimalQsa(gambler);
    DiscreteQsa qsa = DiscreteQsa.build(gambler);
    Random1StepTabularQPlanning rstqp = new Random1StepTabularQPlanning(gambler, qsa);
    ActionValueStatistics avs = new ActionValueStatistics(gambler);
    StateActionCounter sac = new StateActionCounter(gambler);
    rstqp.setLearningRate(RealScalar.of(.3)); // TODO learning rate is wrong
    GifSequenceWriter gsw = GifSequenceWriter.of(UserHome.Pictures("gambler_qsa_rstqp.gif"), 100);
    GifSequenceWriter gsc = GifSequenceWriter.of(UserHome.Pictures("gambler_sac_rstqp.gif"), 200);
    int EPISODES = 30;
    for (int index = 0; index < EPISODES; ++index) {
      Scalar error = TensorValuesUtils.distance(qsa, ref);
      System.out.println(index + " " + error.map(Digits._1));
      TabularSteps.batch(gambler, gambler, rstqp, avs, sac);
      gsw.append(ImageFormat.of(GamblerHelper.qsaPolicyRef(gambler, qsa, ref)));
      gsc.append(ImageFormat.of(GamblerHelper.counts( //
          gambler, sac.qsa(StateActionCounter.LOGARITHMIC))));
    }
    gsw.close();
    gsc.close();
    // ---
    ActionValueIteration avi = new ActionValueIteration(gambler, avs);
    avi.setMachinePrecision();
    avi.untilBelow(RealScalar.of(.0001));
    Scalar error = TensorValuesUtils.distance(ref, avi.qsa());
    System.out.println(error);
    Export.of(UserHome.Pictures("gambler_avs.png"), GamblerHelper.qsaPolicyRef(gambler, avi.qsa(), ref));
  }
}
