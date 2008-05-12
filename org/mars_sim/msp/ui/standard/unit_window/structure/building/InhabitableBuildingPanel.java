/**
 * Mars Simulation Project
 * InhabitableBuildingPanel.java
 * @version 2.84 2008-05-12
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
public class InhabitableBuildingPanel extends BuildingFunctionPanel implements MouseListener {
    
    private LifeSupport inhabitable; // The inhabitable building.
    private DefaultListModel inhabitantListModel;
    private JList inhabitantList;
    private Collection<Person> inhabitantCache;
    private JLabel numberLabel;
    
    /**
     * Constructor
     *
     * @param inhabitable The inhabitable building this panel is for.
     * @param desktop The main desktop.
     */
    public InhabitableBuildingPanel(LifeSupport inhabitable, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super(inhabitable.getBuilding(), desktop);
        
        // Initialize data members.
        this.inhabitable = inhabitable;
        
        // Set panel layout
        setLayout(new BorderLayout());
        
        // Create label panel
        JPanel labelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        add(labelPanel, BorderLayout.NORTH);
        
        // Create inhabitant label
        JLabel inhabitantLabel = new JLabel("Occupants", JLabel.CENTER);
        labelPanel.add(inhabitantLabel);
        
        // Create number label
        numberLabel = new JLabel("Number: " + inhabitable.getOccupantNumber(), JLabel.CENTER);
        labelPanel.add(numberLabel);
        
        // Create capacity label
        JLabel capacityLabel = new JLabel("Capacity: " + 
            inhabitable.getOccupantCapacity(), JLabel.CENTER);
        labelPanel.add(capacityLabel);
        
        // Create inhabitant list panel
        JPanel inhabitantListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(inhabitantListPanel, BorderLayout.CENTER);
        
        // Create scroll panel for inhabitant list
        JScrollPane inhabitantScrollPanel = new JScrollPane();
        inhabitantScrollPanel.setPreferredSize(new Dimension(160, 60));
        inhabitantListPanel.add(inhabitantScrollPanel);
        
        // Create inhabitant list model
        inhabitantListModel = new DefaultListModel();
        inhabitantCache = inhabitable.getOccupants();
        Iterator i = inhabitantCache.iterator();
        while (i.hasNext()) inhabitantListModel.addElement(i.next());
        
        // Create inhabitant list
        inhabitantList = new JList(inhabitantListModel);
        inhabitantList.addMouseListener(this);
        inhabitantScrollPanel.setViewportView(inhabitantList);
    }
    
    /**
     * Update this panel
     */
    public void update() {
        
        // Update population list and number label
        if (!inhabitantCache.equals(inhabitable.getOccupants())) {
            inhabitantCache = inhabitable.getOccupants();
            inhabitantListModel.clear();
            Iterator<Person> i = inhabitantCache.iterator();
            while (i.hasNext()) inhabitantListModel.addElement(i.next());
            
            numberLabel.setText("Number: " + inhabitantCache.size());
        }
    }
    
    /** 
     * Mouse clicked event occurs.
     *
     * @param event the mouse event
     */
    public void mouseClicked(MouseEvent event) {

        // If double-click, open person window.
        if (event.getClickCount() >= 2) 
            desktop.openUnitWindow((Person) inhabitantList.getSelectedValue(), false);
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}
