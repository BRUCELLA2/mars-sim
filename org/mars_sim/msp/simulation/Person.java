/**
 * Mars Simulation Project
 * Person.java
 * @version 2.72 2001-08-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.task.*;

/** The Person class represents a person on the virtual Mars. It keeps
 *  track of everything related to that person and provides
 *  information about him/her.
 */
public class Person extends Unit {

    // Data members
    private Settlement settlement; // Person's current settlement
    private Vehicle vehicle; // Vehicle person is riding in
    private NaturalAttributeManager attributes; // Manager for Person's natural attributes
    private SkillManager skills; // Manager for Person's skills
    private Mind mind; // Person's mind
    private String locationSituation; // Where person is ("In Settlement", "In Vehicle", "Outside")
    private double fatigue; // Person's fatigue level
    private double hunger; // Person's hunger level

    /** Constructs a Person object
     *  @param name the person's name
     *  @param location the person's location
     *  @param mars the virtual Mars
     *  @param manager the person's unit manager
     */
    Person(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

        // Use Unit constructor
        super(name, location, mars, manager);

        // Initialize data members
        settlement = null;
        vehicle = null;
        attributes = new NaturalAttributeManager();
        skills = new SkillManager(this);
        mind = new Mind(this, mars);
        locationSituation = new String("In Settlement");
        fatigue = RandomUtil.getRandomDouble(1000D);
        hunger = RandomUtil.getRandomDouble(1000D);
    }

    /** Returns a string for the person's relative location "In
     *  Settlement", "In Vehicle" or "Outside"
     *  @return the person's location  
     */
    public String getLocationSituation() {
        return locationSituation;
    }

    /** Sets the person's relative location "In Settlement", "In
     *  Vehicle" or "Outside"
     *  @param newLocation the new location
     */
    public void setLocationSituation(String newLocation) {
        locationSituation = newLocation;
    }

    /** Get settlement person is at, null if person is not at
     *  a settlement
     *  @return the person's settlement  
     */
    public Settlement getSettlement() {
        return settlement;
    }

    /** Get vehicle person is in, null if person is not in vehicle 
     *  @return the person's vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /** Makes the person an inhabitant of a given settlement 
     *  @param settlement the person's settlement
     */
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        location.setCoords(settlement.getCoordinates());
        settlement.addPerson(this);
        vehicle = null;
    }

    /** Makes the person a passenger in a vehicle 
     *  @param vehicle the person's vehicle
     */
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        settlement = null;
    }

    /** Gets the person's fatigue level
     *  @return person's fatigue
     */
    public double getFatigue() {
        return fatigue;
    }

    /** Sets the person's fatigue level
     *  @param fatigue new fatigue level
     */
    public void setFatigue(double fatigue) {
        this.fatigue = fatigue;
    }

    /** Adds to the person's fatigue level
     *  @param addFatigue additional fatigue
     */
    public void addFatigue(double addFatigue) {
        fatigue += addFatigue;
    }
    
    /** Gets the person's hunger level
     *  @return person's hunger
     */
    public double getHunger() {
        return hunger;
    }

    /** Sets the person's hunger level
     *  @param hunger new hunger level
     */
    public void setHunger(double hunger) {
        this.hunger = hunger;
    }

    /** Adds to the person's hunger level
     *  @param addHunger additional hunger
     */
    public void addHunger(double addHunger) {
        hunger += addHunger;
    }

    /** Person can take action with time passing 
     *  @param time amount of time passing (in millisols)
     */
    void timePassing(double time) {
        consumeOxygen(time / 1000D);
        consumeWater(time / 1000D);
        
        // Build up fatigue for given time passing.
        addFatigue(time);
        
        // Build up hunger for given time passing.
        addHunger(time);

        mind.takeAction(time);
    }

    /** Returns a reference to the Person's natural attribute manager 
     *  @return the person's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
        return attributes;
    }

    /** Returns a reference to the Person's skill manager 
     *  @return the person's skill manager
     */
    public SkillManager getSkillManager() {
        return skills;
    }

    /** Returns the person's mind 
     *  @return the person's mind 
     */
    public Mind getMind() {
        return mind;
    }
   
    /** Person consumes given amount of oxygen
     *  @param amount amount of oxygen to consume (in units)
     */
    void consumeOxygen(double amount) {
        double amountRecieved = 0D;
        
        if (locationSituation.equals("In Settlement")) {
            FacilityManager manager = settlement.getFacilityManager();
            StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
            amountRecieved = stores.removeOxygen(amount);
        }
        else amountRecieved = vehicle.removeOxygen(amount);
        
        // if (amountRecieved != amount) System.out.println(getName() + " needs oxygen.");
    }
    
    /** Person consumes given amount of water
     *  @param amount amount of water to consume (in units)
     */
    void consumeWater(double amount) {
        double amountRecieved = 0D;
        
        if (locationSituation.equals("In Settlement")) {
            FacilityManager manager = settlement.getFacilityManager();
            StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
            amountRecieved = stores.removeWater(amount);
        }
        else amountRecieved = vehicle.removeWater(amount);
        
        // if (amountRecieved != amount) System.out.println(getName() + " needs water.");
    }
    
    /** Person consumes given amount of food
     *  @param amount amount of food to consume (in units)
     */
    public void consumeFood(double amount) {
        double amountRecieved = 0D;
        
        if (locationSituation.equals("In Settlement")) {
            FacilityManager manager = settlement.getFacilityManager();
            StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
            amountRecieved = stores.removeFood(amount);
        }
        else amountRecieved = vehicle.removeFood(amount);
        
        // if (amountRecieved != amount) System.out.println(getName() + " needs food.");
    }
}
