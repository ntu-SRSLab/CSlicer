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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class Learner extends HistoryAnalyzer {

	// test touching set
	private TouchSet fTestTouchSet;

	private LinkedList<RevCommit> A;
	private LinkedList<RevCommit> D;

	public Learner(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException, IOException {
		super(config);
		// TODO Auto-generated constructor stub
		A = new LinkedList<RevCommit>(fHistory);
		D = new LinkedList<RevCommit>();

		fTestTouchSet = new TouchSet();
	}

	// Chenguang
	public Set<GitRefSourceCodeChange> extractChangesForDaikon() {
		for (String s : fTestTouchSet.getTestNameSet()) {
			System.out.println("[TOUCHSET]: " + s);
		}
		// Collections.reverse(A);
		ChangeExtractor extractor = new ChangeExtractor(fJGit,
				fConfig.getProjectJDKVersion());

		Set<GitRefSourceCodeChange> changeSet = new LinkedHashSet<>();

		// Initially rank changes.
		for (RevCommit c : fHistory) {
			Set<GitRefSourceCodeChange> changes;
			try {
				changes = extractor.extractChangesPrecise(c);

				for (GitRefSourceCodeChange change : changes) // *actually
				// 'changes'
				// should have
				// only one
				// change
				{
					changeSet.add(change);
					change.setRelatedCommit(c);
				}
			} catch (CommitNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ChangeDistillerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return changeSet;
	}

	// Chenguang
	public void extractDataSet()
			throws CommitNotFoundException, BuildScriptInvalidException,
			CoverageDataMissingException, CoverageControlIOException {
		for (String s : fTestTouchSet.getTestNameSet()) {
			System.out.println("[TOUCHSET]: " + s);
		}
		Collections.reverse(A);

		ChangeExtractor extractor = new ChangeExtractor(fJGit,
				fConfig.getProjectJDKVersion());
		List<List<Object>> dataSet = new ArrayList<>();

		for (RevCommit c : A) {
			Set<GitRefSourceCodeChange> changes;
			try {
				changes = extractor.extractChangesPrecise(c);

				for (GitRefSourceCodeChange change : changes) {
					if (change.getSourceCodeChange()
							.getChangeType() == ChangeType.STATEMENT_INSERT
							|| change.getSourceCodeChange()
									.getChangeType() == ChangeType.STATEMENT_DELETE
							|| change.getSourceCodeChange()
									.getChangeType() == ChangeType.STATEMENT_UPDATE
							|| change.getSourceCodeChange()
									.getChangeType() == ChangeType.STATEMENT_ORDERING_CHANGE
							|| change.getSourceCodeChange()
									.getChangeType() == ChangeType.STATEMENT_PARENT_CHANGE) {
						if (!change.getSourceCodeChange().getChangedEntity()
								.getUniqueName().contains("print")
								&& !change.getSourceCodeChange()
										.getChangedEntity().getUniqueName()
										.contains("LOG")
								&& !change.getSourceCodeChange()
										.getChangedEntity().getUniqueName()
										.contains("Log")
								&& !change.getSourceCodeChange()
										.getChangedEntity().getUniqueName()
										.contains("Print")) {
							continue;
						}
					}
					if (change.getSourceCodeChange().getChangedEntity()
							.getType().isClass()
							&& fTestTouchSet
									.hitTestSet(change.getSourceCodeChange()
											.getChangedEntity().getUniqueName())
							|| change.getSourceCodeChange().getRootEntity()
									.getType().isClass()
									&& fTestTouchSet.hitTestSet(change
											.getSourceCodeChange()
											.getChangedEntity().getUniqueName())
							|| change.getSourceCodeChange().getRootEntity()
									.getType().isMethod()
									&& fTestTouchSet.hitTestSet(change
											.getSourceCodeChange()
											.getRootEntity().getUniqueName())) {
						List<Object> l = new ArrayList<>();
						// commit #
						l.add(c.getId());
						// entity name
						if (change.getSourceCodeChange().getRootEntity()
								.getType().isMethod()) {
							l.add(change.getSourceCodeChange().getRootEntity()
									.getUniqueName());
						} else {
							l.add(change.getSourceCodeChange()
									.getChangedEntity().getUniqueName());
						}
						// source code detail
						l.add(change.getSourceCodeChange().getChangedEntity()
								.toString());
						System.out.println(
								"[CHANGETYPE]: " + change.getSourceCodeChange()
										.getChangeType().toString());
						// protected -> public
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.INCREASING_ACCESSIBILITY_CHANGE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// public -> protected
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.DECREASING_ACCESSIBILITY_CHANGE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// remove "final"
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ADDING_ATTRIBUTE_MODIFIABILITY
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.ADDING_CLASS_DERIVABILITY
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.ADDING_METHOD_OVERRIDABILITY) {
							l.add(1);
						} else {
							l.add(0);
						}
						// add "final"
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.REMOVING_ATTRIBUTE_MODIFIABILITY
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.REMOVING_CLASS_DERIVABILITY
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.REMOVING_METHOD_OVERRIDABILITY) {
							l.add(1);
						} else {
							l.add(0);
						}
						// insert a new class
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ADDITIONAL_CLASS) {
							l.add(1);
						} else {
							l.add(0);
						}
						// delete a class
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.REMOVED_CLASS) {
							l.add(1);
						} else {
							l.add(0);
						}
						// insert a new method
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ADDITIONAL_FUNCTIONALITY) {
							l.add(1);
						} else {
							l.add(0);
						}
						// delete a method
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.REMOVED_FUNCTIONALITY) {
							l.add(1);
						} else {
							l.add(0);
						}
						// insert a new field
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ADDITIONAL_OBJECT_STATE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// delete a field
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.REMOVED_OBJECT_STATE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// insert else-part
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ALTERNATIVE_PART_INSERT) {
							l.add(1);
						} else {
							l.add(0);
						}
						// delete else-part
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ALTERNATIVE_PART_DELETE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// class renaming
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.CLASS_RENAMING) {
							l.add(1);
						} else {
							l.add(0);
						}
						// method renaming
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.METHOD_RENAMING) {
							l.add(1);
						} else {
							l.add(0);
						}
						// field renaming
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ATTRIBUTE_RENAMING) {
							l.add(1);
						} else {
							l.add(0);
						}
						// field type change
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.ATTRIBUTE_TYPE_CHANGE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// conditional expression change
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.CONDITION_EXPRESSION_CHANGE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parameter insert
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARAMETER_INSERT) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parameter delete
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARAMETER_DELETE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parameter ordering change
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARAMETER_ORDERING_CHANGE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parameter type change
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARAMETER_TYPE_CHANGE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parameter renaming
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARAMETER_RENAMING) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parent class change/insert/delete
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARENT_CLASS_CHANGE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.PARENT_CLASS_DELETE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.PARENT_CLASS_INSERT) {
							l.add(1);
						} else {
							l.add(0);
						}
						// parent interface change/insert/delete
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.PARENT_INTERFACE_CHANGE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.PARENT_INTERFACE_DELETE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.PARENT_INTERFACE_INSERT) {
							l.add(1);
						} else {
							l.add(0);
						}
						// return type change/insert/delete
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.RETURN_TYPE_CHANGE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.RETURN_TYPE_DELETE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.RETURN_TYPE_INSERT) {
							l.add(1);
						} else {
							l.add(0);
						}
						// print/log method call
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.STATEMENT_DELETE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.STATEMENT_INSERT
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.STATEMENT_UPDATE) {
							if (change.getSourceCodeChange().getChangedEntity()
									.getUniqueName().contains("print")
									|| change.getSourceCodeChange()
											.getChangedEntity().getUniqueName()
											.contains("LOG")
									|| change.getSourceCodeChange()
											.getChangedEntity().getUniqueName()
											.contains("Log")
									|| change.getSourceCodeChange()
											.getChangedEntity().getUniqueName()
											.contains("Print")) {
								l.add(1);
							} else {
								l.add(0);
							}
						} else {
							l.add(0);
						}
						// catch clause
						if (change.getSourceCodeChange().getChangedEntity()
								.getType().toString().equals("CATCH_CLAUSE")) {
							l.add(1);
						} else {
							l.add(0);
						}
						// statement insert/update/delete/ordering change/parent
						// change
						/*
						 * if(change.getSourceCodeChange().getChangeType() ==
						 * ChangeType.STATEMENT_INSERT ||
						 * change.getSourceCodeChange().getChangeType() ==
						 * ChangeType.STATEMENT_DELETE ||
						 * change.getSourceCodeChange().getChangeType() ==
						 * ChangeType.STATEMENT_UPDATE ||
						 * change.getSourceCodeChange().getChangeType() ==
						 * ChangeType.STATEMENT_ORDERING_CHANGE ||
						 * change.getSourceCodeChange().getChangeType() ==
						 * ChangeType.STATEMENT_PARENT_CHANGE) { l.add(1); }
						 * else { l.add(0); }
						 */
						// doc insert/update/delete
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.DOC_INSERT
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.DOC_DELETE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.DOC_UPDATE) {
							l.add(1);
						} else {
							l.add(0);
						}
						// comment insert/update/delete/move
						if (change.getSourceCodeChange()
								.getChangeType() == ChangeType.COMMENT_INSERT
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.COMMENT_UPDATE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.COMMENT_DELETE
								|| change.getSourceCodeChange()
										.getChangeType() == ChangeType.COMMENT_MOVE) {
							l.add(1);
						} else {
							l.add(0);
						}

						dataSet.add(l);
					}
				}
			} catch (ChangeDistillerException e) {
				PrintUtils.print(
						"Exception occurs in change distilling! Result will be unreliable!",
						TAG.WARNING);
				D.add(c); // drop if exception in distilling
				e.printStackTrace();
			}
		}
		File file = new File("/home/polaris/Desktop/dataSet.dat");
		try {
			FileWriter fw = new FileWriter(file);
			for (List<Object> l : dataSet) {
				fw.write("[");
				int i = 0;
				for (Object o : l) {
					fw.write(o.toString());
					if (i < l.size() - 1) {
						fw.write(", ");
					}
					i++;
				}
				fw.write("]\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
