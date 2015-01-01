package org.curiosity;

import com.google.common.collect.Iterables;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.crawl.WebImageCrawler;
import org.curiosity.publish.Publisher;
import org.curiosity.publish.SysOutPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jherwitz
 */
public class CuriosityImageCrawlerStarter {

    /**
     * @param args list of camera to crawl
     */
    public static void main(String[] args) {
        WebImageCrawler crawler = new WebImageCrawler();
        Publisher publisher = SysOutPublisher.Instance;

        Set<Camera> cameras = Arrays.asList(args).stream().map(Camera::valueOf).collect(Collectors.toSet());

        List<Image> images = Iterables.concat(cameras.parallelStream().map(crawler::crawl).collect(Collectors.toList()));

        publisher.publish(images);
    }
}
