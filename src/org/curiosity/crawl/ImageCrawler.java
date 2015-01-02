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
     * Crawl images from the specified {@link Camera} and sol, returning a deduplicated
     * {@link List} of all images found.
     */
    List<Image> crawl(int sol, Camera camera);
}
