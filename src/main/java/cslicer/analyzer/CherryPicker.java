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

import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.callgraph.BcelStaticCallGraphBuilder;
import cslicer.callgraph.CGEdgeType;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.callgraph.StaticCallGraph;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;
import cslicer.utils.graph.Edge;
import cslicer.utils.graph.Vertex;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CherryPicker extends HistoryAnalyzer {

  public CherryPicker(ProjectConfiguration config) throws RepositoryInvalidException, CommitNotFoundException,
          BuildScriptInvalidException, CoverageControlIOException,
          AmbiguousEndPointException, ProjectConfigInvalidException,
          BranchNotFoundException, CoverageDataMissingException, IOException {
    super(config);
    fClassRootPath = config.getClassRootPath();
  }

  public void doSlicing(Set<String> toPick) {
    List<RevCommit> hunks = computeHunkDepSetWithId(toPick);

    PrintUtils.print(toPick);
    PrintUtils.print("Hunk dependencies:", PrintUtils.TAG.OUTPUT);
    for (RevCommit h : hunks) {
      PrintUtils.print("hunk: " + commitSummary(h), PrintUtils.TAG.OUTPUT);
    }
  }

}
