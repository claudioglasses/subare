/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.util.plot;

import java.util.function.Function;

import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.tensor.Scalar;

/** inspired by
 * <a href="https://reference.wolfram.com/language/ref/Histogram.html">Histogram</a> */
public enum Histogram {
  ;
  /** @param visualSet
   * @return */
  public static JFreeChart of(VisualSet visualSet) {
    return of(visualSet, false);
  }

  /* package */ static JFreeChart of(VisualSet visualSet, boolean stacked) {
    return JFreeChartFactory.barChart(visualSet, stacked, Scalar::toString);
  }

  public static JFreeChart of(VisualSet visualSet, Function<Scalar, String> naming) {
    return JFreeChartFactory.barChart(visualSet, false, naming);
  }
}
