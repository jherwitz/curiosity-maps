package org.curiosity.publish;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.curiosity.concept.Camera;
import org.curiosity.concept.Image;
import org.curiosity.concept.RoverLocation;

import java.util.List;

/**
 * A {@link Publisher} that publishes images to system out.
 *
 * @author jherwitz
 */
public class SysOutPublisher implements Publisher {

    public static final SysOutPublisher Instance = new SysOutPublisher();

    private SysOutPublisher() { }

    @Override
    public void publishImages(List<Image> images) {
        Multimap<Camera, Image> imagesByCamera = HashMultimap.create();
        images.forEach(image -> imagesByCamera.put(image.origin(), image));
        imagesByCamera.keySet().forEach(cameraType -> {
            System.out.println("Images for camera " + cameraType.name() + ":");
            System.out.println(imagesByCamera.get(cameraType));
        });
    }

    @Override // TODO: implement
    public void publishLocations(List<RoverLocation> locations) {
        throw new UnsupportedOperationException();
    }
}
