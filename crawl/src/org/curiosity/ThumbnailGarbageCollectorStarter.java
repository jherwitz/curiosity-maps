package org.curiosity;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.curiosity.concept.Camera;
import org.curiosity.crawl.HeaderCrawler;
import org.curiosity.management.ThumbnailGarbageCollector;
import org.curiosity.util.DatabaseInvariants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Starter class for {@link ThumbnailGarbageCollector}.
 *
 * @author jherwitz
 */
public class ThumbnailGarbageCollectorStarter {

    public static void main(String[] args) throws Throwable {
        CommandLineParser cli = new BasicParser();
        Options options = options();

        CommandLine cmd = cli.parse(options, args);

        int startSol = Integer.parseInt(cmd.getOptionValue("s"));
        Set<Camera>  cameras = Arrays.asList(cmd.getOptionValue("c").split(",")).stream().map(Camera::valueOf).collect(Collectors.toSet());

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

        ThumbnailGarbageCollector gc = new ThumbnailGarbageCollector(HeaderCrawler.Instance, conn);
        cameras.parallelStream().forEach(camera -> gc.sweep(startSol, camera));
    }

    private static Options options() {
        Options options = new Options();

        options.addOption("s", "startSol", true, "Sol to start GC at");
        options.addOption("c", "cameras", true, "The cameras to gc for");

        // the following options are applicable to
        options.addOption("user", true, "Database username.");
        options.addOption("pass", true, "Database password.");
        options.addOption("jdbc", true, "JDBC connection url for the database");

        return options;
    }
}
