package cslicer.analyzer;

import java.io.Serializable;
import java.util.Collections;

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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Slicing result. Assign each commit with a flag.
 * 
 * @author Yi Li
 *
 */
public class SlicingResult implements Serializable {

	public enum DEP_FLAG implements Serializable {
		DROP, TEST, COMP, HUNK
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LinkedHashMap<RevCommit, DEP_FLAG> fCommits;
	private int fFUNCCount, fCOMPCount, fHUNKCount;

	public SlicingResult() {
		fCommits = new LinkedHashMap<RevCommit, DEP_FLAG>();
		fFUNCCount = 0;
		fCOMPCount = 0;
		fHUNKCount = 0;
	}

	public SlicingResult(List<RevCommit> history) {
		this();
		for (RevCommit h : history)
			fCommits.put(h, DEP_FLAG.DROP);
	}

	public SlicingResult(List<RevCommit> history,
			Map<RevCommit, DEP_FLAG> labels) {
		this();
		for (RevCommit h : history) {
			DEP_FLAG label = labels.get(h);
			fCommits.put(h, label);
			countLabel(label);
		}
	}

	private void countLabel(DEP_FLAG label) {
		switch (label) {
		case COMP:
			fCOMPCount++;
			break;
		case DROP:
			break;
		case HUNK:
			fHUNKCount++;
			break;
		case TEST:
			fFUNCCount++;
			break;
		default:
			break;
		}
	}

	/**
	 * Add flag for a commit.
	 * 
	 * @param c
	 *            commit reference
	 * @param f
	 *            {@link DEP_FLAG} label
	 */
	public void add(RevCommit c, DEP_FLAG f) {
		fCommits.put(c, f);
		countLabel(f);
	}

	/**
	 * Get label for a commit.
	 * 
	 * @param c
	 *            commit reference
	 * @return {@link DEP_FLAG} label
	 */
	public DEP_FLAG getLabel(RevCommit c) {
		return fCommits.get(c);
	}

	/**
	 * Get the whole labeled history.
	 * 
	 * @return labeled history
	 */
	public List<Pair<RevCommit, DEP_FLAG>> getLabeledHistory() {
		List<Pair<RevCommit, DEP_FLAG>> res = new LinkedList<Pair<RevCommit, DEP_FLAG>>();
		for (RevCommit c : fCommits.keySet()) {
			res.add(Pair.of(c, fCommits.get(c)));
		}
		return res;
	}

	/**
	 * The number of commits in original history.
	 * 
	 * @return length of original history
	 */
	public int getTotalCount() {
		return fCommits.size();
	}

	/**
	 * The number of commits dropped after slicing.
	 * 
	 * @return size of sliced history
	 */
	public int getDropCount() {
		int dropCount = 0;
		for (RevCommit a : fCommits.keySet())
			if (getLabel(a).equals(DEP_FLAG.DROP))
				dropCount++;

		assert dropCount == fCommits.size() - fCOMPCount - fFUNCCount
				- fHUNKCount;

		return dropCount;
	}

	public List<RevCommit> getPicks() {
		List<RevCommit> res = new LinkedList<RevCommit>();
		for (RevCommit c : fCommits.keySet())
			if (!getLabel(c).equals(DEP_FLAG.DROP))
				res.add(c);
		Collections.reverse(res);
		return res;
	}

	public List<RevCommit> getFuncComp() {
		List<RevCommit> res = new LinkedList<RevCommit>();
		for (RevCommit c : fCommits.keySet())
			if (getLabel(c).equals(DEP_FLAG.TEST)
					|| getLabel(c).equals(DEP_FLAG.COMP))
				res.add(c);
		Collections.reverse(res);
		return res;
	}

	public int getTestCount() {
		return fFUNCCount;
	}

	public int getCompCount() {
		return fCOMPCount;
	}

	public int getHunkCount() {
		return fHUNKCount;
	}

	public double getReductionRate(boolean includeHunks) {
		double size = (double) fCommits.size();
		double positive = (double) (fFUNCCount + fCOMPCount);
		if (includeHunks)
			positive += (double) fHUNKCount;
		return (size - positive) * 100 / size;
	}

	public List<RevCommit> getDrops() {
		List<RevCommit> res = new LinkedList<RevCommit>();
		for (RevCommit c : fCommits.keySet())
			if (getLabel(c).equals(DEP_FLAG.DROP))
				res.add(c);
		Collections.reverse(res);
		return res;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();

		// int hunkDeps = 0, semDeps = 0;

		for (RevCommit a : fCommits.keySet()) {

			// if (getLabel(a).equals(DEP_FLAG.TEST)
			// || getLabel(a).equals(DEP_FLAG.COMP))
			// semDeps++;
			// if (!getLabel(a).equals(DEP_FLAG.DROP))
			// hunkDeps++;

			res.append(fCommits.get(a) + ": " + a.abbreviate(8).name() + " : "
					+ a.getShortMessage());
			res.append("\n");
		}

		// float size = (float) fCommits.size();
		//
		// res.append("Reduction Rates: " + (size - semDeps) * 100 / size +
		// "%\n");
		// res.append(
		// "Reduction Hunks: " + (size - hunkDeps) * 100 / size + "%\n");

		return res.toString();
	}

	public List<RevCommit> getHunks() {
		List<RevCommit> res = new LinkedList<RevCommit>();
		for (RevCommit c : fCommits.keySet())
			if (getLabel(c).equals(DEP_FLAG.HUNK))
				res.add(c);
		Collections.reverse(res);
		return res;
	}

	// public int getCommitLineCount() {
	// int total = 0;
	// for (RevCommit c : fCommits.keySet()) {
	// if (!getLabel(c).equals(DEP_FLAG.DROP)) {
	// total += CommitChangedLineCount(c);
	// }
	// }
	// return total;
	// }
}
