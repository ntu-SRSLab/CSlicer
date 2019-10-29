package cslicer.callgraph;

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

import java.util.Set;

/**
 * Interface to all types of call graphs.
 * 
 * @author Yi Li
 *
 */
public interface ICallGraph {

	/**
	 * Insert an edge to the call graph.
	 * 
	 * @param from
	 *            from node
	 * @param to
	 *            to node
	 * @param type
	 *            edge type
	 */
	public void insertEdge(CGNode from, CGNode to, CGEdgeType type);

	/**
	 * Return the callers of a given method.
	 * 
	 * @param method
	 *            target method
	 * @return caller method nodes
	 */
	public Set<MethodNode> getCaller(MethodNode method);

	/**
	 * Return the callees of a given method.
	 * 
	 * @param method
	 *            target method
	 * @return callee method nodes
	 */
	public Set<MethodNode> getCallee(MethodNode method);

	/**
	 * Return the accessors of a given field.
	 * 
	 * @param field
	 *            target field
	 * @return accessor method nodes
	 */
	public Set<MethodNode> getAccessor(FieldNode field);

	/**
	 * Return the accessee of a given method.
	 * 
	 * @param method
	 *            target method
	 * @return accessee field nodes
	 */
	public Set<FieldNode> getAccessee(MethodNode method);

	/**
	 * Return the referencers of a given class.
	 * 
	 * @param clazz
	 *            target class
	 * @return referencer class nodes
	 */
	public Set<ClassNode> getReferencer(ClassNode clazz);

	/**
	 * Return the referencees of a given class.
	 * 
	 * @param clazz
	 *            target class
	 * @return referencee class nodes
	 */
	public Set<ClassNode> getReferencee(ClassNode clazz);
}
