/**
 * Mars Simulation Project
 * EatMeal.java
 * @version 2.72 2001-08-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The EatMail class is a task for eating a meal.
 *  The duration of the task is 20 millisols.
 *
 *  Note: Eating a meal reduces hunger
 */
class EatMeal extends Task {

    // Data members
    private double duration = 20D; // The predetermined duration of task in millisols

    /** Constructs a EatMeal object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public EatMeal(Person person, VirtualMars mars) {
        super("Eating a meal", person, mars);
        
        // System.out.println(person.getName() + " is eating with " + person.getHunger() + " hunger.");
    }

    /** Returns the weighted probability that a person might perform this task.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {

        double result = person.getHunger() - 250D;
        if (result < 0) result = 0;
        
        return result;
    }

    /** This task allows the person to eat for the duration. 
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        person.setHunger(0D);
        timeCompleted += time;
        if (timeCompleted > duration) {
            person.consumeFood(1D / 3D);
            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }
}

