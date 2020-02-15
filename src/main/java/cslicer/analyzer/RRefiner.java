package cslicer.analyzer;

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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.builder.BuildScriptInvalidException;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.TestFailureException;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutBranchFailedException;
import cslicer.jgit.CheckoutFileFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.DeleteBranchFailedException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;

public class RRefiner extends Refiner {

	public RRefiner(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException,
			CheckoutBranchFailedException, ClassPathInvalidException,
			CheckoutFileFailedException, IOException,
			CompilationFailureException, TestFailureException {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<RevCommit> refineSlice(SCHEME scheme)
			throws IOException, BuildScriptInvalidException {
		StatsUtils.resume("total");

		// Empty tried set initially
		Set<List<RevCommit>> tried = new HashSet<>();

		do {
			StatsUtils.count("definer.call");
			computeOneMinimalHistory(scheme, tried);
		} while (!isCurrentSliceMinimal(tried));

		StatsUtils.stop("total");

		List<RevCommit> minimal = fTracker.getPresentHistory();
		for (RevCommit c : minimal) {
			PrintUtils.print("H*: " + commitSummary(c), TAG.OUTPUT);
		}

		PrintUtils.print("|H*| = " + fTracker.getPresentCommits().size(),
				TAG.OUTPUT);
		StatsUtils.setCount("hstar.length",
				fTracker.getPresentCommits().size());

		try {
			cleanBranches();
			fJGit.deleteBranch(REFINE_BRANCH);
		} catch (DeleteBranchFailedException e) {
			PrintUtils.print("Cleanning branches failed!", TAG.WARNING);
		}

		return minimal;
	}

	private boolean isCurrentSliceMinimal(Set<List<RevCommit>> tried) {
		Set<RevCommit> remain = fTracker.getPresentCommits();
		if (tried.size() < Math.pow(2, remain.size()))
			return false;

		// Go through tried set once and count the number of relevant slices.
		int count = 0;
		for (List<RevCommit> t : tried) {
			if (remain.containsAll(t))
				count++;
		}

		// Return true if all subsets have been tried (excluding the empty and
		// full sets).
		return count >= Math.pow(2, remain.size()) - 2;
	}
}
