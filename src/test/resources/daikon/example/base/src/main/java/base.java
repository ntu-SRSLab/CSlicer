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
    public int g()
    {
	int z = 1;
        return z;
    }
}
