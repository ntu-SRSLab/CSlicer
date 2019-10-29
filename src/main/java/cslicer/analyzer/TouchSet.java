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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;

/**
 * A set containing AST entities touched by tests. It also supports various
 * filters which ignore irrelevant entities.
 * 
 * @author liyi
 *
 */
public class TouchSet {

	// set of entities directly executed by tests
	private Hashtable<String, SourceCodeEntity> testSet;
	private HashSet<String> testNameSet;
	// compilation dependencies of the test set
	private HashSet<String> compSet;
	// excluded test set elements
	private HashSet<String> testExclude;

	protected TouchSet() {
		testSet = new Hashtable<String, SourceCodeEntity>();
		testNameSet = new HashSet<String>();
		compSet = new HashSet<String>();
		testExclude = new HashSet<String>();

		// hardcode excludes
		// testExclude.add("gitref.utils.PrintUtils");
	}

	/**
	 * Add a new element to the COMP set.
	 * 
	 * @param key
	 *            identifier
	 */
	public void addToCompSet(String key) {
		if (BytecodeUtils.matchExclude(key))
			return;

		if (compSet.add(key))
			StatsUtils.count("comp.deps");
	}

	/**
	 * Add a new element to the TEST set.
	 * 
	 * @param key
	 *            identifier
	 * @param entity
	 *            source code entity
	 */
	public void addToTestSet(String key, SourceCodeEntity entity) {
		if (BytecodeUtils.matchExclude(key))
			return;

		StatsUtils.count("test.deps");
		String filterKey = BytecodeUtils.filterGenericType(key);
		// check excludes
		// for (String e : testExclude) {
		// if (key.contains(e))
		// return;
		// }

		testSet.put(filterKey, entity);
		testNameSet.add(filterKey);
	}

	/**
	 * Retrieve a TEST set element using its key.
	 * 
	 * @param key
	 *            given key of the target entity
	 * @return {@link SourceCodeEntity}
	 */
	public SourceCodeEntity getTestSetEntity(String key) {
		return testSet.get(BytecodeUtils.filterGenericType(key));
	}

	/**
	 * Return names of all entities touched.
	 * 
	 * @return a set of identifiers
	 */
	public Set<String> getTouchSet() {
		HashSet<String> res = new HashSet<String>();
		res.addAll(testNameSet);
		res.addAll(compSet);
		return Collections.unmodifiableSet(res);
	}

	/**
	 * Return file paths of all touched entities.
	 * 
	 * @return a set of file paths
	 */
	public Set<String> getTouchSetFilePaths() {
		HashSet<String> res = new HashSet<String>();

		return Collections.unmodifiableSet(res);
	}

	/**
	 * Check whether a key hits the COMP set.
	 * 
	 * @param key
	 *            identifier
	 * @return {@code true} if key is in COMP set
	 */
	public boolean hitCompSet(String key) {
		return containsIdentifier(compSet, key);
	}

	/**
	 * Is the name key hit TEST set.
	 * 
	 * @param key
	 *            identifier
	 * @return {@code true} if key is in TEST set
	 */
	public boolean hitTestSet(String key) {
		return containsIdentifier(testNameSet, key);
	}

	private boolean containsIdentifier(Collection<String> set, String id) {
		for (String s : set) {
			if (BytecodeUtils.matchWithGenericType(s, id))
				return true;
		}
		return false;
	}

	/**
	 * Load TEST and COMP set data from file.
	 * 
	 * @param path
	 *            file path
	 * @return {@code true} if successful
	 */
	public boolean loadFromFile(String path) {
		try {
			String content = FileUtils
					.readFileToString(FileUtils.getFile(path));

			if (content.isEmpty())
				return false;

			for (String line : content.split(IOUtils.LINE_SEPARATOR)) {
				if (line.startsWith("Test:"))
					testNameSet.add(line.substring(6, line.length()).trim());
				else if (line.startsWith("Comp:"))
					compSet.add(line.substring(6, line.length()).trim());
			}
		} catch (IOException e) {
			PrintUtils.print("Read touch sets failed!");
			return false;
		}

		return true;
	}

	/**
	 * Save TEST and COMP set data to file.
	 * 
	 * @param path
	 *            file path
	 */
	public void saveToFile(String path) {
		try {
			FileUtils.writeStringToFile(FileUtils.getFile(path), toString());
			PrintUtils.print("Touch sets saved at " + path);
		} catch (IOException e) {
			PrintUtils.print("Save touch sets failed!");
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (String k : testSet.keySet()) {
			res.append("Test: " + k);
			res.append("\n");
		}
		for (String c : compSet) {
			res.append("Comp: " + c);
			res.append("\n");
		}

		return res.toString();
	}

	public HashSet<String> getTestNameSet() {
		return this.testNameSet;
	}
}
