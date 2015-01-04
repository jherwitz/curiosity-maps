#!/usr/bin/ruby

require 'rubygems'
require 'sinatra'
require './db-sentinel'

# run at startup
configure do 
    jdbc = ARGV[0]
    user = ARGV[1]
    pass = ARGV[2]
    set :apikey, ARGV[3] # gmaps api key (although a clientside api key is sort-of pointless...)
    set :sentinel, DatabaseSentinel.new(jdbc, user, pass)
end

get '/' do
    # TODO: make this call once at startup, or peroidically
    logger.info "Querying database for rover location data"
    locations = settings.sentinel.locations()
    logger.info "Retrieved locations for #{locations.length} martian days"

    erb :map, :locals => {:apikey => settings.apikey, :locations => locations}
end

get '/images/:sol/:camera' do
    sol = params[:sol].to_i
    camera = params[:camera]

    logger.info "Querying database for sol #{sol} and camera #{camera}"
    images = settings.sentinel.images(sol, camera)
    logger.info "Retrieved #{images.length} images"

    erb :display, :locals => {:images => images}
end
