package cslicer.coverage;

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

import cslicer.builder.UnitTestScope;

public interface ICoverageAnalyzer {

	/**
	 * Analyze code coverage for the test suite.
	 * 
	 * @return a {@link FullCoverageExtractor} with coverage data
	 * @throws CoverageControlIOException
	 *             if I/O exception happens during coverage analysis
	 * @throws TestFailureException
	 *             if test failure occurs
	 */
	CoverageDatabase analyseCoverage()
			throws CoverageControlIOException, TestFailureException;

	/**
	 * Analyze code coverage for a given test method.
	 * 
	 * @param scope
	 *            {@link UnitTestScope} specifying the tests to run for coverage
	 *            analysis
	 * @return a {@link FullCoverageExtractor} with coverage data
	 * @throws CoverageControlIOException
	 *             if I/O exception happens during coverage analysis
	 * @throws TestFailureException
	 *             if test failure occurs
	 */
	CoverageDatabase analyseCoverage(UnitTestScope scope)
			throws CoverageControlIOException, TestFailureException;

	/**
	 * Type of the coverage analysis.
	 * 
	 * @return {@link COVERAGE_TYPE}
	 */
	COVERAGE_TYPE analysisType();
}
