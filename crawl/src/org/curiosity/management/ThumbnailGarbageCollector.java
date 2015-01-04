package org.curiosity.management;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.curiosity.concept.Camera;
import org.curiosity.concept.ResponseHeader;
import org.curiosity.crawl.HeaderCrawler;
import org.curiosity.util.DatabaseInvariants;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author jherwitz
 */
public class ThumbnailGarbageCollector {

    private final static long fullSizeLength = 10000;

    private final HeaderCrawler crawler;
    private final Connection conn;

    public ThumbnailGarbageCollector(HeaderCrawler crawler, Connection conn) {
        this.crawler = Preconditions.checkNotNull(crawler);
        this.conn = Preconditions.checkNotNull(conn);
    }

    public void collect(int startSol, Camera camera) {
        /**
         * First: Pull list of urls to test.
         */
        String sql = String.format("SELECT imageUrl FROM %s.%s WHERE sol >= %d",
                                   DatabaseInvariants.databaseName(),
                                   DatabaseInvariants.imageTableName(camera),
                                   startSol);
        List<URL> uris;
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            uris = parseResultSet(resultSet);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        /**
         * Second: Crawl each uri.
         */
        uris.stream().forEach(uri -> {
            ResponseHeader header = crawler.crawl(uri);
            if (header.contentLength() < fullSizeLength) {
                System.out.printf("Removing url %s: content length %s < %s (threshold) \n", uri.toExternalForm(), header.contentLength(), fullSizeLength);
                try {
                    removeFromDb(camera, uri);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private List<URL> parseResultSet(ResultSet resultSet) throws SQLException {
        ImmutableList.Builder<URL> builder = ImmutableList.builder();

        resultSet.first();
        while (!resultSet.isAfterLast()) {
            builder.add(resultSet.getURL("imageUrl"));
            resultSet.next();
        }

        return builder.build();
    }

    private void removeFromDb(Camera camera, URL uri) throws SQLException {
        String sql = String.format("DELETE FROM %s.%s where imageUrl = \"%s\"",
                                   DatabaseInvariants.databaseName(),
                                   DatabaseInvariants.imageTableName(camera),
                                   uri.toExternalForm());

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.execute();
        conn.commit();
        statement.close();
    }
}
