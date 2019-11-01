This is the source directory of **DEFINER** ([gitslice/definer](https://bitbucket.org/liyistc/gitslice/commits/tag/definer)) and **CSLICER** ([gitslice/cslicer](https://bitbucket.org/liyistc/gitslice/commits/tag/cslicer)), prototype implementations of a collection of automated semantic history slicing algorithms. The tools currently work for Java projects hosted in Git repositories.

---

## License

The license is in LICENSE.txt

## Introduction

DEFINER is a semantic slicing tool for Git reposiotries under active development by the Software Engineering group at the University of Toronto.
The goal of semantic slicing is to identify a subset of change sets (commits) that implementing a software functionality (e.g., feature, enhancement, or bug fix). The applications include code migration, branch refactoring, etc.
DEFINER currently works only for Java repositories.

Another implementation which is based on an alternative semantic slicing algorithm -- CSLICER is now residing in the master branch.

## Dependencies

* Maven  
[Apache Maven](http://maven.apache.org) is used for dependency control. To build the project, first download and install Maven following the instructions [here](http://maven.apache.org/download.cgi) (version >= 3.2). Make sure environment variables ```M2_HOME```, ```M2```, and ```JAVA_HOME``` are set correctly according to the instructions.
All required dependencies will be fetched and installed by Maven automatically.

* ChangeDistiller  
We use [ChangeDistiller](https://bitbucket.org/sealuzh/tools-changedistiller/wiki/Home) for categorizing and analyzing changes.
A local copy of ChangeDistiller-SNAPSHOT-0.0.1 is included in the lib directory.

* Daikon (optional)
We use [Daikon](https://plse.cs.washington.edu/daikon) for dynamic invariant generation. A local copy of Daikon is included in the lib directory.

## Install DEFINER

Requirements:

* JDK 1.8
* Maven 3.0 or later
* Internet connection for first build (to fetch all Maven and DEFINER dependencies)

Get the source and external dependencies
```shell
git clone https://github.com/ntu-SRSLab/CSlicer.git cslicer
cd cslicer
git submodule init
git submodule update
```

Maven build goals:

* Clean: ```mvn clean```
* Compile: ```mvn compile```
* Run tests: ```mvn test```
* Create JAR: ```mvn package```
* Install JAR in M2 cache: ```mvn install```
* Build javadocs: ```mvn javadoc:javadoc```

Build options:

* Use ```-DskipTests``` to skip tests

## Run DEFINER ##

Find executable jar files at ```definer/target``` and all dependencies at ```definer/target/lib```. Run DEFINER using
 
```shell
java -jar cslicer-XXX-jar-with-dependencies.jar -c <CONFIG_FILE_PATH>
```

The target project configuration file specifies the location, target commits (history), path to source code root, etc. An example of a configuration file (with extension .properties) is as follows.

```properties
# This is an example project configuration file project.properties.
# Assume example repository is located within <EXAMPLE_DIRECTORY>
# Path to target repository (required)
repoPath = <EXAMPLE_DIRECTOR>/commons-collections-examples/IterableUtilsTest#indexOf/commons-collections/.git
# Target history length (optional)
historyLength = 97
# History ending commit (required)
endCommit = dcf3df80
# Path to the pom.xml build script (required)
buildScriptPath = <EXAMPLE_DIRECTOR>/commons-collections-examples/IterableUtilsTest#indexOf/commons-collections/pom.xml
# Test scope settings: 'indexOf' method in 'IterableUtilsTest' class (required)
testScope = IterableUtilsTest#indexOf
# Path to the configuration file used by Daikon (optional)
daikonConfig = /home/polaris/Desktop/123/example-settings.txt
# Path to Daikon jar file (required)
daikonJar = /home/polaris/Desktop/daikonparent/daikon-5.2.22/daikon.jar
# Path to Chicory jar (required by Daikon)
chicoryPath = /home/polaris/Desktop/daikonparent/daikon-5.2.22/java/ChicoryPremain.jar
# Excludes changes in the following files (optional)
excludePaths = pom.xml,src/changes/changes.xml,.gitignore,CONTRIBUTING.md,NOTICE.txt,README.md,README.txt,TODO.txt
```

See detailed usage by entering the command

```shell
java -jar cslicer-XXX-jar-with-dependencies.jar -h
```

## Publications ##
[Semantic Slicing of Software Version Histories](https://www.ntu.edu.sg/home/yi_li/posts/paper-accepted-by-tse/)   
Yi Li, Chenguang Zhu, Julia Rubin and Marsha Chechik   
In Transactions on Software Engineering (TSE), 44(2):182–201

[Precise Semantic History Slicing through Dynamic Delta Refinement](https://www.ntu.edu.sg/home/yi_li/posts/paper-accepted-by-ase-2016/)   
Yi Li, Chenguang Zhu, Julia Rubin and Marsha Chechik   
In Proceedings of the 31st IEEE/ACM International Conference on Automated Software Engineering (ASE 2016)

[Semantic Slicing of Software Version Histories](https://www.ntu.edu.sg/home/yi_li/posts/paper-accepted-by-ase-2015/)  
Yi Li, Julia Rubin and Marsha Chechik  
In Proceedings of the 30th IEEE/ACM International Conference on
Automated Software Engineering (ASE 2015)

[A Dataset for Dynamic Discovery of Semantic Changes in Version Controlled Software Histories](https://www.ntu.edu.sg/home/yi_li/posts/paper-accepted-by-msr-2017/)   
Chenguang Zhu, Yi Li, Julia Rubin and Marsha Chechik   
In Proceedings of the 14th International Conference on Mining Software Repositories (MSR 2017)

[CSlicerCloud: A Web-Based Semantic History Slicing Framework](https://www.ntu.edu.sg/home/yi_li/posts/paper-accepted-by-icse-2018/)   
Yi Li, Chenguang Zhu, Julia Rubin, and Marsha Chechik   
In Proceedings of 40th International Conference on Software Engineering (ICSE 2018)

[FHistorian: Locating Features in Version Histories](ntu.edu.sg/home/yi_li/posts/paper-accepted-by-splc-2017/)    
Yi Li, Chenguang Zhu, Julia Rubin and Marsha Chechik    
In Proceedings of the 21st International Systems and Software Product Line Conference (SPLC 2017)
