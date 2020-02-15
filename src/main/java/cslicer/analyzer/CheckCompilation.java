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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.maven.MavenInvokerDefault;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutBranchFailedException;
import cslicer.jgit.CheckoutFileFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;

public class CheckCompilation extends HistoryAnalyzer {

	public CheckCompilation(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException, IOException {
		super(config);
		
		fCompiler = new MavenInvokerDefault(config.getBuildScriptPath(), config.isBuilderOutputEnabled());
	}
	
	public List<String> check() throws CheckoutBranchFailedException, CheckoutFileFailedException
	{
		List<String> list = new ArrayList<>();
		fJGit.checkOutNewBranch("REFINE", fStart);
		fCompiler.checkCompilation();
		for(RevCommit c : fHistory)
		{
			fJGit.checkOutVersion(c);
			if (fCompiler.checkCompilation())
			{
				list.add("SUCC:" + c.getName() + c.getFullMessage());
			}
			else
			{
				list.add("FAIL:" + c.getName() + c.getFullMessage());
			}
		}
		return list;
	}

}
