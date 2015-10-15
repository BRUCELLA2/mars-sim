/**
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @version 3.08 2015-04-03
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.stage.Popup;
import javafx.scene.Node;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;


// TODO: is extending to JInternalFrame better?
public class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;

	private JMenuItem itemOne, itemTwo, itemThree;
    //private Building building;
    //private Vehicle vehicle;
    private Unit unit;
    private Settlement settlement;
	private MainDesktopPane desktop;
	private MainScene mainScene;
	private MainWindow mainWindow;

	private double initialX, initialY;

    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
    	//this.building = building;
    	//this.vehicle = vehicle;
    	this.unit = unit;
    	this.settlement = swindow.getMapPanel().getSettlement();
        this.desktop = swindow.getDesktop();

        if (desktop.getMainWindow() != null)
        	mainWindow = desktop.getMainWindow();
        else if (desktop.getMainScene() != null)
        	mainScene = desktop.getMainScene();

        UIResource res = new BorderUIResource.LineBorderUIResource(Color.orange);
        UIManager.put("PopupMenu.border", res);
        //force to the Heavyweight Component or able for AWT Components
        this.setLightWeightPopupEnabled(false);

    	itemOne = new JMenuItem(Msg.getString("PopUpUnitMenu.itemOne"));
        itemTwo = new JMenuItem(Msg.getString("PopUpUnitMenu.itemTwo"));
        itemThree = new JMenuItem(Msg.getString("PopUpUnitMenu.itemThree"));
        itemOne.setForeground(new Color(139,69,19));
        itemTwo.setForeground(new Color(139,69,19));
        itemThree.setForeground(new Color(139,69,19));

        if (unit instanceof Person) {
        	add(itemTwo);
        	buildItemTwo(unit);
        }

        else if (unit instanceof Vehicle) {
        	add(itemOne);
        	add(itemTwo);
        	add(itemThree);
        	buildItemOne(unit);
            buildItemTwo(unit);
            buildItemThree(unit);
        }
        else if (unit instanceof Building) {
            add(itemOne);
        	add(itemTwo);
        	buildItemOne(unit);
            buildItemTwo(unit);
        }
        else if (unit instanceof Robot) {
            //add(itemOne);
        	add(itemTwo);
        	//buildItemOne(unit);
            buildItemTwo(unit);
        }
     /*
     // Determine what the GraphicsDevice can support.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        boolean isPerPixelTranslucencySupported =
            gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT);

        //If translucent windows aren't supported, exit.
        if (!isPerPixelTranslucencySupported) {
            System.out.println(
                "Per-pixel translucency is not supported");
                System.exit(0);
        }
        */

    }


    public void buildItemOne(final Unit unit) {

        itemOne.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
  
            	if (mainScene != null) {
					Platform.runLater(() -> {
                       	createDescriptionPanelFX(unit);
					});
                }
                else {
                	createDescriptionPanel(unit);
                }           	
             }
        });
    }
    
    
    public void createDescriptionPanelFX(Unit unit) {
		
		String description;
		String type;
		String name;
		
		if (unit instanceof Vehicle) {
			Vehicle vehicle = (Vehicle) unit;
			description = vehicle.getDescription(vehicle.getVehicleType());
			type = Conversion.capitalize(vehicle.getVehicleType());
			name = Conversion.capitalize(vehicle.getName());
		}
		else {
			Building building = (Building) unit;
			description = building.getDescription();
			type = building.getBuildingType();
			name = building.getNickName();
		}
		
			
		double num = description.length() * 1.3D + 130D;
		if (num > 450)
			num = 450;
		int frameHeight = (int) num;
		

		UnitInfoPanel unitInfoPanel = new UnitInfoPanel(desktop);
		unitInfoPanel.init(name, type, description);
		unitInfoPanel.setOpaque(false);
		unitInfoPanel.setBackground(new Color(0,0,0,128));
		
	 	Stage stage = new Stage();

    	//Popup stage = new Popup();
    	SwingNode swingNode  = new SwingNode();
    	StackPane swingPane = new StackPane();

		swingPane.getChildren().add(swingNode);
		//Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		//swingPane.setPrefWidth(primaryScreenBounds.getWidth());

		SwingUtilities.invokeLater(() -> {
			swingNode.setContent(unitInfoPanel);
	    });

	   	Scene scene = new Scene(swingPane, 350, frameHeight);
	   	scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
	   	//swingPane.setFill(javafx.scene.paint.Color.TRANSPARENT);
	   	
	   	stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));//toString()));

	   	stage.requestFocus();

	   	addDraggableNode(swingNode);

	   	//stage.setTitle("Building Detail");
	   	stage.initStyle(StageStyle.TRANSPARENT);
	    stage.initStyle(StageStyle.UNDECORATED);
		stage.setResizable(false);
	   	stage.setScene(scene);
        stage.show();


	   	stage.focusedProperty().addListener(new ChangeListener<Boolean>()
	   	{
	   	  @Override
	   	  public void changed(javafx.beans.value.ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
	   	  {
	   	    stage.close();
	   	  }
	   	});
	
		
	}
    
    public void createDescriptionPanel(Unit unit) {
		
		String description;
		String type;
		String name;
		
		if (unit instanceof Vehicle) {
			Vehicle vehicle = (Vehicle) unit;
			description = vehicle.getDescription(vehicle.getVehicleType());
			type = Conversion.capitalize(vehicle.getVehicleType());
			name = Conversion.capitalize(vehicle.getName());
		}
		else {
			Building building = (Building) unit;
			description = building.getDescription();
			type = building.getBuildingType();
			name = building.getNickName();
		}
		
		setOpaque(false);
		
		JFrame f = new JFrame();
		f.setAlwaysOnTop(true);
		f.setFocusable(true);
		//JInternalFrame d = new JInternalFrame();
		//final JDialog d = new JDialog();
		f.setForeground(Color.YELLOW); // orange font
		f.setFont( new Font("Arial", Font.BOLD, 14 ) );
		
		
		double num = description.length() * 1.3D + 130D;
		if (num > 450)
			num = 450;
		int frameHeight = (int) num;
		
		f.setSize(350, frameHeight); // undecorated 301, 348 ; decorated : 303, 373
		f.setResizable(false);
		f.setUndecorated(true);
		f.setBackground(new Color(0,0,0,0)); // not working for decorated jframe
		
		UnitInfoPanel b = new UnitInfoPanel(desktop);
		b.init(name, type, description);
		
		f.add(b);
		
		//2014-11-27 Added ComponentMover Class
		ComponentMover mover = new ComponentMover(f, b, f.getContentPane());
		mover.registerComponent(b);
		
		
		// Make the buildingPanel to appear at the mouse cursor
		Point location = MouseInfo.getPointerInfo().getLocation();
		f.setLocation(location);
		
		f.setVisible(true);
		f.addWindowFocusListener(new WindowFocusListener() {
		    public void windowLostFocus(WindowEvent e) {
		    	if (!mover.isMousePressed())
		    		f.dispose();
		    }
		    public void windowGainedFocus(WindowEvent e) {
		    	//f.setVisible(true);
		    }
		});
	}
    
