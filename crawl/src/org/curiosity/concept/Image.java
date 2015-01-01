package org.curiosity.concept;

import com.google.common.base.Preconditions;

import java.net.URL;
import java.util.Date;

/**
 * An image taken by one of Curiosity's cameras.
 *
 * @author jherwitz
 */
public class Image {

    private final URL imageUrl;
    private final Date timestamp;
    private final int sol;
    private final Camera origin;

    public Image(URL imageUrl, Date timestamp, int sol, Camera origin) {
        this.imageUrl = Preconditions.checkNotNull(imageUrl);
        this.timestamp = Preconditions.checkNotNull(timestamp);
        this.sol = sol;
        this.origin = Preconditions.checkNotNull(origin);
    }

    public URL imageUrl() {
        return imageUrl;
    }

    public Date timestamp() {
        return timestamp;
    }

    public Camera origin() {
        return origin;
    }

    public int sol() {
        return sol;
    }

    @Override
    public String toString() {
        return String.format("Image [url=%s, timestamp=%s, origin=%s]", imageUrl, timestamp, origin);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;

        Image that = (Image) obj;
        if(!imageUrl.toExternalForm().equals(that.imageUrl.toExternalForm())) return false;
        if(!timestamp.equals(that.timestamp)) return false;
        if(!origin.equals(that.origin)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + imageUrl.toExternalForm().hashCode();
        result = prime * result + timestamp.hashCode();
        result = prime * result + origin.hashCode();
        return result;
    }
}
