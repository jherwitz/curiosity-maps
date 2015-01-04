require 'json'

class RoverLocation
    def initialize(sol, x, y, z) 
        @sol = sol
        @lat, @lng = convertCartesianOffsetToLatLng(x, y, z)
    end

    def to_json(*a)
        return {
            "sol" => @sol,
            "lat" => @lat,
            "lng" => @lng
        }.to_json
    end

    def sol 
        return @sol
    end

    def lat
        return @lat
    end

    def lng
        return @lng
    end

    # based on the coordinate / linear distance ratio described in
    # http://astrogeology.usgs.gov/maps/mars-science-laboratory-landing-site-selection
    # we use the rough estimation of 1km ~ 0.2236º for calulcating rover offsets on this space.
    #
    # XXX: need to improve this projection method
    def convertCartesianOffsetToLatLng(x, y, z)
        ratio = 0.2236
        return x * ratio, y * ratio
    end
end
