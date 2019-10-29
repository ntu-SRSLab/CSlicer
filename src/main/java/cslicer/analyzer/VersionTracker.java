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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.analyzer.AtomicChange.CHG_TYPE;
import cslicer.analyzer.HistoryAnalyzer.CommitComparator;
import cslicer.analyzer.SlicingResult.DEP_FLAG;
import cslicer.callgraph.CGNode;
import cslicer.callgraph.StaticCallGraph;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;

/**
 * Track the earliest possible version that a code entity should be updated to.
 * 
 * @author Yi Li
 *
 */
public class VersionTracker {

	/**
	 * Information about changed entities.
	 * 
	 * @author Yi Li
	 *
	 */
	class EntityTracker {
		private String fEntity;
		private Set<Version> versions;

		public EntityTracker(String entity) {
			fEntity = entity;
			versions = new HashSet<Version>();
		}

		protected Version getEarlistVersion() {
			assert versions.size() > 0;
			return Collections.min(versions, fVersionComparator);
		}

		protected String getEntityName() {
			return fEntity;
		}

		protected Version getLatestVersion() {
			assert versions.size() > 0;
			assert fCommitComparator != null;
			return Collections.max(versions, fVersionComparator);
		}

		public void trackVersion(Version v) {
			versions.add(v);
		}
	}

	/**
	 * Information about changed files.
	 * 
	 * @author Yi Li
	 *
	 */
	class FileTracker {
		private String filePath;
		private Map<String, EntityTracker> entityTracker;

		protected FileTracker(String path) {
			filePath = path;
			entityTracker = new HashMap<String, EntityTracker>();
		}

		public Set<Version> collectVersions() {
			Set<Version> versions = new HashSet<Version>();
			for (EntityTracker e : entityTracker.values()) {
				versions.add(e.getLatestVersion());
			}
			return versions;
		}

		protected Version getLatestVersion() {
			Set<Version> versions = collectVersions();
			return Collections.max(versions, fVersionComparator);
		}

		protected boolean isConsistent() {
			return collectVersions().size() <= 1;
		}

		protected void trackEntity(AtomicChange change) {
			if (!entityTracker.containsKey(change.getIdentifier()))
				entityTracker.put(change.getIdentifier(),
						new EntityTracker(change.getIdentifier()));
			entityTracker.get(change.getIdentifier())
					.trackVersion(new Version(change.getPostImage().getName()));
			assert change.getFilePath().equals(filePath);
		}
	}

	/**
	 * Abstraction representing a version.
	 * 
	 * @author Yi Li
	 *
	 */
	class Version {
		private String fId;

		protected Version(String id) {
			fId = id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Version other = (Version) obj;
			return other.getId().equals(fId);
		}

		public String getId() {
			return fId;
		}

		@Override
		public int hashCode() {
			return fId.hashCode();
		}

		public String toString() {
			return fId;
		}
	}

	/**
	 * Comparator based on the natural ordering of {@link Version}. Comparator
	 * return negative value if v1 is before v2.
	 * 
	 * @author Yi Li
	 *
	 */
	@Deprecated
	class VersionComparator implements Comparator<Version> {

		// original ordering of commits: reverse chronological order
		private List<String> order;

		public VersionComparator(List<RevCommit> history) {
			order = new LinkedList<String>();
			for (RevCommit h : history)
				order.add(h.getName());
		}

		@Override
		public int compare(Version o1, Version o2) {
			if (o1.getId().equals(o2.getId()))
				return 0;

			int p1 = order.lastIndexOf(o1.getId());
			int p2 = order.lastIndexOf(o2.getId());

			// assert p1 != -1 && p2 != -1;
			p1 = p1 == -1 ? order.size() : p1;
			p2 = p2 == -1 ? order.size() : p2;

			assert p1 != p2;

			if (p1 > p2)
				return -1;
			else
				return 1;
		}
	}

	// map commit to atomic changes
	private Map<RevCommit, Set<AtomicChange>> fChangesets;
	// map commit to labels: {TEST, COMP, HUNK, DROP}
	private Map<RevCommit, DEP_FLAG> fLabels;

	// sequence of commits in original order
	private final List<RevCommit> fHistory;

	// compare two commits in terms of ordering
	private CommitComparator fCommitComparator;
	private VersionComparator fVersionComparator;

