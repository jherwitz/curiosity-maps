require 'mysql2'
require 'date'
require './classes/rover-location'
require './classes/image'

#
# The DatabaseSentinel is responsible for interfacing with the curiosity maps mysql database.
#
# All queries are read-only.
#
class DatabaseSentinel

    def initialize(host, user, pass) 
        @client = Mysql2::Client.new(:host => host, :username => user, :password => pass, :reconnect => true)
        
        # hash for table name to prevent sql injection
        @cameraTableNames = {
            "FrontHazcam" => 1,
            "LeftNavcam" => 1,
            "Mastcam" => 1,
            "RearHazcam" => 1,
            "RightNavcam" => 1
        }

        # TODO: cache ttl
        @cache = {}
    end

    # returns the names of all supported cameras
    def cameraNames()
        return @cameraTableNames
    end

    # all rover locations we have data for
    def locations() 
        query = "SELECT * FROM images.Location"
        unless (value = @cache[query]).nil?
            return value
        end

        results = @client.query(query)

        locations = Array.new
        results.each do |row|
            sol = row["sol"].to_i
            lat = row["lat"].to_f
            lng = row["lng"].to_f
            locations.push(RoverLocation.new(sol, lat, lng))
        end

        @cache[query] = locations
        return locations
    end

    # returns a mapping of all the sols for which we have images for 'camera'.
    def coverage(camera) 
        if @cameraTableNames[camera].nil?
            return []
        end

        query = "SELECT sol FROM images.#{camera} GROUP BY sol"
        unless (value = @cache[query]).nil?
            return value
        end

        results = @client.query(query)

        coverage = Hash.new
        results.each do |row|
            sol = row["sol"].to_i
            coverage.store(sol, 1)
        end

        @cache[query] = coverage
        return coverage
    end

    # returns the images taken by 'camera' on 'sol'
    def images(sol, camera)
        if @cameraTableNames[camera].nil?
            return []
        end     

        query = "SELECT * FROM images.#{camera} WHERE sol = #{sol} ORDER BY timestamp"
        unless (value = @cache[query]).nil?
            return value
        end
        results = @client.query(query)

        images = Array.new
        results.each do |row|
            # remove milliseconds - ruby expects seconds
            epochTime = row["timestamp"].to_i / 1000
            date = DateTime.strptime(epochTime.to_s,'%s')
            images.push(Image.new(row["imageUrl"], date, row["sol"], camera))
        end

        @cache[query] = images
        return images
    end

end
