/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package org.mars_sim.msp.ui.javafx.fxgl.rts;

import com.almasb.fxgl.ai.fsm.State;
import com.almasb.fxgl.ai.msg.Telegram;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public enum WorkerState implements State<GameEntity> {

    IDLE() {
        @Override
        public void update(GameEntity entity) {
            FXGL.getApp()
                    .getGameWorld()
                    .getClosestEntity(entity, e -> {
                        return Entities.getType(e).isType(RTSSampleType.GOLD_MINE) &&
                                !e.getComponent(GoldMineComponent.class).getValue().isFull();
                    })
                    .ifPresent(goldMine -> {
                        entity.setProperty("target", goldMine);
                        changeState(entity, WALK);
                    });
        }
    },

    WALK() {
        @Override
        public void update(GameEntity entity) {
            GameEntity target = entity.getProperty("target");

            entity.translate(target.getPosition()
                            .subtract(entity.getPosition())
                            .normalize()
                            .multiply(100 * 0.016)
            );

            if (entity.isColliding(target)) {
                if (target.isType(RTSSampleType.GOLD_MINE) && entity.getComponent(BackpackComponent.class).getValue().getGold() < 150) {
                    changeState(entity, GATHER_GOLD);
                }

                if (target.isType(RTSSampleType.TOWN_HALL)) {
                    changeState(entity, DEPOSIT_GOLD);
                }
            }
        }
    },

    GATHER_GOLD() {
        @Override
        public void enter(GameEntity entity) {
            GameEntity target = entity.getProperty("target");
            GoldMine mine = target.getComponent(GoldMineComponent.class).getValue();

            if (mine.isFull()) {
                System.out.println("Mine is Full");
                changeState(entity, IDLE);
            } else {
                entity.getView().setVisible(false);
                mine.onStartGathering();
            }
        }

        @Override
        public void update(GameEntity entity) {
            Backpack backpack = entity.getComponent(BackpackComponent.class).getValue();

            backpack.addGold(1);

            if (backpack.getGold() == 150) {
                entity.getView().setVisible(true);
                GameEntity target = entity.getProperty("target");
                GoldMine mine = target.getComponent(GoldMineComponent.class).getValue();
                mine.onEndGathering();

                entity.setProperty("target", FXGL.getApp().getGameWorld().getEntitiesByType(RTSSampleType.TOWN_HALL).get(0));
                changeState(entity, WALK);
            }
        }
    },

    DEPOSIT_GOLD() {
        @Override
        public void update(GameEntity entity) {
            Backpack backpack = entity.getComponent(BackpackComponent.class).getValue();

            FXGL.getApp().getGameState().increment("gold", backpack.getGold());

            backpack.addGold(-backpack.getGold());

            changeState(entity, IDLE);
        }
    };

    void changeState(GameEntity entity, WorkerState state) {
        entity.getControl(FSMControl.class).changeState(state);
    }

    @Override
    public void enter(GameEntity entity) {}

    @Override
    public void update(GameEntity entity) {}

    @Override
    public void exit(GameEntity entity) {}

    @Override
    public boolean onMessage(GameEntity entity, Telegram telegram) {
        return false;
    }
}
