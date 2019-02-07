/**
/ * Mars Simulation Project
 * MainWindow.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;

//import com.alee.managers.UIManagers;
import com.alee.laf.WebLookAndFeel;
import com.alee.managers.UIManagers;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * tool bars and main desktop pane.
 */
public class MainWindow extends JComponent {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MainWindow.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Icon image filename for main window. */
	private static final String ICON_IMAGE = "/images/LanderHab.png";
	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
	private static final String SOL = " Sol ";
	
//	private static int AUTOSAVE_EVERY_X_MINUTE = 15;
	private static final int TIME_DELAY = 960;

	private static JFrame frame;
	
	// Data members
//	private boolean cleanUI;
	private boolean useDefault;
//	private boolean keepSleeping;

	private int solCache = 0;
	
	private String lookAndFeelTheme;
	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private MainWindowMenu mainWindowMenu;

//	private TransportWizard transportWizard;
//	private ConstructionWizard constructionWizard;
//	private BuildingManager mgr; // mgr is very important for FINISH_BUILDING_PLACEMENT_EVENT

//	private static Thread newSimThread;
//	private Thread loadSimThread;
//	private Thread saveSimThread;
	
    private final AtomicBoolean sleeping = new AtomicBoolean(false);

	private Timer delayTimer;
	private Timer delayTimer1;
//	private Timer autosaveTimer;
	private javax.swing.Timer earthTimer;

	// protected ShowDateTime showDateTime;
	private JStatusBar statusBar;
	private JLabel leftLabel;
	private JLabel memMaxLabel;
	private JLabel memUsedLabel;
	// private JLabel dateLabel;
	private JLabel earthTimeLabel;
	private JPanel bottomPane;
	private JPanel mainPane;

	private int memMax;
//	private static int memTotal;
	private int memUsed, memUsedCache;
	private int memFree;

//	private static String earthTimeString;
//	private String statusText;
//	private ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;// = sim.getMasterClock();
	private static EarthClock earthClock;// = masterClock.getEarthClock();
	private static MarsClock marsClock;// = masterClock.getMarsClock();
		
	
	/**
	 * Constructor 1.
	 * 
	 * @param cleanUI
	 *            true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI) {
		logger.config("MainWindow is on " + Thread.currentThread().getName() + " Thread");
		// this.cleanUI = cleanUI;
		
		// Set up the frame
		frame = new JFrame();
		
		// Disable the close button on top right
//		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Set up the look and feel library to be used
		if (OS.contains("linux"))
			setLookAndFeel(false, false);
		else
			setLookAndFeel(false, true);
		
		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this);
		
		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}
		
		// Set look and feel of UI.
		useDefault = UIConfig.INSTANCE.useUIDefault();
		
		// Set the icon image for the frame.
		setIconImage();
		
		// Initialize UI elements for the frame
		init();
		
		// Set up timers for use on the status bar
		setupDelayTimer();
		
		// Add autosave timer
//		startAutosaveTimer();
		
		// Open all initial windows.
		desktop.openInitialWindows();
		
		// Set up timers for caching the settlemnet windows
		setupSettlementWindowTimer();
	}

	/**
	 * Initializes UI elements for the frame
	 */
	public void init() {
		frame.setTitle(Simulation.title);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});
		
		// Set up the main pane
		mainPane = new JPanel(new BorderLayout());
		
		// Add the main pane to the frame
		frame.setContentPane(mainPane);

		// Add main pane
		mainPane.add(desktop, BorderLayout.CENTER);

		// Prepare menu
		mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		mainPane.add(toolToolbar, BorderLayout.NORTH);

		// Add bottomPane for holding unitToolbar and statusBar
		bottomPane = new JPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {
			private static final long serialVersionUID = 1L;
			@Override
			protected JButton createActionComponent(Action a) {
				JButton jb = super.createActionComponent(a);
//				jb.setOpaque(false);
				return jb;
			}
		};

