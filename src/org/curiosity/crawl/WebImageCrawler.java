package org.curiosity.crawl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.util.Conversions;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jherwitz
 */
public class WebImageCrawler {

    // TODO: load the following statics from configuration
    private final String root = "mars.jpl.nasa.gov/msl/multimedia/raw/";
    private final Map<Camera, String> cameraSuffixes = ImmutableMap.of(Camera.FrontHazcam, "FHAZ_",
                                                                       Camera.RearHazcam, "RHAZ_",
                                                                       Camera.LeftNavcam, "NAV_LEFT_",
                                                                       Camera.RightNavcam, "NAV_RIGHT_",
                                                                       Camera.Mastcam, "MAST_");
    private final String selector = "div.RawImageUTC";
    private final int imageListRequestTimeout = 10000; // milliseconds
    private final long backoffTime = 10000; //TODO: exponential backoff
    private final String spoofUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";

    public List<Image> crawl(int sol, Camera camera) {
        String imageListUri = generateImageListUri(sol, camera);
        Document document = getImageList(imageListUri);
        return parseImages(document, sol, camera);
    }

    private String generateImageListUri(int sol, Camera camera) {
        return String.format("http://%s?s=%d&camera=%s", root, sol, cameraSuffixes.get(camera));
    }

    private Document getImageList(String uri) {
        try {
            // TODO: execute network request in a separate thread pool
            return Jsoup.connect(uri)
                        .timeout(imageListRequestTimeout)
                        .header("User-Agent", spoofUserAgent)
                        .get();
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

    private List<Image> parseImages(Document document, int sol, Camera camera) {
        Elements captions = document.select(selector);

        // use set for dedup
        Set<Image> images = captions.stream().map(info -> {

            String imageSrc = info.select("a").attr("href");
            // remove -thm if present so we get the full resolution image
            URL imageUrl = Conversions.fromString(imageSrc);

            String utcTime = info.text().replace("UTC Full Resolution", "").trim();
            Date timestamp = Conversions.fromUTC(utcTime);

            return new Image(imageUrl, timestamp, sol, camera);
        }).collect(Collectors.toSet());

        return ImmutableList.copyOf(images);
    }
}
