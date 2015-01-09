package org.curiosity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.curiosity.concept.RoverLocation;
import org.curiosity.crawl.LocationCrawler;
import org.curiosity.publish.MySqlPublisher;
import org.curiosity.util.DatabaseInvariants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Backup for cspice locations - pulls locations from the NASA locations.xml file.
 *
 * XXX: The NASA locations.xml data is currently BROKEN - we'll use cspice instead. (1/8/2015)
 *
 * @see http://mars.jpl.nasa.gov/msl-raw-images/locations.xml
 * @author jherwitz
 */
public class RoverLocationCrawlerStarter {

    public static void main(String[] args) throws Throwable {
        CommandLineParser cli = new BasicParser();
        Options options = options();

        CommandLine cmd = cli.parse(options, args);

        String username = Preconditions.checkNotNull(cmd.getOptionValue("user"));
        String password = Preconditions.checkNotNull(cmd.getOptionValue("pass"));
        String jdbc = Preconditions.checkNotNull(cmd.getOptionValue("jdbc"));

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

        LocationCrawler crawler = new LocationCrawler();
        MySqlPublisher publisher = new MySqlPublisher(conn);

        List<RoverLocation> locations = crawler.crawl();
        publisher.publishLocations(ImmutableList.copyOf(locations));
    }

    private static Options options() {
        Options options = new Options();

        // the following options are applicable to
        options.addOption("user", true, "Database username.");
        options.addOption("pass", true, "Database password.");
        options.addOption("jdbc", true, "JDBC connection url for the database");

        return options;
    }
}
