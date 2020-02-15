package cslicer.analyzer;

import java.util.Collection;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.utils.BytecodeUtils;

public class ChangedEntityView {

	// view: entity --> set of dependencies
	private Map<String, Set<String>> view;
	private Map<String, RevCommit> version;
	private Set<String> fixDeps;

	public ChangedEntityView() {
		view = new HashMap<>();
		version = new HashMap<>();
		fixDeps = new HashSet<>();
	}

	public ChangedEntityView(Collection<String> fix) {
		this();
		fixDeps.addAll(fix);
	}

	public Set<Pair<String, String>> getMissingDependencies() {
		Set<Pair<String, String>> res = new HashSet<>();

		Set<String> entities = view.keySet();
		for (String e : entities) {
			for (String d : view.get(e))
				if (!entities.contains(d))
					res.add(Pair.of(e, d));
		}

		return res;
	}

	public RevCommit getVersion(String e) {
		return version.get(e);
	}

	public void insertEntity(String e, Set<String> deps) {
		if (!view.containsKey(e))
			view.put(e, deps);
	}

	/**
	 * Return {@code true} if all dependencies are present.
	 * 
	 * @return {@code true} if the view is consistent.
	 */
	public boolean isConsistent() {
		Set<String> entities = view.keySet();

		if (!entities.containsAll(fixDeps))
			return false;

		// for (String e : entities) {
		// Set<String> deps = view.get(e);
		// if (deps == null)
		// continue;
		//
		// for (String d : deps)
		// if (!containsIdentifier(entities, d))
		// return false;
		// // if (!entities.contains(d))
		// // return false;
		// }
		return true;
	}

	private boolean containsIdentifier(Collection<String> set, String id) {
		for (String s : set) {
			if (BytecodeUtils.matchWithGenericType(s, id))
				return true;
		}
		return false;
	}

	public void refineFixedDeps() {
		fixDeps.retainAll(view.keySet());
	}

	public void removeEntity(String e) {
		view.remove(e);
	}

	public void setVersion(String e, Set<String> v, RevCommit c) {
		view.put(e, v);
		version.put(e, c);
	}
}
