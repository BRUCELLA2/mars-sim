/**
 * Mars Simulation Project
 * DeterminingHabitability.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

public class DeterminingHabitability implements MissionAgenda {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Determining Human Habitability";

	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings() {
		System.out.println("I'm putting together a report of the habitability of this local region for human beings.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm analyzing geographic factors of how suitable human beings are to live in this local region.");
	}




}
