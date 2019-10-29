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
 * Copyright (C) 2014 - 2015 Department of Computer Science, University of Toronto
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

import com.google.inject.Guice;
import com.google.inject.Injector;

import cslicer.analyzer.ProjectConfiguration;

/**
 * A wrapper class of {@link GitRefFileDistiller}.
 * 
 * @author Yi Li
 */
public class GitRefDistiller extends Distiller {

	public GitRefDistiller(File left, File right, String jdkVersion)
			throws ChangeDistillerException {

		try {
			if (left != null || right != null) {
				Injector injector = Guice
						.createInjector(new GitRefChangeDistillerModule());
				GitRefFileDistiller distiller = injector
						.getInstance(GitRefFileDistiller.class);

				distiller.extractClassifiedSourceCodeChanges(left, jdkVersion,
						right, jdkVersion);
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

	public GitRefDistiller(File root1, File root2)
			throws ChangeDistillerException {
		this(root1, root2, ProjectConfiguration.DEFAULT_JDK);
	}
}
