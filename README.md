This repository contains code powering [Curiosity Maps](http://www.curiosity-maps.org/), an attempt to visualize [Curiosity](http://en.wikipedia.org/wiki/Curiosity_rover)'s journey on Mars. It contains three major components: 

* `spice/` - a SPICE application to extract the rover's locations.
* `crawl/` - a Java application to pull free use Curiosity images from NASA's website.
* `webserver/` - a Sinatra frontend visualizer.

The end result - an interactive Google Maps rendering allowing access to NASA's collection of over 75,000 high-resolution martian images.
