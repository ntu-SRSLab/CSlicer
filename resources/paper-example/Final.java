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
