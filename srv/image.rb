require 'json'

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
end
