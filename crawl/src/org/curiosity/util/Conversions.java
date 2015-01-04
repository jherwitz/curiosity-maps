package org.curiosity.util;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author jherwitz
 */
public class Conversions {

    private Conversions() { }

    public static URL fromString(String imageSrc) {
        try {
            return new URL(Preconditions.checkNotNull(imageSrc));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Date fromUTC(String utc) {
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return utcFormat.parse(utc);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
