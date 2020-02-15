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
 * Copyright (C) 2014 - 2020 Department of Computer Science, University of Toronto
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
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.Learner;
import cslicer.analyzer.ProjectConfiguration;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.analyzer.Slicer;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.utils.StatsUtils;

public class TestDaikon {

	private RevCommit target;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// the example in gitref.pdf
		target = TestUtils.setupJavaSlicerRepo(tempFolder, BUILD_SYSTEM.MAVEN);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testFull() throws Exception {
		// PrintUtils.supressDebugMessages();
		ProjectConfiguration config = new ProjectConfiguration();

		Learner ref = new Learner(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setEnableBuilderOutput(false).setStartCommitId(target.name())
				.setOutputHunkGraph(true)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		//List<RevCommit> drop = ref.doSlicing().getPicks();
		
		//get original (endcommit) invariant sets
		InvariantSetParser parser = new InvariantSetParser();
		Set<Invariant> lastPassInvariantSet = parser.loadFlatInvariantSetFromHierarchicalSet(
				parser.parseInvariantSetFromDaikonOutputFile("endcommit-trace"));
		
		//initially rank changes by rules
		Set<GitRefSourceCodeChange> changeSet = ref.extractChangesForDaikon();
		ChangesRank.initiallyRankChangesByRules(changeSet);
		
		//refinement loop ================================================
		//loop tag
		Set<GitRefSourceCodeChange> newChangeSet = Refinement.throwLowestLevelChanges(changeSet);
		if(newChangeSet.equals(changeSet)) // this means we cannot throw anymore changes by using levels
		{
			//check if we achieve optimal result;
			//break the refinement loop;
		}
		List<RevCommit> commitList = Refinement.convertChangeSetToCommitList(ref,newChangeSet);
		
		boolean isPass = ref.verifyResultTestPassing(commitList);//generate trace file
		if(isPass)
		{
			//compute new invariant set
			Set<Invariant> newInvariantSet = Refinement.computeNewInvariantSet("new-tracefile");
			//decrease A-B rank
			Set<Invariant> SetAminusSetB = InvariantSetOperators.SetAMinusSetB(lastPassInvariantSet, newInvariantSet);
			InvariantsRank.decreaseInvariantsRank(SetAminusSetB);
			//update A
			lastPassInvariantSet = newInvariantSet;
		}
		else
		{
			//compute new invariant set
			Set<Invariant> newInvariantSet = Refinement.computeNewInvariantSet("new-tracefile");
			//increase A-B rank
			Set<Invariant> SetAminusSetB = InvariantSetOperators.SetAMinusSetB(lastPassInvariantSet, newInvariantSet);
			InvariantsRank.increaseInvariantsRank(SetAminusSetB);
		}
		//re-rank changes based on invariant ranks
		ChangesRank.rerankChanges(newChangeSet);
		//go back to loop tag ============================================
		
		

		// verify the sliced results
		// assertEquals(1, ref.getSlicingReseult().getDropCount());
		//assertTrue(ref.verifyResultTestPassing(drop));
		StatsUtils.print();
	}

}
