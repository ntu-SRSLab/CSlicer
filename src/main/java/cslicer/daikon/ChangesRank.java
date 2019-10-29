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


import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import cslicer.distiller.GitRefSourceCodeChange;

public class ChangesRank {
	public static final int MINIMUM_LEVEL = -1000;
	public static final int LEVEL0 = 0;
	public static final int LEVEL1 = 1;
	public static final int MAXIMUM_LEVEL = 1000;

	public static void initiallyRankChangesByRules(
			Set<GitRefSourceCodeChange> changeSet) {
		for (GitRefSourceCodeChange change : changeSet) {
			SourceCodeChange c = change.getSourceCodeChange();

			// apply a set of rules.
			// modifier
			if (c.getChangeType() == ChangeType.INCREASING_ACCESSIBILITY_CHANGE
					|| c.getChangeType() == ChangeType.DECREASING_ACCESSIBILITY_CHANGE) {
				System.out.println("[SOURCE CODE]: "
						+ c.getChangedEntity().getUniqueName());
				System.out.println(
						"[CHANGE TYPE]: " + c.getChangeType().toString());
				change.setSignificanceRank(LEVEL0);
				continue;
			}
			// print/log
			if (c.getChangeType() == ChangeType.STATEMENT_INSERT
					|| c.getChangeType() == ChangeType.STATEMENT_UPDATE
					|| c.getChangeType() == ChangeType.STATEMENT_DELETE) {
				// System.out.println("[SOURCE CODE]: " +
				// c.getChangedEntity().getUniqueName());
				if (c.getChangedEntity().getUniqueName().contains("print")
						|| c.getChangedEntity().getUniqueName().contains("LOG")
						|| c.getChangedEntity().getUniqueName().contains("Log")
						|| c.getChangedEntity().getUniqueName()
								.contains("Print")) {
					System.out.println("[SOURCE CODE]: "
							+ c.getChangedEntity().getUniqueName());
					change.setSignificanceRank(LEVEL0);
					continue;
				}
			}
			// catch clause
			if (c.getChangedEntity().getType().toString()
					.equals("CATCH_CLAUSE")) {
				System.out.println("[SOURCE CODE]: "
						+ c.getChangedEntity().getUniqueName());
				change.setSignificanceRank(LEVEL0);
				continue;
			}
			// changedistiller none/low
			if (c.getChangeType().getSignificance() == SignificanceLevel.LOW
					|| c.getChangeType()
							.getSignificance() == SignificanceLevel.NONE) {
				System.out.println("[SOURCE CODE]: "
						+ c.getChangedEntity().getUniqueName());
				change.setSignificanceRank(LEVEL0);
				continue;
			}
			// elasticsearch-whitelist
			if (c.getChangedEntity().toString().contains("V_1_3")
					|| c.getChangedEntity().toString().contains("CURRENT")) {
				System.out.println("[SOURCE CODE]: "
						+ c.getChangedEntity().getUniqueName());
				change.setSignificanceRank(LEVEL0);
				continue;
			}
			// changes that can not be judged by any rules.
			change.setSignificanceRank(LEVEL1);
		}
	}
	
	public static void rerankChanges(Set<GitRefSourceCodeChange> newChangeSet)
	{
		for(GitRefSourceCodeChange change : newChangeSet)
		{
			//change happens in a method body
			if(change.getSourceCodeChange().getRootEntity().getType().isMethod())
			{
				//see if context has method call
				int rootStartLine = change.getSourceCodeChange().getRootEntity().getSourceCodeChanges().get(0).getChangedEntity().getStartPosition();
				int changeStartLine = change.getSourceCodeChange().getChangedEntity().getStartPosition();
				int changeEndLine = change.getSourceCodeChange().getChangedEntity().getEndPosition();
				int rootSize = change.getSourceCodeChange().getRootEntity().getSourceCodeChanges().size();
				int rootEndLine = change.getSourceCodeChange().getRootEntity().getSourceCodeChanges().get(rootSize-1).getChangedEntity().getEndPosition();
				
				//should use AST lib to find if context has interesting statements 
				
				//see if context has field access
			}
		}
	}
}
