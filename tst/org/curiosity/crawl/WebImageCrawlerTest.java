package org.curiosity.crawl;

import org.curiosity.concept.Camera;
import org.junit.Ignore;
import org.junit.Test;

public class WebImageCrawlerTest {

    @Ignore
    @Test
    public void testCrawlAgainstRealWebsite() {
        int sol = 853;
        Camera camera = Camera.RightNavcam;
        WebImageCrawler crawler = new WebImageCrawler();
        System.out.println(crawler.crawl(sol, camera));
    }

}