//		BasicToolBarUI ui = new BasicToolBarUI();
//		unitToolbar.setUI(ui);
		
		// unitToolbar.setOpaque(false);
		// unitToolbar.setBackground(new Color(0,0,0,0));
		unitToolbar.setBorder(new MarsPanelBorder());
		// Remove the toolbar border, to blend into figure contents
		unitToolbar.setBorderPainted(true);

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		bottomPane.add(unitToolbar, BorderLayout.CENTER);

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
		toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

		// Create the status bar
		statusBar = new JStatusBar();

		earthTimeLabel = new JLabel();
		earthTimeLabel.setHorizontalAlignment(JLabel.LEFT);
		TooltipManager.setTooltip(earthTimeLabel, "Earth Timestamp", TooltipWay.up);
		statusBar.setLeftComponent(earthTimeLabel, true);

		leftLabel = new JLabel();
		leftLabel.setText(SOL + "1");
		leftLabel.setHorizontalAlignment(JLabel.CENTER);
		TooltipManager.setTooltip(leftLabel, "# of sols since the beginning of the sim", TooltipWay.up);
		statusBar.add(leftLabel, 0);
		
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1_000_000;

		memUsedLabel = new JLabel();
		memUsedLabel.setHorizontalAlignment(JLabel.RIGHT);
		int memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1_000_000;
		memUsed = memTotal - memFree;
		memUsedLabel.setText(memUsed + " MB");//"Used Memory : " + memUsed + " MB");
		TooltipManager.setTooltip(memUsedLabel, "Memory Used", TooltipWay.up);
		statusBar.addRightComponent(memUsedLabel, false);

		memMaxLabel = new JLabel();
		memMaxLabel.setHorizontalAlignment(JLabel.RIGHT);
		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1_000_000;
		memMaxLabel.setText("[ " + memMax + " MB ] ");//"Total Designated Memory : " + memMax + " MB");
		TooltipManager.setTooltip(memMaxLabel, "Memory Designated", TooltipWay.up);
		statusBar.addRightComponent(memMaxLabel, false);

		bottomPane.add(statusBar, BorderLayout.SOUTH);

		// Set frame size
		final Dimension frame_size;
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		if (useDefault) {
			// Make frame size 80% of screen size.
			if (screen_size.width > 800) {
				frame_size = new Dimension((int) Math.round(screen_size.getWidth() * .9D),
						(int) Math.round(screen_size.getHeight() * .9D));
			} else {
				frame_size = new Dimension(screen_size);
			}
		} else {
			frame_size = UIConfig.INSTANCE.getMainWindowDimension();
		}
		frame.setSize(frame_size);

		// Set frame location.
		if (useDefault) {
			// Center frame on screen
			frame.setLocation(((screen_size.width - frame_size.width) / 2),
					((screen_size.height - frame_size.height) / 2));
		} else {
			frame.setLocation(UIConfig.INSTANCE.getMainWindowLocation());
		}

		// Show frame
		frame.setVisible(true);

	}

	/**
	 * Set up the timer for status bar
	 */
	public void setupDelayTimer() {
		if (delayTimer == null) {
			delayTimer = new Timer();
			delayTimer.schedule(new DelayTimer(), 300);
		}
	}

	/**
	 * Defines the delay timer class
	 */
	class DelayTimer extends TimerTask {
		public void run() {
			runStatusTimer();
		}
	}
	
	/**
	 * Set up the timer for caching settlement windows
	 */
	public void setupSettlementWindowTimer() {
		delayTimer1 = new Timer();
		delayTimer1.schedule(new DelayTimer2(), 2000);
	}
	
	/**
	 * Defines the delay timer class
	 */
	class DelayTimer2 extends TimerTask {
		public void run() {
			// Cache each settlement unit window
			desktop.cacheSettlementUnitWindow();
		}
	}
	
	public JPanel getBottomPane() {
		return bottomPane;
	}

