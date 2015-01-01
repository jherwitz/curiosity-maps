package org.curiosity.concept;

import com.google.common.base.Preconditions;

import java.net.URL;
import java.util.Date;

/**
 * An image taken by one of Curiosity's cameras. Timestamped. Immutable.
 *
 * @author jherwitz
 */
public class Image {

    private final Date timestamp;
    private final URL imageUrl;
    private final Camera origin;

    public Image(Date timestamp, URL imageUrl, Camera origin) {
        this.timestamp = Preconditions.checkNotNull(timestamp);
        this.imageUrl = Preconditions.checkNotNull(imageUrl);
        this.origin = Preconditions.checkNotNull(origin);
    }

    public Date timestamp() {
        return timestamp;
    }

    public URL imageUrl() {
        return imageUrl;
    }

    public Camera origin() {
        return origin;
    }

    @Override
    public String toString() {
        return String.format("Image [timestamp=%s, url=%s, origin=%s]", timestamp, imageUrl, origin);
    }

    @Override
    public boolean
}