/* BACKUP
   public void buildItemOne(final Unit unit) {

        itemOne.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	setOpaque(false);
            	final JDialog d = new JDialog();
                d.setForeground(Color.YELLOW); // orange font
                d.setFont( new Font("Arial", Font.BOLD, 14 ) );

                String description;
                String type;
                String name;

                if (unit instanceof Vehicle) {
                	Vehicle vehicle = (Vehicle) unit;
                	description = vehicle.getDescription(vehicle.getVehicleType());
                	type = WordUtils.capitalize(vehicle.getVehicleType());
                	name = WordUtils.capitalize(vehicle.getName());
                }
                else {
                	Building building = (Building) unit;
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getNickName();
                }


			    d.setSize(350, 300); // undecorated 301, 348 ; decorated : 303, 373
		        d.setResizable(false);
		        d.setUndecorated(true);
		        d.setBackground(new Color(0,0,0,0));

			    UnitInfoPanel b = new UnitInfoPanel(desktop);
			    b.init(name, type, description);

			    d.add(b);

            	// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                d.setLocation(location);

                d.setVisible(true);
				d.addWindowFocusListener(new WindowFocusListener() {
				    public void windowLostFocus(WindowEvent e) {
				    	d.dispose();
				    }
				    public void windowGainedFocus(WindowEvent e) {
				    }
				});

			    //2014-11-27 Added ComponentMover Class
			    ComponentMover mover = new ComponentMover(d,b);
			    mover.registerComponent(b);

             }
        });
    }
   */

    public void buildItemTwo(final Unit unit) {
        itemTwo.addActionListener(new ActionListener() {
            @SuppressWarnings("restriction")
			public void actionPerformed(ActionEvent e) {

	            if (unit instanceof Vehicle) {
	            	Vehicle vehicle = (Vehicle) unit;
	            	desktop.openUnitWindow(vehicle, false);
	            }
	            else if (unit instanceof Person) {
	            	Person person =(Person) unit;
	            	desktop.openUnitWindow(person, false);
	            }
	            else if (unit instanceof Robot) {
	            	Robot robot =(Robot) unit;
	            	desktop.openUnitWindow(robot, false);
	            }
	            else {
	            	Building building = (Building) unit;

                    if (mainScene != null) {
    					Platform.runLater(() -> {
                           	createBuildingPanelFX(building);
    					});
                    }
                    else {
                    	createBuildingPanel(building);
                    }
	            } // end of building
	         }
	    });

    }

	/*
	 * Creates a stage for displaying the detail status of a building
	 */
    public void createBuildingPanelFX(Building building) {
    	Stage stage = new Stage();

    	//Popup stage = new Popup();
    	SwingNode swingNode  = new SwingNode();
    	StackPane swingPane = new StackPane();

		swingPane.getChildren().add(swingNode);
		//Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		//swingPane.setPrefWidth(primaryScreenBounds.getWidth());

		final BuildingPanel buildingPanel = new BuildingPanel(true, "Building Detail", building, desktop);

		SwingUtilities.invokeLater(() -> {
			swingNode.setContent(buildingPanel);
	    });

	   	Scene scene = new Scene(swingPane, 400, 400);
	    //stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));//toString()));

	   	stage.requestFocus();

	   	addDraggableNode(swingNode);

	   	//stage.setTitle("Building Detail");
	   	//stage.initStyle(StageStyle.UTILITY);
	   	stage.initStyle(StageStyle.TRANSPARENT);
		stage.setResizable(false);
	   	stage.setScene(scene);
        stage.show();


	   	stage.focusedProperty().addListener(new ChangeListener<Boolean>()
	   	{
	   	  @Override
	   	  public void changed(javafx.beans.value.ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
	   	  {
	   	    stage.close();
	   	  }
	   	});

    }

    private void addDraggableNode(final Node node) {

        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    initialX = me.getSceneX();
                    initialY = me.getSceneY();
              }
            }
        });

        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    node.getScene().getWindow().setX(me.getScreenX() - initialX);
                    node.getScene().getWindow().setY(me.getScreenY() - initialY);
                }
            }
        });
    }
