package org.curiosity.crawl;

import org.curiosity.concept.ResponseHeader;

import javax.xml.ws.ProtocolException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A {@link HeaderCrawler} that returns the {@link ResponseHeader} for given uris.
 *
 * {@link HeaderCrawler} is NOT a Web crawler, as it only deals with http headers.
 *
 * @author jherwitz
 */
public class HeaderCrawler extends Crawler {

    public static HeaderCrawler Instance = new HeaderCrawler();

    private HeaderCrawler() { }

    /**
     * Pulls the {@link ResponseHeader} for {@code uri}.
     */
    public ResponseHeader crawl(URL uri) {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("HEAD");
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Got unexpected response code: " + conn.getResponseCode());
            }
            return new ResponseHeader(conn.getContentLengthLong(), conn.getHeaderField("ETag"));
        } catch (IOException | ProtocolException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }
}
