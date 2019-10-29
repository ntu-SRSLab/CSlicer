# Benchmarks
This directory contains benchmarks from 3 open source projects including Apache commons-io, Apache commons-collections and Apache commons-math.  

>**Functionality Description of Each Benchmark Example:**

>+ **commons-io**
     - **I1**: Byte array output stream.
     - **I2**: Identifies broken symlink files.
     - **I3**: File name utilities.
>+ **commons-collections**
     - **C1**: Index function in iterable utilities.
     - **C2**: Union function in set utilities.
>+ **commons-math**
     - **M1**: Error conditions in continuous output field model.
     - **M2**: Construct median with specific estimation type.
     - **M3**: Large samples in polynomial fitter.
  
***
# Experiment
We did a bunch of experiments on these benchmarks. The results of each configuration are shown in the following tables.  
   
>**Columns in Experimental Data Tables:**  

>+ **Subject ID**: The id of a benchmark example. Their definitions can be found in above Benchmarks section.
>+ **⎮H⎮**: The size of analyzed history. (measurement: number of commits.)
>+ **⎮T⎮**: The size of test suite. (measurement: number of junit test methods.)
>+ **⎮H\*⎮**: The size of the smallest semantic history slice the tool found, where this slice must preserve the functionality. (measurement: number of commits.)
>+ **Test Runs**: The number of test runs required to get the 1-minimal semantic history slice.
>+ **End Point**: The hash code of the last commit of the analyzed history.
>+ **Total time**: The time spent on running Definer on a benchmark example. (measurement: second)  
***  
## Experiment 1: Precision
The first experiment is to compare our tool Definer with previous technique CSlicer in terms of precision of semantic history slicing.
### Running Definer with *DEFAULT* configuration:  
To show the best performance of Definer, we run it with *DEFAULT* configuration, which enables both change siginificance learning and compilation failure prediction.  
The command of running *DEFAULT* configuration is: `gitslice -c /path/config_file_name -e refiner -l default`. The result is shown in Table 1.    
##### Table 1. Running Definer with *DEFAULT* configuration  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |     28    | 89608628  |  1273.601  |
|      I2    |   50  |   1   |  3   |     25    | 82eefc3d  |   365.986  |
|      I3    |  100  |  37   |  2   |     89    | 63cbfe70  |   847.533  |
|      C1    |  100  |  1    |   12 |     28    | 90509ce8  |    601.939 |
|      C2    |  100  |  1    |  5   |     32    | 9314193c  |  788.105   |
|      M1    |  50   |  1    | 13   |      7    | 6e4265d6  |  163.774   |
|      M2    |  50   |  1    |  2   |      5    | afff37e0  |  111.897   |
|      M3    |  100  |  1    |  2   |     35    | b07ecae3  |  634.395   |
  
### Running CSlicer:
We ran CSlicer with its default configuration. The result is shown in Table 2.  
##### Table 2. Running CSlicer with its default configuration  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |   30 |     1     | 89608628  |     34.49  |
|      I2    |   50  |   1   |    4 |     1     | 82eefc3d  |     34.2   |
|      I3    |  100  |  37   |   12 |     1     | 63cbfe70  |     35.37  |
|      C1    |  100  |  1    |   25 |     1     | 90509ce8  |     53.96  |
|      C2    |  100  |  1    |   31 |     1     | 9314193c  |     55.82  |
|      M1    |  50   |  1    |   16 |     1     | 6e4265d6  |    65.63   |
|      M2    |  50   |  1    |    2 |     1     | afff37e0  |    65.22   |
|      M3    |  100  |  1    |    7 |     1     | b07ecae3  |     87.52  |

