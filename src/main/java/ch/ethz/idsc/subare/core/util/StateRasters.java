// code by jph
package ch.ethz.idsc.subare.core.util;

import java.awt.Dimension;
import java.awt.Point;

import ch.ethz.idsc.subare.core.DiscreteModel;
import ch.ethz.idsc.subare.util.Colorscheme;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.opt.Interpolation;

public enum StateRasters {
  ;
  /** @param discreteModel
   * @param dimension
   * @return */
  public static StateRaster create(DiscreteModel discreteModel, Dimension dimension) {
    return new StateRaster() {
      @Override
      public DiscreteModel discreteModel() {
        return discreteModel;
      }

      @Override
      public Dimension dimension() {
        return dimension;
      }

      @Override
      public Point point(Tensor state) {
        return new Point( //
            state.Get(0).number().intValue(), //
            state.Get(1).number().intValue());
      }
    };
  }

  private static final Interpolation COLORSCHEME = Colorscheme.classic();
  private static final Tensor BASE = Tensors.vector(255);

  /** @param stateActionRaster
   * @param vs scaled to contain values in the interval [0, 1]
   * @return */
  public static Tensor render(StateRaster stateRaster, DiscreteVs vs) {
    DiscreteModel discreteModel = stateRaster.discreteModel();
    Dimension dimension = stateRaster.dimension();
    Tensor tensor = Array.zeros(dimension.width, dimension.height, 4);
    for (Tensor state : discreteModel.states()) {
      Point point = stateRaster.point(state);
      if (point != null) {
        Scalar sca = vs.value(state);
        tensor.set(COLORSCHEME.get(BASE.multiply(sca)), point.x, point.y);
      }
    }
    return tensor;
  }
}
