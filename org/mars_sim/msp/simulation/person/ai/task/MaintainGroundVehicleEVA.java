/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVA.java
 * @version 2.78 2005-08-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles outside a settlement.
 */
public class MaintainGroundVehicleEVA extends EVAOperation implements Serializable {
 
    // Phase names
    private static final String MAINTAIN_VEHICLE = "Maintain Vehicle";
 
    private GroundVehicle vehicle; // Vehicle to be maintained.
    private Airlock airlock; // Airlock to be used for EVA.
    
	/** 
	 * Constructor
	 * @param person the person to perform the task
	 * @throws Exception if error constructing task.
	 */
    public MaintainGroundVehicleEVA(Person person) throws Exception {
        super("Performing Vehicle Maintenance", person);
   
        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) vehicle.setReservedForMaintenance(true);
        else endTask();
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();
        
        // Initialize phase.
        addPhase(MAINTAIN_VEHICLE);
        
        // System.out.println(person.getName() + " starting MaintainGroundVehicleEVA task.");
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

		// Get all vehicles needing maintenance.
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			VehicleIterator i = getAllVehicleCandidates(person).iterator();
			while (i.hasNext()) {
				MalfunctionManager manager = i.next().getMalfunctionManager();
				double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 200D);
				if (entityProb > 50D) entityProb = 50D;
				result += entityProb;
			}
		}

		// Determine if settlement has available space in garages.
		boolean garageSpace = false;
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {	
			Settlement settlement = person.getSettlement();
			Iterator j = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
			while (j.hasNext()) {
				try {
					Building building = (Building) j.next();
					VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
					if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) garageSpace = true;
				}
				catch (Exception e) {}
			}
        }
		if (garageSpace) result = 0D;

        // Check if an airlock is available
        if (getAvailableAirlock(person) == null) result = 0D;

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
        	if (!surface.inDarkPolarRegion(person.getCoordinates()))
        		result = 0D;
        } 
        
		// Crowded settlement modifier
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;
		}

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleEVA.class);        
	
        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
    	if (MAINTAIN_VEHICLE.equals(getPhase())) return maintainVehicle(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
    	else return time;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is maintain vehicle, add experience to mechanics skill.
		if (MAINTAIN_VEHICLE.equals(getPhase())) {
			// 1 base experience point per 100 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 100D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
		}
	}
   
    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting the airlock.
     */
    private double exitEVA(double time) throws Exception {
    	
    	try {
    		time = exitAirlock(time, airlock);
        
    		// Add experience points
    		addExperience(time);
    	}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
    	
        
        if (exitedAirlock) setPhase(MAINTAIN_VEHICLE);
        return time;
    }
    
    /**
     * Perform the maintain vehicle phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error maintaining vehicle.
     */
    private double maintainVehicle(double time) throws Exception {
        
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);
        if (finishedMaintenance) vehicle.setReservedForMaintenance(false);
        
        if (finishedMaintenance || malfunction || shouldEndEVAOperation()) {
            setPhase(ENTER_AIRLOCK);
            return time;
        }
        
        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);
        
        // Add experience points
        addExperience(time);
	
        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return 0D;
    }   
    
    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) throws Exception {
        time = enterAirlock(time, airlock);
        
        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) endTask();
        return time;
    }	
    
    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        super.checkForAccident(time);
        
        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while performing maintenance on " + vehicle.getName() + ".");
            vehicle.getMalfunctionManager().accident();
        }
    }
    
    /** 
     * Gets the vehicle  the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getVehicle() {
        return vehicle;
    }
    
    /**
     * Gets all ground vehicles requiring maintenance that are parked outside the settlement.
     *
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    private static VehicleCollection getAllVehicleCandidates(Person person) {
        VehicleCollection result = new VehicleCollection();
        
        VehicleIterator vI = person.getSettlement().getParkedVehicles().iterator();
        while (vI.hasNext()) {
            Vehicle vehicle = vI.next();
            if ((vehicle instanceof GroundVehicle) && !vehicle.isReservedForMission()) result.add(vehicle);
        }
        
        return result;
    }
    
    /**
     * Gets a ground vehicle that requires maintenance outside the settlement.
     * Returns null if none available.
     *
     * @param person person checking.
     * @return ground vehicle
     */
    private GroundVehicle getNeedyGroundVehicle(Person person) {
            
        GroundVehicle result = null;

        // Find all vehicles that can be maintained.
        VehicleCollection availableVehicles = getAllVehicleCandidates(person);
        
        // Determine total probability weight.
        double totalProbWeight = 0D;
        VehicleIterator i = availableVehicles.iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = i.next().getMalfunctionManager();
            totalProbWeight = manager.getEffectiveTimeSinceLastMaintenance();
        }
        
        // Get random value
        double rand = RandomUtil.getRandomDouble(totalProbWeight);
        
        // Determine which vehicle was picked.
        VehicleIterator i2 = availableVehicles.iterator();
        while (i2.hasNext()) {
            Vehicle vehicle = i2.next();
            if (result == null) {
                MalfunctionManager manager = vehicle.getMalfunctionManager();
                double probWeight = manager.getEffectiveTimeSinceLastMaintenance();
                if (rand < probWeight) result = (GroundVehicle) vehicle;
                else rand -= probWeight;
            }
        }
        
        return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.MECHANICS);
		return results;
	}
}