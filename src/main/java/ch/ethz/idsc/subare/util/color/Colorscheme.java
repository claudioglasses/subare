// code by jph
package ch.ethz.idsc.subare.util.color;

import java.io.File;

import ch.ethz.idsc.tensor.io.Import;
import ch.ethz.idsc.tensor.opt.Interpolation;
import ch.ethz.idsc.tensor.opt.LinearInterpolation;

public enum Colorscheme {
  ;
  private static Interpolation of(String string) {
    try {
      return LinearInterpolation.of( //
          Import.of(new File("".getClass().getResource("/util/colorscheme/" + string + ".csv").getPath())));
    } catch (Exception exception) {
      // ---
    }
    return null;
  }

  public static Interpolation classic() {
    return of("classic");
  }

  public static Interpolation parula() {
    return of("parula");
  }
}
