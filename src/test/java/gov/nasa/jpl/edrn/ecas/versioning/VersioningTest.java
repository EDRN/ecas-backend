package gov.nasa.jpl.edrn.ecas.versioning;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class VersioningTest 
    extends TestCase
{
    public VersioningTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( VersioningTest.class );
    }

    public void testApp()
    {
        assertTrue( true );
    }
}
