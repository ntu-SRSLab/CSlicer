package cslicer.builder;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Used to specify which unit tests to run. Now three modes are supported: 1.
 * Run a collection of test classes 2. Run all test classes 3. Run a specific
 * test method in a test class
 * 
 * (subject to future extension)
 * 
 * @author liyi
 *
 */
public class UnitTestScope {
	private Map<String, Collection<String>> includedTests;
	private Map<String, Collection<String>> excludedTests;
	private String fTestDesc;

	public UnitTestScope() {
		includedTests = new Hashtable<String, Collection<String>>();
		excludedTests = new Hashtable<String, Collection<String>>();
		fTestDesc = null;
	}

	public UnitTestScope(final String testDesc) {
		if (testDesc != null) {
			fTestDesc = testDesc.trim();
			includedTests = parseTestDesc(testDesc);
			excludedTests = null;
		}
	}

	private Map<String, Collection<String>> parseTestDesc(String testDesc) {
		Map<String, Collection<String>> tests = new HashMap<String, Collection<String>>();

		for (String testClass : testDesc.split(",")) {
			if (testClass.indexOf('#') == -1) {
				tests.put(testClass, new HashSet<String>());
				continue;
			}

			String className = testClass.substring(0, testClass.indexOf("#"));
			String methods = testClass.substring(testClass.indexOf("#") + 1);
			tests.put(className, new HashSet<String>());
			if (methods.contains("+")) {
				for (String methodName : methods.split("\\+")) {
					tests.get(className).add(methodName);
				}
			} else
				tests.get(className).add(methods);
		}
		return tests;
	}

	public UnitTestScope includeTest(final String className) {
		if (includedTests == null)
			return this;

		includedTests.put(className, new ArrayList<String>());
		return this;
	}

	public UnitTestScope includeTest(final String className,
			final String methodName) {
		if (includedTests == null)
			return this;

		if (includedTests.containsKey(className))
			includedTests.get(className).add(methodName);
		else
			includedTests.put(className, Arrays.asList(methodName));

		return this;
	}

	public UnitTestScope excludeTest(final String className) {
		if (excludedTests == null)
			return this;

		excludedTests.put(className, new ArrayList<String>());
		return this;
	}

	public UnitTestScope excludeTest(final String className,
			final String methodName) {
		if (excludedTests == null)
			return this;

		if (excludedTests.containsKey(className))
			excludedTests.get(className).add(methodName);
		else
			excludedTests.put(className, Arrays.asList(methodName));

		return this;
	}

	public String getMavenTestArguments(boolean isInclude) {

		if (fTestDesc != null)
			return "-Dtest=" + fTestDesc;

		ArrayList<String> testClasses = new ArrayList<String>();
		Map<String, Collection<String>> tests = isInclude ? includedTests
				: excludedTests;

		for (Entry<String, Collection<String>> test : tests.entrySet()) {
			if (test.getValue().isEmpty()) {
				testClasses.add(test.getKey());
				continue;
			}

			testClasses.add(test.getKey() + "#"
					+ StringUtils.join(test.getValue(), "+"));
		}

		return "-Dtest=" + StringUtils.join(testClasses, ",");
	}

	public boolean includeAllTest() {
		return includedTests != null && includedTests.isEmpty();
	}

	public boolean hasExcludedTest() {
		return excludedTests != null && !excludedTests.isEmpty();
	}

	@Override
	public String toString() {
		return fTestDesc;
	}

	public Set<String> getTestMethodFullyQualifiedNames() {
		Set<String> res = new HashSet<String>();

		for (String className : includedTests.keySet()) {
			for (String methodName : includedTests.get(className)) {
				res.add(className + "." + methodName);
			}
		}
		return res;
	}
}
