package org.curiosity.publish;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringEscapeUtils;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.concept.RoverLocation;
import org.curiosity.util.DatabaseInvariants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jherwitz
 */
public class MySqlPublisher implements Publisher {

    private final Connection conn;

    public MySqlPublisher(Connection conn) {
        this.conn = Preconditions.checkNotNull(conn);
    }

    @Override
    public void publishImages(List<Image> images) {
        Multimap<Camera, Image> imagesByCamera = HashMultimap.create();
        images.forEach(image -> imagesByCamera.put(image.origin(), image));
        imagesByCamera.keySet().parallelStream().forEach(camera -> {
            String tableName = DatabaseInvariants.imageTableName(camera);
            String sql = sqlFor(imagesByCamera.get(camera), camera);
            try {
                put(sql, tableName);
            } catch (SQLException e) {
                System.err.println("Error encountered while putting to " + tableName + ".\n" + Arrays.toString(e.getStackTrace()));
            }
        });


    }

    private String sqlFor(Collection<Image> images, Camera camera) {
        // generate tuples to update
        List<String> tuples = images.stream().map(image -> {
            if (image.origin() != camera) {
                System.err.println(String.format("Unexpected camera type. Expected:%s Got:%s Image:%s", camera, image.origin(), image));
                return null;
            }

            return String.format("('%s', '%s', '%s')",
                                 image.timestamp().getTime(),
                                 StringEscapeUtils.escapeSql(image.imageUrl().toExternalForm()),
                                 image.sol());
        }).filter(p -> p != null).collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();

        builder.append(String.format(
                "INSERT INTO %s.%s (timestamp, imageUrl, sol)", DatabaseInvariants.databaseName(), DatabaseInvariants.imageTableName(camera)))
               .append("\n");
        builder.append("VALUES ").append(Joiner.on(",").join(tuples)).append("\n");
        builder.append("ON DUPLICATE KEY UPDATE").append("\n");
        builder.append("timestamp = VALUES(timestamp)").append(",\n");
        builder.append("imageUrl = VALUES(imageUrl)").append(",\n");
        builder.append("sol = VALUES(sol)").append(";\n");

        return builder.toString();
    }

    @Override
    public void publishLocations(List<RoverLocation> locations) {
        String sql = sqlFor(locations);
        try {
            put(sql, DatabaseInvariants.locationTableName());
        } catch (SQLException e) {
            System.err.println("Error encountered while putting to " + DatabaseInvariants.locationTableName() + ".\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    private String sqlFor(List<RoverLocation> locations) {
        List<String> tuples = locations.stream()
                                       .map(location -> String.format("('%s', '%s', '%s', '%s')",
                                                                      location.sol(),
                                                                      location.latitude(),
                                                                      location.longitude(),
                                                                      location.timestamp()))
                                       .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();

        builder.append(String.format(
                "INSERT INTO %s.%s (sol, lat, lng, timestamp)", DatabaseInvariants.databaseName(), DatabaseInvariants.locationTableName()))
               .append("\n");
        builder.append("VALUES ").append(Joiner.on(",").join(tuples)).append("\n");
        builder.append("ON DUPLICATE KEY UPDATE").append("\n");
        builder.append("sol = VALUES(sol)").append(",\n");
        builder.append("lat = VALUES(lat)").append(",\n");
        builder.append("lng = VALUES(lng)").append(",\n");
        builder.append("timestamp = VALUES(timestamp)").append(";\n");

        return builder.toString();
    }

    private void put(String sql, String table) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql);
        try {
            int modifiedRows = statement.executeUpdate();
            System.out.println("Updated " + modifiedRows + "rows in table " + table);
            conn.commit();
        } finally {
            statement.close();
        }
    }

}
