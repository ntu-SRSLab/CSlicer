# Partition Example
This example is to illustrate our enhanced history parition scheme for finding minimal semantic slices. It is the Example 2 in our paper.

Firstly we show two versions of a Java program: **Base** and **Final**, with the target test.
  
### Base:
  
```java
class A {
    int f() {
        B b = new B();
        return b.g();
    }
}

class B {
    int x = 1;
    String s = null;

    int g() {
        int z = h(s, x);
        return z;
    }
    
    int h(String v, int t) {
        return v == null ? 0 : t;
    }
}
```
  
### Final:
  
```java
class A {
    int f() {
        B b = new B();
        b.n();
        return b.g();
    }
}

class B {
    int x = 2;
    int y = 2;
    String s = "abc";

    int g() {
        int z = h(s, x);
        z = lib(*) ? z : m();
        return z;
    }
    
    int h(String v, int t) {
        return v != null ? 0 : t;
    }

    int m() { return ++ y; }
    
    void n() { s = null; }
}
```
  
### Target Test:
  
```java
public class TestExample
{
	@Test
	public void test()
	{
            A a = new A();
            assertEquals(3, a.f());
        }
}
```

The differences between the two versions can be broken down to a series of *atomic changes* defined over the abstract syntax trees (ASTs) of the program -- *insertions* (INS), *deletions* (DEL), or *updates* (UPD).  
  
In particular, there are 8 atomic changes between the **Base** and the **Final** versions:  

> **(1) An update to the field** ```B.x```  

##### Base:
```java
int x = 1;
``` 
##### Final:
```java
int x = 2;
```

> **(2) An insertion of a new field** ```y``` **into to the class** ```B```  

> **(3) An update to the field** ```B.s```  

##### Base:
```java
String s = null;
``` 
##### Final:
```java
String s = "abc";
```

> **(4) An update to the method** ```B.g()```**, which adds an additional statement** ```z = lib(*) ? z : m();```**, conditionally assigning the returned value of** ```m()``` **to the local variable** ```z```**.**

##### Base:
```java
int g() {
        int z = h(s, x);
        return z;
    }
``` 
##### Final:
```java
int g() {
        int z = h(s, x);
        z = lib(*) ? z : m();
        return z;
    }
```

> **(5) An update to the method** ```B.h()```**, which replaces** ```==``` **by** ```!=```**.**

##### Base:
```java
int h(String v, int t) {
        return v == null ? 0 : t;
    }
``` 
##### Final:
```java
int h(String v, int t) {
        return v != null ? 0 : t;
    }
```

> **(6) An insertion of a new method** ```m()``` **into class** ```B```**.**

> **(7) An insertion of a new method** ```n()``` **into class** ```B```**.** 

> **(8) An update to the method** ```A.f()```**, which adds the call of** ```B.n()``` **before return.**

##### Base:
```java
int f() {
        B b = new B();
        return b.g();
    }
``` 
##### Final:
```java
int f() {
        B b = new B();
        b.n();
        return b.g();
    }
```

For each atomic change, we tried reverting it, saw the test outcomes and summarized significance signals learned from it. The result is shown in the table below.  
>**Columns in the Table:**  

>+ **Step**: The step of partition.
>+ **H-**: The set of changes reverted.
>+ **H+**: The set of changes kept.
>+ **I**: The initial set of invariants for the final version of the program.
>+ **I'**: The invariants after changes are reverted.
>+ **T(H+)**: The test outcomes. '-' denotes that some compilation failures were predicted, no need to run test; 'FAIL' denotes that the target test suite was executed but the result was failed; 'PASS' denotes that the test suite was executed successfully and the test passed. 
>+ **Signals**: The significance signals learned from each case. '->low' denotes that the significance level of the atomic change needs to be turned lower while '->high' denotes that the significance level of the atomic change needs to be turned higher.  

| Step       | H-              |H+                      | I\I'  | T(H+)| Signals |
|:----------:|:----------------|:------------------------|:-----:|:----:|:-------:|
|      s1     |  (1), (2), (3), (4) | (5), (6), (7), (8)         |    -  |  -   |     -   |
|      s1     |  (5), (6), (7), (8) | (1), (2), (3), (4)         |    -  |  -   |     -   |
|      s2     |  (1), (2)         | (3), (4), (5), (6), (7), (8) |    -  |  -   |     -   |
|      s2     |  (3), (4)         | (1), (2), (5), (6), (7), (8) |    ```B::s != null```,  ```B.h()::return == 0```,  ```B.y one of {2,3}```,  ```B.g()::return == 3```,  ```B.m()::return == 3```,  ```A.f()::return == 3```  |  FAIL|     -   |
|      s2     |  (5), (6)         | (1), (2), (3), (4), (7), (8) |    -  |  -   |     -   |
|      s2     |  (7), (8)         | (1), (2), (3), (4), (5), (6) |   ```B.s == null```,  ```B.h()::return == 2```   |  PASS   |     (3)->low,  (5)->low,  (7)->low,  (8)->low  |
|      s3     |  (3), (5)         | (1), (2), (4), (6)         |    ```B::s != null```,  ```B.h()::return == 0```  |  PASS   | (3)->low,  (5)->low  |
|      s4     |  (1), (2)         | (4), (6)                 |    -  |  -   |     -   |
|      s4     |  (4), (6)         | (1), (2)                 |  ```B.g()::return == 3```,  ```B::y one of {2, 3}```,  ```B.m()::return == 3```, ```A.f()::return == 3```    |  FAIL   |     -   |
|      s5     |  (1)             | (2), (4), (6)             |  ```B::x == 2``` | PASS | (1)->low  |
|      s6     |  (2)             | (4), (6)                 |  - | - | - |
|      s6     |  (4)             | (2), (6)                 |  ```B.g()::return == 3```,  ```B::y one of {2, 3}```,  ```B.m()::return == 3```, ```A.f()::return == 3```  | FAIL | (2)->high,  (4)->high,  (6)->high  |
|      s6     |  (6)             | (2), (4)                 | - | - | - |
