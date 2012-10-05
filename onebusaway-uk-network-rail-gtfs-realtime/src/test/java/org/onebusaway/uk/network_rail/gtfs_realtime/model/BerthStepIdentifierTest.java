package org.onebusaway.uk.network_rail.gtfs_realtime.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class BerthStepIdentifierTest {

  @Test
  public void testIsLinked01() {
    BerthStepIdentifier a = new BerthStepIdentifier("TB_0677", "TB_N001");
    BerthStepIdentifier b = new BerthStepIdentifier("LA_T677", "LA_0001");
    assertTrue(a.isDirectlyLinked(b));
    assertTrue(b.isDirectlyLinked(a));
  }

  @Test
  public void testIsLinked02() {
    BerthStepIdentifier a = new BerthStepIdentifier("TB_0677", "TB_0679");
    BerthStepIdentifier b = new BerthStepIdentifier("TB_0679", "TB_0681");
    assertTrue(a.isDirectlyLinked(b));
    assertFalse(b.isDirectlyLinked(a));
  }

}
