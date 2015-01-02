package org.curiosity;

import com.google.common.collect.ImmutableList;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.crawl.WebImageCrawler;
import org.curiosity.publish.Publisher;
import org.curiosity.publish.SysOutPublisher;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Starter class for Curiosity rover image crawling.
 *
 * @see #usage()
 * @author jherwitz
 */
public class CuriosityImageCrawlerStarter {

    public static void main(String[] args) {
        WebImageCrawler crawler = new WebImageCrawler();
        Publisher publisher = SysOutPublisher.Instance;

        Set<Camera> cameras = Arrays.asList(args).stream().map(Camera::valueOf).collect(Collectors.toSet());

        ImmutableList.Builder<Image> images = ImmutableList.builder();
        cameras.parallelStream().forEach(camera -> images.addAll(crawler.crawl(0, camera)));

        publisher.publish(images.build());
    }

    private String usage() {
        StringBuilder builder = new StringBuilder();

        return builder.toString();
    }
}
