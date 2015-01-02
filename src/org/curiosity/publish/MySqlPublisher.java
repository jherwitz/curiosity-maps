package org.curiosity.publish;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringEscapeUtils;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author jherwitz
 */
public class MySqlPublisher implements Publisher {

    private final Connection conn;
    private final Map<Camera, String> tableNamesByCamera;
    private final String databaseName;

    public MySqlPublisher(Connection conn, Map<Camera, String> tableNamesByCamera, String databaseName) {
        this.conn = Preconditions.checkNotNull(conn);
        this.tableNamesByCamera = Preconditions.checkNotNull(tableNamesByCamera);
        this.databaseName = Preconditions.checkNotNull(databaseName);
    }

    @Override
    public void publish(List<Image> images) {
        Multimap<Camera, Image> imagesByCamera = HashMultimap.create();
        images.forEach(image -> imagesByCamera.put(image.origin(), image));
        imagesByCamera.keySet().parallelStream().forEach(camera -> {
            String tableName = tableNamesByCamera.get(camera);
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

        builder.append(String.format("INSERT INTO %s.%s (timestamp, imageUrl, sol)", databaseName, tableNamesByCamera.get(camera))).append("\n");
        builder.append("VALUES ").append(Joiner.on(",").join(tuples)).append("\n");
        builder.append("ON DUPLICATE KEY UPDATE").append("\n");
        builder.append("timestamp = VALUES(timestamp)").append(",\n");
        builder.append("imageUrl = VALUES(imageUrl)").append(",\n");
        builder.append("sol = VALUES(sol)").append(";\n");

        return builder.toString();
    }

    private void put(String sql, String table) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql);
        try {
            int modifiedRows = statement.executeUpdate();
            System.out.println("Updated " + modifiedRows + "rows in table " + table);
        } finally {
            statement.close();
        }
    }

}
