class A 
{
    int x;

    private int h()
    {
        x = 0;
        return x;
    }
}

class B 
{
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
        int z = 1;
	z = false ? h() : B.f(v) + 1;
        return z;
    }
    
    public int h()
    {
        return 0;
    }
}
