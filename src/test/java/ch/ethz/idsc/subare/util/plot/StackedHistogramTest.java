/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.util.plot;

import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class StackedHistogramTest extends TestCase {
  public void testEmpty() {
    VisualSet visualSet = new VisualSet();
    TestHelper.draw(StackedHistogram.of(visualSet));
  }

  public void testEmptyRow() {
    VisualSet visualSet = new VisualSet();
    visualSet.add(Tensors.empty());
    TestHelper.draw(StackedHistogram.of(visualSet));
  }
}
