/**
 * Mars Simulation Project
 * ReviewJobReassignment.java
 * @version 3.08 2015-06-15
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ReviewJobReassignment class is a task for reviewing job reassignment submission in an office space
 */
public class ReviewJobReassignment
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private static transient Logger logger = Logger.getLogger(ReviewJobReassignment.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.reviewJobReassignment"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase REVIEWING_JOB_ASSIGNMENT = new TaskPhase(Msg.getString(
            "Task.phase.reviewingJobReassignment")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -1D;

    // Data members
    /** The administration building the person is using. */
    private Administration office;

    private MarsClock clock;

    public RoleType roleType;

    /**
     * Constructor. This is an effort-driven task.
     * @param person the person performing the task.
     */
    public ReviewJobReassignment(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER, true,
                50D + RandomUtil.getRandomDouble(100D));

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

        	//if (roleType == null)
        	//NOTE: sometimes enum is null. sometimes it is NOT. why?
            roleType = person.getRole().getType();

            if (roleType.equals(RoleType.PRESIDENT)
                	|| roleType.equals(RoleType.MAYOR)
            		|| roleType.equals(RoleType.COMMANDER)
        			|| roleType.equals(RoleType.SUB_COMMANDER) ) {

                //System.out.println("ReviewJobReassignment : "
                //		+ person.getName() + " (" + roleType
                //		+ ") is going to review job reassignment");

	            // If person is in a settlement, try to find an office building.
	            Building officeBuilding = getAvailableOffice(person);

	            // Note: office building is optional
	            if (officeBuilding != null) {
	                // Walk to the office building.
	                walkToActivitySpotInBuilding(officeBuilding, false);

	                office = (Administration) officeBuilding.getFunction(BuildingFunction.ADMINISTRATION);
	            }

	            // TODO: add other workplace if administration building is not available

	            // Iterates through each person's

        	    // Get highest person skill level.
        	    Iterator<Person> i = person.getSettlement().getAllAssociatedPeople().iterator();
                while (i.hasNext()) {
                    Person tempPerson = i.next();
                    List<JobAssignment> list = tempPerson.getJobHistory().getJobAssignmentList();
                    int last = list.size() -1 ;
                    JobAssignmentType status = list.get(last).getStatus();

                    if (status != null)
                    	if (status.equals(JobAssignmentType.PENDING)) {
	                        //System.out.println("ReviewJobReassignment : start reviewing job reassignment request from "
	                        //		+ tempPerson.getName() + "\n");

		                	// TODO in future
	                        // 1. Reviews user's rating
		                	int rating = list.get(last).getJobRating();
		                	//if (rating <=1) disapproved !
	                    	// 2. Reviews this person's preference
		                	// 3. May go to him/her to have a chat
		                	// 4. modified by the affinity between them
		                	// 5. Approve/disapprove the job change

		                	String pendingJobStr = list.get(last).getJobType();

		                	// Updates the job
		                	String approvedBy =  person.getName() + "(" + person.getRole().getType() + ")";
		                	tempPerson.getMind().reassignJob(pendingJobStr, true, JobManager.USER, JobAssignmentType.APPROVED, approvedBy);
		                	logger.info("ReviewJobReassignment : " + approvedBy + " just approved "
		                			+ tempPerson + "'s job reassignment as "
	                        		+ pendingJobStr);// + "\n");
	                    }
	                } // end of while
	            } // end of roleType
	            else {
	                endTask();
	            }
	        }
        //}
        //else {
        //    endTask();
        //}

        // Initialize phase
        addPhase(REVIEWING_JOB_ASSIGNMENT);
        setPhase(REVIEWING_JOB_ASSIGNMENT);
    }

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.ADMINISTRATION;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (REVIEWING_JOB_ASSIGNMENT.equals(getPhase())) {
            return reviewingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the reviewingPhasephase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double reviewingPhase(double time) {

        // Do nothing

        return 0D;
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();

        // Remove person from administration function so others can use it.
        if (office != null && office.getNumStaff() > 0) {
            office.removeStaff();
        }
    }

    /**
     * Gets an available building with the administration function.
     * @param person the person looking for the office.
     * @return an available office space or null if none found.
     */
    public static Building getAvailableOffice(Person person) {
        Building result = null;

        // If person is in a settlement, try to find a building with )an office.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            BuildingManager buildingManager = person.getSettlement()
                    .getBuildingManager();
            List<Building> offices = buildingManager.getBuildings(BuildingFunction.ADMINISTRATION);
            offices = BuildingManager.getNonMalfunctioningBuildings(offices);
            offices = BuildingManager.getLeastCrowdedBuildings(offices);

            if (offices.size() > 0) {
                Map<Building, Double> selectedOffices = BuildingManager.getBestRelationshipBuildings(
                        person, offices);
                result = RandomUtil.getWeightedRandomObject(selectedOffices);
            }
        }

        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        office = null;
    }
}