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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public class InstructionSourceCodeEntity extends SourceCodeEntity 
{
	private HashSet<Integer> lineNumberSet;
	private String className;
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public InstructionSourceCodeEntity(String uniqueName, EntityType type,
			int modifiers, SourceRange range, HashSet<Integer> lineNumberSet) {
		super(uniqueName, type, modifiers, range);
		this.lineNumberSet = lineNumberSet;
	}
	
	public HashSet<Integer> getLineNumberSet() {
		return lineNumberSet;
	}

	public void setLineNumberSet(HashSet<Integer> lineNumberSet) {
		this.lineNumberSet = lineNumberSet;
	}

	@Override
    public boolean equals(Object obj) {
       
        InstructionSourceCodeEntity other = (InstructionSourceCodeEntity) obj;
        return (this.getUniqueName().equals(other.getUniqueName()));
    }
	
	@Override
    public int hashCode() {
    	HashCodeBuilder b = new HashCodeBuilder(17, 37);
    	for (SourceCodeEntity e : getAssociatedEntities()) {
    		b.append(e.getUniqueName());
    		b.append(e.getModifiers());
    		b.append(e.getType());
    	}
        return new HashCodeBuilder(17, 37).append(getUniqueName()).append(getType()).append(getModifiers())
                .append(b.toHashCode()).toHashCode();
    }

}
