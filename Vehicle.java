/**
 * Mars Simulation Project
 * Vehicle.java
 * @version 2.70 2000-09-01
 * @author Scott Davis
 */

import java.awt.*;
import java.util.*;
import javax.swing.*;

/** The Vehicle class represents a generic vehicle. It keeps track of
 *  generic information about the vehicle. This class needs to be
 *  subclassed to represent a specific type of vehicle.
 */
public abstract class Vehicle extends Unit {

    private double direction = 0;     // Direction vehicle is traveling in
    private double speed = 0;         // Current speed of vehicle in kph
    private double baseSpeed = 30;            // Base speed of vehicle in kph (can be set in child class)
    private String status;                 // Current status of vehicle ("Moving", "Parked") (other child-specific status allowed)
    private Settlement settlement;       // The settlement which the vehicle is parked at
    private Vector passengers;           // List of people who are passengers in vehicle
    private Person driver;               // Driver of the vehicle
    private double distanceTraveled = 0;     // Total distance traveled by vehicle
    private double distanceMaint = 0;        // Distance traveled by vehicle since last maintenance
    private int maxPassengers = 0;              // Maximum number of passengers the vehicle can carry.
    private double fuel = 0;                      // Current amount of fuel in the vehicle.
    private double fuelCapacity = 0;              // Maximum amount of fuel the vehicle can carry.
	
    private Coordinates destinationCoords;    // Coordinates of the destination
    private Settlement destinationSettlement; // Destination settlement (it applicable)
    private String destinationType;           // Type of destination ("None", "Settlement" or "Coordinates")
    private double distanceToDestination = 0;     // Distance in meters to the destination
    private boolean isReserved = false;       // True if vehicle is currently reserved for a driver and cannot be taken by another
    private int vehicleSize = 1;              // Size of vehicle in arbitrary units.(Value of size units will be established later.)
    private int maintenanceWork = 0;              // Work done for vehicle maintenance.
    private int totalMaintenanceWork;         // Total amount of work necessary for vehicle maintenance.
	
    private HashMap potentialFailures;           // A table of potential failures in the vehicle. (populated by child classes)
    private MechanicalFailure mechanicalFailure; // A list of current failures in the vehicle.
    private boolean distanceMark = false;

    
    public Vehicle(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

	// use Unit constructor
	super(name, location, mars, manager);
		
	// initialize
	setStatus("Parked");
	setDestinationType("None");
	passengers = new Vector();
	potentialFailures = new HashMap();
	totalMaintenanceWork = 12 * 60 * 60; // (12 hours)
    }

    /** Returns vehicle's current status */
    public String getStatus() {
	return status;
    }
	
    /** Sets vehicle's current status */
    public void setStatus(String status) {
	this.status = status;
    }
	
    /** Returns true if vehicle is reserved by someone */
    public boolean isReserved() {
	return isReserved;
    }
	
    /** Reserves a vehicle or cancels a reservation */
    public void setReserved(boolean status) {
	isReserved = status;
    }

    /** Returns speed of vehicle */
    public double getSpeed() {
	return speed;
    }
	
    /** Sets the vehicle's current speed */
    public void setSpeed(double speed) {
	this.speed = speed;
    }
	
    /** Returns base speed of vehicle */
    public double getBaseSpeed() {
	return baseSpeed;
    }
	
    /** Returns base speed of vehicle */
    public void setBaseSpeed(double speed) {
	baseSpeed = speed;
    }
	
    /** Returns the current amount of fuel in the vehicle. */
    public double getFuel() {
	return fuel;
    }
	
    /** Adds fuel to the vehicle. */
    public void addFuel(double addedFuel) {
	fuel += addedFuel;
	if (fuel > fuelCapacity) {
	    fuel = fuelCapacity;
	}
    }
	
    /** Consumes a portion of the vehicle's fuel. */
    public void consumeFuel(double consumedFuel) {
	fuel -= consumedFuel;
	if (fuel < 0D) {
	    fuel = 0D;
	}
    }
	
    /** Returns the fuel capacity of the vehicle. */
    public double getFuelCapacity() {
	return fuelCapacity;
    }
	
    /** Returns the fuel capacity of the vehicle. */
    protected void setFuelCapacity(double capacity) {
	fuelCapacity = capacity;
    }
	
    /** Returns total distance traveled by vehicle (in km.) */
    public double getTotalDistanceTraveled() {
	return distanceTraveled;
    }
	
    /** Adds a distance (in km.) to the vehicle's total distance traveled */
    public void addTotalDistanceTraveled(double distance) {
	distanceTraveled += distance;
    }
	
    /** Returns distance traveled by vehicle since last maintenance (in km.) */
    public double getDistanceLastMaintenance() {
	return distanceMaint;
    }

    /** Adds a distance (in km.) to the vehicle's distance since last maintenance */
    public void addDistanceLastMaintenance(double distance) { 
	distanceMaint += distance; 
	if ((distanceMaint > 5000D) && !distanceMark) {
	    distanceMark = true;
	}
    }
	
    /** Sets vehicle's distance since last maintenance to zero */
    public void clearDistanceLastMaintenance() {
	distanceMaint = 0;
    }

    /** Returns direction of vehicle (0 = north, clockwise in radians) */
    public double getDirection() {
	return direction;
    }

    /** Sets the vehicle's facing direction (0 = north, clockwise in radians) */
    public void setDirection(double direction) {
	this.direction = direction;
    }

    /** Returns the maximum passenger capacity of the vehicle (including the driver). */
    public int getMaxPassengers() {
	return maxPassengers;
    }

    /** Returns the maximum passenger capacity of the vehicle (including the driver). */
    protected void setMaxPassengers(int num) {
	maxPassengers = num;
    }

