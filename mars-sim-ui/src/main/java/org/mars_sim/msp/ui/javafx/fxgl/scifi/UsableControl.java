/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package org.mars_sim.msp.ui.javafx.fxgl.scifi;

import com.almasb.fxgl.ecs.Control;
import com.almasb.fxgl.ecs.Entity;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class UsableControl extends Control {

    private Runnable action;

    public UsableControl(Runnable action) {
        this.action = action;
    }

    @Override
    public void onUpdate(Entity entity, double tpf) {

    }

    public void use() {
        action.run();
    }
}