//	/**
//	 * Start the auto save timer
//	 */
//	public void startAutosaveTimer() {
//		TimerTask timerTask = new TimerTask() {
//			@Override
//			public void run() {
//				autosaveTimer.cancel();
////				saveSimulation(true, true);
////				startAutosaveTimer();
//			}
//		};
//		autosaveTimer = new Timer();
//		autosaveTimer.schedule(timerTask, 1000 * 60 * AUTOSAVE_EVERY_X_MINUTE);
//	}

	/**
	 * Start the earth timer
	 */
	public void runStatusTimer() {
		logger.config("runStatusTimer()");
		earthTimer = new javax.swing.Timer(TIME_DELAY, new ActionListener() {
		
//		String earthTime = null;

			@Override
			public void actionPerformed(ActionEvent evt) {
//				logger.config("runStatusTimer()'s actionPerformed()");
//				try {
					// Check if new simulation is being created or loaded from file.
//					if (!Simulation.isUpdating()) {
////						MasterClock master = sim.getMasterClock();
//						if (masterClock == null) {
//							throw new IllegalStateException("master clock is null");
//						}
////						EarthClock earthclock = master.getEarthClock();
//						if (earthClock == null) {
//							throw new IllegalStateException("earthclock is null");
//						}
//						earthTime = earthClock.getTimeStampF0();
//					}
//				} catch (Exception ee) {
//					ee.printStackTrace(System.err);
//				}

				if (earthClock == null) {
					masterClock = sim.getMasterClock();
					earthClock = masterClock.getEarthClock();
					marsClock = masterClock.getMarsClock();
				}
				
				earthTimeLabel.setText(earthClock.getTimeStampF1());
				
				int memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1_000_000;
				int memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1_000_000;
				int memUsed = memTotal - memFree;

				if (memUsed > memUsedCache * 1.1 && memUsed < memUsedCache * 0.9) {
					memUsedCache = memUsed;
					memUsedLabel.setText(
//							"Used Memory : " + 
							memUsed + " MB");
				}
		
				int sol = marsClock.getMissionSol();
				if (solCache != sol) {
					solCache = sol;
					leftLabel.setText(SOL + sol);
				}
				
//				// Check on whether autosave is due
//				if (masterClock.getAutosave()) {
//					// Trigger an autosave instance
//					saveSimulation(true, true);
//					masterClock.setAutosave(false);
//				}
			}
		});

		earthTimer.start();
	}

	/**
	 * Get the window's frame.
	 * 
	 * @return the frame.
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Gets the main desktop panel.
	 * 
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Gets the Main Window Menu.
	 * 
	 * @return mainWindowMenu
	 */
	public MainWindowMenu getMainWindowMenu() {
		return mainWindowMenu;
	}

	/**
	 * Load a previously saved simulation.
	 */
	public void loadSimulation(boolean autosave) {		
//		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
//			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
					loadSimulationProcess(autosave);
//				}
//			};
//			loadSimThread.start();
//		} else {
//			loadSimThread.interrupt();
//		}

	}

	/**
	 * Performs the process of loading a simulation.
	 * @param autosave
	 */
	public static void loadSimulationProcess(boolean autosave) {
		logger.config("MainWindow's loadSimulationProcess() is on " + Thread.currentThread().getName());

//		if (masterClock != null)
			sim.stop();

		String dir = null;
		String title = null;
		
		// Add autosave
		if (autosave) {
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		} else {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}
		
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle(title); // $NON-NLS-1$
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
//			desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the
			// single steps.
//			sim.endSimulation();

//			logger.config("Done open annoucement window");

//			try {
//				desktop.clearDesktop();
//
//				if (earthTimer != null) {
//					earthTimer.stop();
//				}
//				earthTimer = null;
//
//			} catch (Exception e) {
//				// New simulation process should continue even if there's an exception in the
//				// UI.
//				logger.severe(e.getMessage());
//				e.printStackTrace(System.err);
//			}

//			logger.config("About to call loadSimulation()");
			sim.loadSimulation(chooser.getSelectedFile());
//			logger.config("Done calling loadSimulation()");
			
//			while (masterClock != null) {// while (masterClock.isLoadingSimulation()) {
//				try {
//					Thread.sleep(300L);
//				} catch (InterruptedException e) {
//					logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
//				}
//			}
//
//			desktop.disposeAnnouncementWindow();

//			try {
//				desktop.resetDesktop();
//			} catch (Exception e) {
//				// New simulation process should continue even if there's an exception in the
//				// UI.
//				logger.severe(e.getMessage());
//				e.printStackTrace(System.err);
//			}
//
//			if (masterClock != null)
//				sim.start(false);

//			startEarthTimer();
		}
	}

