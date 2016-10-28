/**
 * Mars Simulation Project
 * ResupplyWindow.java
 * @version 3.08 2015-03-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

/**
 * Window for the resupply tool.
 * TODO externalize strings
 */
public class ResupplyWindow
extends ToolWindow
implements ListSelectionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = "Resupply Tool";

	// Data members
	private boolean isRunning = false;
	private IncomingListPanel incomingListPane;
	private ArrivedListPanel arrivedListPane;
	private TransportDetailPanel detailPane;
	private JButton modifyButton;
	private JButton cancelButton;
	
	private MainDesktopPane desktop;
	private MainScene mainScene;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public ResupplyWindow(MainDesktopPane desktop) {
		// Use the ToolWindow constructor.
		super(NAME, desktop);

		this.desktop = desktop;
		//MainWindow mw = desktop.getMainWindow();	
		mainScene = desktop.getMainScene();
		
		// Create main panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create list panel.
		JPanel listPane = new JPanel(new GridLayout(2, 1));
		mainPane.add(listPane, BorderLayout.WEST);

		// Create incoming list panel.
		incomingListPane = new IncomingListPanel();
		incomingListPane.getIncomingList().addListSelectionListener(this);
		listPane.add(incomingListPane);

		// Create arrived list panel.
		arrivedListPane = new ArrivedListPanel();
		listPane.add(arrivedListPane);

		// Set incoming and arrived list panels to listen to each other's list selections.
		incomingListPane.getIncomingList().addListSelectionListener(arrivedListPane);
		arrivedListPane.getArrivedList().addListSelectionListener(incomingListPane);

		// Create detail panel.
		detailPane = new TransportDetailPanel();
		incomingListPane.getIncomingList().addListSelectionListener(detailPane);
		arrivedListPane.getArrivedList().addListSelectionListener(detailPane);
		mainPane.add(detailPane, BorderLayout.CENTER);

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create new button.
		// 9/29/2014 Changed button text from "New"  to "New Mission"
		JButton newButton = new JButton("New Mission");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				createNewTransportItem();
			}
		});
		buttonPane.add(newButton);

		// Create modify button.
		// 9/29/2014 Changed button text from "Modify"  to "Modify Mission"
		modifyButton = new JButton("Modify Mission");
		modifyButton.setEnabled(false);
		modifyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				modifyTransportItem();
			}
		});
		buttonPane.add(modifyButton);

		// Create cancel button.
		// 9/29/2014 Changed button text from "Discard"  to "Discard Mission"
		cancelButton = new JButton("Discard Mission");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelTransportItem();
			}
		});
		buttonPane.add(cancelButton);

		setMinimumSize(new Dimension(512, 512));
		setResizable(false);
		setMaximizable(true);
		
		if (desktop.getMainScene() != null) {
			setClosable(false);
		}

		
		setVisible(true);	
		pack();
		
		Dimension desktopSize = desktop.getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

		
	}

	/**
	 * Opens a create dialog.
	 */
	private void createNewTransportItem() {		
		
		// 2015-12-16 Track the current pause state
		boolean previous2 = Simulation.instance().getMasterClock().isPaused();
		
		// Pause simulation.
		if (mainScene != null) {
			if (!previous2) {
				mainScene.pauseSimulation();
				//System.out.println("previous2 is false. Paused sim");
			}	
			desktop.getTimeWindow().enablePauseButton(false);
		}	

		new NewTransportItemDialog(desktop, this);	
		
		isRunning = true;
		
		// Unpause simulation.
		if (mainScene != null) {
			boolean now2 = Simulation.instance().getMasterClock().isPaused();
    		if (!previous2) {
    			if (now2) {
    				mainScene.unpauseSimulation();
    	    		//System.out.println("previous2 is false. now2 is true. Unpaused sim");
    			}
    		} else {
    			if (!now2) {
    				mainScene.unpauseSimulation();
    	    		//System.out.println("previous2 is true. now2 is false. Unpaused sim");
    			}				
    		}    
			desktop.getTimeWindow().enablePauseButton(true);
		}	
	}

	/**
	 * Determines if swing or javaFX is in used when loading the modify dialog
	 */
	private void modifyTransportItem() {
		// 2015-12-16 Track the current pause state
		boolean previous3 = Simulation.instance().getMasterClock().isPaused();

		if (mainScene != null) {
			if (!previous3) {
				mainScene.pauseSimulation();
				//System.out.println("previous3 is false. Paused sim");
			}	
			desktop.getTimeWindow().enablePauseButton(false);
		}
		
		modifyTransport();
			
		if (mainScene != null) {
    		boolean now3 = Simulation.instance().getMasterClock().isPaused();
    		if (!previous3) {
    			if (now3) {
    				mainScene.unpauseSimulation();
    	    		//System.out.println("previous3 is false. now3 is true. Unpaused sim");
    			}
    		} else {
    			if (!now3) {
    				mainScene.unpauseSimulation();
    	    		//System.out.println("previous3 is true. now3 is false. Unpaused sim");
    			}				
    		}            
			desktop.getTimeWindow().enablePauseButton(true);
		}
			
	}

	/**
	 * Loads modify dialog for the currently selected transport item.
	 */
	// 2015-03-23 Added modifyTransport()
	private void modifyTransport() {	
		// Get currently selected incoming transport item.
		Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();

		if ((transportItem != null)) {
			if (transportItem instanceof Resupply) {
				// Create modify resupply mission dialog.
				Resupply resupply = (Resupply) transportItem;
				String title = "Modify Resupply Mission";
				//new ModifyTransportItemDialog(mw.getFrame(), title, resupply);
				new ModifyTransportItemDialog(desktop, this, title, resupply);
				
				//isRunning = true;
			}
			else if (transportItem instanceof ArrivingSettlement) {
				// Create modify arriving settlement dialog.
				ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
				String title = "Modify Arriving Settlement";
				//new ModifyTransportItemDialog(mw.getFrame(), title, settlement);
				new ModifyTransportItemDialog(desktop, this, title, settlement);
	
				//isRunning = true;
			}
		}
	}
	
	/**
	 * Cancels the currently selected transport item.
	 */
	private void cancelTransportItem() {     
		String msg = "Are you sure you want to discard the highlighted mission?";
		
		if (mainScene != null) {
			// 2015-12-16 Added askFX()
			Platform.runLater(() -> {
				askFX(msg);
			}); 
		}
		else {
			// 2014-10-04 Added a dialog box asking the user to confirm "discarding" the mission
			JDialog.setDefaultLookAndFeelDecorated(true);
			final int response = JOptionPane.showConfirmDialog(null, msg, "Confirm",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION) {
				// "No" button click, do nothing
			} else if (response == JOptionPane.YES_OPTION) {
				// "Yes" button clicked and go ahead with discarding this mission 
				Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
				if (transportItem != null) {
					// call cancelTransportItem() in TransportManager Class to cancel the selected transport item.
					Simulation.instance().getTransportManager().cancelTransportItem(transportItem);
				}
			} else if (response == JOptionPane.CLOSED_OPTION) {
				// Close the dialogbox, do nothing
			}	  
		}
	}

	/**
	 * Asks users for the confirmation of discarding a transport mission in an alert dialog
	 * @param msg
	 */
	@SuppressWarnings("restriction")
	// 2015-12-16 Added askFX()
	public synchronized void askFX(String msg) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Resupply Tool");
    	alert.initOwner(mainScene.getStage());
		alert.initModality(Modality.NONE); 
		//alert.initModality(Modality.APPLICATION_MODAL);  f
		//alert.initModality(Modality.WINDOW_MODAL); 
		alert.setHeaderText("Confirmation for discarding this transport/ressuply mission");
		alert.setContentText(msg);

		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No");
		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

		alert.showAndWait().ifPresent(response -> {
		     if (response == buttonTypeYes) {
				Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
				if (transportItem != null) {
					// call cancelTransportItem() in TransportManager Class to cancel the selected transport item.
					Simulation.instance().getTransportManager().cancelTransportItem(transportItem);
				}
		     }

		});
   }

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting()) {
			JList<?> incomingList = (JList<?>) evt.getSource();
			Object selected = incomingList.getSelectedValue();
			if (selected != null) {
				// Incoming transport item is selected, 
				// so enable modify and cancel buttons.
				modifyButton.setEnabled(true);
				cancelButton.setEnabled(true);
			}
			else {
				// Incoming transport item is unselected,
				// so disable modify and cancel buttons.
				modifyButton.setEnabled(false);
				cancelButton.setEnabled(false);
			}
		}
	}

	public IncomingListPanel getIncomingListPane() {
		return incomingListPane;
	}
	
	public void setModifyButton(boolean value) {
		modifyButton.setEnabled(value);
	}
	
	//public boolean isRunning() {
	//	return isRunning;
	//}

	//public void setRunning(boolean value){
	//	isRunning = value;
	//}
	
	/**
	 * Prepare this window for deletion.
	 */
	@Override
	public void destroy() {
		incomingListPane.destroy();
		arrivedListPane.destroy();
		detailPane.destroy();
	}
}