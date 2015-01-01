package org.curiosity.crawl;

import com.google.common.collect.ImmutableMap;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A {@link WebCrawler} is responsible for crawling Curiosity data on NASA's website.
 *
 * This mainly exists to be a container for {@link ImageCrawler} configuration (should use configuration file instead).
 *
 * @author jherwitz
 */
public abstract class WebCrawler extends Crawler {

    protected final String root = "mars.jpl.nasa.gov/msl/multimedia/raw/";
    protected final Map<Camera, String> cameraSuffixes = ImmutableMap.of(Camera.FrontHazcam, "FHAZ_",
                                                                         Camera.RearHazcam, "RHAZ_",
                                                                         Camera.LeftNavcam, "NAV_LEFT_",
                                                                         Camera.RightNavcam, "NAV_RIGHT_",
                                                                         Camera.Mastcam, "MAST_");
    protected final int imageListRequestTimeout = 10000; // milliseconds
    protected final long backoffTime = 10000; //TODO: exponential backoff
    protected final String spoofUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";

    protected Document httpGet(Connection conn) {
        try {
            // TODO: execute network request in a separate thread pool
            return conn.get();
        } catch (IOException e) {
            if(e instanceof HttpStatusException) {
                HttpStatusException statusException = (HttpStatusException) e;
                // sleep for backoffTime if we're asked to slow down
                if(statusException.getStatusCode() == 503) {
                    try {
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
            throw new RuntimeException(e);
        }
    }
}
