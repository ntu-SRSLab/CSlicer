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
 * Copyright (C) 2014 - 2015 Department of Computer Science, University of Toronto
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

public enum GitRefChangeType {

	METHOD_BODY_CHANGE(SIG_LEVEL.High, false),
	METHOD_SIGNATURE_CHANGE(SIG_LEVEL.High, true),
	METHOD_ADDITION(SIG_LEVEL.High, true),
	METHOD_DELETION(SIG_LEVEL.High, true),
	FIELD_DECLARATION_CHANGE(SIG_LEVEL.Medium, true),
	//FIELD_MODIFIER_CHANGE(Significance.Medium, true),
	FIELD_ADDITION(SIG_LEVEL.Medium, true),
	FIELD_DELETION(SIG_LEVEL.Medium, true),
	UNCLASSIFIED_CHANGE(SIG_LEVEL.Low, false), 
	CLASS_ADDITION(SIG_LEVEL.High, true),
	CLASS_DELETION(SIG_LEVEL.High, true),
	DOC_ADDITION(SIG_LEVEL.Low, false),
	DOC_DELETION(SIG_LEVEL.Low, false),
	DOC_UPDATE(SIG_LEVEL.Low, false);
	
	private final boolean fIsLookUpChange;
	private final SIG_LEVEL fSignificance;

	private GitRefChangeType(SIG_LEVEL level, boolean isLookUpChange) {
		fSignificance = level;
		fIsLookUpChange = isLookUpChange;
	}
	
	public boolean isLookUpChange() {
		return fIsLookUpChange;
	}
	
	public SIG_LEVEL getSignificance() {
		return fSignificance;
	}
}
