package org.curiosity.concept;

import com.google.common.base.Preconditions;

/**
 * An HTTP response header.
 *
 * @author jherwitz
 */
public class ResponseHeader {

    private final long contentLength;
    private final String etag;

    public ResponseHeader(long contentLength, String etag) {
        this.contentLength = contentLength;
        this.etag = Preconditions.checkNotNull(etag);
    }

    public long contentLength() {
        return contentLength;
    }

    public String etag() {
        return etag;
    }

    @Override
    public String toString() {
        return String.format("ResponseHeader [contentLength=%s, etag=%s", contentLength, etag);
    }
}
