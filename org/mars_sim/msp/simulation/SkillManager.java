//************************** Skill Manager **************************
// Last Modified: 7/27/00

// The SkillManager class manages skills for a given person.
// Each person has one skill manager.

package org.mars_sim.msp.simulation;

import java.util.*;

public class SkillManager {
	
	// Data members

	private Hashtable skills; // A list of the person's skills keyed by name.

	// Constructor

	public SkillManager() {
		skills = new Hashtable();
		
		// Add starting skills randomly for person.
		
		String[] startingSkills = {"Driving", "Greenhouse Farming", "Vehicle Mechanic"};
		
		for (int x=0; x < startingSkills.length; x++) {
			int skillLevel = getInitialSkillLevel(0, 50);
			if (skillLevel > 0) {
				Skill newSkill = new Skill(startingSkills[x]);
				newSkill.setLevel(skillLevel);
				addNewSkill(newSkill);
			}
		}		
	}
	
	// Returns an initial skill level.
	
	private int getInitialSkillLevel(int level, int chance) {
		if (RandomUtil.lessThanRandPercent(chance)) return getInitialSkillLevel(level + 1, chance / 2);
		else return level;
	}
	
	// Returns the number of skills.
	
	public int getSkillNum() { return skills.size(); }
	
	// Returns number of skills at skill level 1 or better.
	
	public int getDisplayableSkillNum() {
		String[] keys = getKeys();
		int count = 0;
		for (int x=0; x < keys.length; x++)
			if (getSkillLevel(keys[x]) >= 1) count++;
			
		return count;
	}
		
	// Returns an array of the skill names as strings.
	
	public String[] getKeys() { 
		
		Object[] tempArray = skills.keySet().toArray();
		String[] keyArray = new String[tempArray.length];
		for (int x=0; x < tempArray.length; x++) keyArray[x] = (String) tempArray[x];
		
		return keyArray;
	}
	
	// Returns true if the SkillManager has the named skill, false otherwise.
	
	public boolean hasSkill(String skillName) {
		if (skills.containsKey(skillName)) return true;
		else return false;
	}
	
	// Returns the integer skill level from a named skill if it exists in the SkillManager.
	// Returns 0 otherwise.
	
	public int getSkillLevel(String skillName) { 
		int result = 0;
		if (skills.containsKey(skillName)) result = ((Skill)skills.get(skillName)).getLevel();
		
		return result;
	}
	
	// Adds a new skill to the SkillManager and indexes it under its name.
	
	public void addNewSkill(Skill newSkill) {
		skills.put(newSkill.getName(), newSkill);
	}
	
	// Adds given experience points to a named skill if it exists in the SkillManager.
	// If it doesn't exist, create a skill of that name in the SkillManager and add the experience points to it.
	
	public void addExperience(String skillName, double experiencePoints) {
		if (hasSkill(skillName)) ((Skill)skills.get(skillName)).addExperience(experiencePoints);
		else {
			addNewSkill(new Skill(skillName));
			addExperience(skillName, experiencePoints);
		}
	}
}	

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
