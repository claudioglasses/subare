// code by fluric
package ch.ethz.idsc.subare.analysis;

import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.Policy;
import ch.ethz.idsc.subare.core.mc.MonteCarloExploringStarts;
import ch.ethz.idsc.subare.core.td.ExpectedSarsa;
import ch.ethz.idsc.subare.core.td.OriginalSarsa;
import ch.ethz.idsc.subare.core.td.QLearning;
import ch.ethz.idsc.subare.core.td.Sarsa;
import ch.ethz.idsc.subare.core.td.TrueOnlineSarsa;
import ch.ethz.idsc.subare.core.td.TrueOnlineSarsaMod;
import ch.ethz.idsc.subare.core.util.ConstantLearningRate;
import ch.ethz.idsc.subare.core.util.DiscreteQsa;
import ch.ethz.idsc.subare.core.util.EGreedyPolicy;
import ch.ethz.idsc.subare.core.util.ExactFeatureMapper;
import ch.ethz.idsc.subare.core.util.ExploringStarts;
import ch.ethz.idsc.subare.core.util.FeatureMapper;
import ch.ethz.idsc.subare.util.Stopwatch;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public enum MonteCarloAlgorithms {
  OriginalSarsa() {
    @Override
    public Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis) {
      Tensor XYsarsa = Tensors.empty();
      DiscreteQsa qsaSarsa = DiscreteQsa.build(monteCarloInterface);
      final Sarsa sarsa = new OriginalSarsa(monteCarloInterface, qsaSarsa, ConstantLearningRate.of(RealScalar.of(0.05)));
      sarsa.setExplore(RealScalar.of(.1));
      Stopwatch stopwatch = Stopwatch.started();
      for (int index = 0; index < batches; ++index) {
        // System.out.println("starting batch " + (index + 1) + " of " + batches);
        Policy policy = EGreedyPolicy.bestEquiprobable(monteCarloInterface, sarsa.qsa(), RealScalar.of(.1));
        ExploringStarts.batch(monteCarloInterface, policy, 1, sarsa);
        XYsarsa.append(Tensors.vector(RealScalar.of(index).number(), errorAnalysis.getError(monteCarloInterface, optimalQsa, sarsa.qsa()).number()));
      }
      System.out.println("Time for OriginalSarsa: " + stopwatch.display_seconds() + "s");
      // Policies.print(GreedyPolicy.bestEquiprobable(airport, sarsa.qsa()), airport.states());
      return XYsarsa;
    }
  }, //
  ExpectedSarsa() {
    @Override
    public Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis) {
      Tensor XYsarsa = Tensors.empty();
      DiscreteQsa qsaSarsa = DiscreteQsa.build(monteCarloInterface);
      final Sarsa sarsa = new ExpectedSarsa(monteCarloInterface, qsaSarsa, ConstantLearningRate.of(RealScalar.of(0.05)));
      sarsa.setExplore(RealScalar.of(.1));
      Stopwatch stopwatch = Stopwatch.started();
      for (int index = 0; index < batches; ++index) {
        // System.out.println("starting batch " + (index + 1) + " of " + batches);
        Policy policy = EGreedyPolicy.bestEquiprobable(monteCarloInterface, sarsa.qsa(), RealScalar.of(.1));
        ExploringStarts.batch(monteCarloInterface, policy, 1, sarsa);
        XYsarsa.append(Tensors.vector(RealScalar.of(index).number(), errorAnalysis.getError(monteCarloInterface, optimalQsa, sarsa.qsa()).number()));
      }
      System.out.println("Time for ExpectedSarsa: " + stopwatch.display_seconds() + "s");
      // Policies.print(GreedyPolicy.bestEquiprobable(airport, sarsa.qsa()), airport.states());
      return XYsarsa;
    }
  }, //
  QLearningSarsa() {
    @Override
    public Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis) {
      Tensor XYsarsa = Tensors.empty();
      DiscreteQsa qsaSarsa = DiscreteQsa.build(monteCarloInterface);
      final Sarsa sarsa = new QLearning(monteCarloInterface, qsaSarsa, ConstantLearningRate.of(RealScalar.of(0.05)));
      sarsa.setExplore(RealScalar.of(.1));
      Stopwatch stopwatch = Stopwatch.started();
      for (int index = 0; index < batches; ++index) {
        // System.out.println("starting batch " + (index + 1) + " of " + batches);
        Policy policy = EGreedyPolicy.bestEquiprobable(monteCarloInterface, sarsa.qsa(), RealScalar.of(.1));
        ExploringStarts.batch(monteCarloInterface, policy, 1, sarsa);
        XYsarsa.append(Tensors.vector(RealScalar.of(index).number(), errorAnalysis.getError(monteCarloInterface, optimalQsa, sarsa.qsa()).number()));
      }
      System.out.println("Time for QLearningSarsa: " + stopwatch.display_seconds() + "s");
      // Policies.print(GreedyPolicy.bestEquiprobable(airport, sarsa.qsa()), airport.states());
      return XYsarsa;
    }
  }, //
  MonteCarlo() {
    @Override
    public Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis) {
      Tensor XYmc = Tensors.empty();
      MonteCarloExploringStarts mces = new MonteCarloExploringStarts(monteCarloInterface);
      Stopwatch stopwatch = Stopwatch.started();
      for (int index = 0; index < batches; ++index) {
        // System.out.println("starting batch " + (index + 1) + " of " + batches);
        Policy policyMC = EGreedyPolicy.bestEquiprobable(monteCarloInterface, mces.qsa(), RealScalar.of(.1));
        ExploringStarts.batch(monteCarloInterface, policyMC, mces);
        XYmc.append(Tensors.vector(RealScalar.of(index).number(), errorAnalysis.getError(monteCarloInterface, optimalQsa, mces.qsa()).number()));
      }
      System.out.println("Time for MonteCarlo: " + stopwatch.display_seconds() + "s");
      // Policies.print(GreedyPolicy.bestEquiprobable(airport, mces.qsa()), airport.states());
      return XYmc;
    }
  }, //
  TrueOnlineSarsa() {
    @Override
    public Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis) {
      Tensor XYtoSarsa = Tensors.empty();
      FeatureMapper mapper = new ExactFeatureMapper(monteCarloInterface);
      TrueOnlineSarsa toSarsa = new TrueOnlineSarsa(monteCarloInterface, RealScalar.of(0.7), RealScalar.of(0.2), mapper, 10);
      Stopwatch stopwatch = Stopwatch.started();
      for (int index = 0; index < batches; ++index) {
        // System.out.println("starting batch " + (index + 1) + " of " + batches);
        toSarsa.executeEpisode(RealScalar.of(0.1));
        DiscreteQsa toQsa = toSarsa.getQsa();
        XYtoSarsa.append(Tensors.vector(RealScalar.of(index).number(), errorAnalysis.getError(monteCarloInterface, optimalQsa, toQsa).number()));
      }
      System.out.println("Time for TrueOnlineSarsa: " + stopwatch.display_seconds() + "s");
      DiscreteQsa toQsa = toSarsa.getQsa();
      // System.out.println(toSarsa.getW());
      // toSarsa.printValues();
      // toSarsa.printPolicy();
      return XYtoSarsa;
    }
  }, //
  TrueOnlineSarsaMod() {
    @Override
    public Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis) {
      Tensor XYtoSarsa = Tensors.empty();
      FeatureMapper mapper = new ExactFeatureMapper(monteCarloInterface);
      TrueOnlineSarsaMod toSarsa = new TrueOnlineSarsaMod(monteCarloInterface, RealScalar.of(0.7), ConstantLearningRate.of(RealScalar.of(0.2)), mapper, 10);
      Stopwatch stopwatch = Stopwatch.started();
      for (int index = 0; index < batches; ++index) {
        // System.out.println("starting batch " + (index + 1) + " of " + batches);
        toSarsa.executeEpisode(RealScalar.of(0.1));
        DiscreteQsa toQsa = toSarsa.getQsa();
        XYtoSarsa.append(Tensors.vector(RealScalar.of(index).number(), errorAnalysis.getError(monteCarloInterface, optimalQsa, toQsa).number()));
      }
      System.out.println("Time for TrueOnlineSarsaMod: " + stopwatch.display_seconds() + "s");
      DiscreteQsa toQsa = toSarsa.getQsa();
      // System.out.println(toSarsa.getW());
      // toSarsa.printValues();
      // toSarsa.printPolicy();
      return XYtoSarsa;
    }
  }, //
  ;
  public abstract Tensor analyse(MonteCarloInterface monteCarloInterface, int batches, DiscreteQsa optimalQsa, MonteCarloErrorAnalysis errorAnalysis);
}