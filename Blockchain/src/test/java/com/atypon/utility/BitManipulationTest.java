package com.atypon.utility;

import org.junit.Test;

import static com.atypon.utility.BitManipulation.*;
import static org.junit.Assert.*;

public class BitManipulationTest {

    @Test
    public void switchingTest() {
        String s1 = "e1486c57ab9d320f";
        String s2 = byteArrayToString(stringToByteArray(s1));
        assertEquals(s1, s2);
    }

    @Test
    public void getFirstBitsTest() {
        String s = getFirstBits(11, "f3a5");
        assertEquals(s, "11110011101");
    }


    @Test
    public void getLeadingZerosTest() {
        StringBuilder zeros = new StringBuilder();
        for (int i = 0; i <= 256; ++i) {
            assertEquals(BitManipulation.getLeadingZeros(i), zeros.toString());
            zeros.append('0');
        }
    }
}