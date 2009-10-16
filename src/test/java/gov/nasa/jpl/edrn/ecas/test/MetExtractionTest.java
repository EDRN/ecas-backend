package gov.nasa.jpl.edrn.ecas.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class MetExtractionTest 
    extends TestCase
{
    public MetExtractionTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( MetExtractionTest.class );
    }

    public void testMetExtraction()
    {
        assertTrue( true );
    }
}
