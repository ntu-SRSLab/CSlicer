import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestExample
{
	@Test
	public void test()
	{
            A a = new A();
            assertEquals(3, a.f());
        }
}
