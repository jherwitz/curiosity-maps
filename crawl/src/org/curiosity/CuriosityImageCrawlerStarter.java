package org.curiosity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.*;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.crawl.ImageCrawler;
import org.curiosity.publish.MySqlPublisher;
import org.curiosity.publish.Publisher;
import org.curiosity.publish.PublisherType;
import org.curiosity.publish.SysOutPublisher;
import org.curiosity.util.DatabaseInvariants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Starter class for Curiosity rover image crawling.
 *
 * @author jherwitz
 */
public class CuriosityImageCrawlerStarter {

    // the latest sol for which any camera has images
    // hardcoded as pages with invalid sols still 200, and we don't want to hammer the site
    // we should be able to calculate or scrape for it though
    private static final int maxSol = 854;


    public static void main(String[] args) throws Throwable {
        CommandLineParser cli = new BasicParser();
        Options options = options();

        CommandLine cmd = cli.parse(options, args);

        if (cmd.hasOption("h")) {
            System.out.println("----------------------------------------------");
            System.out.println("Curiosity image crawler options:\n");
            System.out.println(options);
            System.out.println("----------------------------------------------");
            System.exit(0);
        }

        SolDelta solDelta = null;
        if (cmd.hasOption("s")) {
            String[] parts = cmd.getOptionValue("s").split(",");
            if (parts.length != 2) {
                fail(options.getOption("s"));
            }

            int startSol = Integer.parseInt(parts[0]);
            int endSol = Integer.parseInt(parts[1]);

            solDelta = new SolDelta(startSol, endSol);
        } else {
            fail(options.getOption("s"));
        }

        Set<Camera> cameras = null;
        if (cmd.hasOption("c")) {
            if (cmd.getOptionValues("c").length > 1) {
                fail(options.getOption("c"));
            }

            String commafied = cmd.getOptionValue("c");
            cameras = Arrays.asList(commafied.split(",")).stream().map(Camera::valueOf).collect(Collectors.toSet());
        } else {
            fail(options.getOption("c"));
        }

        Publisher publisher = null;
        if (cmd.hasOption("p")) {
            if (cmd.getOptionValues("p").length > 1) {
                fail(options.getOption("p"));
            }

            PublisherType publisherType = PublisherType.valueOf(cmd.getOptionValue("p"));
            switch (publisherType) {
                case SysOut:
                    publisher = SysOutPublisher.Instance;
                    break;
                case MySql:
                    if (!cmd.hasOption("user") || !cmd.hasOption("pass") || !cmd.hasOption("jdbc")) {
                        System.err.println("Database values (user, pass, jdbc) not set");
                        fail(options);
                    }
                    String username = cmd.getOptionValue("user");
                    String password = cmd.getOptionValue("pass");
                    String jdbc = cmd.getOptionValue("jdbc");

                    Connection conn = DatabaseInvariants.newConnection(username, password, jdbc);
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    publisher = new MySqlPublisher(conn);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized publisher type: " + publisherType);
            }

        } else {
            fail(options.getOption("c"));
        }

        start(solDelta, cameras, publisher);
    }

    private static Options options() {
        Options options = new Options();

        options.addOption("s",
                          "sols",
                          true,
                          "The interval of sols to pull images for. Syntax: \" -s <START_SOL>,<END_SOL> \". Must be less than " + maxSol + ".");
        options.addOption("c",
                          "cameras",
                          true,
                          "Space-less commafied list of cameras to pull images for. Possible values: " + Arrays.toString(Camera.values()));
        options.addOption("p", "publisher", true, "The publisher to report pulled images to. Possible values: " + Arrays.toString(PublisherType.values()));
        options.addOption("h", "help", false, "Print help text. Supersedes other options.");

        // the following options are applicable to
        options.addOption("user", true, "Database username.");
        options.addOption("pass", true, "Database password.");
        options.addOption("jdbc", true, "JDBC connection url for the database");

        return options;
    }

    private static void fail(Option o) {
        System.err.println("Required option not properly specified: " + o);
        System.exit(1);
    }

    private static void fail(Options o) {
        System.err.println("Required options not properly specified: " + o);
        System.exit(1);
    }

    private static void start(SolDelta solDelta, Set<Camera> cameras, Publisher publisher) {
        Preconditions.checkNotNull(solDelta, "solDelta not null");
        Preconditions.checkArgument(cameras != null && !cameras.isEmpty(), "cameras not null or empty");
        Preconditions.checkNotNull(publisher, "publisher not null");

        ImageCrawler crawler = new ImageCrawler();

        System.out.println("Starting image crawl...");

        ImmutableList.Builder<Image> images = ImmutableList.builder();
        cameras.parallelStream()
               .forEach(camera -> IntStream.range(solDelta.startSol(), solDelta.endSol())
                                           .forEach(sol -> {
                                               System.out.printf("Crawling sol:%d camera:%s\n", sol, camera);
                                               try {
                                                   publisher.publishImages(crawler.crawl(sol, camera));
                                               } catch (Throwable t) {
                                                   t.printStackTrace();
                                               }
                                           }));

        System.out.println("Crawl completed!");
    }

    private static class SolDelta {
        private final int startSol;
        private final int endSol;

        public SolDelta(int startSol, int endSol) {
            if (startSol > endSol || startSol < 0 || endSol < 0 || startSol > maxSol || endSol > maxSol) {
                throw new IllegalArgumentException(String.format(
                        "Sol values invalid: startSol %d: endSol: %d. Must satisfy startSol <= endSol, where startSol >- 0 and endSol >= 0",
                        startSol,
                        endSol));
            }
            this.startSol = startSol;
            this.endSol = endSol;
        }

        public int startSol() {
            return startSol;
        }

        public int endSol() {
            return endSol;
        }
    }
}
