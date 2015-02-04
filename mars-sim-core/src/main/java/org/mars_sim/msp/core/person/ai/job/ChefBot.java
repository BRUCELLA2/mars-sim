/**
 * Mars Simulation Project
 * ChefBot.java
 * @version 3.07 2015-02-02
 * @author Manny
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/** 
 * The Chef class represents a job for a chef.
 */
public class ChefBot
extends RobotJob
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//	private static Logger logger = Logger.getLogger(Chef.class.getName());

	/** constructor. */
	public ChefBot() {
		// Use Job constructor
		super(ChefBot.class);

		// Add chef-related tasks.
		
		jobTasks.add(CookMeal.class);
		//jobTasks.add(PrepareDessert.class);
		jobTasks.add(ProduceFood.class);

	}

	/**
	 * Gets a robot's capability to perform this job.
	 * @param robot the person to check.
	 * @return capability (min 0.0).
	 */	
	public double getCapability(Robot robot) {

		double result = 0D;

		int cookingSkill = robot.getMind().getSkillManager().getSkillLevel(SkillType.COOKING);
		result = cookingSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);	

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;

		// Add all kitchen work space in settlement.
		List<Building> kitchenBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.COOKING);
		Iterator<Building> i = kitchenBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING); 
			result += (double) kitchen.getCookCapacity();
		}

		// Add total population / 10.
		int population = settlement.getCurrentPopulationNum();
		result+= ((double) population / 10D);

		return result;			
	}
}