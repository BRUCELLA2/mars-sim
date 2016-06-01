/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.08 2015-12-18
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

//import com.jidesoft.swing.MarqueePane;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;
//import com.sibvisions.rad.ui.javafx.ext.mdi.FXDesktopPane;
//import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.eclipse.fx.ui.controls.tabpane.DndTabPane;
//import org.eclipse.fx.ui.controls.tabpane.DndTabPaneFactory;
//import org.eclipse.fx.ui.controls.tabpane.DndTabPaneFactory.FeedbackType;
//import org.eclipse.fx.ui.controls.tabpane.skin.DnDTabPaneSkin;

//import jfxtras.scene.menu.CornerMenu;

import com.sun.management.OperatingSystemMXBean;

import eu.hansolo.enzo.notification.Notification;
import eu.hansolo.enzo.notification.Notification.Notifier;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;
import org.mars_sim.msp.ui.swing.tool.construction.ConstructionWizard;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.TransportWizard;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.PlannerWindow;


/**
 * The MainScene class is the primary Stage for MSP. It is the container for
 * housing desktop swing node, javaFX UI, pull-down menu and icons for tools.
 */
public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	public static final Image QUOTE_ICON = new Image(MainScene.class.getResource("/icons/quote_24.png").toExternalForm());
	//public static final Image QUOTE_ICON = new Image(MainScene.class.getResourceAsStream("/icons/quote.png"));
	
    //Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec-2k.jpg").toExternalForm());

	private static final int TIME_DELAY = SettlementWindow.TIME_DELAY;

	// Categories of loading and saving simulation
	public static final int DEFAULT = 1;
	public static final int AUTOSAVE = 2;
	public static final int OTHER = 3; // load other file
	public static final int SAVE_AS = 3; // save as other file

	private static int theme = 7; // 7 is the standard nimrod theme

    private MenuItem navMenuItem = registerAction(new MenuItem("Navigator", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.globe.wire.png")))));
    private MenuItem mapMenuItem = registerAction(new MenuItem("Map", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.map.folds.png")))));
    private MenuItem missionMenuItem = registerAction(new MenuItem("Mission", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.flag.wavy.png")))));
    private MenuItem monitorMenuItem = registerAction(new MenuItem("Monitor", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.eye.png")))));
    private MenuItem searchMenuItem = registerAction(new MenuItem("Search", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.magnify.png")))));
    private MenuItem eventsMenuItem = registerAction(new MenuItem("Events", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.page.new.png")))));

	private int memMax;
	private int memTotal;
	private int memUsed, memUsedCache;
	private int memFree;
	private int processCpuLoad;
	//private int systemCpuLoad;

	private boolean isMainSceneDoneLoading = false;

	private double width;
	private double height;

	private StringProperty timeStamp;

	private String lookAndFeelTheme = "nimrod";

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;

	private Text timeText;
	private Text memUsedText;
	//private Text memMaxText;
	//private Text processCpuLoadText;
	//private Text systemCpuLoadText;
	private Button memBtn, clkBtn;//, cpuBtn;

	private Stage stage;
	//private Scene scene;
	private StackPane rootStackPane;
	private SwingNode swingNode;
	private StatusBar statusBar;
	private Flyout flyout;
	//private ToggleButton marsNetButton;
	private MaterialDesignToggleButton marsNetButton;
	private ChatBox cb;
	//private CornerMenu cornerMenu;
	private StackPane swingPane;
	private Tab swingTab;
	private Tab nodeTab;
	private BorderPane borderPane;
	private DndTabPane dndTabPane;
	//private FXDesktopPane fxDesktopPane;
	private ESCHandler esc = null;

	private Timeline timeline, notify_timeline;
	private static NotificationPane notificationPane;


	private static MainDesktopPane desktop;
	private MainSceneMenu menuBar;
	private MarsNode marsNode;
	private TransportWizard transportWizard;
	private ConstructionWizard constructionWizard;

	ObservableList<Screen> screens;

	private DecimalFormat twoDigitFormat = new DecimalFormat(Msg.getString("twoDigitFormat")); //$NON-NLS-1$

	@SuppressWarnings("restriction")
	private OperatingSystemMXBean osBean;

    private static String[] quote = new String[10];
    
	private static String quote0 = 
			  "\" All the conditions necessary for murder are \n "
    		+ " met if you shut 2 men in a cabin measuring \n"
    		+ " 18'x20' and leave them together for 2 months.\"\n"
    		+ "                                                 - Valery Ryumin";				
    
	private static String quote1 = 
			  "\" We have some special space shampoo that doesn't\n "
    		+ " require water, and it does a pretty good job. So at\n "
			+ " the end of the mission, even though it's 6 months\n "
    		+ " without a bath, we're still pretty good, and we \n"
    		+ " don't smell too bad.\"\n"
    		+ "                                         -Mike Fincke, NASA Astronaut";

	private static String quote2 = 
			  "\" The Dog, and the Plough, and the Hunter, and all\n "
    		+ "          And the star of the sailor, and Mars,\n"
    		+ " These shone in the sky, and the pail by the wall \n"
    		+ "          Would be half full of water and stars.\"\n"
    		+ "              - Robert Louis Steenson, 'Escape at Bedtime'";

	private static String quote3 = 
			  "\" A human being should be able to change a diaper,\n "
			+ " plan an invasion, conn a ship, design a building,\n"
			+ " write a sonnet, balance accounts, build a wall, \n"
			+ " comfort the dyding, take orders, cooperate, act alone,\n"
			+ " solve equations, analyze a new problem, pitch manure,\n"
			+ " program a computer, cook a tasty meal, fight efficiently\n"
			+ " , and die gallantly. Specialization is for insects.\"\n"
			+ "                                                - Robert A. Heinlein";

	private static String quote4 = 
			  "\" Don't tell me that man doesn't belong out there.\n "
			+ " Man belongs wherever he wants to go, and he'll do \n"
			+ " plenty well when he gest there.\"\n"
			+ "                - Wernher von Braun, Time Magazine, 1958";

	
/*	
	//private static final Random         RND           = new Random();
    private static final Notification[] NOTIFICATIONS = {
        new Notification("Quote", quote[0], Notification.INFO_ICON),
        new Notification("Warning", "Attention, somethings wrong", Notification.WARNING_ICON),
        new Notification("Success", "Great it works", Notification.SUCCESS_ICON),
        new Notification("Error", "ZOMG", Notification.ERROR_ICON)
    };
*/
	
    private Notification.Notifier notifier;
    
	//static {
   //     Font.loadFont(MainScene.class.getResource("/fxui/fonts/fontawesome-webfont.ttf").toExternalForm(), 10);
    //}

	/**
	 * Constructor for MainScene
	 *
	 * @param stage
	 */
	public MainScene(Stage stage) {
		//logger.info("MainScene's constructor() is on " + Thread.currentThread().getName() + " Thread");
		this.stage = stage;
		this.isMainSceneDoneLoading = false;
 
		//stage.setResizable(true);

		//stage.setMinWidth(1280);
		//stage.setMinHeight(600);

		//stage.setWidth(1280);
		//stage.setHeight(600);

		//stage.setMaxWidth(1920);
		//stage.setMaxHeight(1200);

		stage.setFullScreenExitHint("Use Ctrl+F (or Meta+C in Mac) to toggle full screen mode");
		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        //stage.setFullScreen(false);
        //stage.setFullScreen(true);
        
		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(e -> {
			boolean exit = alertOnExit();
			if (!exit)
				e.consume();
			else {
				Platform.exit();
				System.exit(0);
			}
		} );

		// Detect if a user hits ESC
		setEscapeEventHandler(true);
		
	}

	// 2015-12-28 Added setEscapeEventHandler()
	public void setEscapeEventHandler(boolean value) {
		if (value) {
			esc = new ESCHandler();
			stage.addEventHandler(KeyEvent.KEY_PRESSED, esc);
		}
		else
			stage.removeEventHandler(KeyEvent.KEY_PRESSED, esc);

	}

	class ESCHandler implements EventHandler<KeyEvent> {

		public void handle(KeyEvent t) {
			if (t.getCode() == KeyCode.ESCAPE) {
				boolean isOnPauseMode = Simulation.instance().getMasterClock().isPaused();
				if (isOnPauseMode) {
					unpauseSimulation();
					//desktop.getTimeWindow().enablePauseButton(true);
				}
				else {
					pauseSimulation();
					//desktop.getTimeWindow().enablePauseButton(false);
				}
				// Toggle the full screen mode to OFF in the pull-down menu
				// under setting
				//menuBar.exitFullScreen();
				// close the MarsNet side panel
				//openSwingTab();
			}
		}


	}


	/**
	 * Calls an thread executor to submit MainSceneTask
	 */
	public void prepareMainScene() {
		//logger.info("MainScene's prepareMainScene() is in " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().getSimExecutor().submit(new MainSceneTask());
	}

	/**
	 * Sets up the UI theme and the two timers as a thread pool task
	 */
	public class MainSceneTask implements Runnable {
		public void run() {
			//logger.info("MainScene's MainSceneTask is in " + Thread.currentThread().getName() + " Thread");
			// Set look and feel of UI.
			UIConfig.INSTANCE.useUIDefault();
			SwingUtilities.invokeLater(() -> {
				setLookAndFeel(1);
			});
			// System.out.println("done running createMainScene()");
		}
	}

	/**
	 * Prepares the transport wizard, construction wizard, autosave timer and earth timer
	 */
	public void prepareOthers() {
		//logger.info("MainScene's prepareOthers() is on " + Thread.currentThread().getName() + " Thread");
		//startAutosaveTimer();
		startEarthTimer();
		transportWizard = new TransportWizard(this, desktop);
		constructionWizard = new ConstructionWizard(this, desktop);
		//logger.info("done with MainScene's prepareOthers()");
	}

	/**
	 * Pauses sim and opens the transport wizard
	 * @param buildingManager
	 */
	public synchronized void openTransportWizard(BuildingManager buildingManager) {
		logger.info("MainScene's openTransportWizard() is in " + Thread.currentThread().getName() + " Thread");
		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			pauseSimulation();
	    	//System.out.println("previous is false. Paused sim");
		}
		desktop.getTimeWindow().enablePauseButton(false);

		Platform.runLater(() -> {
			//System.out.println("calling transportWizard.deliverBuildings() ");
			transportWizard.deliverBuildings(buildingManager);
			//System.out.println("ended transportWizard.deliverBuildings() ");
		});

		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				unpauseSimulation();
   	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				unpauseSimulation();
   	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);

	}

	public TransportWizard getTransportWizard() {
		return transportWizard;
	}

	/**
 	 * Pauses sim and opens the construction wizard
	 * @param constructionManager
	 */
	// 2015-12-16 Added openConstructionWizard()
	public void openConstructionWizard(BuildingConstructionMission mission) { // ConstructionManager constructionManager,
		//logger.info("MainScene's openConstructionWizard() is in " + Thread.currentThread().getName() + " Thread");
		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			pauseSimulation();
	    	//System.out.println("previous is false. Paused sim");
		}
		desktop.getTimeWindow().enablePauseButton(false);

		//try {
			//FXUtilities.runAndWait(() -> {
			Platform.runLater(() -> {
				constructionWizard.selectSite(mission);
			});
		//catch (InterruptedException | ExecutionException e) {
		//	e.printStackTrace();
		//}

		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				unpauseSimulation();
   	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				unpauseSimulation();
   	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);

	}

	// 2015-12-16 Added getConstructionWizard()
	public ConstructionWizard getConstructionWizard() {
		return constructionWizard;
	}

	/**
	 * initializes the scene
	 *
	 * @return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene initializeScene() {
		//logger.info("MainScene's initializeScene() is on " + Thread.currentThread().getName() + " Thread");
		marsNode = new MarsNode(this, stage);

		// ImageView bg1 = new ImageView();
		// bg1.setImage(new Image("/images/splash.png")); // in lieu of the
		// interactive Mars map
		// root.getChildren().add(bg1);

		// Obtain screens
		//ObservableList<Screen> screens = Screen.getScreens();
		//Screen primaryScreen = Screen.getPrimary();
		
		// Create group to hold swingNode1 which holds the swing desktop
		swingPane = new StackPane();
		swingNode = new SwingNode();

		
/*		
 *  * 	// Failed since the mouse cursor never enter swingPane 
		swingPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
		      @Override public void handle(MouseEvent event) {
		    	  
		  		screens = Screen.getScreensForRectangle(event.getX(), event.getY(), 1, 1);  	
		        //String msg =
		        //  "(x: "       + event.getX()      + ", y: "       + event.getY()       + ") -- " +
		        //  "(sceneX: "  + event.getSceneX() + ", sceneY: "  + event.getSceneY()  + ") -- " +
		        //  "(screenX: " + event.getScreenX()+ ", screenY: " + event.getScreenY() + ")";
		  		System.out.println("(x: " + event.getX()      
		  					+ ", y: "  + event.getY());
		      }
		    });
		 
		System.out.println("screens have the size of " + screens.size());		
		Screen currentScreen = screens.get(0);
		Rectangle2D rect = currentScreen.getVisualBounds();
		width = rect.getWidth();
		height = rect.getHeight();
*/		
		
/*		
 * 		// below doesn't not work for multi-monitor setup. The width will become the total width of both monitors
		Rectangle2D rect = Screen.getPrimary().getVisualBounds();
		width = rect.getWidth();
		height = rect.getHeight();
*/			
		
/*
 * 		// Issue: can't run the MSP on the "active" monitor.
		// by default MSP runs on the primary monitor (aka monitor 1 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762 
/
		StartUpLocation startUpLoc = new StartUpLocation(swingPane.getPrefWidth(), swingPane.getPrefHeight());
        double xPos = startUpLoc.getXPos();
        double yPos = startUpLoc.getYPos();
        // Set Only if X and Y are not zero and were computed correctly
        if (xPos > 0 && yPos > 0) {
            //stage.setX(xPos);
            //stage.setY(yPos);
        	screens = Screen.getScreensForRectangle(xPos, yPos, 1, 1); 
        	Screen currentScreen = screens.get(0);
    		Rectangle2D rect = currentScreen.getVisualBounds();
    		width = rect.getWidth()-80;
    		height = rect.getHeight()-80;
    		stage.setX(0);
    	    stage.setY(0);
            System.out.println(" x : " + xPos + "   y : " + yPos);

        } else {
            stage.setX(0);
            stage.setY(0);
            width = 1366-80;
    		height = 768-80;
            stage.centerOnScreen();
            System.out.println("calling centerOnScreen()");
        }
*/        
 
        
		width = 1366-80;
		height = 768-80;
		
		createSwingNode();
		swingPane.getChildren().add(swingNode);
		swingPane.setPrefWidth(width);
		swingPane.setPrefHeight(height);

		// Create ControlFX's StatusBar
		statusBar = createStatusBar();
		VBox bottomBox = new VBox();
		bottomBox.getChildren().addAll(statusBar);

		// Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		//menuBar.getStylesheets().addAll("/fxui/css/mainskin.css");

		// Create BorderPane
		borderPane = new BorderPane();
		borderPane.setTop(menuBar);
		// borderPane.setTop(toolbar);
		borderPane.setBottom(bottomBox);
		// borderPane.setStyle("-fx-background-color: palegorange");

/*
		// 2015-10-23 Creates marqueeNode and marqueePane
		SwingNode marqueeNode = null;

		SwingUtilities.invokeLater(() -> {
			marqueePane = new MarqueePane();
			marqueeNode.setContent(marqueePane);
		} );
*/
		
/*		
		// 2015-05-26 Create fxDesktopPane
		fxDesktopPane = marsNode.createFXDesktopPane();
		//fxDesktopPane.getStylesheets().add(getClass().getResource("/materialdesign/material-fx-v0_3.css").toExternalForm());

		// 2015-05-26 Create the dndTabPane.
		dndTabPane = new DndTabPane();
		StackPane containerPane = new StackPane(dndTabPane);
		//containerPane.getStylesheets().add(getClass().getResource("/materialdesign/material-fx-v0_3.css").toExternalForm());

		// We need to create the skin manually, could also be your custom skin.
		DnDTabPaneSkin skin = new DnDTabPaneSkin(dndTabPane);

		// Setup the dragging.
		DndTabPaneFactory.setup(FeedbackType.MARKER, containerPane, skin);

		// Set the skin.
		dndTabPane.setSkin(skin);
		dndTabPane.setSide(Side.RIGHT);

		// Create nodeTab
		nodeTab = new Tab();
		nodeTab.setClosable(false);
		nodeTab.setText("JavaFX UI");
		nodeTab.setContent(fxDesktopPane);
*/
		/*
		 * // create a button to toggle floating. final RadioButton floatControl
		 * = new RadioButton("Toggle floating");
		 * floatControl.selectedProperty().addListener(new
		 * ChangeListener<Boolean>() {
		 *
		 * @Override public void changed(ObservableValue<? extends Boolean>
		 * prop, Boolean wasSelected, Boolean isSelected) { if (isSelected) {
		 * tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING); } else {
		 * tabPane.getStyleClass().remove(TabPane.STYLE_CLASS_FLOATING); } } });
		 */
/*
		// Create swing tab to hold classic UI
		swingTab = new Tab();
		swingTab.setClosable(false);
		swingTab.setText("Classic UI");
		swingTab.setContent(swingPane);
		//Tab noteTab = new Tab("");

		// Set to select the swing tab at the start of simulation
		// Used dndTabPane instead of the regular TabPane
		dndTabPane.getSelectionModel().select(swingTab);
		dndTabPane.getTabs().addAll(swingTab, nodeTab);
		//borderPane.setCenter(dndTabPane);

		// wrap dndTabPane inside notificationNode
		Node notificationNode = createNotificationPane();
		borderPane.setCenter(notificationNode);
		
		
*/		
		
		borderPane.setCenter(swingPane);

		rootStackPane = new StackPane(borderPane);
		borderPane.setMinWidth(width);//1024);
		borderPane.setMinHeight(height);//480);
        
		Scene scene = new Scene(rootStackPane, width, height, Color.BROWN);

		//System.out.println("w : " + scene.getWidth() + "   h : " + scene.getHeight());
		borderPane.prefHeightProperty().bind(scene.heightProperty());
		borderPane.prefWidthProperty().bind(scene.widthProperty());

		//createCornerMenu();

		return scene;
	}
/*
	private void createCornerMenu() {
        // uninstall the current cornerMenu
        //if (cornerMenu != null) {
        //    cornerMenu.autoShowAndHideProperty().unbind();
        //    cornerMenu.removeFromPane();
         //   cornerMenu = null;
        //}
        // create a new one
        cornerMenu = new CornerMenu(CornerMenu.Location.BOTTOM_RIGHT, borderPane, true);
        //if (CornerMenu.Location.TOP_LEFT.equals(cornerMenu.getLocation())) {
        cornerMenu.getItems().addAll(navMenuItem, mapMenuItem, missionMenuItem, monitorMenuItem, searchMenuItem, eventsMenuItem);
        //cornerMenu.autoShowAndHideProperty().bind(autoShowAndHideCheckBox.selectedProperty());
        //cornerMenu.show();
        cornerMenu.setAutoShowAndHide(false);
    }
*/

	/*
	 * Sets the theme skin after calling stage.show() at the start of the sim
	 */
	public void initializeTheme() {
		//logger.info("MainScene's initializeTheme()");
		// NOTE: it is mandatory to change the theme from 1 to 2 below at the start of the sim
		// This avoids two display issues:
		// (1). the crash of Mars Navigator Tool when it was first loaded
		// (2). the inability of loading the tab icons of the Monitor Tool at the beginning
		// Also, when clicking a tab at the first time, a NullPointerException results)
		// TODO: find out if it has to do with nimrodlf and/or JIDE-related
		//rootStackPane.getStylesheets().clear();

		changeTheme(theme);
/*
		theme = 1;
		//rootStackPane
		swingPane.getStylesheets().add(getClass().getResource("/fxui/css/oliveskin.css").toExternalForm());
		menuBar.getStylesheets().add(getClass().getResource("/fxui/css/oliveskin.css").toExternalForm());

		updateStatusBarThemeColor(Color.GREEN, Color.PALEGREEN);
		lookAndFeelTheme = "LightTabaco";
*/
		// SwingUtilities is needed for MacOSX compatibility
		SwingUtilities.invokeLater(() -> {
			setLookAndFeel(theme);
			//swingNode.setContent(desktop);
		});

		//logger.info("done with MainScene's initializeTheme()");

		//isMainSceneDone = true;
	}

	/*
	 * Changes the theme skin of desktop
	 */
	public void changeTheme(int theme) {
		this.theme = theme;
		swingPane.getStylesheets().clear();
		menuBar.getStylesheets().clear();
		statusBar.getStylesheets().clear();
		
		//marsNode.getFXDesktopPane().getStylesheets().clear();
		
		marsNetButton.getStylesheets().clear();

		String cssColor;

		//logger.info("MainScene's changeTheme()");
		if (theme == 1) { // olive green
			cssColor = "/fxui/css/oliveskin.css";
			updateThemeColor(Color.GREEN, Color.PALEGREEN, cssColor); //DARKOLIVEGREEN
			//notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
			//notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/oliveskin.css").toExternalForm());
			lookAndFeelTheme = "LightTabaco";

		} else if (theme == 2) { // burgundy red
			cssColor = "/fxui/css/burgundyskin.css";
			updateThemeColor(Color.rgb(140,0,26), Color.YELLOW, cssColor); // ORANGERED
			//notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
			//notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/burgundyskin.css").toExternalForm());
			lookAndFeelTheme = "Burdeos";

		} else if (theme == 3) { // dark chocolate
			cssColor = "/fxui/css/darkTabaco.css";
			updateThemeColor(Color.DARKGOLDENROD, Color.BROWN, cssColor);
			//notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			lookAndFeelTheme = "DarkTabaco";

		} else if (theme == 4) { // grey
			cssColor = "/fxui/css/darkGrey.css";
			updateThemeColor(Color.DARKSLATEGREY, Color.DARKGREY, cssColor);
			lookAndFeelTheme = "DarkGrey";

		} else if (theme == 5) { // + purple
			cssColor = "/fxui/css/nightViolet.css";
			updateThemeColor(Color.rgb(73,55,125), Color.rgb(73,55,125), cssColor); // DARKMAGENTA, SLATEBLUE
			lookAndFeelTheme = "Night";

		} else if (theme == 6) { // + skyblue
			cssColor = "/fxui/css/snowBlue.css";
			updateThemeColor(Color.rgb(0,107,184), Color.rgb(0,107,184), cssColor); // CADETBLUE // Color.rgb(23,138,255)
			lookAndFeelTheme = "Snow";

		} else if (theme == 7) { // standard
			cssColor = "/fxui/css/nimrodskin.css";
			updateThemeColor(Color.rgb(156,77,0), Color.rgb(156,77,0), cssColor); //DARKORANGE, CORAL
			//updateThemeColor(Color.rgb(0,0,0,128), Color.rgb(0,0,0,128), cssColor); //DARKORANGE, CORAL
			lookAndFeelTheme = "nimrod";
		}

		//logger.info("done with MainScene's changeTheme()");
	}

	/*
	 * Updates the theme colors of statusBar, swingPane and menuBar
	 */
	// 2015-08-29 Added updateThemeColor()
	public void updateThemeColor(Color txtColor, Color btnTxtColor, String cssColor) {
		swingPane.getStylesheets().add(getClass().getResource(cssColor).toExternalForm());
		menuBar.getStylesheets().add(getClass().getResource(cssColor).toExternalForm());
		
		//marsNode.getFXDesktopPane().getStylesheets().add(getClass().getResource(cssColor).toExternalForm());

		memUsedText.setFill(txtColor);
		//memMaxText.setFill(txtColor);
		timeText.setFill(txtColor);
		//systemCpuLoadText.setFill(txtColor);
		//processCpuLoadText.setFill(txtColor);

		statusBar.getStylesheets().add(getClass().getResource(cssColor).toExternalForm());

		//memBtn.setTextFill(btnTxtColor);
		//clkBtn.setTextFill(btnTxtColor);
		//cpuBtn.setTextFill(btnTxtColor);
		//marsNetButton.setTextFill(btnTxtColor);

		//memBtn.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		//clkBtn.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		//cpuBtn.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		//marsNetButton.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
	}
	/**
	 * Creates and starts the earth timer
	 */
	public void startEarthTimer() {
		// Set up earth time text update
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> updateStatusBarText()));
		// Note: Infinite Timeline might result in a memory leak if not stopped properly.
		// All the objects with animated properties would not be garbage collected.
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();

	}


    /**
     * Creates and returns a {@link Flyout}
     * @return  a new {@link Flyout}
     */
    //2015-11-11 Added createFlyout()
    public Flyout createFlyout() {
        //marsNetButton = new ToggleButton(" MarsNet ");
        marsNetButton = new MaterialDesignToggleButton(" MarsNet ");
        flyout = new Flyout(marsNetButton, createChatBox());
        marsNetButton.setId("marsNetButton");
        marsNetButton.setTooltip(new Tooltip ("Open/Close MarsNet Chat Box"));
        marsNetButton.setPadding(new Insets(5, 5, 5, 5));
        marsNetButton.setOnAction(e -> {
            if (marsNetButton.isSelected()) {
                flyout.flyout();
            } else {
                flyout.dismiss();
            }
        });

        return flyout;
    }
    
    public Flyout getFlyout() {
    	return flyout;
    }

    /*
     * Creates a chat box
     * @return StackPane
     */
    //2015-11-11 Added createChatBox()
  	public StackPane createChatBox() {
  		cb = new ChatBox(this);
        cb.getAutoFillTextBox().getTextbox().requestFocus();
  		StackPane pane = new StackPane(cb);
  		pane.setPadding(new Insets(0, 0, 0, 0));
        //pane.setHgap(0);
  		return pane;
  	}

	/*
	 * Creates the status bar for MainScene
	 */
	public StatusBar createStatusBar() {
		//StatusBar statusBar = null;
		if (statusBar == null) {
			statusBar = new StatusBar();
			statusBar.setText(""); // needed for deleting the default text "OK"
		}
		// statusBar.setAlignment(Pos.BASELINE_RIGHT);
		// statusBar.setStyle("-fx-background-color: gainsboro;");
		// statusBar.setAlignment(Pos.CENTER);
		// statusBar.setStyle("-fx-border-stylel:solid; -fx-border-width:2pt;
		// -fx-border-color:grey; -fx-font: 14 arial; -fx-text-fill: white;
		// -fx-base: #cce6ff;");
		// statusBar.setMinHeight(memMaxText.getBoundsInLocal().getHeight() +
		// 10);
		// statusBar.setMijnWidth (memMaxText.getBoundsInLocal().getWidth() +
		// 10);

	    //2015-11-11 Added createFlyout()
		flyout = createFlyout();
        EffectUtilities.makeDraggable(flyout.getStage(), cb);

		statusBar.getLeftItems().add(new Separator(VERTICAL));
		statusBar.getLeftItems().add(flyout);
		//statusBar.getLeftItems().add(new Separator(VERTICAL));

		osBean = ManagementFactory.getPlatformMXBean(
				com.sun.management.OperatingSystemMXBean.class);

		statusBar.getRightItems().add(new Separator(VERTICAL));
		//cpuBtn = new Button();//" CPU ");
		//cpuBtn.setTooltip(new Tooltip(" % CPU Usage"));
		//cpuBtn.setBackground(new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(2), new Insets(1))));
		//cpuBtn.setTextFill(Color.ORANGE);
		//statusBar.getRightItems().add(new Separator(VERTICAL));
		//statusBar.getRightItems().add(cpuBtn);
		//statusBar.getRightItems().add(new Separator(VERTICAL));
		
/*
		double cpuText = osBean.getProcessCpuLoad() * 100D;
		if (cpuText > 0)
			processCpuLoad = (int) (cpuText);
		else
			processCpuLoad = 0;	
		processCpuLoadText = new Text(" CPU : " + twoDigitFormat.format(processCpuLoad) + " % ");
*/
		
		//double cpuText;
		//try {
		//	cpuText = getProcessCpuLoad();
		//	processCpuLoadText = new Text(" CPU : " + twoDigitFormat.format(processCpuLoad) + " % ");
		//} catch (Exception e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//} 
		
		
		//cpuBtn.setText(" CPU : " + twoDigitFormat.format(processCpuLoad) + " % ");
		//processCpuLoadText.setFill(Color.GREY);
		//statusBar.getRightItems().add(processCpuLoadText);
		statusBar.getRightItems().add(new Separator(VERTICAL));

		//systemCpuLoad = (int) (osBean.getSystemCpuLoad() * 100D);
		//systemCpuLoadText = new Text(" System : " + twoDigitFormat.format(systemCpuLoad) + " % ");
		//systemCpuLoadText.setFill(Color.GREY);
		//statusBar.getRightItems().add(new Separator(VERTICAL));
		//statusBar.getRightItems().add(systemCpuLoadText);


		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
		//memBtn = new Button();//" Memory ");
		//memBtn.setBackground(new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(2), new Insets(1))));
		//memBtn.setTextFill(Color.ORANGE);
		//memBtn.setTooltip(new Tooltip(" Memory used out of " + memMax + " MB designated"));
		//statusBar.getRightItems().add(memBtn);

		//memMaxText = new Text(" Designated : " + memMax + " MB ");
		//memMaxText.setFill(Color.GREY);
		//statusBar.getRightItems().add(memMaxText);

		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;
		memUsedText = new Text(" Memory : " + memUsed + " MB ");
		memUsedText.setId("mem-text");
		//memBtn
		memUsedText.setText(" Memory : " + memUsed + " MB ");
		
		//memUsedText.setStyle("-fx-text-inner-color: orange;");
		//memUsedText.setFill(Color.GREY);
		statusBar.getRightItems().add(memUsedText);
		statusBar.getRightItems().add(new Separator(VERTICAL));

		MasterClock master = Simulation.instance().getMasterClock();
		if (master == null) {
			throw new IllegalStateException("master clock is null");
		}
		EarthClock earthclock = master.getEarthClock();
		if (earthclock == null) {
			throw new IllegalStateException("earthclock is null");
		}

		//clkBtn = new Button(" Earth Time ");
		//clkBtn.setTooltip(new Tooltip("Simulation begins at 2043-Sep-30 00:00:00 (UT)"));
		//clkBtn.setTextFill(Color.ORANGE);
		//clkBtn.setBackground(new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(2), new Insets(1))));
		//statusBar.getRightItems().add(clkBtn);

		timeText = new Text(" " + timeStamp + "  ");
		// timeText.setStyle("-fx-text-inner-color: orange;");
		//timeText.setId("time-text");
		//timeText.setFill(Color.GREY);
		//clkBtn
		timeText.setText(" Earth Time : " + timeStamp + "  ");
		statusBar.getRightItems().add(timeText);
		statusBar.getRightItems().add(new Separator(VERTICAL));

		return statusBar;
	}



	public NotificationPane getNotificationPane() {
		return notificationPane;
	}

	public Node createNotificationPane() {
		// wrap the dndTabPane inside notificationNode
		notificationPane = new NotificationPane(dndTabPane);

		String imagePath = getClass().getResource("/notification/notification-pane-warning.png").toExternalForm();
		ImageView image = new ImageView(imagePath);
		notificationPane.setGraphic(image);
		notificationPane.getActions().addAll(new Action("Close", ae -> {
			// do sync, then hide...
			notificationPane.hide();
		} ));

		notificationPane.setShowFromTop(false);
		// notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		 notificationPane.setText("Breaking news for mars-simmers !!");
		// notificationPane.hide();
		return notificationPane;
	}


	public String getSampleName() {
		return "Notification Pane";
	}

	public String getControlStylesheetURL() {
		return "/org/controlsfx/control/notificationpane.css";
	}


	/*
	 * Updates the cpu loads, memory usage and time text in the status bar
	 */
	public void updateStatusBarText() {

		String t = null;
		// try {
		// Check if new simulation is being created or loaded from file.
		if (!Simulation.isUpdating()) {

			MasterClock master = Simulation.instance().getMasterClock();
			if (master == null) {
				throw new IllegalStateException("master clock is null");
			}
			EarthClock earthclock = master.getEarthClock();
			if (earthclock == null) {
				throw new IllegalStateException("earthclock is null");
			}
			t = earthclock.getTimeStamp();
			// timeStamp = new SimpleStringProperty(earthclock.getTimeStamp());
		}
		// }
		// catch (Exception ee) {
		// ee.printStackTrace(System.err);
		// }
		//timeText.setText(" " + t + "  ");
		//clkBtn
		timeText.setText(" Earth Time : " + t + " ");
		timeText.setStyle("-fx-text-inner-color: orange;");
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;
		// int mem = ( memUsedCache + memUsed ) /2;
		if (memUsed > memUsedCache * 1.1 || memUsed < memUsedCache * 0.9) {
			memUsedText.setText(" Memory : " + memUsed + " MB ");
			//memBtn.setText(" Memory : " + memUsed + " MB ");
			memUsedText.setStyle("-fx-text-inner-color: orange;");
		}
		memUsedCache = memUsed;

		//processCpuLoad = (int) (osBean.getProcessCpuLoad() * 100D);
		
		//double cpuText;
		//try {
		//	cpuText = getProcessCpuLoad();
		//	processCpuLoadText.setText(" CPU : " + twoDigitFormat.format(processCpuLoad) + " % ");
		//} catch (Exception e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//} 
/*		
		//osBean.getProcessCpuLoad() * 100D;

		if (cpuText > 0) {
			processCpuLoad = (int) (cpuText);
			processCpuLoadText.setText(" CPU : " + twoDigitFormat.format(processCpuLoad) + " % ");		
		}
		else {
			osBean = ManagementFactory.getPlatformMXBean(
					com.sun.management.OperatingSystemMXBean.class);
		}
		
		//processCpuLoadText.setFill(Color.GREY);
*/
		//systemCpuLoad = (int) (osBean.getSystemCpuLoad() * 100D);
		//systemCpuLoadText.setText(" System : " + twoDigitFormat.format(systemCpuLoad) + " % ");
		//systemCpuLoadText.setFill(Color.GREY);

	}

	public static double getProcessCpuLoad() throws Exception {

	    MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
	    ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
	    AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

	    if (list.isEmpty())     return Double.NaN;

	    Attribute att = (Attribute)list.get(0);
	    Double value  = (Double)att.getValue();

	    // usually takes a couple of seconds before we get real values
	    if (value == -1.0)      return Double.NaN;
	    // returns a percentage value with 1 decimal point precision
	    return ((int)(value * 1000) / 10.0);
	}
	
	/**
	 * Gets the main desktop panel.
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		// return mainWindow.getDesktop();
		return desktop;
	}

	public boolean isMainSceneDone() {
		return isMainSceneDoneLoading;
	}
	/**
	 * Load a previously saved simulation.
	 * @param type
	 */
	// 2015-01-25 Added autosave
	public void loadSimulation(int type) {
		//logger.info("MainScene's loadSimulation() is on " + Thread.currentThread().getName() + " Thread");
		
		// if (earthTimer != null)
		// earthTimer.stop();
		// earthTimer = null;

		// timeline.stop(); // Note: no need to stop and restart at all

		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						loadSimulationProcess(type);
					} );
				}
			};
			loadSimThread.start();
		} else {
			loadSimThread.interrupt();
		}
		
	}

	/**
	 * Performs the process of loading a simulation.
	 * @param type
	 */
	public void loadSimulationProcess(int type) {
		logger.info("MainScene's loadSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");

		String dir = null;
		String title = null;
		File fileLocn = null;

		if (type == DEFAULT) {
			dir = Simulation.DEFAULT_DIR;
		}

		else if (type == AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		}

		else if (type == OTHER) {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}

		if (type == AUTOSAVE || type == OTHER) {
			FileChooser chooser = new FileChooser();
			// chooser.setInitialFileName(dir);
			// Set to user directory or go to default if cannot access
			// String userDirectoryString = System.getProperty("user.home");
			File userDirectory = new File(dir);
			chooser.setInitialDirectory(userDirectory);
			chooser.setTitle(title); // $NON-NLS-1$

			// Set extension filter
			FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
					"*.sim");
			FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");

			chooser.getExtensionFilters().addAll(simFilter, allFilter);

			// Show open file dialog
			File selectedFile = chooser.showOpenDialog(stage);

			if (selectedFile != null)
				fileLocn = selectedFile;
			else
				return;
		}

		else if (type == DEFAULT) {
			fileLocn = null;
		}

		desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
		desktop.clearDesktop();

		
		//Simulation.instance().loadSimulation(fileLocn);
		logger.info("");
		logger.info("Restarting " + Simulation.WINDOW_TITLE);

		Simulation.instance().loadSimulation(fileLocn);
		//imulation.instance().getSimExecutor().submit(new LoadSimulationTask(fileLocn));

		try {
			TimeUnit.MILLISECONDS.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while (Simulation.instance().getMasterClock() == null) {// || Simulation.instance().getMasterClock().isLoadingSimulation()) {
			System.out.println("MainScene : the master clock instance is not ready yet. Wait for another 1/2 secs");
			try {
				TimeUnit.MILLISECONDS.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		try {
			SwingUtilities.invokeLater(() -> {
				desktop.resetDesktop();
			});		
			logger.info("LoadSimulationProcess() : desktop.resetDesktop() is done");
		} catch (Exception e) {
			// New simulation process should continue even if there's an
			// exception in the UI.
			logger.severe(e.getMessage());
			e.printStackTrace(System.err);
		}

		
		// load UI config
		//UIConfig.INSTANCE.parseFile();

		desktop.disposeAnnouncementWindow();

		
		// 2016-03-22 uncheck all tool windows in the menu bar
        Collection<ToolWindow> toolWindows = desktop.getToolWindowsList();   
        Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
			menuBar.uncheckToolWindow(i.next().getToolName());
		}
		
/*
 * 		// Note: it should save and load up the previous desktop setting instead of the Guide Tool
		// Open Guide tool after loading.
        desktop.openToolWindow(GuideWindow.NAME);
        GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
    	int Xloc = (int)((stage.getScene().getWidth() - ourGuide.getWidth()) * .5D);
		int Yloc = (int)((stage.getScene().getHeight() - ourGuide.getHeight()) * .5D);
		ourGuide.setLocation(Xloc, Yloc);
        ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$	
*/
		
		unpauseSimulation();
		
	}


   /*
    * Loads settlement data from a default saved sim
    */
	public class LoadSimulationTask implements Runnable {
		File fileLocn;
		
		LoadSimulationTask(File fileLocn){
			this.fileLocn = fileLocn;
		}
		
		public void run() {
			logger.info("LoadSimulationTask is on " + Thread.currentThread().getName() + " Thread");
			logger.info("Loading settlement data from the default saved simulation...");
			
			//MasterClock clock = Simulation.instance().getMasterClock();
			//clock.loadSimulation(fileLocn);
			
			Simulation.instance().loadSimulation(fileLocn); // null means loading "default.sim"
			Simulation.instance().stop();
			//Simulation.instance().getMasterClock().removeClockListener(oldListener);
			Simulation.instance().start(false);
			
			//Simulation.instance().stop();
			//Simulation.instance().start();
		}
	}


	
	/**
	 * Create a new simulation.
	 */
	public void newSimulation() {
		//logger.info("MainScene's newSimulation() is on " + Thread.currentThread().getName() + " Thread");

		if ((newSimThread == null) || !newSimThread.isAlive()) {
			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						newSimulationProcess();
					} );
				}
			};
			newSimThread.start();
		} else {
			newSimThread.interrupt();
		}

	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	private void newSimulationProcess() {
		//logger.info("MainScene's newSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Starting new sim");
		alert.setHeaderText(Msg.getString("MainScene.new.header"));
		alert.setContentText(Msg.getString("MainScene.new.content"));
		ButtonType buttonTypeOne = new ButtonType("Save & Exit");
		//ButtonType buttonTypeTwo = new ButtonType("End Sim");
		ButtonType buttonTypeCancel = new ButtonType("Back to Sim");//, ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, 
				//buttonTypeTwo, 
				buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			saveOnExit();
			desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
			exitSimulation();
			Platform.exit();
		//} else if (result.get() == buttonTypeTwo) {
		//	desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
		//	endSim();
		} else if (result.get() == buttonTypeCancel) {//!result.isPresent())
			return;
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 *
	 * @param useDefault
	 *            Should the user be allowed to override location?
	 */
	public void saveSimulation(int type) {
		//logger.info("MainScene's saveSimulation() is on " + Thread.currentThread().getName() + " Thread");

		// 2015-12-18 Check if it was previously on pause
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		// Pause simulation.
		if (!previous) {
			pauseSimulation();
			//System.out.println("previous2 is false. Paused sim");
		}
		desktop.getTimeWindow().enablePauseButton(false);


		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						saveSimulationProcess(type);
					} );
				}
			};
			saveSimThread.start();
		} else {
			saveSimThread.interrupt();
		}


		// 2015-12-18 Check if it was previously on pause
		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				unpauseSimulation();
	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				unpauseSimulation();
	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);

	}

	/**
	 * Performs the process of saving a simulation.
	 */
	// 2015-01-08 Added autosave
	private void saveSimulationProcess(int type) {
		//logger.info("MainScene's saveSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");


		File fileLocn = null;
		String dir = null;
		String title = null;
		// 2015-01-25 Added autosave
		if (type == AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			// title = Msg.getString("MainWindow.dialogAutosaveSim"); don't need
		} else if (type == DEFAULT || (type == SAVE_AS)) {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainScene.dialogSaveSim");
		}

		if (type == SAVE_AS) {
			FileChooser chooser = new FileChooser();
			File userDirectory = new File(dir);
			chooser.setTitle(title); // $NON-NLS-1$
			chooser.setInitialDirectory(userDirectory);
			// Set extension filter
			FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
					"*.sim");
			//FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");
			chooser.getExtensionFilters().add(simFilter); // , allFilter);
			File selectedFile = chooser.showSaveDialog(stage);
			if (selectedFile != null)
				fileLocn = selectedFile; // + Simulation.DEFAULT_EXTENSION;
			else
				return;
		}

		MasterClock clock = Simulation.instance().getMasterClock();

		if (type == AUTOSAVE) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation();
		} else if (type == SAVE_AS || type == DEFAULT) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}

