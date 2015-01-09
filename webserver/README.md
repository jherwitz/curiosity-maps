# Webserver 

This directory encapsualtes the webserver component of Curiosity Maps, which is repsonsible for rendering 
processed image and location data. The webserver is built on the [Sinatra](http://www.sinatrarb.com/) stack
and makes use of the [Google Maps](https://developers.google.com/maps/documentation/javascript/) and 
[Slick](http://kenwheeler.github.io/slick/) libraries for rendering.

## Usage

This section assumes that database has been populated as detailed in `crawl/README.md`.

First install ruby and the necessary gems - sinatra and mysql2 are required. 

Start the server by running main.rb:

`ruby main.rb <JDBC> <DB_USER> <DB_PASS> -p <PORT_NUMBER> -e production`

* XXX: Remember to include the `-e production` Sinatra argument if you want to accept external requests! *

Practical server invocation (port 80, prevents hang, persists logs):

`sudo nohup ruby main.rb <JDBC> <DB_USER> <DB_PASS> -p 80 -e production >> ~/logs/sinatra.log 2>&1 &`

## Resources

- [Ruby MySql connector](https://rubygems.org/gems/mysql)
- [Google Maps polylines](https://developers.google.com/maps/documentation/javascript/examples/polyline-simple)
- [Custom Google Map type](https://developers.google.com/maps/documentation/javascript/examples/maptype-image)
- [Google Maps Marker API](https://developers.google.com/maps/documentation/javascript/reference#Marker)
- [USGS MSL Landing Site](http://astrogeology.usgs.gov/maps/mars-science-laboratory-landing-site-selection)
- [USGS Mars FTP listing](http://webgis.wr.usgs.gov/pigwad/down/mars_dl.htm)
- [Embedded Ruby on Wikipedia](http://en.wikipedia.org/wiki/ERuby)
