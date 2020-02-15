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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.MultiSourceFileLocator;

import com.google.inject.Inject;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import cslicer.analyzer.ProjectConfiguration;
import cslicer.utils.CompilationUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

/**
 * Interpret and store coverage results from {@link FullCoverageAnalyzer}.
 * 
 * @author Yi Li
 *
 */
public class FullCoverageExtractor {

	public static final int TAB_WIDTH = 2;
	private CoverageDatabase fDataStore;

	private ASTHelperFactory fASTHelperFactory;

	public enum Granularity {
		CLASS, METHOD, STATEMENT
	}

	private String fJDKVersion = ProjectConfiguration.DEFAULT_JDK;

	@Inject
	public FullCoverageExtractor(ASTHelperFactory factory) {
		fDataStore = new CoverageDatabase();
		fASTHelperFactory = factory;
	}

	public final CoverageDatabase getCoverageDatabase() {
		return fDataStore;
	}

	public void buildCoverageData(IBundleCoverage bundle, File sourcePath) {
		buildCoverageData(bundle, Collections.singleton(sourcePath),
				fJDKVersion);
	}

	public void buildCoverageData(IBundleCoverage bundle,
			Collection<File> sourcePaths) {
		buildCoverageData(bundle, sourcePaths, fJDKVersion);
	}

	public void buildCoverageData(IBundleCoverage bundle,
			Collection<File> sourcePaths, String jdkVersion) {
		MultiSourceFileLocator locator = new MultiSourceFileLocator(
				sourcePaths.size());
		for (File dir : sourcePaths) {
			DirectorySourceFileLocator l = new DirectorySourceFileLocator(dir,
					null, TAB_WIDTH);
			locator.add(l);
		}

		for (IPackageCoverage pack : bundle.getPackages()) {
			PrintUtils.print("Analyzing package: " + pack.getName() + " ...");

			for (ISourceFileCoverage source : pack.getSourceFiles()) {
				PrintUtils.print("File: " + source.getName() + " ...", 1);

				Reader fileReader = null;
				try {
					// read source file and compile to AST
					fileReader = locator.getSourceFile(pack.getName(),
							source.getName());
					if (fileReader == null)
						throw new IOException();

					// create an ASTHelper associated with the source file
					org.apache.commons.io.FileUtils.getTempDirectory();
					File tempSource = File.createTempFile("gitref", ".ast");
					tempSource.deleteOnExit();

					String sourceContent = IOUtils.toString(fileReader);
					IOUtils.write(sourceContent,
							FileUtils.openOutputStream(tempSource));
					JavaASTHelper astHelper = (JavaASTHelper) fASTHelperFactory
							.create(tempSource, jdkVersion);

					JavaCompilation jcu = CompilationUtils.compile(
							sourceContent, source.getName(), jdkVersion);

					// label AST using coverage data
					final CompilationUnitDeclaration cu = jcu
							.getCompilationUnit();
					JavaStructureNode root = new JavaStructureNode(
							ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode.Type.CU,
							null, null, cu);
					ASTCoverageBuilder astBuilder = new ASTCoverageBuilder(root,
							source, jcu.getScanner(), fDataStore, astHelper);
					cu.traverse(astBuilder, (CompilationUnitScope) null);

				} catch (IOException e) {
					PrintUtils.print(
							"Cannot find file " + source.getName() + "!",
							TAG.WARNING);
					continue;
				} finally {
					IOUtils.closeQuietly(fileReader);
				}
			}
		}
	}

	public void exclude(FullCoverageExtractor computeCoverage) {
		this.fDataStore.exclude(computeCoverage.getCoverageDatabase());
	}

	/**
	 * Check if a source code change is covered by the test.
	 * 
	 * @param name
	 *            {@code String} representation of the source code change
	 * @param isShortName
	 *            use short representation to match
	 * @return {@code true} if the source code change is possibly covered
	 */
	// public boolean isMethodCovered(String name) {
	// // filter out generic types
	// final String nameWithoutGenericType = name.replaceAll(
	// "<[\\p{L}][\\p{L}\\p{N}]*>", "");
	//
	// return fDataStore.coveredMethodNames.contains(nameWithoutGenericType);
	// }

	// public String getPackageName(final String vmname) {
	// if (vmname.length() == 0) {
	// return "default";
	// }
	// return vmname.replace('/', '.');
	// }

