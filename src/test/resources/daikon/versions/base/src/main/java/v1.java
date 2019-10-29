class A 
{
    int x;
    int h()
    {
        return B.f(x);
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
        int z = (new B()).y + 1;
        return z;
    }
}
