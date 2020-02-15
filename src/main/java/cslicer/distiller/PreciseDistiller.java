package cslicer.distiller;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import cslicer.analyzer.ProjectConfiguration;

/**
 * Precise change distiller which uses statement level granularity.
 * 
 * @author Yi Li
 *
 */
public class PreciseDistiller extends Distiller {

	public PreciseDistiller(File left, File right, String jdkVersion)
			throws ChangeDistillerException {

		try {
			if (left != null && right != null) {
				FileDistiller distiller = ChangeDistiller
						.createFileDistiller(Language.JAVA);
				distiller.extractClassifiedSourceCodeChanges(left, right,
						jdkVersion);
				changes = distiller.getSourceCodeChanges();
			} else {
				throw new ChangeDistillerException(
						"Both versions of the file are set null!");
			}
		} catch (Exception e) {
			/*
			 * An exception most likely indicates a bug in ChangeDistiller.
			 * Please file a bug report at
			 * https://bitbucket.org/sealuzh/tools-changedistiller/issues and
			 * attach the full stack trace along with the two files that you
			 * tried to distill.
			 */
			throw new ChangeDistillerException(
					"Error occured while change distilling.", e);
		}
	}

	public Set<String> getUpdatedEntityNames() {
		Set<String> res = new HashSet<>();
		for (SourceCodeChange c : changes) {
			if (c.getRootEntity().getType().isMethod()) {
				if (c.getChangeType()
						.getSignificance() != SignificanceLevel.NONE) {
					res.add(c.getRootEntity().getUniqueName());
				}
			}
		}
		return res;
	}

