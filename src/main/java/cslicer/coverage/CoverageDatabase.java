package cslicer.coverage;

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

import java.util.HashSet;
import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public class CoverageDatabase {
	// fully covered entities
	protected Set<SourceCodeEntity> fullyCoveredClassEntity;
	protected Set<SourceCodeEntity> fullyCoveredMethodEntity;
	protected Set<SourceCodeEntity> fullyCoveredFieldEntity;
	// partially covered entities
	protected Set<SourceCodeEntity> partiallyCoveredClassEntity;
	protected Set<SourceCodeEntity> partiallyCoveredMethodEntity;
	protected Set<SourceCodeEntity> partiallyCoveredFieldEntity;
	// unknown entities
	private Set<SourceCodeEntity> unknownClassEntity;
	private Set<SourceCodeEntity> unknownMethodEntity;
	private Set<SourceCodeEntity> unknownFieldEntity;

	public CoverageDatabase() {
		fullyCoveredClassEntity = new HashSet<SourceCodeEntity>();
		fullyCoveredMethodEntity = new HashSet<SourceCodeEntity>();
		fullyCoveredFieldEntity = new HashSet<SourceCodeEntity>();
		partiallyCoveredClassEntity = new HashSet<SourceCodeEntity>();
		partiallyCoveredMethodEntity = new HashSet<SourceCodeEntity>();
		partiallyCoveredFieldEntity = new HashSet<SourceCodeEntity>();
		unknownClassEntity = new HashSet<SourceCodeEntity>();
		unknownMethodEntity = new HashSet<SourceCodeEntity>();
		unknownFieldEntity = new HashSet<SourceCodeEntity>();
	}

	public void addClassEntity(SourceCodeEntity entity,
			StructureNodeStatus status) {
		if (status == StructureNodeStatus.NOT_COVERED)
			return;

		else if (status == StructureNodeStatus.FULLY_COVERED)
			fullyCoveredClassEntity.add(entity);
		else if (status == StructureNodeStatus.PARTIALLY_COVERED)
			partiallyCoveredClassEntity.add(entity);
		else if (status == StructureNodeStatus.UNKNOWN) {
			unknownClassEntity.add(entity);
			// assert (entity.isAbstract()); // XXX abstract class?
		}
	}

	public void addMethodEntity(SourceCodeEntity entity,
			StructureNodeStatus status) {
		if (status == StructureNodeStatus.NOT_COVERED)
			return;

		else if (status == StructureNodeStatus.FULLY_COVERED)
			fullyCoveredMethodEntity.add(entity);
		else if (status == StructureNodeStatus.PARTIALLY_COVERED)
			partiallyCoveredMethodEntity.add(entity);
		else if (status == StructureNodeStatus.UNKNOWN) {
			unknownMethodEntity.add(entity);
			// assert (entity.isAbstract()); // XXX abstract class?
		}
	}

	public void addFieldEntity(SourceCodeEntity entity,
			StructureNodeStatus status) {
		if (status == StructureNodeStatus.NOT_COVERED)
			return;

		else if (status == StructureNodeStatus.FULLY_COVERED)
			fullyCoveredFieldEntity.add(entity);
		else if (status == StructureNodeStatus.PARTIALLY_COVERED)
			partiallyCoveredFieldEntity.add(entity);
		else if (status == StructureNodeStatus.UNKNOWN) {
			unknownFieldEntity.add(entity);
			// XXX need to check if field is accessed
		}
	}

	/**
	 * Return only fully-covered source code entities.
	 * 
	 * @return a set of {@link SourceCodeEntity}
	 */
	public Set<SourceCodeEntity> getFullyCoveredEntities() {
		Set<SourceCodeEntity> res = new HashSet<SourceCodeEntity>();
		res.addAll(fullyCoveredClassEntity);
		res.addAll(fullyCoveredMethodEntity);
		res.addAll(fullyCoveredFieldEntity);

		return res;
	}

	/**
	 * Return both fully-covered and partially-covered source code entities.
	 * 
	 * @return a set of {@link SourceCodeEntity}
	 */
	public Set<SourceCodeEntity> getPartiallyCoveredEntities() {
		Set<SourceCodeEntity> res = new HashSet<SourceCodeEntity>();
		res.addAll(getFullyCoveredEntities());
		res.addAll(partiallyCoveredClassEntity);
		res.addAll(partiallyCoveredMethodEntity);
		res.addAll(partiallyCoveredFieldEntity);

		return res;
	}

	/**
	 * Return both fully- and partially-covered class names.
	 * 
	 * @return a set of class names
	 */
	public Set<String> getPartiallyCoveredClassNames() {
		Set<String> res = new HashSet<String>();
		for (SourceCodeEntity s : fullyCoveredClassEntity) {
			res.add(s.getUniqueName());
		}
		for (SourceCodeEntity s : partiallyCoveredClassEntity) {
			res.add(s.getUniqueName());
		}

		return res;
	}

	/**
	 * Return all partially-covered source code entities plus all remaining
	 * fields. We need to compute field access to filter out unused fields.
	 * 
	 * @return a set of {@link SourceCodeEntity}
	 */
	public Set<SourceCodeEntity> getAllRelevantEntities() {
		Set<SourceCodeEntity> res = new HashSet<SourceCodeEntity>();
		res.addAll(getPartiallyCoveredEntities());
		// res.addAll(unknownFieldEntity);

		return res;
	}

	public void exclude(CoverageDatabase coverageStore) {
		this.fullyCoveredClassEntity
				.removeAll(coverageStore.fullyCoveredClassEntity);
		this.fullyCoveredFieldEntity
				.removeAll(coverageStore.fullyCoveredFieldEntity);
		this.fullyCoveredMethodEntity
				.removeAll(coverageStore.fullyCoveredMethodEntity);
		this.partiallyCoveredClassEntity
				.removeAll(coverageStore.partiallyCoveredClassEntity);
		this.partiallyCoveredFieldEntity
				.removeAll(coverageStore.partiallyCoveredFieldEntity);
		this.partiallyCoveredMethodEntity
				.removeAll(coverageStore.partiallyCoveredMethodEntity);
	}
}
