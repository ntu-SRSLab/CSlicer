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
import com.google.common.collect.Sets;
import cslicer.analyzer.AtomicChange.CHG_TYPE;
import cslicer.analyzer.SlicingResult.DEP_FLAG;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.FullCoverageAnalyzer;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.*;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Collect metrics of a given range of history
 *
 * @author Chenguang Zhu
 * @since JDK1.7
 */
public class MetricsCollector extends HistoryAnalyzer {

    private LinkedList<RevCommit> A;
    private LinkedList<RevCommit> D;

    private Set<String> fIgnoreFilesTotal; // files in which changes ignored

    private VersionTracker fTracker; // track version of each changed entity


    public MetricsCollector(ProjectConfiguration config)
            throws RepositoryInvalidException, CommitNotFoundException,
            BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException,
            BranchNotFoundException, CoverageDataMissingException, IOException {

        super(config);

        // initialize lists to be used by the slicing algorithm
        D = new LinkedList<RevCommit>();
        A = new LinkedList<RevCommit>(fHistory);
        fTracker = new VersionTracker(A, fComparator);
    }

    private static int countNumOfLinesOfAFile(String filePath) {
        int numOfLines = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                numOfLines += 1;
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numOfLines;
    }

    private static String extractPackageName(String fqn) {
        String packageName = "";
        for (String segment : fqn.split("\\.")) {
            if (Character.isUpperCase(segment.charAt(0))) {
                packageName = packageName.substring(0, packageName.length() - 1);
                break;
            } else {
                packageName += segment + ".";
            }
        }
        return packageName;
    }

    private static int computePackageDistance(String packageName1, String packageName2) {
        int packageDistance = 0;
        if (packageName1.contains(packageName2)) {
            return (packageName1.split("\\.").length - packageName2.split("\\.").length);
        } else if (packageName2.contains(packageName1)) {
            return (packageName2.split("\\.").length - packageName1.split("\\.").length);
        }
        String[] segmentArray1 = packageName1.split("\\.");
        String[] segmentArray2 = packageName2.split("\\.");
        String[] longerArray = segmentArray1.length > segmentArray2.length? segmentArray1 : segmentArray2;
        String[] shorterArray = segmentArray1.length > segmentArray2.length? segmentArray2 : segmentArray1;
        for (int i = 0; i < shorterArray.length; i++) {
            if (!shorterArray[i].equals(longerArray[i])) {
                // TODO: look forward ?
                packageDistance += shorterArray.length - i;
                packageDistance += longerArray.length - i;
            }
        }
        return packageDistance;
    }

