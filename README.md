This repository contains code powering [Curiosity Maps](http://www.curiosity-maps.org/), an attempt to visualize [Curiosity](http://en.wikipedia.org/wiki/Curiosity_rover)'s journey on Mars. It contains three major components: 

* `spice/` - a [SPICE](http://naif.jpl.nasa.gov/naif/aboutspice.html) application to extract the rover's locations.
* `crawl/` - a Java application to pull free use Curiosity images from NASA's [raw image gallery](http://mars.jpl.nasa.gov/msl/multimedia/raw/).
* `webserver/` - a [Sinatra](http://www.sinatrarb.com/) frontend visualizer.

The end result - an interactive Martian map with markers enabling access to the tens of thousands of high-resolution photographs Curiosity has taken thus far.

Suggestions/Comments/Pull requests welcome!
