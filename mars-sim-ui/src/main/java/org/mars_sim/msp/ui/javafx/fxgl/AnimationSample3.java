/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package org.mars_sim.msp.ui.javafx.fxgl;

import com.almasb.fxgl.animation.Animation;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.settings.GameSettings;
import javafx.animation.Interpolator;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Shows how to use common animations patterns for entities.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class AnimationSample3 extends GameApplication {

    private GameEntity player;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("AnimationSample3");
        settings.setVersion("0.1");
        settings.setFullScreen(false);
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(false);
        settings.setProfilingEnabled(true);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Grow") {
            @Override
            protected void onActionBegin() {
                Entities.animationBuilder()
                        .duration(Duration.seconds(3))
                        .scale(player)
                        .from(new Point2D(2, 2))
                        .to(new Point2D(1, 1))
                        .buildAndPlay();
            }
        }, KeyCode.F);
    }

    @Override
    protected void initGame() {
        player = Entities.builder()
                .at(100, 100)
                .viewFromNode(new Rectangle(40, 40))
                .buildAndAttach(getGameWorld());

        Animation<?> animation = Entities.animationBuilder()
                .duration(Duration.seconds(1))
                .repeat(1)
                .rotate(player)
                .rotateTo(360)
                .build();

        animation.setCycleCount(2);
        animation.setAutoReverse(true);
        animation.startInPlayState();

        Entities.animationBuilder()
                .duration(Duration.seconds(3))
                .repeat(2)
                .delay(Duration.millis(1002))
                .interpolator(Interpolator.EASE_OUT)
                .translate(player)
                .alongPath(new QuadCurve(33, 33, 450, 544, 750, 4))
                .buildAndPlay();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