	// private String getClassName(final String vmname) {
	// final int pos = vmname.lastIndexOf('/');
	// final String name = pos == -1 ? vmname : vmname.substring(pos + 1);
	// return name.replace('$', '.');
	// }
	//
	// private String getShortClassName(final String vmname) {
	// final String name = getClassName(vmname);
	// return name.substring(name.lastIndexOf('.') + 1);
	// }
	//
	// private boolean isAnonymous(final String vmname) {
	// final int dollarPosition = vmname.lastIndexOf('$');
	// if (dollarPosition == -1) {
	// return false;
	// }
	// final int internalPosition = dollarPosition + 1;
	// if (internalPosition == vmname.length()) {
	// // shouldn't happen for classes compiled from Java source
	// return false;
	// }
	// // assume non-identifier start character for anonymous classes
	// final char start = vmname.charAt(internalPosition);
	// return !Character.isJavaIdentifierStart(start);
	// }

	// private String getClassName(final String vmname, final String
	// vmsignature,
	// final String vmsuperclass, final String[] vminterfaces) {
	// if (isAnonymous(vmname)) {
	// final String vmsupertype;
	// if (vminterfaces != null && vminterfaces.length > 0) {
	// vmsupertype = vminterfaces[0];
	// } else if (vmsuperclass != null) {
	// vmsupertype = vmsuperclass;
	// } else {
	// vmsupertype = null;
	// }
	// // append Eclipse style label, e.g. "Foo.new Bar() {...}"
	// if (vmsupertype != null) {
	// final StringBuilder builder = new StringBuilder();
	// final String vmenclosing = vmname.substring(0,
	// vmname.lastIndexOf('$'));
	// builder.append(getClassName(vmenclosing)).append(".new ")
	// .append(getClassName(vmsupertype)).append("() {...}");
	// return builder.toString();
	// }
	// }
	// return getClassName(vmname);
	// }

	// public String getQualifiedClassName(final String vmname) {
	// return vmname.replace('/', '.').replace('$', '.');
	// }
	//
	// public String getMethodName(final String vmclassname,
	// final String vmmethodname, final String vmdesc,
	// final String vmsignature) {
	// return getMethodName(vmclassname, vmmethodname, vmdesc, false);
	// }
	//
	// public String getQualifiedMethodName(final String vmclassname,
	// final String vmmethodname, final String vmdesc,
	// final String vmsignature) {
	// return getQualifiedClassName(vmclassname) + "."
	// + getMethodName(vmclassname, vmmethodname, vmdesc, false);
	// }
	//
	// private String getMethodName(final String vmclassname,
	// final String vmmethodname, final String vmdesc,
	// final boolean qualifiedParams) {
	//
	// if ("<clinit>".equals(vmmethodname)) {
	// return "static {...}";
	// }
	// final StringBuilder result = new StringBuilder();
	// if ("<init>".equals(vmmethodname)) {
	// if (isAnonymous(vmclassname)) {
	// return "{...}";
	// } else {
	// result.append(getShortClassName(vmclassname));
	// }
	// } else {
	// result.append(vmmethodname);
	// }
	// result.append('(');
	// final Type[] arguments = Type.getArgumentTypes(vmdesc);
	// boolean comma = false;
	// for (final Type arg : arguments) {
	// if (isInnerClass(vmclassname)
	// && arg.getClassName().equals(
	// getOutterClassName(vmclassname)))
	// continue;
	//
	// if (comma) {
	// result.append(",");
	// } else {
	// comma = true;
	// }
	// if (qualifiedParams) {
	// result.append(getQualifiedClassName(arg.getClassName()));
	// } else {
	// result.append(getShortTypeName(arg));
	// }
	// }
	// result.append(')');
	// return result.toString();
	// }
	//
	// private Object getOutterClassName(String vmclassname) {
	// final int pos = vmclassname.lastIndexOf('$');
	// final String res = pos == -1 ? getClassName(vmclassname)
	// : getClassName(vmclassname.substring(0, pos));
	// return res;
	// }
	//
	// private boolean isInnerClass(String vmclassname) {
	// return vmclassname.lastIndexOf('$') != -1;
	// }
	//
	// private String getShortTypeName(final Type type) {
	// final String name = type.getClassName();
	// final int pos = name.lastIndexOf('.');
	// final String shortName = pos == -1 ? name : name.substring(pos + 1);
	// return shortName.replace('$', '.');
	// }
}
