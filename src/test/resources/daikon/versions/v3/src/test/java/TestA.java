import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestA
{
	@Test
	public void test()
	{
            C c = new C();
            int result = c.g();
            assertEquals(1, result);
        }
}
