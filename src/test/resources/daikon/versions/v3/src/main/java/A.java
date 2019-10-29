class A 
{
    int x;
    int h()
    {
        return B.f(x);
    }
    // hunk deps
    int g()
    { 
        return(new B()).y;
    }
}

class B 
{
    int y = 1;
    static int f(int x)
    { 
        return x - 1;
    }
}

class C extends A
{
    int g()
    {
        int z = (new B()).y + 1;
        return z;
    }
}