	public Set<String> getPossibleUpdatedEntityNames() {
		Set<String> res = new HashSet<>();
		Map<String, List<SourceCodeChange>> tempMap = new HashMap<String, List<SourceCodeChange>>();
		for (SourceCodeChange c : changes) {
			if (c.getRootEntity().getType().isMethod()) {
				System.out.println(
						"[METHOD PARENT]: " + c.getChangedEntity().toString());
				if (tempMap.get(c.getRootEntity().getUniqueName()) == null) {
					List<SourceCodeChange> tempList = new ArrayList<SourceCodeChange>();
					tempList.add(c);
					tempMap.put(c.getRootEntity().getUniqueName(), tempList);
				} else {
					List<SourceCodeChange> tempList = (ArrayList<SourceCodeChange>) (tempMap
							.get(c.getRootEntity().getUniqueName()));
					tempList.add(c);
					tempMap.put(c.getRootEntity().getUniqueName(), tempList);
				}
			} else if (c.getChangedEntity().getType().isField()) {
				System.out.println("[FIELD ENTITY]: "
						+ c.getChangedEntity().getUniqueName());
				if (tempMap.get(c.getChangedEntity().getUniqueName()) == null) {
					List<SourceCodeChange> tempList = new ArrayList<SourceCodeChange>();
					tempList.add(c);
					tempMap.put(c.getChangedEntity().getUniqueName(), tempList);
				} else {
					List<SourceCodeChange> tempList = (ArrayList<SourceCodeChange>) (tempMap
							.get(c.getChangedEntity().getUniqueName()));
					tempList.add(c);
					tempMap.put(c.getChangedEntity().getUniqueName(), tempList);
				}
			} else {
				System.out
						.println("[OTHER]: " + c.getChangedEntity().toString());
			}
		}
		Iterator<Entry<String, List<SourceCodeChange>>> iter = tempMap
				.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, List<SourceCodeChange>> entry = iter.next();
			List<SourceCodeChange> list = (ArrayList<SourceCodeChange>) (entry
					.getValue());
			boolean isAllLow = true;
			for (SourceCodeChange change : list) {
				System.out.println(
						"[CHANGE]: " + change.getChangedEntity().toString());
				if (!isLowSignificance(change)) {
					isAllLow = false;
				}
			}
			if (isAllLow) {
				res.add((String) (entry.getKey()));
			}
		}
		return res;
	}

	public List<List<Object>> extractFeaturesFromChanges() {
		List<List<Object>> dataSet = new ArrayList<>();
		for (SourceCodeChange c : changes) {
			List<Object> l = new ArrayList<>();
			// entity name
			if (c.getRootEntity().getType().isMethod()) {
				l.add(c.getRootEntity().getUniqueName());
			} else {
				l.add(c.getChangedEntity().getUniqueName());
			}
			// source code detail
			l.add(c.getChangedEntity().toString());
			// protected -> public
			if (c.getChangeType() == ChangeType.INCREASING_ACCESSIBILITY_CHANGE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// public -> protected
			if (c.getChangeType() == ChangeType.DECREASING_ACCESSIBILITY_CHANGE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// remove "final"
			if (c.getChangeType() == ChangeType.ADDING_ATTRIBUTE_MODIFIABILITY
					|| c.getChangeType() == ChangeType.ADDING_CLASS_DERIVABILITY
					|| c.getChangeType() == ChangeType.ADDING_METHOD_OVERRIDABILITY) {
				l.add(1);
			} else {
				l.add(0);
			}
			// add "final"
			if (c.getChangeType() == ChangeType.REMOVING_ATTRIBUTE_MODIFIABILITY
					|| c.getChangeType() == ChangeType.REMOVING_CLASS_DERIVABILITY
					|| c.getChangeType() == ChangeType.REMOVING_METHOD_OVERRIDABILITY) {
				l.add(1);
			} else {
				l.add(0);
			}
			// insert a new class
			if (c.getChangeType() == ChangeType.ADDITIONAL_CLASS) {
				l.add(1);
			} else {
				l.add(0);
			}
			// delete a class
			if (c.getChangeType() == ChangeType.REMOVED_CLASS) {
				l.add(1);
			} else {
				l.add(0);
			}
			// insert a new method
			if (c.getChangeType() == ChangeType.ADDITIONAL_FUNCTIONALITY) {
				l.add(1);
			} else {
				l.add(0);
			}
			// delete a method
			if (c.getChangeType() == ChangeType.REMOVED_FUNCTIONALITY) {
				l.add(1);
			} else {
				l.add(0);
			}
			// insert a new field
			if (c.getChangeType() == ChangeType.ADDITIONAL_OBJECT_STATE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// delete a field
			if (c.getChangeType() == ChangeType.REMOVED_OBJECT_STATE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// insert else-part
			if (c.getChangeType() == ChangeType.ALTERNATIVE_PART_INSERT) {
				l.add(1);
			} else {
				l.add(0);
			}
			// delete else-part
			if (c.getChangeType() == ChangeType.ALTERNATIVE_PART_DELETE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// class renaming
			if (c.getChangeType() == ChangeType.CLASS_RENAMING) {
				l.add(1);
			} else {
				l.add(0);
			}
			// method renaming
			if (c.getChangeType() == ChangeType.METHOD_RENAMING) {
				l.add(1);
			} else {
				l.add(0);
			}
			// field renaming
			if (c.getChangeType() == ChangeType.ATTRIBUTE_RENAMING) {
				l.add(1);
			} else {
				l.add(0);
			}
			// field type change
			if (c.getChangeType() == ChangeType.ATTRIBUTE_TYPE_CHANGE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// conditional expression change
			if (c.getChangeType() == ChangeType.CONDITION_EXPRESSION_CHANGE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parameter insert
			if (c.getChangeType() == ChangeType.PARAMETER_INSERT) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parameter delete
			if (c.getChangeType() == ChangeType.PARAMETER_DELETE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parameter ordering change
			if (c.getChangeType() == ChangeType.PARAMETER_ORDERING_CHANGE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parameter type change
			if (c.getChangeType() == ChangeType.PARAMETER_TYPE_CHANGE) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parameter renaming
			if (c.getChangeType() == ChangeType.PARAMETER_RENAMING) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parent class change/insert/delete
			if (c.getChangeType() == ChangeType.PARENT_CLASS_CHANGE
					|| c.getChangeType() == ChangeType.PARENT_CLASS_DELETE
					|| c.getChangeType() == ChangeType.PARENT_CLASS_INSERT) {
				l.add(1);
			} else {
				l.add(0);
			}
			// parent interface change/insert/delete
			if (c.getChangeType() == ChangeType.PARENT_INTERFACE_CHANGE
					|| c.getChangeType() == ChangeType.PARENT_INTERFACE_DELETE
					|| c.getChangeType() == ChangeType.PARENT_INTERFACE_INSERT) {
				l.add(1);
			} else {
				l.add(0);
			}
			// return type change/insert/delete
			if (c.getChangeType() == ChangeType.RETURN_TYPE_CHANGE
					|| c.getChangeType() == ChangeType.RETURN_TYPE_DELETE
					|| c.getChangeType() == ChangeType.RETURN_TYPE_INSERT) {
				l.add(1);
			} else {
				l.add(0);
			}
			// print/log method call
			if (c.getChangeType() == ChangeType.STATEMENT_DELETE
					|| c.getChangeType() == ChangeType.STATEMENT_INSERT
					|| c.getChangeType() == ChangeType.STATEMENT_UPDATE) {
				if (c.getChangedEntity().getUniqueName().contains("print")
						|| c.getChangedEntity().getUniqueName().contains("LOG")
						|| c.getChangedEntity().getUniqueName().contains("Log")
						|| c.getChangedEntity().getUniqueName()
								.contains("Print")) {
					l.add(1);
				} else {
					l.add(0);
				}
			}
			// catch clause
			if (c.getChangedEntity().getType().toString()
					.equals("CATCH_CLAUSE")) {
				l.add(1);
			} else {
				l.add(0);
			}
			dataSet.add(l);
		}
		return dataSet;
	}

	public boolean isLowSignificance(SourceCodeChange c) {
		// modifier
		if (c.getChangeType() == ChangeType.INCREASING_ACCESSIBILITY_CHANGE || c
				.getChangeType() == ChangeType.DECREASING_ACCESSIBILITY_CHANGE) {
			System.out.println(
					"[SOURCE CODE]: " + c.getChangedEntity().getUniqueName());
			System.out
					.println("[CHANGE TYPE]: " + c.getChangeType().toString());
			return true;
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
					|| c.getChangedEntity().getUniqueName().contains("Print")) {
				System.out.println("[SOURCE CODE]: "
						+ c.getChangedEntity().getUniqueName());
				return true;
			}
		}
		// catch clause
		if (c.getChangedEntity().getType().toString().equals("CATCH_CLAUSE")) {
			System.out.println(
					"[SOURCE CODE]: " + c.getChangedEntity().getUniqueName());
			return true;
		}
		// changedistiller none/low
		if (c.getChangeType().getSignificance() == SignificanceLevel.LOW || c
				.getChangeType().getSignificance() == SignificanceLevel.NONE) {
			System.out.println(
					"[SOURCE CODE]: " + c.getChangedEntity().getUniqueName());
			return true;
		}
		// elasticsearch-version
		if (c.getChangedEntity().toString().contains("V_1_3")
				|| c.getChangedEntity().toString().contains("CURRENT")) {
			System.out.println(
					"[SOURCE CODE]: " + c.getChangedEntity().getUniqueName());
			return true;
		}
		return false;
	}

	public PreciseDistiller(File root1, File root2)
			throws ChangeDistillerException {
		this(root1, root2, ProjectConfiguration.DEFAULT_JDK);
	}
}
