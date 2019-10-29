import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestExample {
    @Test
    void test () {
	C c = new C();
	assertEquals(c.g(), 0);
    }
}