***
## Experiment 2: Effectiveness
To evaluate the effectiveness of using change siginificance learning and compilation failure prediction, in this experiment, we ran Definer with configuration 
*DEFAULT*, *LEARNING*, and *BASIC* separately.
### Running Definer with *DEFAULT* configuration:
The result of running Definer with *DEFAULT* configuration is shown in Table 3.  
##### Table 3. Running Definer with *DEFAULT* configuration  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |     28    | 89608628  |  1273.601  |
|      I2    |   50  |   1   |  3   |     25    | 82eefc3d  |   365.986  |
|      I3    |  100  |  37   |  2   |     89    | 63cbfe70  |   847.533  |
|      C1    |  100  |  1    |   12 |     28    | 90509ce8  |    601.939 |
|      C2    |  100  |  1    |  5   |     32    | 9314193c  |  788.105   |
|      M1    |  50   |  1    | 13   |      7    | 6e4265d6  |  163.774   |
|      M2    |  50   |  1    |  2   |      5    | afff37e0  |  111.897   |
|      M3    |  100  |  1    |  2   |     35    | b07ecae3  |  634.395   |
  
### Running Definer with *LEARNING* configuration:
The *LEARNING* configuration disables the compilation failure prediction. With this configuration, Definer only applies change significance learning in 
speeding up the delta refinement loop. 
The command of running *LEARNING* configuration is: `gitslice -c /path/config_file_name -e refiner -l nocomp`. The experimental result is shown in Table 4.  
##### Table 4. Running Definer with *LEARNING* configuration  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |     168   | 89608628  |  2574.06   |
|      I2    |   50  |   1   |  3   |     25    | 82eefc3d  |   380.417  |
|      I3    |  100  |  37   |  2   |     89    | 63cbfe70  |   1404.475 |
|      C1    |  100  |  1    |   12 |    172    | 90509ce8  |   2842.378 |
|      C2    |  100  |  1    |  5   |     57    | 9314193c  |   793.287  |
|      M1    |  50   |  1    | 13   |    28     | 6e4265d6  |    542.63  |
|      M2    |  50   |  1    |  2   |     7     | afff37e0  |   193.443  |
|      M3    |  100  |  1    |  2   |    35     | b07ecae3  |   668.285  |
  
### Running Definer with *BASIC* configuration:
The *BASIC* configuration turns off both compilation failure prediction and significance learning. Definer uses random partition to split the minimal semantic 
slice at each round of the delta refinement loop.  
The command of running *BASIC* configuration is: `gitslice -c /path/config_file_name -e refiner -l nocomp,nolearn`. The result is shown in Table 5.    
##### Table 5. Running Definer with *BASIC* configuration  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |    286    | 89608628  |   4680.607 |
|      I2    |   50  |   1   |  3   |     39    | 82eefc3d  |    485.639 |
|      I3    |  100  |  37   |  2   |     18    | 63cbfe70  |    339.035 |
|      C1    |  100  |  1    |   12 |    174    | 90509ce8  |   2484.511 |
|      C2    |  100  |  1    |  5   |     74    | 9314193c  |    989.765 |
|      M1    |  50   |  1    | 13   |    126    | 6e4265d6  |   1618.433 |
|      M2    |  50   |  1    |  2   |     20    | afff37e0  |    282.642 |
|      M3    |  100  |  1    |  2   |     29    | b07ecae3  |    930.839 |

***
## Experiment 3: Partition Scheme
In this experiment, we aim to compare the performance of Definer under 3 different partition schemes -- *NEG*, *NONPOS*, *LOW-3*, and their combination, *COMBINED*. 
To avoid the influence of compilation failure prediction on the effectiveness, we firstly disabled it then did the following experiments.  
### Running Definer with *NEG* scheme:
The *NEG* scheme is the most conservative one among the three schemes. With this scheme, Definer only reverts commits that have negative scores in each round of refinement.  
The command of running *NEG* scheme is: `gitslice -c /path/config_file_name -e refiner -l nocomp,neg`. The result is in Table 6.    
##### Table 6. Running Definer with *NEG* Scheme  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |     258   | 89608628  | 4706.249   |
|      I2    |   50  |   1   |  3   |      33   | 82eefc3d  |  584.725   |
|      I3    |  100  |  37   |  2   |      89   | 63cbfe70  |   1542.91  |
|      C1    |  100  |  1    |   12 |     176   | 90509ce8  |   2713.616 |
|      C2    |  100  |  1    |  5   |      72   | 9314193c  |   1162.191 |
|      M1    |  50   |  1    | 13   |      27   | 6e4265d6  |   524.433  |
|      M2    |  50   |  1    |  2   |       7   | afff37e0  |   181.932  |
|      M3    |  100  |  1    |  2   |      31   | b07ecae3  |   677.573  |
  
