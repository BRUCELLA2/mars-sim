/**
 * Mars Simulation Project
 * JobAssignmentType.java
 * @version 3.08 2015-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.Msg;

public enum JobAssignmentType {

	PENDING				(Msg.getString("JobAssignmentType.pending")), //$NON-NLS-1$
	APPROVED			(Msg.getString("JobAssignmentType.approved")), //$NON-NLS-1$
	SETTLEMENT			(Msg.getString("JobAssignmentType.settlement")), //$NON-NLS-1$
	MARSSIMMER			(Msg.getString("JobAssignmentType.marssimmer")), //$NON-NLS-1$
	MISSION_CONTROL		(Msg.getString("JobAssignmentType.missionControl")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private JobAssignmentType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