//	/**
//	 * Create a new simulation.
//	 */
//	public void newSimulation() {
//		if ((newSimThread == null) || !newSimThread.isAlive()) {
//			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
//					newSimulationProcess();
//					// Simulation.instance().runStartTask(false);
//				}
//			};
//			newSimThread.start();
//		} else {
//			newSimThread.interrupt();
//		}
//	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	void newSimulationProcess() {
		logger.config("newSimulationProces() is on " + Thread.currentThread().getName());

		if (JOptionPane.showConfirmDialog(desktop, Msg.getString("MainWindow.abandonRunningSim"), //$NON-NLS-1$
				UIManager.getString("OptionPane.titleText"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.creatingNewSim") + "  "); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the
			// single steps.
			sim.endSimulation();
			sim.endMasterClock();

			desktop.closeAllToolWindow();
			desktop.disposeAnnouncementWindow();

			try {
				desktop.clearDesktop();

				if (earthTimer != null) {
					earthTimer.stop();
				}
				earthTimer = null;

			} catch (Exception e) {
				// New simulation process should continue even if there's an exception in the
				// UI.
				logger.severe(e.getMessage());
				e.printStackTrace(System.err);
			}

			try {
				sim.startSimExecutor();
				// sim.runLoadConfigTask();
				sim.getSimExecutor().submit(new SimConfigTask(this));

			} catch (Exception e) {
				logger.warning("error in restarting a new sim.");
				e.printStackTrace();
			}

			try {
				desktop.resetDesktop();
			} catch (Exception e) {
				// New simulation process should continue even if there's an exception in the
				// UI.
				logger.severe(e.getMessage());
				e.printStackTrace(System.err);
			}

		}
	}

	public class SimConfigTask implements Runnable {
		MainWindow win;

		SimConfigTask(MainWindow win) {
			this.win = win;
		}

		public void run() {
			SimulationConfig.loadConfig();
			new SimulationConfigEditor(SimulationConfig.instance(), null);
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * 
	 * @param loadingDefault
	 *            Should the user be allowed to override location?
	 */
	public void saveSimulation(boolean loadingDefault, final boolean isAutosave) {
//		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
//			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
					saveSimulationProcess(loadingDefault, isAutosave);
//				}
//			};
//			saveSimThread.start();
//		} 
//		
//		else {
//			saveSimThread.interrupt();
//			stopSleeping();
//		}
	}

	/**
	 * Performs the process of saving a simulation.
	 */
	private void saveSimulationProcess(boolean loadingDefault, boolean isAutosave) {
//		logger.config("saveSimulationProcess() is on " + Thread.currentThread().getName());
		if (masterClock.isPaused()) {
			logger.config("Cannot save when the simulation is on pause.");
		}
		
		else {

				if (isAutosave) {
					SwingUtilities.invokeLater(() -> {
						desktop.disposeAnnouncementWindow();
						desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.autosavingSim") + "  "); //$NON-NLS-1$
					});
					masterClock.setSaveSim(Simulation.AUTOSAVE, null);
//					sim.getSimExecutor().submit(() -> masterClock.setSaveSim(Simulation.AUTOSAVE, null));
				} 
				
				else {
//					File fileLocn = null;
					
					SwingUtilities.invokeLater(() -> {
	
						desktop.disposeAnnouncementWindow();
						desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.savingSim") + "  "); //$NON-NLS-1$
					});
						
					if (!loadingDefault) {
						JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
						chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
						if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
							final File fileLocn = chooser.getSelectedFile();
							masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn);
//							sim.getSimExecutor().submit(() -> masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn));
						} else {
							return;
						}
					}
					
					else {
//					if (fileLocn == null)
						masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
//						sim.getSimExecutor().submit(() -> masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null));
					}
						
				}
	
				sleeping.set(true);
		        while (sleeping.get() && masterClock.isSavingSimulation()) {
		            try { 
	//	                Thread.sleep(interval); 
						TimeUnit.MILLISECONDS.sleep(100L);
		            } catch (InterruptedException e){ 
		                Thread.currentThread().interrupt();
						logger.log(Level.SEVERE, Msg.getString("MainWindow.log.sleepInterrupt") + ". " + e); //$NON-NLS-1$
						e.printStackTrace(System.err);
		            }
		            // do something here 
		         }
	        
			
//			try {
//				
//				// Save the current main window ui config
//	//			UIConfig.INSTANCE.saveFile(this);
//		
//				while (keepSleeping && masterClock.isSavingSimulation())
//					TimeUnit.MILLISECONDS.sleep(100L);
//	
//			} catch (Exception e) {
//				logger.log(Level.SEVERE, Msg.getString("MainWindow.log.sleepInterrupt") + e); //$NON-NLS-1$
//				e.printStackTrace(System.err);
//			}
			
//	        
		        SwingUtilities.invokeLater(() -> desktop.disposeAnnouncementWindow());
		}
	}

    public void stopSleeping() {
        sleeping.set(false);
    }
    
//	/**
//	 * Save the current simulation. This displays a FileChooser to select the
//	 * location to save the simulation if the default is not to be used.
//	 * 
//	 * @param type
//	 */
//	public void saveSimulation(int type) {
//		if (!masterClock.isPaused()) {
//			// hideWaitStage(PAUSED);
//			if (type == Simulation.SAVE_DEFAULT || type == Simulation.SAVE_AS) {
//				desktop.disposeAnnouncementWindow();
//				desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.savingSim") + "  "); //$NON-NLS-1$
//			}
//			
//			else {
//				desktop.disposeAnnouncementWindow();
//				desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.autosavingSim") + "  "); //$NON-NLS-1$
//				masterClock.setSaveSim(Simulation.AUTOSAVE, null);
//			}
//
//			saveExecutor.execute(new Task<Void>() {
//				@Override
//				protected Void call() throws Exception {
//					saveSimulationProcess(type);
//					while (masterClock.isSavingSimulation())
//						TimeUnit.MILLISECONDS.sleep(200L);
//					return null;
//				}
//
//				@Override
//				protected void succeeded() {
//					super.succeeded();
//					desktop.disposeAnnouncementWindow();
//				}
//			});
//
//		}
//		// endPause(previous);
//	}
//
//
//	/**
//	 * Performs the process of saving a simulation.
//	 */
//	private void saveSimulationProcess(int type) {
//		// logger.config("MainScene's saveSimulationProcess() is on " +
//		// Thread.currentThread().getName() + " Thread");
//		fileLocn = null;
//		dir = null;
//		title = null;
//
//		hideWaitStage(PAUSED);
//
//		if (type == Simulation.AUTOSAVE) {
//			dir = Simulation.AUTOSAVE_DIR;
//			masterClock.setSaveSim(Simulation.AUTOSAVE, null);
//
//		} else if (type == Simulation.SAVE_DEFAULT) {
//			dir = Simulation.DEFAULT_DIR;
//			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
//
//		} else if (type == Simulation.SAVE_AS) {
//
//			Platform.runLater(() -> {
//				FileChooser chooser = new FileChooser();
//				dir = Simulation.DEFAULT_DIR;
//				File userDirectory = new File(dir);
//				title = Msg.getString("MainScene.dialogSaveSim");
//				chooser.setTitle(title); // $NON-NLS-1$
//				chooser.setInitialDirectory(userDirectory);
//				// Set extension filter
//				FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
//						"*.sim");
//				FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");
//				chooser.getExtensionFilters().addAll(simFilter, allFilter);
//				File selectedFile = chooser.showSaveDialog(stage);
//				if (selectedFile != null)
//					fileLocn = selectedFile;
//				else {
//					hideWaitStage(PAUSED);
//					return;
//				}
//
//				showWaitStage(SAVING);
//
//				saveExecutor.execute(new Task<Void>() {
//					@Override
//					protected Void call() throws Exception {
//						try {
//							masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn);
//
//							while (masterClock.isSavingSimulation())
//								TimeUnit.MILLISECONDS.sleep(200L);
//
//						} catch (Exception e) {
//							logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
//							e.printStackTrace(System.err);
//						}
//
//						return null;
//					}
//
//					@Override
//					protected void succeeded() {
//						super.succeeded();
//						hideWaitStage(SAVING);
//					}
//				});
//			});
//		}
//	}
	
	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.pausingSim") + "  "); //$NON-NLS-1$
		masterClock.setPaused(true, false);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		masterClock.setPaused(false, false);
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Create a new unit button in toolbar.
	 * 
	 * @param unit
	 *            the unit the button is for.
	 */
	public void createUnitButton(Unit unit) {
		unitToolbar.createUnitButton(unit);
	}

	/**
	 * Disposes a unit button in toolbar.
	 * 
	 * @param unit
	 *            the unit to dispose.
	 */
	public void disposeUnitButton(Unit unit) {
		unitToolbar.disposeUnitButton(unit);
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
//		Simulation sim = Simulation.instance();
//		try {
//			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
//			e.printStackTrace(System.err);
//		}
		endSimulationClass();
		masterClock.exitProgram();
		System.exit(0);
		destroy();
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves
	 * the main menu running
	 */
	private void endSimulationClass() {
		sim.endSimulation();
		sim.getSimExecutor().shutdown();//.shutdownNow();
	}
	
	/**
	 * Sets the look and feel of the UI
	 * 
	 * @param nativeLookAndFeel
	 *            true if native look and feel should be used.
	 */
	public void setLookAndFeel(boolean nativeLookAndFeel, boolean nimRODLookAndFeel) {
		boolean changed = false;
   
		// use the weblaf skin
		WebLookAndFeel.install();
		UIManagers.initialize();
		
//		 final XStream xs = XmlUtils.getXStream();
//		 XStream.setupDefaultSecurity(xs);
//		 xs.allowTypesByWildcard(new String[] { "com.alee.**" });
    
//		 XStream xstream = new XStream(new StaxDriver()) {
//		      @Override
//		      protected void setupConverters() {
//		      }
//		    };
//		    xstream.registerConverter(new ReflectionConverter(xstream.getMapper(), xstream.getReflectionProvider()), XStream.PRIORITY_VERY_LOW);
//		    xstream.registerConverter(new IntConverter(), XStream.PRIORITY_NORMAL);
//		    xstream.registerConverter(new StringConverter(), XStream.PRIORITY_NORMAL);
//		    xstream.registerConverter(new CollectionConverter(xstream.getMapper()), XStream.PRIORITY_NORMAL);
		    
		String currentTheme = UIManager.getLookAndFeel().getClass().getName();

		if (nativeLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				lookAndFeelTheme = "system";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (nimRODLookAndFeel) {
			try {
//				UIManager.setLookAndFeel(new NimRODLookAndFeel());
//				changed = true;
//				lookAndFeelTheme = "nimrod";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else {
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

			frame.validate();
			frame.repaint();

			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateAnnouncementWindowLF();
			}
		}
	}

	/**
	 * Sets the icon image for the main window.
	 */
	public void setIconImage() {

		String fullImageName = ICON_IMAGE;
		URL resource = ImageLoader.class.getResource(fullImageName);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(resource);
		frame.setIconImage(img);
	}

	/**
	 * Gets the unit toolbar.
	 * 
	 * @return unit toolbar.
	 */
	public UnitToolBar getUnitToolBar() {
		return unitToolbar;
	}

	/**
	 * Gets the tool toolbar.
	 * 
	 * @return tool toolbar.
	 */
	public ToolToolBar getToolToolBar() {
		return toolToolbar;
	}

	public String getLookAndFeelTheme() {
		return lookAndFeelTheme;
	}
	
	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		frame = null;
		unitToolbar = null;
		toolToolbar = null;
		desktop.destroy();
		desktop = null;
		mainWindowMenu = null;
//		mgr = null;
//		newSimThread = null;
//		loadSimThread = null;
//		saveSimThread = null;
		delayTimer = null;
//		autosaveTimer = null;
		earthTimer = null;
		statusBar = null;
		leftLabel = null;
		memMaxLabel = null;
		memUsedLabel = null;
		earthTimeLabel = null;
		bottomPane = null;
		mainPane = null;
		sim = null;
		masterClock = null;
		earthClock = null;
	}

}