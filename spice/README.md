# SPICE

This directory contains the SPICE interface for Curiosity Maps.
The rover's location is estimated by applying the [NAIF SPICE](http://naif.jpl.nasa.gov/naif/aboutspice.html) system
to open NASA MSL kernels.

## Usage

Build the executable:

`gcc spice/src/locations.c -I /path/to/lib/cspice/include/ /path/to/lib/cspice/lib/cspice.a -lm -g -o bin/locations`

Run the executable:

`bin/locations spice/out/locations.csv`

An example session is as follows:

> $ bin/locations spice/out/locations.csv 

> Enter the name of the metakernel file: spice/kernels/metakernel.txt
 
> Enter the name of the observing body: MARS
 
> Enter the name of a target body: MSL
 
> Enter the number of states to be calculated: 707
 
> Enter the beginning UTC time: 2012-08-06 06:23:34 UTC
 
> Enter the ending UTC time: 2014-08-02 15:02:26 UTC
 
> Enter the inertial reference frame (e.g.:J2000): IAU_MARS
 
> Type of correction                              Type of state
> -------------------------------------------------------------
> 'LT+S'    Light-time and stellar aberration    Apparent state
> 'LT'      Light-time only                      True state
> 'NONE'    No correction                        Geometric state
 
> Enter LT+S, LT, or NONE: LT+S

> Working......................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................completed!
> Results written to spice/out/locations.csv


For information on how these data are stored or used, see `crawl/README.md` or `webserver/README.md`.

## Resources

* [SPICE tutorial](http://naif.jpl.nasa.gov/naif/tutorials.html)
* [cspice download](http://naif.jpl.nasa.gov/naif/toolkit_C.html)
* SPICE documentation (in toolkit package)
* [cspice geometry example](http://naif.jpl.nasa.gov/pub/naif/FIDO/misc/njb/src/geom.c)
