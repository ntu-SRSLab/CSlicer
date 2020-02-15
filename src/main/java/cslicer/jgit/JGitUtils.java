package cslicer.jgit;

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

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import cslicer.utils.PrintUtils;

public class JGitUtils {

	/**
	 * Prepare a tree parser for the given commit in target repository.
	 * 
	 * @param repo
	 *            target repository
	 * @param commit
	 *            given commit
	 * @return an {@link AbstractTreeIterator}
	 * @throws IOException
	 *             if I\O exception occurs
	 * @throws MissingObjectException
	 *             if object cannot be located
	 * @throws IncorrectObjectTypeException
	 *             if object type is incorrect
	 */
	public static AbstractTreeIterator prepareTreeParser(Repository repo,
			RevCommit commit) throws IOException, MissingObjectException,
					IncorrectObjectTypeException {
		RevWalk walk = new RevWalk(repo);
		RevTree tree = walk.parseTree(commit.getTree().getId());

		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		ObjectReader oldReader = repo.newObjectReader();

		try {
			oldTreeParser.reset(oldReader, tree.getId());
		} finally {
			oldReader.close();
		}

		walk.close();
		walk.dispose();

		return oldTreeParser;
	}

	/**
	 * Determine if a repository has any commits. This is determined by checking
	 * the for loose and packed objects.
	 * 
	 * @param repo
	 *            target repository
	 * @return {@code true} if the repository has commits
	 */
	public static boolean hasCommits(Repository repo) {
		if (repo != null && repo.getDirectory().exists()) {
			return (FileUtils.getFile(repo.getDirectory(), "objects")
					.list().length > 2)
					|| (FileUtils
							.getFile(repo.getDirectory(), "objects", "pack")
							.list().length > 0);
		}
		return false;
	}

	/**
	 * Return the merge base of two commits.
	 * 
	 * @param repo
	 *            target repository
	 * @param c1
	 *            first commit
	 * @param c2
	 *            second commit
	 * @return the merge base commit of c1 and c2
	 */
	public static @Nullable RevCommit getMergeBase(Repository repo,
			RevCommit c1, RevCommit c2) {
		RevCommit mergeBase = null;
		RevWalk walk = new RevWalk(repo);
		walk.setRevFilter(RevFilter.MERGE_BASE);

		try {
			// need to parse commits using this walker
			RevCommit cc1 = walk.parseCommit(c1.getId());
			RevCommit cc2 = walk.parseCommit(c2.getId());
			walk.markStart(cc1);
			walk.markStart(cc2);
			mergeBase = walk.next();
		} catch (IOException e) {
			PrintUtils.print("Error finding merge-base!");
			e.printStackTrace();
		} finally {
			walk.close();
			walk.dispose();
		}

		return mergeBase;
	}

	/**
	 * A string summarizes the commit info.
	 * 
	 * @param commit
	 *            given commit
	 * @return a summary string
	 */
	public static String summary(RevCommit commit) {
		if (commit == null)
			return "";
		return commit.abbreviate(8).name() + " : " + commit.getShortMessage();
	}
}
