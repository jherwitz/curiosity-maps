require 'json'

#
# RoverLocation represents a location of the Curiosity rover at a given sol. 
#
class RoverLocation

    #
    # Construct a new RoverLocation from Cartesian coordinates.
    #
    def initialize(sol, lat, lng) 
        @sol = sol
        @lat = lat
        @lng = lng
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
end
