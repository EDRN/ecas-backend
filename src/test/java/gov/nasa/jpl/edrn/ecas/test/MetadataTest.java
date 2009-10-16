package gov.nasa.jpl.edrn.ecas.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class MetadataTest 
    extends TestCase
{
    public MetadataTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( MetadataTest.class );
    }

    public void testMetadata()
    {
        assertTrue( true );
    }
}
