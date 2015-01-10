/**
 * This file contains the functionality for the main map view.
 * Specifically, it:
 *
 * 1) Constructs a martian map using the Google Maps v3 api and preprocessed map tiles.
 * 2) Plots generated rover locations with markers.
 * 3) Prepares image slider flyouts for each marker (assets load lazily though).
 * 4) Draws a polyline to illustrate rover path.
 * 5) Hides all markers (sols) for which we have no images.
 *
 * @see http://curiosity-maps.org/
 */

// global definitions
var ns = {
    map: undefined,

    // map bounds, in LatLng
    mapBounds: new google.maps.LatLngBounds(
                        new google.maps.LatLng(-5.991029, 135.918796), new google.maps.LatLng(-2.989471, 138.919805)),

    // minimum zoom level. used to locate tile assets.
    mapMinZoom: 3,

    // maximum zoom level. used to locate tile assets.
    mapMaxZoom: 15,

    // intiial zoom
    mapInitZoom: 11,

    // rover landing site according to http://en.wikipedia.org/wiki/Curiosity_rover.
    // aka "Bradbury Landing"
    mslLandingSite: new google.maps.LatLng(-4.5895, 137.4417),

    // list of {marker, sol} pairs we've added to the map
    markers: [],

    // lookup table for which markers we have images for
    cameraCoverage: {},

    // lifecycle guard
    initialzed: false
};

/**
 * Initializes the map view.
 */
function initialize(locations, cameraCoverage) {
    if(ns.initialized) { 
        return;
    }

    // construct map and set globals
    
    var visible = newMapType("visible");
    var infrared = newMapType("infrared")
    var mapTypeIds = ["visible", "infrared"];
    
    var map = newMap(mapTypeIds);

    map.mapTypes.set("visible", visible);
    map.mapTypes.set("infrared", infrared);

    var path = [];
    ns.map = map;
    ns.cameraCoverage = cameraCoverage;

    // add markers for each rover location
    var marker;
    for(var location = 0; location < locations.length; location++) {
        marker = addRoverMarker(locations[location], map);
        path.push(marker.getPosition());
        ns.markers.push({marker: marker, sol: locations[location].sol});
    }

    drawPolyline(path);

    // hide markers we don't have images for
    maskMarkers();

    ns.initialized = true;
}

/**
 * Constructs a new google map.
 */
function newMap(mapTypeIds) {
    var opts = {
        streetViewControl: false,
        backgroundColor: "rgb(0,0,0)",
        center: ns.mslLandingSite,
        mapTypeId: "visible",
        zoom: ns.mapInitZoom,
        scaleControl: true,
        mapTypeControlOptions: {
            mapTypeIds: mapTypeIds,
            style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
            position: google.maps.ControlPosition.TOP_LEFT
        }
    };
    return new google.maps.Map(document.getElementById("map"), opts);
}

/**
 * Creates a custom map type implementing the ImageMapType interface.
 *
 * Derived from http://hirise.lpl.arizona.edu/js/google-map-types.js.
 */
function newMapType(spectrum) {
    return new google.maps.ImageMapType({
        getTileUrl: function (coord, zoom) {
            // google tiles only exist for zoom levels 1-9, so use our custom tiles for anything further in
            if(zoom < 10){
                return getHorizontallyRepeatingTileUrl(coord, zoom, function (coord, zoom) {
                    return getLowZoomMarsTileUrl("http://mw1.google.com/mw-planetary/mars/"+ spectrum +"/", coord, zoom);
                });
            } else {
                // I uploaded the infrared set of images to the root directory by mistake
                // due to the required reupload time fixing the path structure will be a TODO
                if(spectrum === "visible"){
                    return getHighZoomMarsTileUrl("https://s3-us-west-2.amazonaws.com/curiosity-maps-assets/tiles/visible/", coord, zoom);
                } else {
                    return getHighZoomMarsTileUrl("https://s3-us-west-2.amazonaws.com/curiosity-maps-assets/", coord, zoom);
                }
            }
        },
        tileSize: new google.maps.Size(256, 256),
        isPng: false,
        name: spectrum,
        alt: spectrum,
        minZoom: ns.mapMinZoom,
        maxZoom: ns.mapMaxZoom,
        credit: 'Image Credit: NASA/JPL/ASU/MSSS'
    });
}

/*
 * Handles wrapping at the map edges.
 */ 
