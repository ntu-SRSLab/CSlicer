package cslicer.daikon;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _                  
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2019 Department of Computer Science, University of Toronto
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.analyzer.Learner;
import cslicer.analyzer.Slicer;
import cslicer.distiller.GitRefSourceCodeChange;


public class Refinement {
	public static Set<GitRefSourceCodeChange> throwLowestLevelChanges(
			Set<GitRefSourceCodeChange> changeSet) {
		// Set<GitRefSourceCodeChange> toKeepSet = new LinkedHashSet<>();
		Set<GitRefSourceCodeChange> toThrowSet = new LinkedHashSet<>();
		int currentLowestLevel = ChangesRank.MAXIMUM_LEVEL;

		for (GitRefSourceCodeChange change : changeSet) {
			// update current lowest level
			if (change.getSignificanceRank() < currentLowestLevel) {
				currentLowestLevel = change.getSignificanceRank();
				toThrowSet.clear();
			}
			if (change.getSignificanceRank() == currentLowestLevel) {
				toThrowSet.add(change);
			}
		}
		System.out.println("[TO THROW]: " + toThrowSet.size());
		if (toThrowSet.size() == changeSet.size()) // this means we cannot throw
													// anymore changes by
													// checking levels
		{
			return changeSet;
		}
		changeSet.removeAll(toThrowSet);
		System.out.println("[TO KEEP]: " + changeSet.size());

		return changeSet;
	}

	public static List<RevCommit> convertChangeSetToCommitList(Learner ref,
			Set<GitRefSourceCodeChange> newChangeSet) {
		// List<RevCommit> originalCommitList = ref.getOriginalCommitList();
		List<RevCommit> newCommitList = new LinkedList<>();

		for (GitRefSourceCodeChange change : newChangeSet) {
			newCommitList.add(change.getRelatedCommit());
		}
		
		return newCommitList;
	}

	public static Set<Invariant> computeNewInvariantSet(String filePath) {
		Set<Invariant> newInvariantSet = new LinkedHashSet<>();
		InvariantSetParser parser = new InvariantSetParser();
		newInvariantSet = parser.loadFlatInvariantSetFromHierarchicalSet(
				parser.parseInvariantSetFromDaikonOutputFile(filePath));

		return newInvariantSet;
	}
}
