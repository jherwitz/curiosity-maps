# Crawl

This directory contains the image data source for Curiosity Maps as well as the backend interface with MySql.

This application currently exists as several starter classes run manually as needed. Soon these starters will become scheduled tasks that automatically pull and publish new rover data.

## Starters

CuriosityImageCrawlerStarter.java - crawls images from the NASA Curiosity image gallery.

RoverLocationPublisherStarter.java - publishes cspice-generated location data.

ThumbnailGarbageCollectorStarter.java - detects and removes low-resolution images from the database.

## Dependencies

[JSoup](http://jsoup.org/) (1.8.1)
[Guava](https://github.com/google/guava) (18.0)
[JUnit](http://junit.org/) (4.11)
[Hamcrest](http://hamcrest.org/) (1.3)
[Apache Commons CLI](http://commons.apache.org/proper/commons-cli/) (1.2)
[Apache Commons Lang](http://commons.apache.org/proper/commons-lang/) (2.6)
[MySql Connector](http://dev.mysql.com/downloads/connector/j/) (5.1.34)
