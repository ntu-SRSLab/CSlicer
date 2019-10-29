class A 
{
    int x;
    int h()
    {
	....
        return 1;
    }
    // hunk deps
    private int g()
    { 
        return(new B()).y;
    }
}

class B 
{
    int y = 0;
    static int f(int x)
    { 
        return x - 1;
    }
}

class C extends A
{
    int v = 0;
    public int g()
    {
	int z;
	z = A.h();
	z = B.f(v) + 1;
        return z;
    }
}

class Test {
    @Test
    void test () {
	C c = new C();
	assertEquals(c.g(), 1);
    }
}
