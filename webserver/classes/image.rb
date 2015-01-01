require 'json'

#
# Image represents an image taken by Curiosity, using 'camera' on 'sol' at 'timestamp'.
#
class Image
    def initialize(imageUrl, timestamp, sol, camera) 
        @imageUrl = imageUrl
        @timestamp = timestamp
        @sol = sol
        @camera = camera
    end

    def to_json(*a)
        return {
            "imageUrl" => @imageUrl,
            "timestamp" => @timestamp,
            "sol" => @sol,
            "camera" => @camera
        }.to_json
    end

    def imageUrl 
        return @imageUrl
    end

    def timestamp
        return @timestamp
    end

    def sol
        return @sol
    end

    def camera
        return @camera
    end
end
