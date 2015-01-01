#!/usr/bin/ruby

#
# The webserver controller.
#

require 'rubygems'
require 'sinatra'
require './classes/db-sentinel'

# run at startup
configure do 
    jdbc = ARGV[0]
    user = ARGV[1]
    pass = ARGV[2]
    set :sentinel, DatabaseSentinel.new(jdbc, user, pass)
end

# the main map view
get '/' do
    logger.info "Querying database for rover location data"
    locations = settings.sentinel.locations()
    logger.info "Retrieved locations for #{locations.length} martian days"

    logger.info "Querying database for camera mask"
    cameraCoverage = Hash.new
    settings.sentinel.cameraNames.keys.each do |camera|
        cameraCoverage.store(camera, settings.sentinel.coverage(camera))
    end
    logger.info "Mask constructed"

    erb :map, :locals => {:locations => locations, :cameraCoverage => cameraCoverage}
end

# the about page
get '/about' do
    erb :about
end

# the image slider - usually iframed
get '/images/:sol/:camera' do
    sol = params[:sol].to_i
    camera = params[:camera]

    logger.info "Querying database for sol #{sol} and camera #{camera}"
    images = settings.sentinel.images(sol, camera)
    if images.length == 0
        status 404
    end
    logger.info "Retrieved #{images.length} images"

    erb :images, :locals => {:images => images}
end

# before each request set cache-control header
before do
  expires 0, :public, :must_revalidate
end
