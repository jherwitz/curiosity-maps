package org.curiosity.publish;

import org.curiosity.concept.Image;
import org.curiosity.concept.RoverLocation;

import java.util.List;

/**
 * Publishes location or image data.
 *
 * @author jherwitz
 */
public interface Publisher {

    /**
     * Publishes a list of {@link Image}s to the database.
     */
    void publishImages(List<Image> images);

    /**
     * Publishes a list of {@link RoverLocation}s to the database.
     */
    void publishLocations(List<RoverLocation> locations);
}
