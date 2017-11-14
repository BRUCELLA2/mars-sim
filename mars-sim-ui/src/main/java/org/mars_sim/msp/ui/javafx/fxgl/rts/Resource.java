/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package org.mars_sim.msp.ui.javafx.fxgl.rts;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public interface Resource {

    void onStartGathering();

    void onEndGathering();
}
