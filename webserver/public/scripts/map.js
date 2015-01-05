/**
 * This file contains the functionality for the main map view.
 * Specifically, it:
 *
 * 1) Constructs a GIS martian map using the Google Maps v3 api.
 * 2) Plots generated rover locations with markers.
 * 3) Prepares image slider flyouts for each marker (assets load lazily though).
 * 4) Draws a polyline to illustrate rover path.
 * 5) Hides all markers (sols) for which we have no images.
 */

// global definitions
var ns = {
    // the GIS map
    map: undefined,

    // map bounds, in LatLng
    mapBounds: new google.maps.LatLngBounds(
                        new google.maps.LatLng(-5.991029, 135.918796), new google.maps.LatLng(-2.989471, 138.919805)),

    // minimum zoom level. corresponds to image index (bucket in s3)
    mapMinZoom: 6,

    // maximum zoom level. corresponds to image index (bucket in s3)
    mapMaxZoom: 13,

    mapInitZoom: 10,

    // rover landing site according to usgs. "MSL_LANDING_SITE" in spice
    mslLandingSite: new google.maps.LatLng(-4.490250, 137.419301),

    // list of {marker, sol} pairs we've added to the map
    markers: [],

    // lookup table for which markers we have images for
    cameraCoverage: {},

    initialzed: false
};

/**
 * Initializes the map view.
 *
 * @param locations
 * @param cameraCoverage
 */
function initialize(locations, cameraCoverage) {
    if(ns.initialized) { 
        return;
    }

    var map = newMap();
    var maptiler = newMapTiler();
    var path = [];
    map.mapTypes.set('maptiler', maptiler);
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
function newMap() {
    var opts = {
        streetViewControl: false,
        mapTypeId: 'maptiler',
        backgroundColor: "rgb(0,0,0)",
        center: ns.mslLandingSite,
        zoom: ns.mapInitZoom,
        scaleControl: true,
        mapTypeControlOptions: {
            mapTypeIds: []
        },
    }
    return new google.maps.Map(document.getElementById("map"), opts);
}

/**
 * Creates a custom map tiler by implementing the ImageMapType interface.
 *
 * Most of this code was autogenerated by MapTiler.
 */
function newMapTiler() {
    return new google.maps.ImageMapType({
        getTileUrl: function(coord, zoom) { 
            var proj = ns.map.getProjection();
            var z2 = Math.pow(2, zoom);
            var tileXSize = 256 / z2;
            var tileYSize = 256 / z2;
            var tileBounds = new google.maps.LatLngBounds(
                proj.fromPointToLatLng(new google.maps.Point(coord.x * tileXSize, (coord.y + 1) * tileYSize)),
                proj.fromPointToLatLng(new google.maps.Point((coord.x + 1) * tileXSize, coord.y * tileYSize))
            );
            var y = coord.y;
            if (ns.mapBounds.intersects(tileBounds) && (ns.mapMinZoom <= zoom) && (zoom <= ns.mapMaxZoom))
                return "https://s3-us-west-2.amazonaws.com/curiosity-maps-assets/" + zoom + "/" + coord.x + "/" + y + ".png";
            else
                return "http://www.maptiler.org/img/none.png";
        },
        tileSize: new google.maps.Size(256, 256),
        isPng: true,
        name: "MapTiler",
        alt: "MapTiler",
        minZoom: ns.mapMinZoom,
        maxZoom: ns.mapMaxZoom
    });
}

/**
 * Adds a rover marker to the specified {@code map} at {@code location}.
 *
 * (Gale Crater, for reference is located at new google.maps.LatLng(-4.490250, 137.419301))
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
        var iframe = document.createElement("iframe");
        var camera = document.getElementById("cameras").value;
        iframe.src = "/images/"+ location.sol + "/" + camera;
        iframe.className = "images";
        iframe.onload = function() {
            iframe.contentWindow.addEventListener("message", function(event) {
                if(event.data === "close") {
                    // it'll be http cached  anyways in case user wants to go back (and reload frame)
                    iframe.parentNode.removeChild(iframe);
                }
            });
            iframe.contentWindow.postMessage("open", "*");
            iframe.style.display = "block";
            iframe.contentWindow.focus();
        }
        document.body.appendChild(iframe);
    });

    return marker;
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
