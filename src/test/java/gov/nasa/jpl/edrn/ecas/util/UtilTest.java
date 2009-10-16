package gov.nasa.jpl.edrn.ecas.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilTest 
    extends TestCase
{

	public UtilTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( UtilTest.class );
    }

    public void testUtil()
    {
        assertTrue( true );
    }
}
