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
    set :gmapsKey, ARGV[3]
    set :sentinel, DatabaseSentinel.new(jdbc, user, pass)
end

get '/' do
    erb :index, :locals => {:key => settings.gmapsKey}
end

get '/display/:sol/:camera' do
    sol = params[:sol].to_i
    camera = params[:camera]

    logger.info "Querying database for sol #{sol} and camera #{camera}"
    images = settings.sentinel.images(sol, camera)
    logger.info "retrieved #{images.length} images"

    erb :display, :locals => {:images => images}
end
