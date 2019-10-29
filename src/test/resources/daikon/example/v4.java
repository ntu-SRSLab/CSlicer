class A 
{
    int x;

    public int h()
    {
        x = 1;
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
        z = false ? z : B.f(v) + 1;
        return z;
    }

    public int h()
    {
        return 0;
    }
}
