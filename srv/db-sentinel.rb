require 'mysql'
require 'date'
require './rover-location'
require './image'

# knows how to query tables
# TODO: caching
class DatabaseSentinel

    def initialize(host, user, pass) 
        @client = Mysql.new(host, user, pass)
        # query hash for table name to prevent sql injection
        @tableNames = {
            "FrontHazcam" => 1,
            "LeftNavcam" => 1,
            "Mastcam" => 1,
            "RearHazcam" => 1,
            "RightNavcam" => 1
        }
    end

    def positions()
        return []
    end

    def tableNames()
        return @tableNames
    end

    # all rover locations we have data for
    def locations() 
        results = @client.query("SELECT * FROM images.Location")

        locations = Array.new
        results.each_hash do |row|
            sol = row["sol"].to_i
            x = row["x"].to_f
            y = row["y"].to_f
            z = row["z"].to_f
            locations.push(RoverLocation.new(sol, x, y, z))
        end

        return locations
    end

    def lookupTable(camera) 
        if @tableNames[camera].nil?
            return []
        end

        results = @client.query("SELECT sol FROM images.#{camera} GROUP BY sol")

        lookupTable = Hash.new
        results.each_hash do |row|
            sol = row["sol"].to_i
            lookupTable.store(sol, 1)
        end

        return lookupTable
    end

    # images taken by camera on sol
    def images(sol, camera)
        if @tableNames[camera].nil?
            return []
        end     

        results = @client.query("SELECT * FROM images.#{camera} WHERE sol = #{sol} ORDER BY timestamp")

        images = Array.new
        results.each_hash do |row|
            # remove milliseconds - ruby expects seconds
            epochTime = row["timestamp"].to_i / 1000
            date = DateTime.strptime(epochTime.to_s,'%s')
            images.push(Image.new(row["imageUrl"], date, row["sol"], camera))
        end

        return images
    end

end
