package com.atypon.utility;

import org.junit.Test;

import static org.junit.Assert.*;

public class HashTest {

    @Test
    public void hashTest() {
        assertNotEquals("", Hash.hash("Hello"));
        assertNotEquals("", Hash.hash(""));
        assertNotEquals("", Hash.hash("What"));
        assertNotEquals("", Hash.hash(" "));
        assertNotEquals("", Hash.hash("this is nothing"));
    }

    @Test
    public void hashAndCombineTest() {
        String a = "hello";
        String b = "there";
        assertNotEquals(Hash.hash(a, b), Hash.hash(b, a));
        assertEquals(Hash.hash(a, b, b, a), Hash.hash(a, b, b, a));
        String c = "hell";
        String d = "othere";
        assertNotEquals(Hash.hash(a, b), Hash.hash(c, d));
        assertNotEquals(Hash.hash(a, b), Hash.hash(a + b));
        assertNotEquals(Hash.hash(c, d), Hash.hash(a + b));
        assertEquals(Hash.hash(a + b), Hash.hash(a + b));
    }


}