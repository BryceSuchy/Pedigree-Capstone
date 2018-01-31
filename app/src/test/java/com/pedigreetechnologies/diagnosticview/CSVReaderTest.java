package com.pedigreetechnologies.diagnosticview;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Joe on 2/19/2017.
 */

public class CSVReaderTest {
    @Test
    public void readIgnoreHeader_Test() throws UnsupportedEncodingException {
        String testCSVString = "data_type,parameter_id,start_byte,start_bit,bit_length,endianness,resolution,offset,units,type" +
                "\n0,65248,4,0,32,0,0.125,0,km,Odometer" +
                "\n0,65217,0,0,32,0,0.005,0,km,Odometer";

        ArrayList<String[]> expected = new ArrayList<>();
        expected.add("0,65248,4,0,32,0,0.125,0,km,Odometer".split(","));
        expected.add("0,65217,0,0,32,0,0.005,0,km,Odometer".split(","));

        InputStream stream = new ByteArrayInputStream(testCSVString.getBytes("UTF-8"));
        InputStreamReader streamReader = new InputStreamReader(stream);
        ArrayList<String[]> actual = CSVReader.readIgnoreHeader(streamReader);
        assertEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void read_Test() throws UnsupportedEncodingException {
        String testCSVString = "data_type,parameter_id,start_byte,start_bit,bit_length,endianness,resolution,offset,units,type" +
                "\n0,65248,4,0,32,0,0.125,0,km,Odometer" +
                "\n0,65217,0,0,32,0,0.005,0,km,Odometer";

        ArrayList<String[]> expected = new ArrayList<>();
        expected.add("data_type,parameter_id,start_byte,start_bit,bit_length,endianness,resolution,offset,units,type".split(","));
        expected.add("0,65248,4,0,32,0,0.125,0,km,Odometer".split(","));
        expected.add("0,65217,0,0,32,0,0.005,0,km,Odometer".split(","));

        InputStream stream = new ByteArrayInputStream(testCSVString.getBytes("UTF-8"));
        InputStreamReader streamReader = new InputStreamReader(stream);
        ArrayList<String[]> actual = CSVReader.read(streamReader);
        assertEquals(expected.toArray(), actual.toArray());
    }
}
