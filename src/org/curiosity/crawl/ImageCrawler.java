package org.curiosity.crawl;

import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;

import java.util.List;

/**
 * A {@link ImageCrawler} is responsible for crawling Curiosity's images.
 *
 * @author jherwitz
 */
public interface ImageCrawler {

    /**
     * Crawl images from the specified {@link Camera}, returning a {@link List} of all crawled images.
     */
    List<Image> crawl(Camera camera);
}
