package org.curiosity.crawl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final String captionSelector = "div.RawImageCaption";

    public List<Image> crawl(int sol, Camera camera) {
        String imageListUri = String.format("http://%s?s=%d&camera=%s", root, sol, camera);
        Document document = get(imageListUri);
        return parse(document);
    }

    private Document get() {

    }

    private List<Image> parse(Document document) {
        Elements captions = document.select(captionSelector);
        // use set for dedup
        Set<Image> images = captions.stream().map(caption -> {

        });
        return ImmutableList.copyOf(images);
    }
}