    private List<String> extractChangedFiles(RevCommit commit) throws IOException {
        List<String> changed_files = new ArrayList<>();
        RevWalk rw = new RevWalk(fJGit.getRepo());
        RevCommit current = commit;
        RevCommit parent = rw.parseCommit(current.getParent(0).getId());
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(fJGit.getRepo());
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), current.getTree());
        for (DiffEntry diff : diffs) {
            String f = diff.getNewPath();
            changed_files.add(f);
        }
        return changed_files;
    }

    private int countNumOfChangedLines(RevCommit commit) throws IOException {
        int numOfAddedLines = 0;
        int numOfDeletedLines = 0;
        RevWalk rw = new RevWalk(fJGit.getRepo());
        RevCommit current = commit;
        RevCommit parent = rw.parseCommit(current.getParent(0).getId());
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(fJGit.getRepo());
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), current.getTree());
        for (DiffEntry diff : diffs) {
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                numOfDeletedLines += edit.getEndA() - edit.getBeginA();
                numOfAddedLines += edit.getEndB() - edit.getBeginB();
            }
        }
        return numOfDeletedLines + numOfAddedLines;
    }

    /**
     * Do slicing with the default options.
     *
     * @return a list of {@link RevCommit} to drop
     * @throws CommitNotFoundException      if provided commit cannot be found
     * @throws CoverageControlIOException   if coverage dump file cannot be read
     * @throws CoverageDataMissingException if coverage data is missing
     * @throws BuildScriptInvalidException  if build script is invalid
     * @throws IOException                  if write JSON file has failed
     */
    public SlicingResult doSlicing() throws CommitNotFoundException,
            BuildScriptInvalidException, CoverageDataMissingException,
            CoverageControlIOException, IOException {
        return doSlicing(false, false);
    }

    /**
     * Implementation of the CSLICER semantic slicing algorithm.
     *
     * @param skipHunk      skip hunk dependency computation
     * @param skipCallGraph skip call graph construction
     * @return a list of {@link RevCommit} to drop
     * @throws CommitNotFoundException      if provided commit cannot be found
     * @throws CoverageControlIOException   if coverage dump file cannot be read
     * @throws CoverageDataMissingException if coverage data is missing
     * @throws BuildScriptInvalidException  if build script is invalid
     * @throws IOException                  if write JSON file has failed
     */
    public SlicingResult doSlicing(boolean skipHunk, boolean skipCallGraph)
            throws CommitNotFoundException, BuildScriptInvalidException,
            CoverageDataMissingException, CoverageControlIOException,
            IOException {

        Collections.reverse(A);
        PrintUtils.breakLine();
        PrintUtils.print("Initial |S| = " + A.size(), PrintUtils.TAG.OUTPUT);
        ChangeExtractor extractor = new ChangeExtractor(fJGit,
                fConfig.getProjectJDKVersion());

        // inspecting commits from newest to oldest
        int i = 0;
        PrintUtils.print("Analysing Commits ... ", TAG.OUTPUT);
        for (RevCommit c : A) {

            //PrintUtils.print(
            //        "=== Inspecting commit: " + commitSummary(c) + " ===");

            Set<GitRefSourceCodeChange> changes;
            try {
                changes = extractor.extractChangesMerge(c);
            } catch (ChangeDistillerException e) {
                PrintUtils.print(
                        "Exception occurs in change distilling! Result will be unreliable!",
                        TAG.WARNING);
                D.add(c); // drop if exception in distilling
                e.printStackTrace();
                return new SlicingResult();
            }

            // this set is to grow by the entities deleted
            // i.e. the entity has to exist so that it can be deleted later
            Set<SourceCodeEntity> compGrowth = new HashSet<SourceCodeEntity>();

            for (GitRefSourceCodeChange gitChange : changes) {

                // get change distiller change
                SourceCodeChange change = gitChange.getSourceCodeChange();
                // get file path to changed entity
                String filePath = gitChange.getChangedFilePath();
                // unique identifier of changed entity
                String uniqueName = null;
                // dependency type (reason for keeping)
                DEP_FLAG depType = DEP_FLAG.DROP;
                // change operation type
                CHG_TYPE chgType = null;
                // number of lines changed
                int locChanged = 0;

                // testTouchSet is the set of field/method that are touched
                // by the tests
                // parent entity is field/method/class which contains the
                // change
                if (change instanceof Delete) {
                    // do nothing for delete, since it shouldn't appear
                    // in the touch set
                    // XXX need to consider lookup changes here
                    Delete del = (Delete) change;
                    uniqueName = del.getChangedEntity().getUniqueName();
                    depType = DEP_FLAG.DROP;
                    chgType = CHG_TYPE.DEL;
                    locChanged = del.getChangedEntity().getEndPosition()
                            - del.getChangedEntity().getStartPosition() + 1;
                    compGrowth.add(del.getChangedEntity());

                } else if (change instanceof Insert) {
                    Insert ins = (Insert) change;
                    uniqueName = ins.getChangedEntity().getUniqueName();

                    locChanged = ins.getChangedEntity().getEndPosition()
                            - ins.getChangedEntity().getStartPosition() + 1;

                    chgType = CHG_TYPE.INS;
                } else if (change instanceof Update) {
                    Update upd = (Update) change;
                    uniqueName = upd.getNewEntity().getUniqueName();

                    // is signature updated?
                    boolean signatureChange = !upd.getChangedEntity()
                            .getUniqueName().equals(uniqueName);

                    assert !signatureChange;

                    chgType = CHG_TYPE.UPD;
                    // chgType = signatureChange ? CHG_TYPE.SIG_UPD
                    // : CHG_TYPE.BODY_UPD;

                    locChanged = upd.getChangedEntity().getEndPosition()
                            - upd.getChangedEntity().getStartPosition() + 1;

                } else if (change instanceof Move) {
                    // shouldn't detect move for structure nodes
                    assert false;
                } else
                    assert false;

                // track this atomic change
                fTracker.trackAtomicChangeAdd(new AtomicChange(uniqueName,
                        filePath, gitChange.getPreImage(),
                        gitChange.getPostImage(), i, depType, chgType,
                        locChanged));
            }

            // grow touch set if the commit is affecting -- this is to make sure
            // deleted entity exists beforehand.
            if (fTracker.isKeeping(c)) {

            } else {
                D.add(c); // drop set from newest to oldest
            }

            //PrintUtils.print("");
            //PrintUtils.printProgress("Slicing history: ", i++ * 100 / A.size());
        }

        // --- Collect metrics -----------------
        ArrayList<AtomicChange> atomic_changes_list = new ArrayList<>();
        for (AtomicChange change : fTracker.getHeadChanges()) {
            //PrintUtils.print("ATOMIC CHANGE: " + change, TAG.OUTPUT);
            if (change.getChangeType() == CHG_TYPE.DEL)
                continue;
            atomic_changes_list.add(change);
        }
        // 1. file distance
        Map<String, Double> file_distance_map = new TreeMap<>();
        for (AtomicChange change1 : atomic_changes_list) {
            for (AtomicChange change2 : atomic_changes_list) {
                if (change1.toString().equals(change2.toString())) {
                    continue;
                }
                if (file_distance_map.get(change2 + "," + change1) != null) {
                    continue;
                }
                String key = change1 + "," + change2;
                if (change1.getFilePath().equals(change2.getFilePath())) {
                    double value = Math.abs(change2.getChangedLOC() - change1.getChangedLOC()) /
                            countNumOfLinesOfAFile(fConfig.getRepositoryPath().replace(".git", "") + change1.getFilePath());
                    file_distance_map.put(key, value);
                } else {
                    double value = 1.0;
                    file_distance_map.put(key, value);
                }
            }
        }
        int numOfChangePairs = file_distance_map.size();
        //System.out.println("num of changes: " + atomic_changes_list.size());
        //System.out.println("num of change pairs: " + numOfChangePairs);
        double totalFileDistance = 0.0;
        for (Map.Entry<String, Double> pair : file_distance_map.entrySet()) {
            totalFileDistance += pair.getValue();
        }
        double avgFileDistance = totalFileDistance / numOfChangePairs;
        PrintUtils.print("[METRIC] Avg file distance: " + avgFileDistance);

        // 2. package distance
        Map<String, Integer> package_distance_map = new TreeMap<>();
        for (AtomicChange change1 : atomic_changes_list) {
            for (AtomicChange change2 : atomic_changes_list) {
                if (change1.toString().equals(change2.toString())) {
                    continue;
                }
                if (package_distance_map.get(change2 + "," + change1) != null) {
                    continue;
                }
                String key = change1 + "," + change2;
                String package1 = extractPackageName(change1.getIdentifier());
                String package2 = extractPackageName(change2.getIdentifier());
                //System.out.println("package 1 : " + package1 + ", package 2 : " + package2);
                int packageDistance = computePackageDistance(package1, package2);
                package_distance_map.put(key, packageDistance);
            }
        }
        double totalPackageDistance = 0.0;
        for (Map.Entry<String, Integer> pair : package_distance_map.entrySet()) {
            //System.out.println("package distance = " + pair.getValue());
            totalPackageDistance += pair.getValue();
        }
        double avgPackageDistance = totalPackageDistance / numOfChangePairs;
        PrintUtils.print("[METRIC] Avg package distance: " + avgPackageDistance);

        // ================
        ArrayList<RevCommit> commits_list = new ArrayList<>();
        for (RevCommit c : A) {
            //System.out.println(c);
            //System.out.println(extractChangedFiles(c));
            //System.out.println(countNumOfChangedLines(c));
            //PrintUtils.print("COMMIT: " + c, TAG.OUTPUT);
            commits_list.add(c);
        }
        // 3. change coupling
        Map<String, Double> change_coupling_map = new TreeMap<>();
        for (RevCommit commit1 : commits_list) {
            for (RevCommit commit2 : commits_list) {
                if (commit1.equals(commit2)) {
                    continue;
                }
                if (change_coupling_map.get(commit2 + "," + commit1) != null) {
                    continue;
                }
                String key = commit1 + "," + commit2;
                List<String> commit1ChangedFiles = extractChangedFiles(commit1);
                List<String> commit2ChangedFiles = extractChangedFiles(commit2);
                int numOfFilesChangedByBoth = 0;
                int numOfTotalChangedFiles = commit1ChangedFiles.size() + commit2ChangedFiles.size();
                for (String file : commit1ChangedFiles) {
                    if (commit2ChangedFiles.contains(file)) {
                        numOfFilesChangedByBoth += 1;
                        numOfTotalChangedFiles -= 1;
                    }
                }
                double changeCouplingScore = numOfFilesChangedByBoth / numOfTotalChangedFiles;
                change_coupling_map.put(key, changeCouplingScore);
            }
        }
        int numOfCommitPairs = change_coupling_map.size();
        //System.out.println("num of commits: " + commits_list.size());
        //System.out.println("num of commit pairs: " + numOfCommitPairs);
        double totalChangeCouplingScore = 0.0;
        for (Map.Entry<String, Double> entry : change_coupling_map.entrySet()) {
            totalChangeCouplingScore += entry.getValue();
        }
        double avgChangeCouplingScore = totalChangeCouplingScore / numOfCommitPairs;
        PrintUtils.print("[METRIC] Avg change coupling score: " + avgChangeCouplingScore);

        // 4. time difference
        Map<String, Double> time_difference_map = new TreeMap<>();
        int historyEndTime = commits_list.get(0).getCommitTime();
        int historyStartTime = commits_list.get(commits_list.size()-1).getCommitTime();
        int historyLen = historyEndTime - historyStartTime;
        for (RevCommit commit1 : commits_list) {
            for (RevCommit commit2 : commits_list) {
                if (commit1.equals(commit2)) {
                    continue;
                }
                if (time_difference_map.get(commit2 + "," + commit1) != null) {
                    continue;
                }
                String key = commit1 + "," + commit2;
                int commit1TimeInSeconds = commit1.getCommitTime();
                int commit2TimeInSeconds = commit2.getCommitTime();
                int timeDiff = commit1TimeInSeconds - commit2TimeInSeconds;
                //System.out.println("time diff = " + timeDiff + ", hist len = " + historyLen);
                double timeDiffScore = (double) timeDiff / historyLen;
                time_difference_map.put(key, timeDiffScore);
            }
        }
        double totalTimeDiffScore = 0.0;
        for (Map.Entry<String, Double> entry : time_difference_map.entrySet()) {
            //System.out.println("time diff: " + entry.getValue());
            totalTimeDiffScore += entry.getValue();
        }
        double avgTimeDiffScore = totalTimeDiffScore / numOfCommitPairs;
        PrintUtils.print("[METRIC] Avg time diff score: " + avgTimeDiffScore);

        // 5. author difference
        Map<String, Double> author_difference_map = new TreeMap<>();
        Set<String> allAuthors = new TreeSet<>();
        for (RevCommit commit : commits_list) {
            allAuthors.add(commit.getAuthorIdent().getName());
        }
        for (RevCommit commit1 : commits_list) {
            //System.out.println("AUTHOR: " + commit1.getAuthorIdent().getName());
            for (RevCommit commit2 : commits_list) {
                if (commit1.equals(commit2)) {
                    continue;
                }
                if (author_difference_map.get(commit2 + "," + commit1) != null) {
                    continue;
                }
                String key = commit1 + "," + commit2;
                String author1 = commit1.getAuthorIdent().getName();
                String author2 = commit2.getAuthorIdent().getName();
                if (author1.equals(author2)) {
                    double authorDiffScore = 1.0 / allAuthors.size();
                    author_difference_map.put(key, authorDiffScore);
                } else {
                    double authorDiffScore = 2.0 / allAuthors.size();
                    author_difference_map.put(key, authorDiffScore);
                }
            }
        }
        double totalAuthorDiffScore = 0.0;
        for (Map.Entry<String, Double> entry : author_difference_map.entrySet()) {
            //System.out.println("author diff: " + entry.getValue());
            totalAuthorDiffScore += entry.getValue();
        }
        double avgAuthorDiffScore = totalAuthorDiffScore / numOfCommitPairs;
        PrintUtils.print("[METRIC] Avg author diff score: " + avgAuthorDiffScore);

        // 6. num of changed lines / files per commit
        int totalNumOfChangedLines = 0;
        int totalNumOfChangedFiles = 0;
        Map<String, Integer> changedLinesMap = new TreeMap<>();
        Map<String, Integer> changedFilesMap = new TreeMap<>();
        for (RevCommit commit : commits_list) {
            //System.out.println("AUTHOR: " + commit1.getAuthorIdent().getName());
            List<String> changedFiles = extractChangedFiles(commit);
            changedFilesMap.put(commit.toString(), changedFiles.size());
            int numOfChangedLines = countNumOfChangedLines(commit);
            changedLinesMap.put(commit.toString(), numOfChangedLines);
        }
        for (Map.Entry<String, Integer> entry : changedFilesMap.entrySet()) {
            totalNumOfChangedFiles += entry.getValue();
        }
        for (Map.Entry<String, Integer> entry : changedLinesMap.entrySet()) {
            totalNumOfChangedLines += entry.getValue();
        }
        double avgNumOfChangedFiles = (double) totalNumOfChangedFiles / commits_list.size();
        double avgNumOfChnagedLines = (double) totalNumOfChangedLines / commits_list.size();
        PrintUtils.print("[METRIC] Avg num of changed files: " + avgNumOfChangedFiles);
        PrintUtils.print("[METRIC] Avg num of changed lines: " + avgNumOfChnagedLines);

        // 7. absolute file distance
        Map<String, Double> abs_file_distance_map = new TreeMap<>();
        for (AtomicChange change1 : atomic_changes_list) {
            for (AtomicChange change2 : atomic_changes_list) {
                if (change1.toString().equals(change2.toString())) {
                    continue;
                }
                if (file_distance_map.get(change2 + "," + change1) != null) {
                    continue;
                }
                String key = change1 + "," + change2;
                if (change1.getFilePath().equals(change2.getFilePath())) {
                    double value = Math.abs(change2.getChangedLOC() - change1.getChangedLOC());
                    abs_file_distance_map.put(key, value);
                } else {
                    double value = countNumOfLinesOfAFile(fConfig.getRepositoryPath().replace(".git", "") + change1.getFilePath())
                            + countNumOfLinesOfAFile(fConfig.getRepositoryPath().replace(".git", "") + change2.getFilePath());
                    abs_file_distance_map.put(key, value);
                }
            }
        }
        double totalAbsFileDistance = 0.0;
        for (Map.Entry<String, Double> pair : abs_file_distance_map.entrySet()) {
            totalAbsFileDistance += pair.getValue();
        }
        double avgAbsFileDistance = totalAbsFileDistance / numOfChangePairs;
        PrintUtils.print("[METRIC] Avg absolute file distance: " + avgAbsFileDistance);

        return null;
    }

    /**
     * Dummy {@code doSlicing} method where H' is given.
     *
     * @param keep sliced sub-history H'
     * @return H' plus hunk dependencies
     */
    public List<RevCommit> doSlicing(Set<String> keep) {
        List<RevCommit> hunkDeps = computeHunkDepSetWithId(keep);

        Set<String> hunkId = new HashSet<String>();
        for (RevCommit h : hunkDeps)
            hunkId.add(h.getName());

        List<RevCommit> drop = new LinkedList<RevCommit>();

        Collections.reverse(A);
        for (RevCommit a : A) {
            if (hunkId.contains(a.getName())) {
                PrintUtils.print("hunk: " + commitSummary(a));
                continue;
            }
            if (keep.contains(a.getName())) {
                PrintUtils.print("keep: " + commitSummary(a));
                continue;
            }

            drop.add(a);
            PrintUtils.print("Drop: " + commitSummary(a));
        }

        return drop;
    }

    // enumerate all possible cherry-pickable subsets
    // with respect to existing hunk dependencies among
    // FUNC + COMP commits
    private Set<Set<RevCommit>> getLayeredCommitSet(
            List<Set<RevCommit>> layers) {
        Set<Set<RevCommit>> res = new HashSet<>();

        if (layers == null || layers.size() == 0)
            return res;

        Set<RevCommit> total = new HashSet<>();
        for (Set<RevCommit> l : layers)
            total.addAll(l);

        for (int i = 0; i < layers.size(); i++) {
            Set<RevCommit> top = layers.get(i);
            PrintUtils.print("Layer " + i + " : " + top.size());

            for (Set<RevCommit> s : Sets.powerSet(top)) {
                // avoiding empty set
                if (i == layers.size() - 1 && s.size() == top.size())
                    continue;

                if (s.size() > 0)
                    res.add(new HashSet<>(Sets.difference(total, s)));
            }

            total.removeAll(layers.get(i));
        }

        assert total.isEmpty();

        return res;
    }

    // Chenguang
    public List<RevCommit> getOriginalCommitList() {
        return A;
    }

    protected Set<String> getReducedSet() {
        Set<String> dIds = new HashSet<String>();
        for (RevCommit d : D)
            dIds.add(commitSummary(d));
        return dIds;
    }

    public final SlicingResult getSlicingReseult() {
        return fTracker.getSlicingResult();
    }

    private void initializeCoverage(ProjectConfiguration config)
            throws BuildScriptInvalidException, CoverageDataMissingException,
            CoverageControlIOException {

        // setup coverage analyzer
        if (config.isClassRootPathSet() && config.isSourceRootPathSet()) {

            // if (config.isJavaSlicerDumpPathSet()) {
            // fCoverage = new CheckedCoverageAnalyzer(
            // config.getJavaSlicerDumpPath(),
            // config.getJavaSlicerCriteria());
            // } else
            if (config.isJacocoExecPathSet())
                fCoverage = new FullCoverageAnalyzer(config.getJacocoExecPath(),
                        config.getSourceRootPath(), config.getClassRootPath());
            else
                fCoverage = null;
        } else {
            // if (config.isJavaSlicerDumpPathSet()) {
            // fCoverage = new CheckedCoverageAnalyzer(fCompiler,
            // config.getJavaSlicerDumpPath(),
            // config.getJavaSlicerCriteria());
            // } else {

            fCoverage = (config.isSubModuleSet())
                    ? new FullCoverageAnalyzer(fCompiler,
                    config.getSubModuleBuildScriptPath())
                    : new FullCoverageAnalyzer(fCompiler);
            // }
        }
    }

    /**
     * Further shorten slicing result by enumeration with respect to hunk
     * dependencies.
     *
     * @param result {@link SlicingResult} to shorten
     * @return a set of shorter slices
     */
    public Set<List<RevCommit>> shortenSlice(SlicingResult result) {
        DependencyCache cache = new DependencyCache();
        // compute restricted hunk dependency: only on COMP+FUNC
        List<RevCommit> hunks = computeHunkDepSet(result.getFuncComp(), cache);

        assert hunks.size() == result.getTotalCount() - result.getDropCount();

        Set<Set<RevCommit>> candidate = cache
                .applicableSubSets(result.getFuncComp());
        // getLayeredCommitSet(
        // cache.getLayeredDeps(result.getFuncComp(), false));

        // sort candidate snapshots by their length
        List<Set<RevCommit>> snapshots = new ArrayList<>(candidate);
        Collections.sort(snapshots, new Comparator<Set<RevCommit>>() {
            @Override
            public int compare(Set<RevCommit> o1, Set<RevCommit> o2) {
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;

                return o1.size() - o2.size();
            }
        });

        Set<List<RevCommit>> res = new HashSet<List<RevCommit>>();

        // PrintUtils.print("candidate.count = " + candidate.size());
        StatsUtils.setCount("snapshot.size", candidate.size());

        for (Set<RevCommit> r : snapshots) {
            if (res.size() > 50) {
                PrintUtils.print("Too many branches to try. Quit!");
                break;
            }

            if (!r.isEmpty()) {
                List<RevCommit> h = computeHunkDepSet(r, cache);

                // assert !res.contains(h);
                res.add(h);

                if (fConfig.getSkipPicking())
                    continue;

                if (!tryPickCommits(h))
                    PrintUtils.print("picking failed!");
            }
        }

        return res;
    }

    /**
     * Display AST differencing results for a commit.
     *
     * @param commitID target commit
     * @throws CommitNotFoundException if target commit cannot be found
     */
    public void showASTDiff(String commitID) throws CommitNotFoundException {
        ChangeExtractor extractor = new ChangeExtractor(fJGit,
                fConfig.getProjectJDKVersion());

        RevCommit target = fJGit.getCommit(commitID);

        try {
            PrintUtils.print("AST Diff for commit: " + commitSummary(target));
            extractor.extractChangesMerge(target);
            PrintUtils.print("ChangeDistiller Diff for commit: "
                    + commitSummary(target));
            extractor.extractChangesPrecise(target);
        } catch (ChangeDistillerException e) {
            PrintUtils.print(
                    "Exception occurs in change distilling! Result will be unreliable!",
                    TAG.WARNING);
            e.printStackTrace();
        }
    }

    /**
     * Revert selected files silently.
     *
     * @return {@code true} if revert successes
     */
    protected boolean tryRevertFiles() {
        try {
            fJGit.checkOutFiles(fIgnoreFilesTotal, fStart);
            return true;
        } catch (CheckoutFileFailedException e) {
            e.printStackTrace();
        }
        return false;
    }
}

