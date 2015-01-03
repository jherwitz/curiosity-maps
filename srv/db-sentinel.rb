require 'mysql'
require 'date'
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
