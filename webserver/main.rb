#!/usr/bin/ruby

require 'rubygems'
require 'sinatra'
require './classes/db-sentinel'

# run at startup
configure do 
    jdbc = ARGV[0]
    user = ARGV[1]
    pass = ARGV[2]
    set :apikey, ARGV[3] # gmaps api key (although a clientside api key is sort-of pointless...)
    set :sentinel, DatabaseSentinel.new(jdbc, user, pass)
end

get '/' do
    logger.info "Querying database for rover location data"
    locations = settings.sentinel.locations()
    logger.info "Retrieved locations for #{locations.length} martian days"

    logger.info "Querying database for camera mask"
    cameraCoverage = Hash.new
    settings.sentinel.tableNames.keys.each do |camera|
        cameraCoverage.store(camera, settings.sentinel.coverage(camera))
    end
    logger.info "Mask constructed"

    erb :map, :locals => {:apikey => settings.apikey, :locations => locations, :cameraCoverage => cameraCoverage}
end

get '/about' do
    erb :about
end

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
