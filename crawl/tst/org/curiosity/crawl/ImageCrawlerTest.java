package org.curiosity.crawl;

import org.curiosity.concept.Camera;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests {@link ImageCrawler}.
 *
 * @author jherwitz
 */
public class ImageCrawlerTest {

    @Ignore
    @Test
    public void testCrawlAgainstRealWebsite() {
        int sol = 853;
        Camera camera = Camera.RightNavcam;
        ImageCrawler crawler = new ImageCrawler();
        System.out.println(crawler.crawl(sol, camera));
    }

}