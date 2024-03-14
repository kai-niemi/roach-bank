// The star of every good animation
var requestAnimationFrame = window.requestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        window.webkitRequestAnimationFrame ||
        window.msRequestAnimationFrame;

var transforms = [
    "transform",
    "msTransform",
    "webkitTransform",
    "mozTransform",
    "oTransform"];

var transformProperty = getSupportedPropertyName(transforms);
// Array to store our Snowflake objects
var snowflakes = [];
// Global variables to store our browser's window size
var browserWidth;
var browserHeight;
// Specify the number of snowflakes you want visible
var numberOfSnowflakes = 15;
// Flag to reset the position of the snowflakes
var resetPosition = false;

//
// It all starts here...
//
function setup() {
    var month = new Date().getMonth();
    var snowing = month >= 10 || month <= 1; // nov-feb

    // snowing = true;
    // month = 11;

    if (month == 9) { // Oct
        numberOfSnowflakes = 15; // flurry
    }
    if (month == 10) {
        numberOfSnowflakes = 50;
    }
    if (month == 11) {
        numberOfSnowflakes = 80;
    }
    if (month == 0) {
        numberOfSnowflakes = 120; // blizzard
    }
    if (month == 1) {
        numberOfSnowflakes = 80;
    }
    if (month == 2) {
        numberOfSnowflakes = 15;
    }

    if (snowing) {
        window.addEventListener("DOMContentLoaded", generateSnowflakes, false);
        window.addEventListener("resize", setResetFlag, false);
    } else {
        console.log("Only snowing between Oct-Mar");
    }
}

setup();

//
// Vendor prefix management
//
function getSupportedPropertyName(properties) {
    for (var i = 0; i < properties.length; i++) {
        if (typeof document.body.style[properties[i]] != "undefined") {
            return properties[i];
        }
    }
    return null;
}

//
// Constructor for our Snowflake object
//
function Snowflake(element, radius, speed, xPos, yPos) {
    // set initial snowflake properties
    this.element = element;
    this.radius = radius;
    this.speed = speed;
    this.xPos = xPos;
    this.yPos = yPos;

    // declare variables used for snowflake's motion
    this.counter = 0;
    this.sign = Math.random() < 0.5 ? 1 : -1;

    // setting an initial opacity and size for our snowflake
    this.element.style.opacity = .1 + Math.random();
    this.element.style.fontSize = 12 + Math.random() * 50 + "px";
}

//
// The function responsible for actually moving our snowflake
//
Snowflake.prototype.update = function () {
    // using some trigonometry to determine our x and y position
    this.counter += this.speed / 5000;
    this.xPos += this.sign * this.speed * Math.cos(this.counter) / 40;
    this.yPos += Math.sin(this.counter) / 40 + this.speed / 30;

    // setting our snowflake's position
    setTranslate3DTransform(this.element, Math.round(this.xPos), Math.round(this.yPos));

    // if snowflake goes below the browser window, move it back to the top
    if (this.yPos > browserHeight) {
        this.yPos = -50;
    }
}

//
// A performant way to set your snowflake's position
//
function setTranslate3DTransform(element, xPosition, yPosition) {
    var val = "translate3d(" + xPosition + "px, " + yPosition + "px" + ", 0)";
    element.style[transformProperty] = val;
}

//
// The function responsible for creating the snowflake
//
function generateSnowflakes() {
    // get our snowflake element from the DOM and store it
    var originalSnowflake = document.querySelector(".snowflake");

    // access our snowflake element's parent container
    var snowflakeContainer = originalSnowflake.parentNode;

    // get our browser's size
    browserWidth = document.documentElement.clientWidth;
    browserHeight = document.documentElement.clientHeight;

    // create each individual snowflake
    for (var i = 0; i < numberOfSnowflakes; i++) {

        // clone our original snowflake and add it to snowflakeContainer
        var snowflakeCopy = originalSnowflake.cloneNode(true);
        snowflakeContainer.appendChild(snowflakeCopy);

        // set our snowflake's initial position and related properties
        var initialXPos = getPosition(50, browserWidth);
        var initialYPos = getPosition(50, browserHeight);
        var speed = 5 + Math.random() * 40;
        var radius = 4 + Math.random() * 10;

        // create our Snowflake object
        var snowflakeObject = new Snowflake(snowflakeCopy,
                radius,
                speed,
                initialXPos,
                initialYPos);
        snowflakes.push(snowflakeObject);
    }

    // remove the original snowflake because we no longer need it visible
    snowflakeContainer.removeChild(originalSnowflake);

    // call the moveSnowflakes function every 30 milliseconds
    moveSnowflakes();
}

//
// Responsible for moving each snowflake by calling its update function
//
function moveSnowflakes() {
    for (var i = 0; i < snowflakes.length; i++) {
        var snowflake = snowflakes[i];
        snowflake.update();
    }

    // Reset the position of all the snowflakes to a new value
    if (resetPosition) {
        browserWidth = document.documentElement.clientWidth;
        browserHeight = document.documentElement.clientHeight;

        for (var i = 0; i < snowflakes.length; i++) {
            var snowflake = snowflakes[i];

            snowflake.xPos = getPosition(50, browserWidth);
            snowflake.yPos = getPosition(50, browserHeight);
        }

        resetPosition = false;
    }

    requestAnimationFrame(moveSnowflakes);
}

//
// This function returns a number between (maximum - offset) and (maximum + offset)
//
function getPosition(offset, size) {
    return Math.round(-1 * offset + Math.random() * (size + 2 * offset));
}

//
// Trigger a reset of all the snowflakes' positions
//
function setResetFlag(e) {
    resetPosition = true;
}