### Running Definer with *NONPOS* scheme:
*NONPOS* is the most aggressive scheme which reverts all commits with non-positive scores in each round.  
The command of running *NONPOS* scheme is: `gitslice -c /path/config_file_name -e refiner -l nocomp,nonpos`. The result is shown in Table 7.    
##### Table 7. Running Definer with *NONPOS* Scheme  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |     258   | 89608628  |  5064.251  |
|      I2    |   50  |   1   |  3   |     33    | 82eefc3d  |  641.037   |
|      I3    |  100  |  37   |  2   |     60    | 63cbfe70  |  1152.841  |
|      C1    |  100  |  1    |   12 |    176    | 90509ce8  |  3254.925  |
|      C2    |  100  |  1    |  5   |     72    | 9314193c  |  1078.997  |
|      M1    |  50   |  1    | 13   |     32    | 6e4265d6  |  565.258   |
|      M2    |  50   |  1    |  2   |      8    | afff37e0  |  170.612   |
|      M3    |  100  |  1    |  2   |     23    | b07ecae3  |  476.473   |
  
### Running DEFINER with *LOW-3* scheme:
The *LOW-3* partition scheme always reverts the lowest 1/3 of the commits according to their siginificance ranking in each round.  
The command of running *LOW-3* scheme is: `gitslice -c /path/config_file_name -e refiner -l nocomp,low3`. The result is shown in Table 8.  
##### Table 8. Running Definer with *LOW-3* Scheme  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |    168    | 89608628  |  2361.5    |
|      I2    |   50  |   1   |  3   |     25    | 82eefc3d  |  295.978   |
|      I3    |  100  |  37   |  2   |     89    | 63cbfe70  |  1428.607  |
|      C1    |  100  |  1    |   12 |    176    | 90509ce8  |  2619.478  |
|      C2    |  100  |  1    |  5   |     57    | 9314193c  |   914.455  |
|      M1    |  50   |  1    | 13   |     36    | 6e4265d6  |  694.164   |
|      M2    |  50   |  1    |  2   |     11    | afff37e0  |  258.207   |
|      M3    |  100  |  1    |  2   |     35    | b07ecae3  |  682.394   |
  
### Running *DEFINER* with *COMBINED* scheme:
*COMBINED* is the combination of the previous 3 schemes. With this scheme, in each round, Definer makes a greedy choice by picking one out of the 3 schemes 
in which it can get the smallest subset of history at current step. The result is shown in Table 9. In our experiments, the *COMBINED* scheme achieved the 
best overall performance. The command of running *COMBINED* configuration is: `gitslice -c /path/config_file_name -e refiner -l nocomp`.  
##### Table 9. Running Definer with *COMBINED* Scheme  
| Subject ID | ⎮H⎮   | ⎮T⎮   | ⎮H\*⎮| Test Runs | End Point | Total Time |
|:----------:|------:|------:|-----:|----------:|----------:|-----------:|
|      I1    |  150  |    3  |  28  |     168   | 89608628  |  2574.06   |
|      I2    |   50  |   1   |  3   |     25    | 82eefc3d  |   380.417  |
|      I3    |  100  |  37   |  2   |     89    | 63cbfe70  |   1404.475 |
|      C1    |  100  |  1    |   12 |    172    | 90509ce8  |   2842.378 |
|      C2    |  100  |  1    |  5   |     57    | 9314193c  |   793.287  |
|      M1    |  50   |  1    | 13   |    28     | 6e4265d6  |    542.63  |
|      M2    |  50   |  1    |  2   |     7     | afff37e0  |   193.443  |
|      M3    |  100  |  1    |  2   |    35     | b07ecae3  |   668.285  |