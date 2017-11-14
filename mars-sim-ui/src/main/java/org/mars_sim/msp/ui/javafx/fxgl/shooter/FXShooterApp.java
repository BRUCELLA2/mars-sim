/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package org.mars_sim.msp.ui.javafx.fxgl.shooter;

import com.almasb.fxgl.annotation.OnUserAction;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.ecs.Entity;
import com.almasb.fxgl.entity.EntityView;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.component.CollidableComponent;
import com.almasb.fxgl.input.ActionType;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.InputMapping;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.settings.GameSettings;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXShooterApp extends GameApplication {

    public enum EntityType {
        BULLET, ENEMY
    }

    private GameEntity player;
    private PlayerControl playerControl;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("FXShooterApp");
        settings.setWidth(1200);
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(false);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initInput() {
        Input input = getInput();

        input.addInputMapping(new InputMapping("Shoot", KeyCode.F));
    }

    @Override
    protected void initAssets() {

    }

    @Override
    protected void initGame() {
        initTreasure();
        initPlayer();

        //getMasterTimer().runAtInterval(this::spawnEnemy, Duration.seconds(1));
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                BulletComponent bulletData = bullet.getComponent(BulletComponent.class);

                bulletData.setHp(bulletData.getHp() - 1);

                HPComponent hp = enemy.getComponent(HPComponent.class);
                hp.decrement(bulletData.getDamage() + player.getComponent(WeaponComponent.class).getDamage());
                if (hp.getValue() <= 0)
                    enemy.removeFromWorld();

                if (bulletData.getHp() <= 0)
                    bullet.removeFromWorld();
            }
        });
    }

    @Override
    protected void initUI() {

//        TextArea textArea = new TextArea();
//        textArea.setPrefSize(400, 500);
//
//        Button btn = FXGL.getUIFactory().newButton("Execute");
//        btn.setTranslateY(500);
//        btn.setOnAction(e -> {
//            player.removeControl(JSControl.class);
//            player.addControl(new JSControl(textArea.getText()));
//        });
//
//        Pane pane = new Pane();
//        pane.setPrefSize(400, 600);
//        pane.setTranslateX(800);
//        pane.getChildren().addAll(textArea, btn);
//
//        getGameScene().addUINode(pane);
    }

    @Override
    protected void onUpdate(double tpf) {

    }

    @OnUserAction(name = "Shoot", type = ActionType.ON_ACTION_BEGIN)
    public void shoot() {
        playerControl.shoot(getInput().getVectorToMouse(player.getPositionComponent().getValue()));
    }

    private void initTreasure() {
        GameEntity treasure = new GameEntity();
        treasure.getPositionComponent().setValue(getWidth() / 2, getHeight() / 2);
        treasure.getViewComponent().setView(new Rectangle(40, 40, Color.YELLOW));

        getGameWorld().addEntity(treasure);
    }

    private void initPlayer() {
        player = new GameEntity();
        player.getPositionComponent().setValue(getWidth() / 2, getHeight() / 2);
        player.getViewComponent().setView(new Rectangle(40, 40, Color.BLUE));

        WeaponComponent weapon = new WeaponComponent();
        weapon.setDamage(2);
        weapon.setFireRate(1.0);
        weapon.setMaxAmmo(10);

        player.addComponent(weapon);

        playerControl = new PlayerControl();
        player.addControl(playerControl);

        getGameWorld().addEntity(player);
    }

    private void spawnEnemy() {
        GameEntity enemy = new GameEntity();
        enemy.getTypeComponent().setValue(EntityType.ENEMY);
        enemy.getPositionComponent().setValue(getWidth(), 460);
        enemy.getViewComponent().setView(new EntityView(new Rectangle(40, 40, Color.RED)), true);

        enemy.addComponent(new CollidableComponent(true));
        enemy.addComponent(new HPComponent(5));
        enemy.addControl(new EnemyControl(new Point2D(getWidth() / 2, getHeight() / 2)));

        getGameWorld().addEntity(enemy);
    }


    /*
    Weapon

    damage
    fire rate
    ammo capacity
    bonus?

    num bullets shot at once (optional)
     */


    /*
    Bullet

    damage
    move speed
    hp (hits before destroyed)

    range (optional)

     */
}
