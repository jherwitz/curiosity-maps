# Geographic Information System

This directory contains the Geographic Information System (GIS) interface for curiosity-maps. 
The rover's location is estimated by applying the [NAIF SPICE](http://naif.jpl.nasa.gov/naif/aboutspice.html) system
to 


## Usage

Build the executable:

`gcc gis/src/locations.c -I ~/lib/cspice/include/ ~/lib/cspice/lib/cspice.a -lm -g -o bin/locations`

Run the executable:

`bin/locations gis/out/locations.csv`

An example session is as follows:

> Enter the name of the metakernel file: gis/kernels/metakernel.txt
 
> Enter the name of the observing body: MARS
 
> Enter the name of a target body: MSL_ROVER
 
> Enter the number of states to be calculated: 1
 
> Enter the UTC time: 2012-08-06 06:23:34 UTC
 
> Enter the inertial reference frame (e.g.:J2000): IAU_MARS
 
>  Type of correction                              Type of state
>  -------------------------------------------------------------
> 'LT+S'    Light-time and stellar aberration    Apparent state
> 'LT'      Light-time only                      True state
> 'NONE'    No correction                        Geometric state
 
> Enter LT+S, LT, or NONE: LT+S

For information on how these data are stored or used, see `crawl/README.md` or `webserver/README.md`.

## Resources

* [SPICE tutorial](http://naif.jpl.nasa.gov/naif/tutorials.html)
* SPICE documentation (in source package)
* [cspice download](http://naif.jpl.nasa.gov/naif/toolkit_C.html)
* [cspice geometry example](http://naif.jpl.nasa.gov/pub/naif/FIDO/misc/njb/src/geom.c)
