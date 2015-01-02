package org.curiosity.crawl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.util.Conversions;
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
    // the latest sol for which any camera has images
    // hardcoded as pages with invalid sols still 200, and we don't want to hammer the site
    // we should be able to calculate or scrape for it though
    private final int maxSol = 854;
    private final String selector = "div.RawImageUTC";
    private final int imageListRequestTimeout = 10000; // milliseconds
    private final String spoofUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";

    public List<Image> crawl(int sol, Camera camera) {
        String imageListUri = generateImageListUri(sol, camera);
        Document document = getImageList(imageListUri);
        return parseImages(document, camera);
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
            throw new RuntimeException(e);
        }
    }

    private List<Image> parseImages(Document document, Camera camera) {
        Elements captions = document.select(selector);

        // use set for dedup
        Set<Image> images = captions.stream().map(info -> {

            String imageSrc = info.select("a").attr("href");
            // remove -thm if present so we get the full resolution image
            URL imageUrl = Conversions.fromString(imageSrc);

            String utcTime = info.text().replace("UTC Full Resolution", "").trim();
            Date timestamp = Conversions.fromUTC(utcTime);

            return new Image(imageUrl, timestamp, camera);
        }).collect(Collectors.toSet());

        return ImmutableList.copyOf(images);
    }
}
