/**
 * This file contains the functionality for the camera image view.
 * Specifically, it:
 *
 * - Constructs a lazy-loading image slider using the "slick" library.
 * - Adds click and keyboard handlers for various interface uses.
 * - Defines message senders handlers for x-frame communication. 
 * - Implements a continuous fade timeout for extraneous UI elements.
 *
 * @see http://curiosity-maps.org/images/<SOL>/<CAMERA>
 */

// global definitions
var ns = {
    fade: undefined,

    initialized: false
}

/**
 * Prepares handlers for x-frame messaging and dynamic page functionality. 
 */
function setup() {
    addMessageHandlers();
    addUIResponsiveness();
}

/**
 * Initializes the slider and kicks off auto-fade.
 */
function initialize() {
    if(ns.initialized) {
        return;
    }

    // initialize carousel
    $('.images').slick({
        speed: 300,
        infinite: true,
        slidesToShow: 1,
        touchThreshold: 25,
        lazyLoad: 'ondemand'
    });

    // kick off auto-fade
    ns.fade = setTimeout(function() {
        $(".quietable").fadeOut();
        $(".slick-prev").fadeOut();
        $(".slick-next").fadeOut();
        $('html').css({
            cursor: 'none'
        });
    }, 3000);

    ns.initialized = true;
}

/**
 * Add handlers for x-frame messages.
 */ 
function addMessageHandlers() {
    // add frame controls (i.e., close button added if we're in an iframe)
    window.addEventListener("message", function(event){
        if(!event.data){
            return;
        }

        var message = JSON.parse(event.data);
        if(message.type === "open"){
            // TODO: use jquery for consistency
            document.getElementById("close").style.display = "block";
        }
    }, false);
}

/**
 * Adds responsivness to the UI. Specifically, functionaliy for the close
 * button, the control panel, and the auto-fade are specified here.
 */
function addUIResponsiveness() {
    // send 'close' message on close button click or 'esc' press
    // this will close the iframe if open
    $("#close").click(function() {
        window.postMessage(JSON.stringify({type: "close"}), "*");
    });
    $(document).keydown(function(e) {
        if (e.keyCode == 27) { 
            window.postMessage(JSON.stringify({type: "close"}), "*");
        }   
    });

    // send 'redirect' message if a new sol or camera is submitted
    // this will close the current iframe and open the requested iframe
    $(".controls .sol").keydown(function (e) {
        if (e.keyCode == 13) {
            var message = JSON.stringify({type: "redirect", sol: $("#sol").val(), camera: $("#camera").val()});
            window.postMessage(message, "*");
        }
    });
    $(".controls .camera").change(function (e) {
        var message = JSON.stringify({type: "redirect", sol: $("#sol").val(), camera: $("#camera").val()});
        window.postMessage(message, "*");
    });

    // auto-fade
    $(document).mousemove(function(event) {
        $(".quietable").fadeIn();
        $(".slick-prev").fadeIn();
        $(".slick-next").fadeIn();
        $('html').css({
            cursor: 'default'
        });
        if(ns.fade) { clearTimeout(ns.fade); }

        ns.fade = setTimeout(function() {
            $(".quietable").fadeOut();
            $(".slick-prev").fadeOut();
            $(".slick-next").fadeOut();
            $('html').css({
            cursor: 'none'
        });
        }, 3000);
    });
}
