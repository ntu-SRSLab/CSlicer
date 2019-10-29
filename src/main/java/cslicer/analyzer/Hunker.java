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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hunker extends HistoryAnalyzer {

    public Hunker(ProjectConfiguration config) throws RepositoryInvalidException, CommitNotFoundException,
            BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException,
            BranchNotFoundException, CoverageDataMissingException, IOException {
        super(config);
        fClassRootPath = config.getClassRootPath();
    }

    public String generateHunkDependencyFacts() {
        DependencyCache hunkGraph = new DependencyCache();
        computeHunkDepSet(fHistory, hunkGraph);
        return hunkGraph.toString();
    }

    private String findFactOperatorOfDepsEdge(Edge edge) {
        if (edge.getLabel() == CGEdgeType.FIELD_READ
                || edge.getLabel() == CGEdgeType.FIELD_WRITE || edge.getLabel() == CGEdgeType.STATIC_READ
                || edge.getLabel() == CGEdgeType.STATIC_WRITE || edge.getLabel() == CGEdgeType.CLASS_REFERENCE
                || edge.getLabel() == CGEdgeType.FIELD_REFERENCE) {
            return "reference";
        } else if (edge.getLabel() == CGEdgeType.INVOKE_INTERFACE || edge.getLabel() == CGEdgeType.INVOKE_SPECIAL
                || edge.getLabel() == CGEdgeType.INVOKE_STATIC || edge.getLabel() == CGEdgeType.INVOKE_VIRTUAL) {
            return "call";
        } else if (edge.getLabel() == CGEdgeType.CLASS_FIELD || edge.getLabel() == CGEdgeType.CLASS_METHOD) {
            return "contain";
        } else { // default
            return "reference";
        }
    }

    public String generateDependencyFacts() throws ClassPathInvalidException {
        String factsFileContent = "";
        // file header
        factsFileContent += "FACT TUPLE :\n";
        BcelStaticCallGraphBuilder depsCGBuilder = new BcelStaticCallGraphBuilder(
                fClassRootPath);
        depsCGBuilder.buildCallGraph();
        StaticCallGraph depsCG = depsCGBuilder.getCallGraph();
        //depsCG.outputDOTFile("/tmp/deps-graph.txt");
        for (Edge e : depsCG.getEdges()) {
            Vertex fromVertex = e.getFrom();
            Vertex toVertex = e.getTo();
            String operator = findFactOperatorOfDepsEdge(e);
            String fact = String.format("%s %s %s", operator, calcMD5(fromVertex.getName()), calcMD5(toVertex.getName()));
            factsFileContent += fact + "\n";
        }
        String depFactsFile = Paths.get(fOutputPath, "deps.ta").toString();
        try {
            PrintUtils.print(String.format("Start writing to file @ %s", depFactsFile), PrintUtils.TAG.DEBUG);
            FileWriter fWriter = new FileWriter(depFactsFile, false);
            fWriter.write(factsFileContent);
            fWriter.flush();
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return factsFileContent;
    }

    public boolean generateDifferentialFacts() throws CommitNotFoundException {
        return preProcessHistory();
    }

    public String generateCoverageFacts() {
        /// TODO
        return "";
    }

    private String calcMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(md5digest);
        } catch (NoSuchAlgorithmException e) {
            PrintUtils.print("MD5 is not supported now.", PrintUtils.TAG.WARNING);
            e.printStackTrace();
            return "";
        }
    }

    private boolean preProcessHistory() throws CommitNotFoundException {
        String diffFactsFile = Paths.get(fOutputPath.toString(), "diff.ta").toString();
        try {
            FileWriter fWriter = new FileWriter(diffFactsFile, false);
            // factsFileContent.append("FACT TUPLE :\n");
            fWriter.write("FACT TUPLE :\n");
            // Set<String> fChangedClasses = new HashSet<>();
            List<RevCommit> A = new LinkedList<>(fHistory);
            VersionTracker fTracker = new VersionTracker(A, fComparator);

            ChangeExtractor extractor = new ChangeExtractor(fJGit,
                    fConfig.getProjectJDKVersion());

            StatsUtils.resume("history.preprocess");

            int i = 0; // commit sequence number
            for (RevCommit c : fHistory) {
                Set<GitRefSourceCodeChange> changes;
                try {
                    changes = extractor.extractChangesMerge(c);
                } catch (ChangeDistillerException e) {
                    PrintUtils.print(
                            "Exception occurs in change distilling! Result will be unreliable!",
                            PrintUtils.TAG.WARNING);
                    e.printStackTrace();
                    continue;
                }
                StringBuilder factsPerCommit = new StringBuilder();

                for (GitRefSourceCodeChange gitChange : changes) {
                    // get change distiller change
                    SourceCodeChange change = gitChange.getSourceCodeChange();
                    // get file path to changed entity
                    String filePath = gitChange.getChangedFilePath();
                    // unique identifier of changed entity
                    String uniqueName = null;
                    // dependency type (reason for keeping)
                    SlicingResult.DEP_FLAG depType = SlicingResult.DEP_FLAG.DROP;
                    // change operation type
                    AtomicChange.CHG_TYPE chgType = null;

                    // fChangedClasses.add(gitChange.getEnclosingClassName());

                    String fact = "";
                    // parent entity is field/method/class which contains the
                    // change
                    if (change instanceof Delete) {
                        Delete del = (Delete) change;
                        uniqueName = del.getChangedEntity().getUniqueName();
                        String parentUniqueName = del.getParentEntity().getUniqueName();
                        chgType = AtomicChange.CHG_TYPE.DEL;
                        fact = String.format("Delete %s %s\n", calcMD5(uniqueName),calcMD5(parentUniqueName));
                    } else if (change instanceof Insert) {
                        Insert ins = (Insert) change;
                        uniqueName = ins.getChangedEntity().getUniqueName();
                        String parentUniqueName = ins.getParentEntity().getUniqueName();
                        chgType = AtomicChange.CHG_TYPE.INS;
                        fact = String.format("Insert %s %s\n", calcMD5(uniqueName), calcMD5(parentUniqueName));
                    } else if (change instanceof Update) {
                        Update upd = (Update) change;
                        String oldUniqueName = upd.getChangedEntity().getUniqueName();
                        uniqueName = upd.getNewEntity().getUniqueName();
                        // is signature updated?
                        boolean signatureChange = !upd.getChangedEntity()
                                .getUniqueName().equals(uniqueName);
                        assert !signatureChange;
                        chgType = AtomicChange.CHG_TYPE.UPD;
                        fact = String.format("Update %s %s\n",calcMD5(oldUniqueName),calcMD5(uniqueName));
                    } else if (change instanceof Move) {
                        // shouldn't detect move for structure nodes
                        assert false;
                    } else
                        assert false;

                    factsPerCommit.append(fact);
                    // track this atomic change
                    /*
                    fTracker.trackAtomicChangeAdd(new AtomicChange(uniqueName,
                            filePath, gitChange.getPreImage(),
                            gitChange.getPostImage(), i, depType, chgType));
                     */
                }
                fWriter.append(factsPerCommit.toString());
                i++;
            }
            fWriter.flush();
            fWriter.close();
            return true;
            // return factsFileContent.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
