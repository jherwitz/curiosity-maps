package org.curiosity.publish;

import org.curiosity.concept.Image;

import java.util.List;

/**
 *
 * @author jherwitz
 */
public interface Publisher {

    /**
     * Publishes a list of images to the durable store.
     */
    public void publish(List<Image> images);
}
