/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** The EatMeal class is a task for eating a meal.
 *  The duration of the task is 20 millisols.
 *
 *  Note: Eating a meal reduces hunger to 0.
 */
class EatMeal extends Task implements Serializable {

    // Static members
    private static final double DURATION = 20D; // The predetermined duration of task in millisols
    private static final double STRESS_MODIFIER = -.2D; // The stress modified per millisol.

    /** Constructs a EatMeal object
     *  @param person the person to perform the task
     */
    public EatMeal(Person person) {
        super("Eating a meal", person, false, false, STRESS_MODIFIER);
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
        	try {
				// If person is in a settlement, try to find a dining area.
        		Building diningBuilding = getAvailableDiningBuilding(person);
        		if (diningBuilding != null) 
        			BuildingManager.addPersonToBuilding(person, diningBuilding);
        	}
        	catch (BuildingException e) {
        		System.err.println("EatMeal.constructor(): " + e.getMessage());
        		endTask();
        	}
        }
        else if (location.equals(Person.OUTSIDE)) endTask();
    }

    /** Returns the weighted probability that a person might perform this task.
     *
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {

        double result = person.getPhysicalCondition().getHunger() - 250D;
        if (result < 0D) result = 0D;
        
        if (person.getLocationSituation().equals(Person.OUTSIDE)) result = 0D;
	
		try {
			Building building = getAvailableDiningBuilding(person);
			result *= Task.getCrowdingProbabilityModifier(person, building);
		}
		catch (BuildingException e) {
			System.err.println("EatMeal.getProbability(): " + e.getMessage());
		}
	
        return result;
    }

    /** 
     * This task allows the person to eat for the duration.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        person.getPhysicalCondition().setHunger(0D);
        timeCompleted += time;
        if (timeCompleted > DURATION) {
        	try {
        		SimulationConfig simConfig = Simulation.instance().getSimConfig();
            	PersonConfig config = simConfig.getPersonConfiguration();
            	person.consumeFood(config.getFoodConsumptionRate() * (1D / 3D));
        	}
        	catch (Exception e) {
        		System.err.println(person.getName() + " unable to eat meal: " + e.getMessage());
        	}
            endTask();
           
            return timeCompleted - DURATION;
        }
        else return 0D; 
    }
    
    /**
     * Gets an available dining building that the person can use.
     * Returns null if no dining building is currently available.
     *
     * @param person the person
     * @return available dining building
     * @throws BuildingException if error finding dining building.
     */
    private static Building getAvailableDiningBuilding(Person person) throws BuildingException {
     
        Building result = null;
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
        	BuildingManager manager = settlement.getBuildingManager();
        	List diningBuildings = manager.getBuildings(Dining.NAME);
        	diningBuildings = BuildingManager.getNonMalfunctioningBuildings(diningBuildings);
        	diningBuildings = BuildingManager.getLeastCrowdedBuildings(diningBuildings);
        	
			if (diningBuildings.size() > 0) {
				// Pick random dining building from list.
				int rand = RandomUtil.getRandomInt(diningBuildings.size() - 1);
				result = (Building) diningBuildings.get(rand);
			}
		}
        
        return result;
    }
}