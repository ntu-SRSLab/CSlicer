package cslicer.soot.slicing;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Local;
import soot.Unit;

public class DependencyFlowSet {

	protected Set<Unit> fDataDependency;
	protected Map<Local, Set<Unit>> fLocalLastDef;

	public DependencyFlowSet(DependencyFlowSet in) {
		in.copy(this);
	}

	public DependencyFlowSet() {
		fDataDependency = new HashSet<Unit>();
		fLocalLastDef = new HashMap<Local, Set<Unit>>();
	}

	public void updateLocal(Local l, Set<Unit> u) {
		if (!fLocalLastDef.containsKey(l))
			fLocalLastDef.put(l, new HashSet<Unit>());

		fLocalLastDef.get(l).addAll(u);
	}

	public void updateLocal(Local l, Unit u) {
		updateLocal(l, Collections.singleton(u));
	}

	public void copy(DependencyFlowSet dest) {
		dest.fDataDependency = new HashSet<Unit>(fDataDependency);
		dest.fLocalLastDef = new HashMap<Local, Set<Unit>>();
		for (Entry<Local, Set<Unit>> entry : fLocalLastDef.entrySet()) {
			dest.fLocalLastDef.put(entry.getKey(),
					new HashSet<Unit>(entry.getValue()));
		}
	}

	public void merge(DependencyFlowSet in2) {
		// merge data deps
		for (Entry<Local, Set<Unit>> entry : in2.fLocalLastDef.entrySet()) {
			updateLocal(entry.getKey(), entry.getValue());
		}
		fDataDependency.addAll(in2.fDataDependency);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DependencyFlowSet))
			return false;

		DependencyFlowSet otherSet = (DependencyFlowSet) other;
		return fDataDependency.equals(otherSet.fDataDependency)
				&& fLocalLastDef.equals(otherSet.fLocalLastDef);
	}

	@Override
	public String toString() {
		String res = "";
		res += "Data: {" + fDataDependency.toString() + "}";
		return res;
	}
}
