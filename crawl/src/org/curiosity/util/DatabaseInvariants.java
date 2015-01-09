package org.curiosity.util;

import com.google.common.collect.ImmutableMap;
import org.curiosity.concept.Camera;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * Static values and methods related to interfacing with the database.
 *
 * @author jherwitz
 */
public class DatabaseInvariants {

    private DatabaseInvariants() { }

    private static final Map<Camera, String> tableNamesByCamera = ImmutableMap.of(Camera.FrontHazcam, Camera.FrontHazcam.name(),
                                                                                  Camera.RearHazcam, Camera.RearHazcam.name(),
                                                                                  Camera.LeftNavcam, Camera.LeftNavcam.name(),
                                                                                  Camera.RightNavcam, Camera.RightNavcam.name(),
                                                                                  Camera.Mastcam, Camera.Mastcam.name());
    private static final String databaseName = "images";
    private static final String locationTableName = "Location";

    public static String imageTableName(Camera camera) {
        return tableNamesByCamera.get(camera);
    }

    public static String locationTableName() {
        return locationTableName;
    }

    public static String databaseName() {
        return databaseName;
    }

    public static Connection newConnection(String username, String password, String jdbc) {
        try {
            Connection conn = DriverManager.getConnection(jdbc, username, password);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
