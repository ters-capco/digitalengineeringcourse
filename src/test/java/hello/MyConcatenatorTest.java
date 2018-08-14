package hello;

import org.testng.Assert;
import org.testng.annotations.Test;


@Test
public class MyConcatenatorTest {
    @Test(groups = { "functest" })
    public void testConcatenate() throws Exception {
        String concat = MyConcatenator.concatanate("one", "two", "three");
        Assert.assertEquals("one,two,three", concat);
    }

}