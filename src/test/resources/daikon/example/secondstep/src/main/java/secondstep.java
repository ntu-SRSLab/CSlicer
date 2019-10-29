class A 
{
    int x;
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
	z = false ? z : B.f(v) + 1;
        return z;
    }
}
