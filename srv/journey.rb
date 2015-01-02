#!/usr/bin/ruby

require 'rubygems'
require 'sinatra'
require './db-sentinel'

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
    images = settings.sentinel.images(:sol, :camera)
    erb :display, :locals => {:images => images}
end