	// changed entities in the history
	// changes are pushed into the stack from old to new
	private final Map<String, PriorityQueue<AtomicChange>> fChangedEntities;
	private final Set<String> fInitialEntities;

	// the current versions of changed entities
	// private final Map<String, RevCommit> fChangedEntityVersions;
	// changes in a file
	private Map<String, FileTracker> fFileTrackers;

	protected VersionTracker(List<RevCommit> history, CommitComparator cmp) {
		fHistory = history;
		fChangesets = new LinkedHashMap<>();
		fLabels = new LinkedHashMap<>();
		fChangedEntities = new HashMap<>();
		fInitialEntities = new HashSet<>();
		// fChangedEntityVersions = new HashMap<>();
		fCommitComparator = cmp;
		fVersionComparator = new VersionComparator(history);

		fFileTrackers = new HashMap<>();
		// updated entity -> entities directly use it (need to be updated)
		// fEntityUsedBy = new HashMap<String, Set<String>>();

		for (RevCommit h : history) {
			fChangesets.put(h, new HashSet<AtomicChange>());
			fLabels.put(h, DEP_FLAG.DROP);
		}
	}

	public String changedEntityVersionPrettyPring() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");

		for (String e : fChangedEntities.keySet()) {
			builder.append(e + " : " + fChangedEntities.get(e).peek()
					.getPostImage().getFullMessage() + "\n");
		}
		return builder.toString();
	}

	// before calling this method, make sure fChangedEntities is propagated with
	// all entities changed in the history.
	private Set<RevCommit> computeSideEffects(String uniqueName,
			final StaticCallGraph callGraph) {
		Set<String> entities = new HashSet<String>();

		// look at nodes which reference the given entity
		for (CGNode node : callGraph.getIncomingNodes(
				BytecodeUtils.filterGenericType(uniqueName))) {
			entities.add(node.getName());
		}

		// keep only those appeared in the history
		entities.retainAll(fChangedEntities.keySet());
		entities.retainAll(getPresentEntities());

		Set<RevCommit> sides = new HashSet<RevCommit>();
		// side-effects only occur on dropped entities
		for (String r : entities) {
			for (AtomicChange c : fChangedEntities.get(r)) {
				assert c.getDependencyType() == DEP_FLAG.DROP;
			}

			RevCommit s = getHeadChangeFor(r).getPostImage();
			if (fLabels.get(s) == DEP_FLAG.HUNK)
				sides.add(s);
		}

		return sides;
	}

	/**
	 * Process gathered information and produce the history slice (after hunk
	 * dependencies are computed).
	 * 
	 * @param callGraph
	 *            static call graph
	 * @return a set of side-effects for including the additional
	 *         hunk-dependencies
	 */
	public Set<RevCommit> computeSlicingResult(
			final StaticCallGraph callGraph) {
		Set<RevCommit> sides = new HashSet<RevCommit>();

		for (RevCommit c : fHistory) {

			// analyze side effects of *dropped* entities
			if (fLabels.get(c) == DEP_FLAG.DROP) {

				// track versions
				for (AtomicChange delta : fChangesets.get(c)) {
					if (delta.getChangeType() == CHG_TYPE.SIG_UPD
							|| delta.getChangeType() == CHG_TYPE.INS) {

						// the set of entities which reference delta entity
						Set<RevCommit> sideEffects = computeSideEffects(
								delta.getIdentifier(), callGraph);

						if (!sideEffects.isEmpty()) {
							PrintUtils.print("Side-effects of: " + c.getName());
							PrintUtils.print(sideEffects);

							sides.add(c);
						}

						// String filePath = delta.getFilePath();
						// if (!fFileTrackers.containsKey(filePath))
						// fFileTrackers.put(filePath,
						// new FileTracker(filePath));
						//
						// fFileTrackers.get(filePath).trackEntity(delta);
					}
				}
			}
		}

		return sides;
		// for (String file : fFileTrackers.keySet()) {
		// FileTracker fTracker = fFileTrackers.get(file);
		// if (fTracker.isConsistent()) {
		// PrintUtils.print(file + " : " + fTracker.getLatestVersion());
		// } else {
		// PrintUtils.print(file + " : inconsistent!");
		// }
		// }
	}

	public float getChangeCommitRatio() {
		float totalAtomicChangeKept = 0;
		float totalAtomicChangeRequired = 0;

		for (RevCommit c : fHistory) {
			if (fLabels.get(c) == DEP_FLAG.DROP)
				continue;

			Set<AtomicChange> changeset = getChangesVersion(c);
			if (changeset == null)
				continue;

			totalAtomicChangeKept += changeset.size();

			for (AtomicChange a : changeset) {
				if (a.isKept())
					totalAtomicChangeRequired++;
			}
		}

		return totalAtomicChangeRequired / totalAtomicChangeKept;
	}

	/**
	 * Return the set of all changed entities during the whole history.
	 * 
	 * @return a set of entity names
	 */
	public Set<String> getChangedEntities() {
		return fChangedEntities.keySet();
	}

	public int getChangedEntityLineCount() {
		int total = 0;

		for (RevCommit c : fHistory) {
			for (AtomicChange delta : fChangesets.get(c)) {
				if (delta.isKept()) {
					total += delta.getChangedLOC();
				}
			}
		}

		return total;
	}

	/**
	 * Return atomic changes of a commit.
	 * 
	 * @param c
	 *            a {@link RevCommit}.
	 * @return a set of {@link AtomicChange}.
	 */
	public Set<AtomicChange> getChangesVersion(RevCommit c) {
		if (!fChangesets.containsKey(c))
			return Collections.emptySet();
		return Collections.unmodifiableSet(fChangesets.get(c));
	}

	private DEP_FLAG getDependencyType(RevCommit commit) {
		Set<AtomicChange> changeset = fChangesets.get(commit);
		DEP_FLAG type = DEP_FLAG.DROP;

		if (changeset == null)
			return type;

		for (AtomicChange c : changeset) {
			if (c.getDependencyType().equals(DEP_FLAG.TEST))
				return DEP_FLAG.TEST;

			if (c.getDependencyType().equals(DEP_FLAG.COMP))
				type = DEP_FLAG.COMP;
		}

		return type;
	}

	public AtomicChange getHeadChangeFor(String entity) {
		if (!fChangedEntities.containsKey(entity))
			return null;
		return fChangedEntities.get(entity).peek();
	}

	public Set<AtomicChange> getHeadChanges() {
		Set<AtomicChange> res = new HashSet<>();

		for (String e : fChangedEntities.keySet()) {
			if (fChangedEntities.get(e).peek() != null)
				res.add(fChangedEntities.get(e).peek());
		}
		return res;
	}

	/**
	 * Return the set of commits currently present.
	 * 
	 * @return a set of {@link RevCommit}.
	 */
	public Set<RevCommit> getPresentCommits() {
		return Collections.unmodifiableSet(fChangesets.keySet());
	}

	/**
	 * Return the set of entities currently present.
	 * 
	 * @return set of present entity names
	 */
	public Set<String> getPresentEntities() {
		Set<String> res = new HashSet<>();

		for (String e : fChangedEntities.keySet()) {
			AtomicChange head = fChangedEntities.get(e).peek();

			if (head == null) {
				if (fInitialEntities.contains(e))
					res.add(e);
				continue;
			}

			if (head.getChangeType() == CHG_TYPE.DEL)
				continue;
			else if (head.getChangeType() == CHG_TYPE.UPD
					|| head.getChangeType() == CHG_TYPE.INS)
				res.add(head.getIdentifier());
			else
				continue;
		}
		return res;
	}

	/**
	 * Return computed slicing results.
	 * 
	 * @return {@link SlicingResult}
	 */
	public SlicingResult getSlicingResult() {
		return new SlicingResult(fHistory, fLabels);
	}

	public boolean hasUpdateChange(RevCommit commit) {
		if (fChangesets.get(commit) != null)
			for (AtomicChange a : fChangesets.get(commit))
				if (a.getChangeType().equals(CHG_TYPE.UPD))
					return true;

		return false;
	}

	public boolean isHeadChange(AtomicChange atom) {
		if (fChangedEntities.containsKey(atom.getIdentifier()))
			if (atom.equals(fChangedEntities.get(atom.getIdentifier()).peek()))
				return true;
		return false;
	}

	/**
	 * Return if a change set should be kept.
	 * 
	 * @param commit
	 *            target {@link RevCommit}
	 * @return {@code true} if change set hits TEST or COMP set.
	 */
	public boolean isKeeping(RevCommit commit) {
		Set<AtomicChange> changeset = fChangesets.get(commit);

		if (changeset == null)
			return false;

		for (AtomicChange c : changeset) {
			if (c.isKept())
				return true;
		}

		return false;
	}

	private void markFuncDependency() {
		// label commit type
		for (RevCommit h : fHistory) {
			fLabels.put(h, getDependencyType(h));
		}
	}

	/**
	 * Mark all {@link RevCommit} in {@code keep} set as HUNK if it is
	 * previously labeled as DROP.
	 * 
	 * @param keep
	 *            a set of {@link RevCommit} to keep as HUNK dependencies
	 */
	public void markHunkDependency(Collection<RevCommit> keep) {
		markFuncDependency();
		for (RevCommit k : keep) {
			if (fLabels.get(k) == DEP_FLAG.DROP)
				fLabels.put(k, DEP_FLAG.HUNK);
		}
	}

	/**
	 * Compute the current tracked changes after given version.
	 * 
	 * @param c
	 *            a {@link RevCommit}
	 * @param entity
	 *            a entity name
	 * @return a set of {@link AtomicChange}.
	 */
	public Set<AtomicChange> outStandingChanges(RevCommit c, String entity) {
		Set<AtomicChange> res = new HashSet<>();

		if (!fChangedEntities.containsKey(entity))
			return res;

		for (AtomicChange ac : fChangedEntities.get(entity)) {
			if (fCommitComparator.compare(c, ac.getPreImage()) <= 0)
				res.add(ac);
		}

		return res;
	}

	public String toString() {
		String res = "[\n";
		for (RevCommit version : fChangesets.keySet()) {
			res += version.getName() + " : "
					+ fChangesets.get(version).toString() + "\n";
		}
		res += "]";

		return res;
	}

	/**
	 * Track addition of an {@link AtomicChange}.
	 * 
	 * @param delta
	 *            basic information about an atomic change
	 */
	public void trackAtomicChangeAdd(AtomicChange delta) {
		String key = delta.getIdentifier();

		// check if entity present initially
		if (!fInitialEntities.contains(key)) {
			if (delta.getChangeType() != CHG_TYPE.INS)
				fInitialEntities.add(key);
		}

		// update change sets
		if (!fChangesets.containsKey(delta.getPostImage()))
			fChangesets.put(delta.getPostImage(), new HashSet<AtomicChange>());
		fChangesets.get(delta.getPostImage()).add(delta);

		// update changed entities
		if (!fChangedEntities.containsKey(key))
			fChangedEntities.put(key, new PriorityQueue<AtomicChange>(11,
					Collections.reverseOrder()));
		fChangedEntities.get(key).add(delta);
	}

	/**
	 * Track deleting an {@link AtomicChange}
	 * 
	 * @param delta
	 *            basic information about an atomic change
	 */
	public void trackAtomicChangeDel(AtomicChange delta) {
		String key = delta.getIdentifier();

		// update change sets
		if (fChangesets.containsKey(delta.getPostImage())) {
			fChangesets.get(delta.getPostImage()).remove(delta);
			if (fChangesets.get(delta.getPostImage()).isEmpty())
				fChangesets.remove(delta.getPostImage());
		}

		// update changed entities
		if (fChangedEntities.containsKey(key)) {
			// only allow the head to be removed
			// assert fChangedEntities.get(key).peek().equals(delta);
			fChangedEntities.get(key).remove(delta);
		}
	}

	protected void trackCommitDel(Collection<RevCommit> commits) {
		for (RevCommit c : commits) {
			trackCommitDel(c);
		}
	}

	protected void trackCommitDel(RevCommit commit) {
		Set<AtomicChange> changes = new HashSet<>(fChangesets.get(commit));

		for (AtomicChange atom : changes) {
			trackAtomicChangeDel(atom);
		}

		assert fChangesets.get(commit) == null
				|| fChangesets.get(commit).isEmpty();

		fChangesets.remove(commit);
	}

	public List<RevCommit> getPresentHistory() {
		List<RevCommit> res = new LinkedList<RevCommit>(fHistory);
		CollectionUtils.filter(res, new Predicate<RevCommit>() {

			@Override
			public boolean evaluate(RevCommit object) {
				return fChangesets.containsKey(object);
			}
		});

		return res;
	}
}
