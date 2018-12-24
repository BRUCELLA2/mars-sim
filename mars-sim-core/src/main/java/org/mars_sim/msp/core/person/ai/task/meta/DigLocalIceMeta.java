/**
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Inventory;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;

import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** default logger. */
    //private static Logger logger = Logger.getLogger(DigLocalIceMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new DigLocalIce(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
   
        if (person.isInSettlement()) {
        	
	    	Settlement settlement = person.getSettlement();
	     
	    	// Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;
	
	        // Checked for radiation events
	    	boolean[] exposed = settlement.getExposed();
	
	
			if (exposed[2]) {
				// SEP can give lethal dose of radiation
	            return 0;
			}
			
            // Check if it is night time.          
            if (EVAOperation.isGettingDark(person))
            	return 0;
            
            Inventory inv = settlement.getInventory();

            // Check at least one EVA suit at settlement.
            int numSuits = inv.findNumUnitsOfClass(EVASuit.class);
            if (numSuits == 0) {
                return 0;
            }

            // Check if at least one empty bag at settlement.
            int numEmptyBags = inv.findNumEmptyUnitsOfClass(Bag.class, false);
            if (numEmptyBags == 0) {
                return 0;
            }

            result = settlement.getIceProbabilityValue() * 4000D;

            // Stress modifier
            result -= person.getStress() * 1D;
            // fatigue modifier
            result -= (person.getFatigue()-100) / 50D;

            if (result < 1)
            	return 0;

            // Crowded settlement modifier
            if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
                result *= 1.5D;

            if (settlement.getIndoorPeopleCount() <= 4)
                result *= 1.5D;

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
     
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null)
                result *= job.getStartTaskProbabilityModifier(DigLocalIce.class);

            // Modify if field work is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity() == FavoriteType.FIELD_WORK)
                result *= RandomUtil.getRandomInt(1, 3);

            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

            //logger.info("DigLocalIceMeta's probability : " + Math.round(result*100D)/100D);

	    	if (exposed[0]) {
				result = result/2D;// Baseline can give a fair amount dose of radiation
			}

	    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result/4D;
			}
	    	
            if (result < 0D) {
                result = 0D;
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}