class A 
{
    int x;

    public int h()
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
	int z = h();
	
        return z;
    }

    public int h()
    {
        return 1;
    }
}
