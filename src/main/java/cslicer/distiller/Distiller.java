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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class Distiller {
	protected List<SourceCodeChange> changes;

	/**
	 * Print atomic {@link SourceCodeChange}.
	 */
	public void printChanges() {

		if (changes != null) {
			for (SourceCodeChange change : changes) {

				if (change instanceof Insert) {
					Insert ins = (Insert) change;
					PrintUtils.print("Insert: " + ins.getChangedEntity() + " : "
							+ ins.getRootEntity().getUniqueName() + " : ("
							+ ins.getChangedEntity().getStartPosition() + ", "
							+ ins.getChangedEntity().getEndPosition() + ")",
							TAG.DEBUG);
				} else if (change instanceof Delete) {
					Delete del = (Delete) change;
					PrintUtils.print("Delete: " + del.getChangedEntity() + " : "
							+ del.getRootEntity().getUniqueName() + " : ("
							+ del.getChangedEntity().getStartPosition() + ", "
							+ del.getChangedEntity().getEndPosition() + ")",
							TAG.DEBUG);
				} else if (change instanceof Update) {
					Update upd = (Update) change;
					PrintUtils.print("Update: " + upd.getChangedEntity() + " : "
							+ upd.getRootEntity().getUniqueName() + " : ("
							+ upd.getChangedEntity().getStartPosition() + ", "
							+ upd.getChangedEntity().getEndPosition()
							+ ") --> (" + upd.getNewEntity().getStartPosition()
							+ ", " + upd.getNewEntity().getEndPosition() + ")",
							TAG.DEBUG);
				} else if (change instanceof Move) {
					Move mov = (Move) change;
					PrintUtils.print("Move: " + mov.getChangedEntity() + " : "
							+ mov.getRootEntity().getUniqueName() + " : ("
							+ mov.getChangedEntity().getStartPosition() + ", "
							+ mov.getChangedEntity().getEndPosition()
							+ ") --> (" + mov.getNewEntity().getStartPosition()
							+ ", " + mov.getNewEntity().getEndPosition() + ")",
							TAG.DEBUG);
				} else {
					PrintUtils.print("Change type not captured!", TAG.WARNING);
				}
			}
		} else {
			PrintUtils.print("No change detected!");
		}
	}

	/**
	 * Get a set of {@link SourceCodeChange}.
	 * 
	 * @return a set of atomic changes
	 */
	public Set<SourceCodeChange> getAtomicChanges() {
		if (changes == null)
			return new HashSet<SourceCodeChange>();

		return new HashSet<SourceCodeChange>(changes);
	}
}
