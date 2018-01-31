package com.pedigreetechnologies.diagnosticview;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DiagnosticParameterTest {
    @Test(expected = DiagnosticParameter.CSVParseError.class)
    public void createObject_InvalidShortCSV() throws Exception {
        DiagnosticParameter parameter = DiagnosticParameter.createObject("0,65248,4,0,32,0".split(","));
    }

    @Test(expected = DiagnosticParameter.CSVParseError.class)
    public void createObject_InvalidLongCSV() throws Exception {
        DiagnosticParameter parameter = DiagnosticParameter.createObject("0,65265,0,1,0,16,0,65535,0.002485484,0,0,150,mph,Speed,Speed (Engine), 7, 28, 9".split(","));
    }

    @Test
    public void createObject_Valid() throws Exception {
        DiagnosticParameter expected = new DiagnosticParameter(0, 65265, 0, 1, 0, 16, false, 65535, 0.002485484, 0, 0, 150, "mph", "Speed", "Speed (Engine)");
        DiagnosticParameter actual = DiagnosticParameter.createObject("0,65265,0,1,0,16,0,65535,0.002485484,0,0,150,mph,Speed,Speed (Engine)".split(","));

        assertEquals(expected, actual);
    }

    @Test
    public void createObject_Valid2() throws Exception {
        DiagnosticParameter expected = new DiagnosticParameter(2, 1, 0, 36, 0, 32, true, 4294967295L, 0.000264172D, 0, 0, 20, "gallons/h", "Fuel Rate", "Fuel Rate");
        DiagnosticParameter actual = DiagnosticParameter.createObject("2,1,0,36,0,32,1,4294967295,0.000264172,0,0,20,gallons/h,Fuel Rate,Fuel Rate".split(","));

        assertEquals(expected, actual);
    }
}