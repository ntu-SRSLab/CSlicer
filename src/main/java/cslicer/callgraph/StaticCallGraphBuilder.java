package cslicer.callgraph;

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

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

public abstract class StaticCallGraphBuilder {

	protected List<File> fClassPath;

	public StaticCallGraphBuilder(List<String> classPath,
			@Nullable Set<String> rootClasses)
			throws ClassPathInvalidException {

		fClassPath = new LinkedList<File>();

		for (String path : classPath) {
			File dir = FileUtils.getFile(path);
			if (!dir.exists() || !dir.isDirectory())
				throw new ClassPathInvalidException(path);

			fClassPath.add(dir);
		}
	}

	public abstract void buildCallGraph();

	public StaticCallGraphBuilder(String classPath,
			@Nullable Set<String> rootClasses)
			throws ClassPathInvalidException {
		this(Arrays.asList(classPath), rootClasses);
	}

	public StaticCallGraphBuilder() {
		fClassPath = null;
	}

	public abstract StaticCallGraph getCallGraph();

	public abstract void saveCallGraph(String path);

	public abstract void loadCallGraph(String path);
}
