package org.curiosity.util;


import org.junit.Test;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests {@link Conversions}.
 *
 * @author jherwitz
 */
public class ConversionsTest {

    @Test
    public void testUTCDateConversion() {
        String utcTime = "2014-12-31 11:24:16";
        long epochTime = 1420025056000L; // milliseconds

        Date time = Conversions.fromUTC(utcTime);
        assertEquals(epochTime, time.getTime());
    }
}