    /** Returns number of passengers in vehicle */
    public int getPassengerNum() {
	return passengers.size();
    }

    /** Returns a particular passenger by vector index number */
    public Person getPassenger(int index) { 
	Person result = null;
	if (index < passengers.size()) {
	    result = (Person) passengers.elementAt(index);
	}
	return result;
    }
	
    /** Returns true if a given person is currently in the vehicle */
    public boolean isPassenger(Person person) {
	
	for (int x=0; x < passengers.size(); x++) {
	    if (person == (Person) passengers.elementAt(x)) {
		return true;
	    }
	}
	return false;
    } 

    /** Add a new passenger to the vehicle if enough capacity and person is not alreay aboard. */
    public void addPassenger(Person passenger) { 
	if ( (passengers.size() < maxPassengers) && !isPassenger(passenger) ) {
	    passengers.addElement(passenger);
	}
    }
	
    /** Removes a passenger from a vehicle */
    public void removePassenger(Person passenger) {
	if (isPassenger(passenger)) {
	    passengers.removeElement(passenger);
	    if (passenger == driver) {
		driver = null;
	    }
	}
    }

    /** Returns driver of the vehicle */
    public Person getDriver() {
	return driver;
    }
	
    /** Sets the driver of the vehicle */
    public void setDriver(Person driver) {
	this.driver = driver;
    }

    /** Returns the current settlement vehicle is parked at.
     *  Returns null if vehicle is not currently parked at a settlement. */
    public Settlement getSettlement() { 
	if ((status.equals("Parked") || status.equals("Periodic Maintenance")) && (settlement != null)) {
	    return settlement;
	} else {
	    return null;
	}
    }
    
    /** Sets the settlement which the vehicle is parked at */
    public void setSettlement(Settlement settlement) {
	this.settlement = settlement;
	if (settlement != null) {
	    location.setCoords(settlement.getCoordinates());
	    settlement.addVehicle(this);
	}
    }
	
    /** Returns distance to destination in kilometers
     *  Returns 0 if vehicle is not currently moving toward a destination */
    public double getDistanceToDestination() {
	return distanceToDestination;
    }
	
    /** Sets the vehicle's distance to its destination */
    public void setDistanceToDestination(double distanceToDestination) {
	this.distanceToDestination = distanceToDestination;
    }

    /** Gets the type of destination for the vehicle */
    public String getDestinationType() {
	return destinationType;
    }

    /** Sets the type of destination for the vehicle ("Coordinates", "Settlement" or "None") */
    public void setDestinationType(String destinationType) {
	this.destinationType = destinationType;
    }

    /** Sets the destination coordinates */
    public void setDestination(Coordinates destinationCoords) {
	this.destinationCoords = destinationCoords;
	destinationType = "Coordinates";
    }
	
    /** Returns the destination coordinates.
     *  (null if no destination). */
    public Coordinates getDestination() { 
	return destinationCoords;
    }
	
    /** Sets the destination settlement */
    public void setDestinationSettlement(Settlement destinationSettlement) {
	this.destinationSettlement = destinationSettlement;
	if (destinationSettlement != null) {
	    setDestination(destinationSettlement.getCoordinates());
	    destinationType = "Settlement";
	}
    }

    /** Returns the destination settlement.
     *  (null if no destination settlement). */
    public Settlement getDestinationSettlement() {
	return destinationSettlement;
    }
	
    /** Returns the vehicle's size. */
    public int getSize() {
	return vehicleSize;
    }
	
    /** sets the vehicle's size. */
    protected void setSize(int size) {
	vehicleSize = size;
    }
	
    public void addPotentialFailure(String failureName) {
	potentialFailures.put(failureName, new Integer(1));
    }

    /** Returns a vector of the vehicle's current failures. */
    public MechanicalFailure getMechanicalFailure() {
	return mechanicalFailure;
    }
	
    /** Creates a new mechanical failure for the vehicle from its list
     *  of potential failures. */
    public void newMechanicalFailure() {
	
	Object keys[] = potentialFailures.keySet().toArray();
	
	// Sum weights
	int totalWeight = 0;
		
	for (int x=0; x < keys.length; x++) {
	    totalWeight += ((Integer) potentialFailures.get((String) keys[x])).intValue();
	}
		
	// Get a random number from 0 to the total weight	
	int r = (int) Math.round(Math.random() * (double) totalWeight);
		
	// Determine which failure is selected  
	int tempWeight = ((Integer) potentialFailures.get((String) keys[0])).intValue();
	int failureNum = 0;
	while (tempWeight < r) {
	    failureNum++;
	    tempWeight += ((Integer) potentialFailures.get((String) keys[failureNum])).intValue();
	}
	String failureName = (String) keys[failureNum];
		
	mechanicalFailure = new MechanicalFailure(failureName);
	// System.out.println(name + " has mechanical failure: " + mechanicalFailure.getName());
    }
	
    /** Add work to periodic vehicle maintenance. */
    public void addWorkToMaintenance(int seconds) {
		
	// If vehicle has already been maintained, return.
	if (distanceMaint == 0D) {
	    return;
	}
		
	// Add work to maintenance work done.
	maintenanceWork += seconds;
		
	// If maintenance work is complete, vehicle good for 5,000 km.
	if (maintenanceWork >= totalMaintenanceWork) {
	    maintenanceWork = 0;
	    distanceMaint = 0D;
	}
    }
	
    /** Returns the current amount of work towards maintenance. */
    public int getCurrentMaintenanceWork() {
	return maintenanceWork;
    }
	
    /** Returns the total amount of work needed for maintenance. */
    public int getTotalMaintenanceWork() {
	return totalMaintenanceWork;
    }
}
