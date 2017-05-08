// code by jph
package ch.ethz.idsc.subare.ch05.infvar;

import ch.ethz.idsc.subare.core.EpisodeInterface;
import ch.ethz.idsc.subare.core.EpisodeSupplier;
import ch.ethz.idsc.subare.core.MonteCarloInterface;
import ch.ethz.idsc.subare.core.PolicyInterface;
import ch.ethz.idsc.subare.core.StandardModel;
import ch.ethz.idsc.subare.core.mc.MonteCarloEpisode;
import ch.ethz.idsc.subare.util.Index;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.ZeroScalar;

class InfiniteVariance implements StandardModel, MonteCarloInterface, EpisodeSupplier {
  private final Tensor states = Tensors.vector(0, 1).unmodifiable();
  final Tensor actions = Tensors.vector(0, 1).unmodifiable(); // increment
  final Index statesIndex;

  public InfiniteVariance() {
    statesIndex = Index.build(states);
  }

  @Override
  public Tensor states() {
    return states;
  }

  @Override
  public Tensor actions(Tensor state) {
    return isTerminal(state) ? Tensors.of(ZeroScalar.get()) : actions;
  }

  @Override
  public Scalar qsa(Tensor state, Tensor action, Tensor gvalues) {
    Tensor stateS = move(state, action);
    Scalar reward = reward(state, action, stateS);
    return reward.add(gvalues.Get(statesIndex.of(stateS)));
  }

  /**************************************************/
  @Override
  public Scalar reward(Tensor state, Tensor action, Tensor next) {
    return state.equals(ZeroScalar.get()) && action.equals(RealScalar.ONE) ? //
        RealScalar.ONE : ZeroScalar.get();
  }

  @Override
  public Tensor move(Tensor state, Tensor action) {
    return state.add(action);
  }

  /**************************************************/
  @Override
  public EpisodeInterface kickoff(PolicyInterface policyInterface) {
    return new MonteCarloEpisode(this, policyInterface, ZeroScalar.get());
  }

  @Override
  public boolean isTerminal(Tensor state) {
    return state.equals(RealScalar.ONE);
  }
}