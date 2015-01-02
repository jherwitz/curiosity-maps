require 'mysql'

# knows how to query tables
# TODO: caching
class DatabaseSentinel

    def initialize(host, user, pass, dbname) 
        @client = Mysql.new(host, user, pass, dbname)
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
        
        results = @client.query("SELECT * FROM images.#{camera} where sol = #{sol}")

        images = Array.new
        results.each_hash do |row|
            images.push(image.initialize(row["imageUrl"], row["timestamp"], row["sol"], camera))
        end

        return images
    end

end
