package org.curiosity.crawl;

import com.google.common.collect.ImmutableList;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.util.Conversions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@link ImageCrawler} pulls free use images from NASA's Curiosity image gallery.
 *
 * @see WebCrawler
 * @author jherwitz
 */
public class ImageCrawler extends WebCrawler {

    protected final String selector = "div.RawImageUTC";

    /**
     * Crawls NASA images for {@code sol} and {@code camera}.
     */
    public List<Image> crawl(int sol, Camera camera) {
        String imageGalleryUri = generateImageGalleryUri(sol, camera);
        Document document = httpGet(Jsoup.connect(imageGalleryUri)
                                         .timeout(imageListRequestTimeout)
                                         .header("User-Agent", spoofUserAgent));
        return parseImages(document, sol, camera);
    }

    /**
     * Generate the NASA image list url for the given {@code sol} and {@code camera}.
     */
    private String generateImageGalleryUri(int sol, Camera camera) {
        return String.format("http://%s?s=%d&camera=%s", root, sol, cameraSuffixes.get(camera));
    }

    /**
     * Parse the images contained in {@link Document}.
     */
    private List<Image> parseImages(Document document, int sol, Camera camera) {
        Elements captions = document.select(selector);

        // use set for dedup
        Set<Image> images = captions.stream().map(info -> {

            String imageSrc = info.select("a").attr("href");
            URL imageUrl = Conversions.fromString(imageSrc);

            String utcTime = info.text().replace("UTC Full Resolution", "").trim();
            Date timestamp = Conversions.fromUTC(utcTime);

            return new Image(imageUrl, timestamp, sol, camera);
        }).collect(Collectors.toSet());

        return ImmutableList.copyOf(images);
    }
}
