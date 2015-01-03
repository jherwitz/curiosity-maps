package org.curiosity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.curiosity.concept.Camera;
import org.curiosity.concept.RoverLocation;
import org.curiosity.publish.MySqlPublisher;
import org.curiosity.publish.PublisherType;
import org.curiosity.util.DatabaseInvariants;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jherwitz
 */
public class RoverLocationPublisherStarter {

    public static void main(String[] args) throws Throwable {
        CommandLineParser cli = new BasicParser();
        Options options = options();

        CommandLine cmd = cli.parse(options, args);

        String filepath = Preconditions.checkNotNull(cmd.getOptionValue("f"));

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
        MySqlPublisher publisher = new MySqlPublisher(conn);

        List<RoverLocation> locations = Files.readAllLines(Paths.get(filepath)).stream().map(line -> {
            String[] parts = line.split(",");
            int sol = Integer.parseInt(parts[0].trim());
            String timestamp = parts[1].trim();
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[2].trim());
            return new RoverLocation(sol, x, y, z, timestamp);
        }).collect(Collectors.toList());
        
        publisher.publishLocations(ImmutableList.copyOf(locations));
    }

    private static Options options() {
        Options options = new Options();

        options.addOption("f", "file", true, "The csv file to upload locations from");

        // the following options are applicable to
        options.addOption("user", true, "Database username.");
        options.addOption("pass", true, "Database password.");
        options.addOption("jdbc", true, "JDBC connection url for the database");

        return options;
    }
}
