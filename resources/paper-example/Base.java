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