/*
  		// Note: the following Thread.sleep() causes system to hang in MacOSX, but not in Windows
		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}

*/

		//desktop.disposeAnnouncementWindow();

	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow(Msg.getString("MainWindow.pausingSim")); //$NON-NLS-1$
		//autosaveTimeline.pause();
		desktop.getMarqueeTicker().pauseMarqueeTimer(true);
		Simulation.instance().getMasterClock().setPaused(true);
		//desktop.getTimeWindow().enablePauseButton(false);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		Simulation.instance().getMasterClock().setPaused(false);
		desktop.getMarqueeTicker().pauseMarqueeTimer(false);
		//autosaveTimeline.play();
		desktop.disposeAnnouncementWindow();
		//desktop.getTimeWindow().enablePauseButton(true);
	}


	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves the main menu running
	 */
	private void endSim() {
		//logger.info("MainScene's endSim() is on " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().endSimulation();
		Simulation.instance().getSimExecutor().shutdownNow();

		// Simulation.instance().destroyOldSimulation();
		getDesktop().clearDesktop();
		// getDesktop().resetDesktop();
		// Simulation.instance().getMasterClock().exitProgram();

		statusBar = null;
		stage.close();
		// Simulation.instance().endMasterClock();
		//Simulation.instance().startSimExecutor();
	}

	/**
	 * Exits the current simulation and the main menu.
	 */
	public void exitSimulation() {
		//logger.info("MainScene's exitSimulation() is on " + Thread.currentThread().getName() + " Thread");
		desktop.openAnnouncementWindow(Msg.getString("MainScene.exitSim"));

		logger.info("Exiting simulation");

		Simulation sim = Simulation.instance();
		/*
		 * // Save the UI configuration. UIConfig.INSTANCE.saveFile(this);
		 *
		 * // Save the simulation.
		 *
		 * try { sim.getMasterClock().saveSimulation(null); } catch (Exception
		 * e) { logger.log(Level.SEVERE,
		 * Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
		 * e.printStackTrace(System.err); }
		 */

		sim.getMasterClock().exitProgram();
		Platform.exit();
	}

	/**
	 * Sets the look and feel of the UI
	 *
	 * @param nativeLookAndFeel
	 *            true if native look and feel should be used.
	 */
	// 2015-05-02 Edited setLookAndFeel()
	public void setLookAndFeel(int choice) {
		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
		boolean changed = false;
		// String currentTheme =
		// UIManager.getLookAndFeel().getClass().getName();
		// System.out.println("CurrentTheme is " + currentTheme);
		if (choice == 0) { // theme == "nativeLookAndFeel"
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				lookAndFeelTheme = "system";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (choice == 1) { // theme == "nimRODLookAndFeel"
			try {

				if (lookAndFeelTheme.equals("nimrod"))
					// Use default theme
					try {
						UIManager.setLookAndFeel(new NimRODLookAndFeel());
						changed = true;
					} catch (Exception e) {
						e.printStackTrace();
					}

				else {
					/*
					 * //TODO: let user customize theme in future NimRODTheme nt
					 * = new NimRODTheme(); nt.setPrimary1(new
					 * java.awt.Color(10,10,10)); nt.setPrimary2(new
					 * java.awt.Color(20,20,20)); nt.setPrimary3(new
					 * java.awt.Color(30,30,30)); NimRODLookAndFeel NimRODLF =
					 * new NimRODLookAndFeel(); NimRODLF.setCurrentTheme( nt);
					 */
					NimRODTheme nt = new NimRODTheme(
							getClass().getClassLoader().getResource("theme/" + lookAndFeelTheme + ".theme"));
					NimRODLookAndFeel nf = new NimRODLookAndFeel();
					nf.setCurrentTheme(nt);
					UIManager.setLookAndFeel(nf);
					changed = true;
				}

			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (choice == 2) {
			try {
				// Set Nimbus look & feel if found in JVM.
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) { //$NON-NLS-1$
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						lookAndFeelTheme = "nimbus";
						changed = true;
						break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					lookAndFeelTheme = "metal";
					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {
			if (desktop != null) {
				SwingUtilities.updateComponentTreeUI(desktop);
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
				desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
			}
		}

		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
	}

	public MainSceneMenu getMainSceneMenu() {
		return menuBar;
	}

	public Stage getStage() {
		return stage;
	}

	private void createSwingNode() {
		setLookAndFeel(1);
		desktop = new MainDesktopPane(this);
		SwingUtilities.invokeLater(() -> {
			//desktop = new MainDesktopPane(this);
			setLookAndFeel(1);
			swingNode.setContent(desktop);
		} );
		// desktop.openInitialWindows();
	}

	public SwingNode getSwingNode() {
		return swingNode;
	}

/*	
	public void openSwingTab() {
		// splitPane.setDividerPositions(1.0f);
		dndTabPane.getSelectionModel().select(swingTab);
		//rootStackPane.getStylesheets().add("/fxui/css/mainskin.css");
	}

	public void openMarsNet() {
		// splitPane.setDividerPositions(0.8f);
		dndTabPane.getSelectionModel().select(nodeTab);
	}
*/
	
	/**
	 * Creates an Alert Dialog to confirm ending or exiting the simulation or
	 * MSP
	 */
	public boolean alertOnExit() {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Leaving the sim");
		alert.initOwner(stage);
		alert.setHeaderText(Msg.getString("MainScene.exit.header"));
		alert.setContentText(Msg.getString("MainScene.exit.content"));
		ButtonType buttonTypeOne = new ButtonType("Save & Exit");
		//ButtonType buttonTypeTwo = new ButtonType("End Sim");
		ButtonType buttonTypeThree = new ButtonType("Exit Sim");
		ButtonType buttonTypeCancel = new ButtonType("Back to Sim", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, 
				//buttonTypeTwo, 
				buttonTypeThree, buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			saveOnExit();
			endSim();
			exitSimulation();
			return true;
		//} else if (result.get() == buttonTypeTwo) {
		//	desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
		//	endSim();			
		//	return true;
		} else if (result.get() == buttonTypeThree) {
			endSim();
			exitSimulation();
			return true;
		} else { //if (result.get() == buttonTypeCancel) {
			return false;
		}
	}

	/**
	 * Initiates the process of saving a simulation.
	 */
	public void saveOnExit() {
		//logger.info("MainScene's saveOnExit() is on " + Thread.currentThread().getName() + " Thread");

		desktop.openAnnouncementWindow(Msg.getString("MainScene.defaultSaveSim"));
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
		Simulation sim = Simulation.instance();
		try {
			sim.getMasterClock().saveSimulation(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}
	}

	public void openInitialWindows() {
		//logger.info("MainScene's openInitialWindows() is on " + Thread.currentThread().getName() + " Thread");
		String OS = System.getProperty("os.name").toLowerCase();
		//System.out.println("OS is " + OS);
		if (OS.equals("mac os x")) {
		// SwingUtilities needed below for MacOSX
			SwingUtilities.invokeLater(() -> {
				desktop.openInitialWindows();
			});
		}
		else {
/*
			GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
			System.out.println("ourGuide.getWidth() is " + ourGuide.getWidth());
			System.out.println("ourGuide.getHeight() is " + ourGuide.getHeight());
			System.out.println("swingPane.getWidth() is " + swingPane.getWidth());
			System.out.println("swingPane.getHeight() is " + swingPane.getHeight());
			System.out.println("stage.getScene().getWidth() is " + stage.getScene().getWidth());
			System.out.println("stage.getScene().getHeight() is " + stage.getScene().getHeight());

			int Xloc = (int)((stage.getScene().getWidth() - ourGuide.getWidth()) * .5D);
			int Yloc = (int)((stage.getScene().getHeight() - ourGuide.getHeight()) * .5D);
			//System.out.println("Xloc is " + Xloc + "  Yloc is " + Yloc);
			ourGuide.setLocation(Xloc, Yloc);
			ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
*/
			desktop.openInitialWindows();
		}

		//2015-09-27 for testing the use of fxml
		//marsNode.createMaterialDesignWindow();
		//marsNode.createSettlementWindow();
		
		// 2016-02-25 Disabled marsNode
		//marsNode.createStory();
		//marsNode.createDragDrop();
		
		//marsNode.createEarthMap();
		//marsNode.createMarsMap();
		//marsNode.createChatBox();
		
		isMainSceneDoneLoading = true;
		
		
		openQuote(quote);
	}

	/*
	 * Create a quote using Enzo's Notification  
	 */
	// 2016-04-21 Added openQuote()
	public void openQuote(String[] quoteArray) {

        Array.set(quoteArray, 0 ,quote0);
        Array.set(quoteArray, 1 ,quote1);
        Array.set(quoteArray, 2 ,quote2);
        Array.set(quoteArray, 3 ,quote3);
        Array.set(quoteArray, 4 ,quote4);
        
		// Randomly select a quote
		int rand = RandomUtil.getRandomInt(0, 3);	
		String quoteString = quoteArray[rand];
		
		int length = quoteString.length();
		int lines = length/45 + 1;
		int height = lines * 23;
		
        notifier = Notification.Notifier.INSTANCE;
		notifier.setHeight(height);
        notifier.setWidth(370);
        notifier.setNotificationOwner(stage);
        Duration duration = new Duration(20000);
        notifier.setPopupLifetime(duration); 
		//Notification n0 = new NotificationFX("QUOTATION", quoteString, QUOTE_ICON);//QUOTE_ICON);	
		notifier.notify("QUOTATION", quoteString, Notification.INFO_ICON);// QUOTE_ICON);//n0);
				
		notify_timeline = new Timeline(new KeyFrame(Duration.millis(21000), ae -> stopNotification()));
		notify_timeline.setCycleCount(1);//javafx.animation.Animation.INDEFINITE);
		notify_timeline.play();
		
	}
	
	public void stopNotification() {
		notify_timeline.stop();
		notifier.stop();
	}
	
	public MarsNode getMarsNode() {
		return marsNode;
	}

	public static int getTheme() {
		return theme;
	}

	//public Scene getScene() {
	//	return scene;
	//}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public StackPane getRootStackPane() {
		return rootStackPane;
	}

	public BorderPane getBorderPane() {
		return borderPane;
	}

    private MenuItem registerAction(MenuItem menuItem) {

        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //showPopup(borderPane, "You clicked the " + menuItem.getText() + " icon");
            	System.out.println("You clicked the " + menuItem.getText() + " icon");
            }

        });

        return menuItem;

    }

	public void destroy() {

	    navMenuItem = null;
	    mapMenuItem = null;
	    missionMenuItem = null;
	    monitorMenuItem = null;
	    searchMenuItem = null;
	    eventsMenuItem = null;
	    timeStamp = null;
	    memUsedText = null;
		//processCpuLoadText = null;
		memBtn = null;
		clkBtn = null;
		//cpuBtn = null;
		rootStackPane = null;
		statusBar = null;
		flyout = null;
		marsNetButton = null;
		cb = null;
		//cornerMenu = null;
		swingPane = null;
		borderPane = null;
		//fxDesktopPane = null;
		//autosaveTimeline = null;

		newSimThread = null;
		loadSimThread = null;
		saveSimThread = null;

		stage = null;

		swingTab = null;
		nodeTab = null;
		dndTabPane = null;
		timeline = null;
		notify_timeline = null;
		notificationPane = null;

		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
		transportWizard = null;
		constructionWizard = null;

	}

}
