package gov.nasa.jpl.edrn.ecas.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BackendTest 
    extends TestCase
{

	public BackendTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( BackendTest.class );
    }

    public void testBackendApp()
    {
    	assertTrue( true );
    }
}
