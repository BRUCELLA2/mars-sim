/**
 * Mars Simulation Project
 * GreenhouseFacility.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation; 
 
/**
 * The GreenhouseFacility class represents the greenhouses in a settlement.
 * It defines the amount of fresh and dried foods generated by the greenhouses.
 */

public class GreenhouseFacility extends Facility {

	// Data members
	
	private float fullHarvestAmount;       // Number of number of food units the greenhouse can produce at full harvest.
	private float workLoad;                // Number of work-hours tending greenhouse required during growth period for full harvest.
	private float growingWork;             // Number of work-hours completed for growing phase.
	private float workCompleted;           // Number of work-hours completed for current phase.
	private float growthPeriod;            // Number of days for growth period.
	private float growthPeriodCompleted;   // Number of days completed in current growth period..
	private String phase;                  // "Inactive", "Planting", "Growing" or "Harvesting"

	// Constructor for random creation.

	public GreenhouseFacility(FacilityManager manager) {
	
		// Use Facility's constructor.
		
		super(manager, "Greenhouse");
	
		// Initialize data members
		
		workCompleted = 0F;
		growthPeriod = 20F;
		growthPeriodCompleted = 0F;
		phase = "Inactive";
	
		// Randomly determine full harvest amount.
		
		fullHarvestAmount = 10 + RandomUtil.getRandomInteger(20);
		
		// Determine work load based on full harvest amount.
		// (80hrs for 10 food - 160hrs for 30 food)
		
		workLoad = 40F + (4F * fullHarvestAmount);
	}
	
	// Constructor for set values (used later when facilities can be built or upgraded.)
	
	public GreenhouseFacility(FacilityManager manager, float workLoad, float growthPeriod, float fullHarvestAmount) {
	
		// Use Facility's constructor.
		
		super(manager, "Greenhouse");
		
		// Initialize data members.
		
		this.workLoad = workLoad;
		this.growthPeriod = growthPeriod;
		this.fullHarvestAmount = fullHarvestAmount;
		workCompleted = 0F;
		growthPeriodCompleted = 0F;
		phase = "Inactive";
	}
	
	// Returns the harvest amount of the greenhouse.
	
	public float getFullHarvestAmount() { return fullHarvestAmount; }
	
	// Returns the work load of the greenhouse. (in work-hours)
	
	public float getWorkLoad() { return workLoad; }
	
	// Returns the work completed in this cycle in the growing phase.
	
	public float getGrowingWork() { return growingWork; }
	
	// Returns the growth period of the greenhouse. (in days)
	
	public float getGrowthPeriod() { return growthPeriod; }
	
	// Returns the current work completed on the current phase. (in work-hours)
	
	public float getWorkCompleted() { return workCompleted; }
	
	// Returns the time completed of the current growth cycle. (in days)
	
	public float getTimeCompleted() { return growthPeriodCompleted; }
	
	// Returns true if a harvest cycle has been started.
	
	public String getPhase() { return phase; }
	
	// Adds work to the work completed on a growth cycle.
	
	public void addWorkToGrowthCycle(int seconds) { 
		
		float plantingWork = 4F * 60F * 60F;
		float harvestingWork = (.25F * fullHarvestAmount) * 60F * 60F;
		float workInPhase = (workCompleted * 60F * 60F) + seconds;
		
		if (phase.equals("Inactive")) phase = "Planting";
		
		if (phase.equals("Planting")) {
			if (workInPhase >= plantingWork) {
				workInPhase -= plantingWork;
				phase = "Growing";
			}
		}
		
		if (phase.equals("Growing")) growingWork = workInPhase / (60F * 60F);
		
		if (phase.equals("Harvesting")) {
			if (workInPhase >= harvestingWork) {
				workInPhase -= harvestingWork;
				double foodProduced = fullHarvestAmount * (growingWork / workLoad);
				((StoreroomFacility) manager.getFacility("Storerooms")).addFood(foodProduced);
				phase = "Planting";
				growingWork = 0F;
				growthPeriodCompleted = 0F;
			}
		}
		
		workCompleted = workInPhase / (60F * 60F);
	}
	
	// Override Facility's timePasses method to allow for harvest cycle.
	
	public void timePasses(int seconds) { 	
		
		if (phase.equals("Growing")) {
			growthPeriodCompleted += (seconds / (60F * 60F * 25F));
			if (growthPeriodCompleted >= growthPeriod) {
				phase = "Harvesting";
				workCompleted = 0F;
			}
		}
	}
}
