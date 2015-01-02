#!/usr/bin/ruby

require 'rubygems'
require 'sinatra'
require './db-sentinel'
require 'json'

# run at startup
configure do 
    jdbc = ARGV[0]
    user = ARGV[1]
    pass = ARGV[2]
    dbname = ARGV[4]
    set :sentinel, DatabaseSentinel.new(jdbc, user, pass, dbname)
end

get '/display/index.html' do
    erb :index
end

get '/display/:sol/:camera' do
    sol = params[:sol].to_i
    camera = params[:camera]

    logger.info "Querying database for sol #{sol} and camera #{camera}"
    images = settings.sentinel.images(sol, camera)
    logger.info "retrieved #{images.length} images"

    erb :display, :locals => {:images => images.to_json}
end