/*
    public void createBuildingPanelFX(Building building) {
    	//System.out.println("PopUpUnitMenu.java : createBuildingPanelFX()");
      	@SuppressWarnings("restriction")
		Alert alert = new Alert(AlertType.INFORMATION);
		//alert.initModality(Modality.APPLICATION_MODAL);
		//alert.initModality(Modality.WINDOW_MODAL);

		alert.initOwner(mainScene.getStage());
		double x = mainScene.getStage().getWidth();
		double y = mainScene.getStage().getHeight();
		double xx = alert.getDialogPane().getWidth();
		double yy = alert.getDialogPane().getHeight();
		alert.setX((x - xx)/2);
		alert.setY((y - yy)*3/4);

		alert.setTitle(settlement.getName());
		alert.setHeaderText("Inside " + building.getNickName());
		alert.setContentText("Building Detail");
		//DialogPane dialogPane = alert.getDialogPane();

		alert.show();

		// move mouse focus to Alert. if off close, execute alert.close();
	   	//System.out.println("PopUpUnitMenu.java : done createBuildingPanelFX()");
    }
*/
    public void createBuildingPanel(Building building) {

    	JFrame f = new JFrame();
    	//final JDialog d = new JDialog();
    	f.setAlwaysOnTop (true);
    	f.setFocusable(true);
    	//d.setModal (true);
    	//d.setModalityType (ModalityType.APPLICATION_MODAL);
		//2014-11-27 Added ComponentMover Class
        // Make panel drag-able
		ComponentMover mover = new ComponentMover(f, f.getContentPane());
		mover.registerComponent(f);

		final BuildingPanel buildingPanel = new BuildingPanel(true, "Building Detail", building, desktop);
		buildingPanel.setOpaque(false);
        buildingPanel.setBackground(new Color(0,0,0,150));
        buildingPanel.setTheme(true);

		// Make the buildingPanel to appear at the mouse cursor
        Point location = MouseInfo.getPointerInfo().getLocation();
        f.setLocation(location);

		f.setUndecorated(true);
        f.setBackground(new Color(51,25,0,128)); // java.awt.IllegalComponentStateException: The dialog is decorated
        f.add(buildingPanel);
		f.setSize(320, 350);  // undecorated: 300, 335; decorated: 310, 370
		f.setLayout(new FlowLayout());

		f.setVisible(true);
		f.getRootPane().setBorder( BorderFactory.createLineBorder(Color.orange) );

	    f.addWindowFocusListener(new WindowFocusListener() {
			public void windowLostFocus(WindowEvent e) {
		    	//JWindow w = (JWindow) e.getSource();
		    	if (!mover.isMousePressed())
		    		f.dispose();
		    	//w.dispose();
			}
			public void windowGainedFocus(WindowEvent e) {
				//f.setVisible(true);
			}
		});

    }

	public void buildItemThree(final Unit unit) {
	        itemThree.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	//if (unit instanceof Vehicle) {
		            Vehicle vehicle = (Vehicle) unit;
		            vehicle.determinedSettlementParkedLocationAndFacing();
		    		repaint();
	            }
	        });
	}

	public void destroy() {
		settlement = null;
		settlement.destroy();
		unit = null;
		unit.destroy();
		itemOne = null;
		itemTwo = null;
	}
}