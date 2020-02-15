package cslicer.distiller;

import static org.junit.Assert.assertEquals;

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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;

public class FileDistillTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		File leftFile = FileUtils.getFile(tempFolder.getRoot(), "Boo-1.java");
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/refactor/left"),
				leftFile);

		File rightFile = FileUtils.getFile(tempFolder.getRoot(), "Boo-2.java");
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/refactor/right"),
				rightFile);
		String fJDKVersion = "1.7";

		Distiller distiller = new GitRefDistiller(leftFile, rightFile,
				fJDKVersion);
		Distiller pd = new PreciseDistiller(leftFile, rightFile, fJDKVersion);

		// distiller.printChanges();
		pd.printChanges();
		assertEquals(pd.getAtomicChanges().size(),4);
		assertEquals(distiller.getAtomicChanges().size(), 2);
	}

}
