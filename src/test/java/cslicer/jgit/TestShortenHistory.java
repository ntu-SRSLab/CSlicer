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

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cslicer.analyzer.ProjectConfiguration;
import cslicer.analyzer.Slicer;
import cslicer.analyzer.SlicingResult;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;

public class TestShortenHistory {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration(
				Paths.get("/home/liyi/Downloads/jacoco.properties"));
		config.setSkipPicking(true);

		Slicer slicer = new Slicer(config);
		// slicer.doSlicing();
		SlicingResult result = slicer.loadSlicingResult();
		// SlicingResult result = slicer.getSlicingReseult();
		PrintUtils.print("shortening");
		// slicer.showASTDiff("a14dd02");
		// slicer.showASTDiff("1d148be8");
		Set<List<RevCommit>> shorts = slicer.shortenSlice(result);

		for (List<RevCommit> p : shorts)
			PrintUtils.print(p);
		// slicer.verifyResultPicking(result.getPicks());

		StatsUtils.print();
	}

}
