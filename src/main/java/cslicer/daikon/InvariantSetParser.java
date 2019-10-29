package cslicer.daikon;

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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class InvariantSetParser {

	public Set<Invariant> loadFlatInvariantSetFromHierarchicalSet(
			Set<Set<Invariant>> hSet) {
		Set<Invariant> fSet = new LinkedHashSet<>();
		for (Set<Invariant> set : hSet) {
			for (Invariant i : set) {
				fSet.add(i);
			}
		}
		return fSet;
	}

	public Set<Set<Invariant>> parseInvariantSetFromDaikonOutputFile(
			String filePath) {
		Set<Set<Invariant>> fileInvariantSet = new LinkedHashSet<>();
		File f = new File(filePath);
		FileInputStream in;
		try {
			in = new FileInputStream(f);
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			String textContent = new String(bytes);
			String[] splitedContent = textContent.split(
					"===========================================================================\n");
			int methodNumber = 0;
			for (int i = 1; i < splitedContent.length; i++) {
				if (InvariantParentType
						.isUninterestingParent(splitedContent[i])) {
					continue;
				}
				Set<Invariant> methodInvariantSet = new LinkedHashSet<>();
				String[] splitedMethodContent = splitedContent[i].split("\n");
				System.out.println(
						"[INVARIANT NUMBER]: " + splitedMethodContent.length);
				for (String invariantString : splitedMethodContent) {
					if (invariantString.startsWith("warning:")) {
						continue;
					}
					Invariant inv = new Invariant();
					inv.setFullName(invariantString);
					inv.setType(0);
					inv.setParentFullName(splitedContent[i].split("\n")[0]);
					inv.setParentName(splitedContent[i].split(":::")[0]);
					inv.setParentType(
							InvariantParentType.getParentTypeFromFirstInvariant(
									splitedContent[i]));
					if (InvariantsRank.invariatRankMap.get(inv) != null) {
						inv.setRank(InvariantsRank.invariatRankMap.get(inv));
					} else {
						inv.setRank(InvariantsRank.BASE_RANK);
						InvariantsRank.invariatRankMap.put(inv,
								InvariantsRank.BASE_RANK);
					}

					methodInvariantSet.add(inv);
				}
				fileInvariantSet.add(methodInvariantSet);
				methodNumber += 1;
			}
			System.out.println("[METHOD NUMBER]: " + methodNumber);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileInvariantSet;
	}

	public Map<String, Set<Invariant>> getAllInvariantSetsFromDaikonOutputDir(
			String dirPath) {
		Map<String, Set<Invariant>> allInvariantSetsMap = new TreeMap<>();
		File dir = new File(dirPath);
		File[] fileList = dir.listFiles();
		System.out.println("[FILE NUMBER]: " + fileList.length);
		for (File f : fileList) {
			if (f.isFile()) {
				String fileName = f.getName();
				Set<Set<Invariant>> fileInvariantSet = new LinkedHashSet<>();
				FileInputStream in;
				try {
					in = new FileInputStream(f);
					byte[] bytes = new byte[in.available()];
					in.read(bytes);
					String textContent = new String(bytes);
					String[] splitedContent = textContent.split(
							"===========================================================================\n");
					int methodNumber = 0;
					for (int i = 1; i < splitedContent.length; i++) {
						if (InvariantParentType
								.isUninterestingParent(splitedContent[i])) {
							continue;
						}
						Set<Invariant> methodInvariantSet = new LinkedHashSet<>();
						String[] splitedMethodContent = splitedContent[i]
								.split("\n");
						System.out.println("[INVARIANT NUMBER]: "
								+ splitedMethodContent.length);
						for (String invariantString : splitedMethodContent) {
							if (invariantString.startsWith("warning:")) {
								continue;
							}
							Invariant inv = new Invariant();
							inv.setFullName(invariantString);
							inv.setType(0);
							inv.setParentFullName(
									splitedContent[i].split("\n")[0]);
							inv.setParentName(
									splitedContent[i].split(":::")[0]);
							inv.setParentType(InvariantParentType
									.getParentTypeFromFirstInvariant(
											splitedContent[i]));
							if (InvariantsRank.invariatRankMap
									.get(inv) != null) {
								inv.setRank(InvariantsRank.invariatRankMap
										.get(inv));
							} else {
								inv.setRank(InvariantsRank.BASE_RANK);
								InvariantsRank.invariatRankMap.put(inv,
										InvariantsRank.BASE_RANK);
							}

							methodInvariantSet.add(inv);
						}
						fileInvariantSet.add(methodInvariantSet);
						methodNumber += 1;
					}
					System.out.println("[METHOD NUMBER]: " + methodNumber);
					in.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Set<Invariant> flatFileInvariantSet = this
						.loadFlatInvariantSetFromHierarchicalSet(
								fileInvariantSet);
				allInvariantSetsMap.put(fileName, flatFileInvariantSet);
			}
		}
		return allInvariantSetsMap;
	}
}
