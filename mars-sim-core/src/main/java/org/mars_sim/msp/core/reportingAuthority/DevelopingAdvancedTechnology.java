/**
 * Mars Simulation Project
 * DevelopingAdvancedTechnology.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

public class DevelopingAdvancedTechnology implements MissionAgenda {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final String name = "Developing Advanced Technologies";

	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings() {
		System.out.println("I'm putting together a report of how advanced technologies may be tested and successfully deployed here.");
	}

	@Override
	public void gatherSamples() {
		System.out.println("I'm mappping the morphology of this local region and where to use as test bed for developing advanced technologies of interest.");
	}


}