function getHorizontallyRepeatingTileUrl(coord, zoom, urlfunc) {
    var y = coord.y,
        x = coord.x,
        tileRange = 1 << zoom;

    if (y < 0 || y >= tileRange) {
        return null;
    }

    if (x < 0 || x >= tileRange) {
        x = (x % tileRange + tileRange) % tileRange;
    }

    return urlfunc({x: x, y: y}, zoom);
}

/**
 * For this schema each tile is split into four quadrants, defined with character codes:
 *  
 *    q  | r
 *       |
 *   ---------
 *       |     
 *    t  | s
 *
 * In order to find the correct asset for a certain tile we recursively traverse from the lowest zoom, with base case 't'. 
 * Each concatenated character corresponds to the quadrant to enter at the character index zoom level.
 */
function getLowZoomMarsTileUrl(baseUrl, coord, zoom) {
    var bound = Math.pow(2, zoom),
        x = coord.x,
        y = coord.y,
        quads = ['t']; 

    for (var z = 0; z < zoom; z++) {
        bound /= 2;
        if (y < bound) {
            if (x < bound) {
                quads.push('q');
            } else {
                quads.push('r');
                x -= bound;
            }
        } else {
            if (x < bound) {
                quads.push('t');
                y -= bound;
            } else {
                quads.push('s');
                x -= bound;
                y -= bound;
            }
        }
    }

    return baseUrl + quads.join('') + ".jpg";
}

/**
 * For this schema we address the tile assets with:
 *
 * <zoom level>/<x coordinate>/<y coordinate>.png
 */
function getHighZoomMarsTileUrl(baseUrl, coord, zoom) {
    var proj = ns.map.getProjection();
     var z2 = Math.pow(2, zoom);
     var tileXSize = 256 / z2;
     var tileYSize = 256 / z2;
     var tileBounds = new google.maps.LatLngBounds(
         proj.fromPointToLatLng(new google.maps.Point(coord.x * tileXSize, (coord.y + 1) * tileYSize)),
         proj.fromPointToLatLng(new google.maps.Point((coord.x + 1) * tileXSize, coord.y * tileYSize))
     );
     var y = coord.y;
     return baseUrl + zoom + "/" + coord.x + "/" + y + ".png";
}

/**
 * Adds a rover marker to the specified {@code map} at {@code location}.
 *
 * @return the added marker
 */
function addRoverMarker(location) {
    var position = new google.maps.LatLng(location.lat, location.lng);

    // construct marker and add to map
    var marker = new google.maps.Marker({
        position: position,
        map: ns.map,
        title: "Sol " + location.sol
    });

    // prepare flyout on marker click
    google.maps.event.addListener(marker, 'click', function() {
        var camera = document.getElementById("cameras").value;
        var iframe = createFrame(location.sol, camera);
        document.body.appendChild(iframe);
    });

    return marker;
}

function createFrame(sol, camera) {
    var iframe = document.createElement("iframe");
    iframe.src = "/images/"+ sol + "/" + camera;
    iframe.className = "images";
    iframe.onload = function() {
        iframe.contentWindow.addEventListener("message", function(event){
            if(!event.data) {
                return;
            }

            var message = JSON.parse(event.data);
            if(message.type === "redirect") {
                // redirect to a new frame

                var camera = message.camera;
                var sol = message.sol;
                var newFrame = createFrame(sol, camera);

                // swap frames
                document.body.appendChild(newFrame);
                iframe.parentNode.removeChild(iframe);


            } else if(message.type === "close") {
                // it'll be http cached  anyways in case user wants to go back (and reload frame)
                iframe.parentNode.removeChild(iframe);
            } 
        });
        iframe.contentWindow.postMessage(JSON.stringify({type: "open"}), "*");
        iframe.style.display = "block";
        iframe.contentWindow.focus();
    }
    return iframe;
}

/**
 * Draws lines on map connecting path vertices.
 */
function drawPolyline(path) {
    var polyline = new google.maps.Polyline({
        path: path,
        geodesic: true,
        strokeColor: '#FF0000',
        strokeOpacity: 1.0,
        strokeWeight: 2
    });
    polyline.setMap(ns.map);
}

/**
 * Hides all markers for which we don't have images from the currently selected camera for.
 */
function maskMarkers() {
    var camera = document.getElementById("cameras").value;
    var markers = ns.markers;
    for(var i=0; i<markers.length; i++) {
        if(!ns.cameraCoverage[camera][markers[i].sol]) {
            markers[i].marker.setVisible(false);
        } else {
            markers[i].marker.setVisible(true);
        }
    }
}
