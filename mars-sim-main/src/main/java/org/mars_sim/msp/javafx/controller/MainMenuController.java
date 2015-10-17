package org.mars_sim.msp.javafx.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.mars_sim.msp.javafx.ControlledScreen;
import org.mars_sim.msp.javafx.MainMenu;
import org.mars_sim.msp.javafx.ScreensSwitcher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class MainMenuController implements Initializable, ControlledScreen {

	@FXML
	Label rotationRate;
	


    ScreensSwitcher switcher;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setScreenParent(ScreensSwitcher screenParent){
        switcher = screenParent;
    }

    //@FXML
    //private void onMouseExited(ActionEvent event) {
    //	menu.setFont("-fx-text-fill: #FFB03B;");
    //}
    
    @FXML
    private void goToOne(ActionEvent event){
    	switcher.getMainMenu().runOne();
    }

    @FXML
    private void goToTwo(ActionEvent event){
    	switcher.getMainMenu().runTwo();
    }

    @FXML
    private void goToThree(ActionEvent event){
    	switcher.getMainMenu().runThree();
    }

    @FXML
    private void goToScreen2(ActionEvent event){
       switcher.setScreen(MainMenu.screen2ID);
    }

    @FXML
    private void goToScreen3(ActionEvent event){
       switcher.setScreen(MainMenu.screen3ID);
    }

    @FXML
    private void exit(ActionEvent event){
    	switcher.exitDialog(switcher.getMainMenu().getStage());
    }

    @FXML
    private void setDefaultRotation(ActionEvent event) {
    	rotationRate.setText("500X");
    	switcher.getMainMenu().setDefaultRotation();
    }

    public void setRotation(int rate) {
    	rotationRate.setText(rate + "X");
    }
}
