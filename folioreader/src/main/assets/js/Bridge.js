//
//  Bridge.js
//  FolioReaderKit
//
//  Created by Heberti Almeida on 06/05/15.
//  Copyright (c) 2015 Folio Reader. All rights reserved.
//

var thisHighlight;
var audioMarkClass;
var wordsPerMinute = 180;

document.addEventListener("DOMContentLoaded", function(event) {
//    var lnk = document.getElementsByClassName("lnk");
//    for (var i=0; i<lnk.length; i++) {
//        lnk[i].setAttribute("onclick","return callVerseURL(this);");
//    }
});

// Generate a GUID
function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
    }
    var guid = s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    return guid.toUpperCase();
}

// Get All HTML
function getHTML() {
    Highlight.getHtmlAndSaveHighlight(document.documentElement.outerHTML);
    //return document.documentElement.outerHTML;
}

// Class manipulation
function hasClass(ele,cls) {
  return !!ele.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'));
}

function addClass(ele,cls) {
  if (!hasClass(ele,cls)) ele.className += " "+cls;
}

function removeClass(ele,cls) {
  if (hasClass(ele,cls)) {
    var reg = new RegExp('(\\s|^)'+cls+'(\\s|$)');
    ele.className=ele.className.replace(reg,' ');
  }
}

// Font name class
function setFontName(cls) {
    var elm = document.documentElement;
    removeClass(elm, "andada");
    removeClass(elm, "lato");
    removeClass(elm, "lora");
    removeClass(elm, "raleway");
    addClass(elm, cls);
}

// Toggle night mode
function nightMode(enable) {
    var elm = document.documentElement;
    if(enable) {
        addClass(elm, "nightMode");
    } else {
        removeClass(elm, "nightMode");
    }
}

// Set font size
function setFontSize(cls) {
    var elm = document.documentElement;
    removeClass(elm, "textSizeOne");
    removeClass(elm, "textSizeTwo");
    removeClass(elm, "textSizeThree");
    removeClass(elm, "textSizeFour");
    removeClass(elm, "textSizeFive");
    addClass(elm, cls);
}


// Menu colors
function setHighlightStyle(style) {
    Highlight.getUpdatedHighlightId(thisHighlight.id, style);
}

function removeThisHighlight() {
    return thisHighlight.id;
}

function removeHighlightById(elmId) {
    var elm = document.getElementById(elmId);
    elm.outerHTML = elm.innerHTML;
    return elm.id;
}

function getHighlightContent() {
    return thisHighlight.textContent
}

function getBodyText() {
    return document.body.innerText;
}

// Method that returns only selected text plain
var getSelectedText = function() {
    return window.getSelection().toString();
}

// Method that gets the Rect of current selected text
// and returns in a JSON format
var getRectForSelectedText = function(elm) {
    if (typeof elm === "undefined") elm = window.getSelection().getRangeAt(0);

    var rect = elm.getBoundingClientRect();
    return "{{" + rect.left + "," + rect.top + "}, {" + rect.width + "," + rect.height + "}}";
}

// Method that call that a hightlight was clicked
// with URL scheme and rect informations
var callHighlightURL = function(elm) {
    event.stopPropagation();
    var URLBase = "highlight://";
    var currentHighlightRect = getRectForSelectedText(elm);
    thisHighlight = elm;

    window.location = URLBase + encodeURIComponent(currentHighlightRect);
}

// Reading time
function getReadingTime() {
    var text = document.body.innerText;
    var totalWords = text.trim().split(/\s+/g).length;
    var wordsPerSecond = wordsPerMinute / 60; //define words per second based on words per minute
    var totalReadingTimeSeconds = totalWords / wordsPerSecond; //define total reading time in seconds
    var readingTimeMinutes = Math.round(totalReadingTimeSeconds / 60);

    return readingTimeMinutes;
}

/**
 Get Vertical or Horizontal paged #anchor offset
 */
var getAnchorOffset = function(target, horizontal) {
    var elem = document.getElementById(target);

    if (!elem) {
        elem = document.getElementsByName(target)[0];
    }

    if (horizontal) {
        return document.body.clientWidth * Math.floor(elem.offsetTop / window.innerHeight);
    }

    return elem.offsetTop;
}

function scrollAnchor(id) {
    console.log("LOG id === " + id)
    window.location.hash = id;
}

function findElementWithID(node) {
    if( !node || node.tagName == "BODY")
        return null
    else if( node.id )
        return node
    else
        return findElementWithID(node)
}

function findElementWithIDInView() {

    if(audioMarkClass) {
        // attempt to find an existing "audio mark"
        var el = document.querySelector("."+audioMarkClass)

        // if that existing audio mark exists and is in view, use it
        if( el && el.offsetTop > document.body.scrollTop && el.offsetTop < (window.innerHeight + document.body.scrollTop))
            return el
    }

    // @NOTE: is `span` too limiting?
    var els = document.querySelectorAll("span[id]")

    for(indx in els) {
        var element = els[indx];

        // Horizontal scroll
        if (document.body.scrollTop == 0) {
            var elLeft = document.body.clientWidth * Math.floor(element.offsetTop / window.innerHeight);
            // document.body.scrollLeft = elLeft;

            if (elLeft == document.body.scrollLeft) {
                return element;
            }

        // Vertical
        } else if(element.offsetTop > document.body.scrollTop) {
            return element;
        }
    }

    return null
}


/**
 Play Audio - called by native UIMenuController when a user selects a bit of text and presses "play"
 */
function playAudio() {
    var sel = getSelection();
    var node = null;

    // user selected text? start playing from the selected node
    if (sel.toString() != "") {
        node = sel.anchorNode ? findElementWithID(sel.anchorNode.parentNode) : null;

    // find the first ID'd element that is within view (it will
    } else {
        node = findElementWithIDInView()
    }

    playAudioFragmentID(node ? node.id : null)
}


/**
 Play Audio Fragment ID - tells page controller to begin playing audio from the following ID
 */
function playAudioFragmentID(fragmentID) {
    var URLBase = "play-audio://";
    window.location = URLBase + (fragmentID?encodeURIComponent(fragmentID):"")
}

function bodyOrHtml() {
    if ('scrollingElement' in document) {
        return document.scrollingElement;
    }
    // Fallback for legacy browsers
    if (navigator.userAgent.indexOf('WebKit') != -1) {
        return document.body;
    }
    return document.documentElement;
}

/**
 Go To Element - scrolls the webview to the requested element
 */
function goToElement(element) {

    var scrollingElement = bodyOrHtml();
    var top = scrollingElement.scrollTop;
    var elementTop = element.offsetTop - 20;
    var bottom = window.innerHeight + top;
    var elementBottom = element.offsetHeight + element.offsetTop + 60;

    //console.log(window);
    //console.log("-> top = " + top);
    //console.log("-> elementTop = " + elementTop);
    //console.log("-> bottom = " + bottom);
    //console.log("-> elementBottom = " + elementBottom);
    console.log("-> FolioPageFragment.getDirection() = " + FolioPageFragment.getDirection() + "element = " + element.id);

    if (FolioPageFragment.getDirection() == "VERTICAL" &&
            (elementBottom > bottom || elementTop < top)) {

        var newScrollTop = elementTop;
        console.log("-> newScrollTop = " + newScrollTop);
        scrollingElement.scrollTop = newScrollTop;

    } else if (FolioPageFragment.getDirection() == "HORIZONTAL" && top == 0) {
        var clientWidth = document.documentElement.clientWidth;
        var pageIndex = Math.floor(element.offsetLeft / clientWidth);
        var newScrollLeft = clientWidth * pageIndex;
        console.log(">>> Scroll Horizontal pageIndex = " + pageIndex + " --- newScrollLeft = " + newScrollLeft);
        WebViewPager.setCurrentPage(pageIndex);
        scrollingElement.scrollLeft = newScrollLeft;
    }
    return element;
}

/**
 Go To Element - scrolls the webview to the requested element
 */
function goToImageElement(element) {

    var scrollingElement = bodyOrHtml();
    var top = scrollingElement.scrollTop;
    var elementTop = element.offsetTop - 20;
    var bottom = window.innerHeight + top;
    var elementBottom = element.offsetHeight + element.offsetTop + 60;

    //console.log(window);
    //console.log("-> top = " + top);
    //console.log("-> elementTop = " + elementTop);
    //console.log("-> bottom = " + bottom);
    //console.log("-> elementBottom = " + elementBottom);
    console.log("-> FolioPageFragment.getDirection() = " + FolioPageFragment.getDirection());

    if (FolioPageFragment.getDirection() == "VERTICAL" &&
            (elementBottom > bottom || elementTop < top)) {

        var newScrollTop = elementTop;
        console.log("-> newScrollTop = " + newScrollTop);
        scrollingElement.scrollTop = newScrollTop;

    } else if (FolioPageFragment.getDirection() == "HORIZONTAL" && top == 0) {
        console.log(">>> Scroll Horizontal");
        var clientWidth = document.documentElement.clientWidth;
        var pageIndex = Math.floor(element.offsetLeft / clientWidth);
        var newScrollLeft = clientWidth * pageIndex;
        WebViewPager.setCurrentPage(pageIndex);
    }

    return element;
}

function scrollToElement(id) {
    console.log("---->>>>" + id)
    //document.getElementById(id).scrollIntoView()

    var element = document.getElementById(id);
    console.log("---->>>>" + element)
        if (element)
            goToImageElement(element);

    
    FolioPageFragment.hideLoading();

}

/**
 Remove All Classes - removes the given class from all elements in the DOM
 */
function removeAllClasses(className) {
    var els = document.body.getElementsByClassName(className)
    if( els.length > 0 )
    for( i = 0; i <= els.length; i++) {
        els[i].classList.remove(className);
    }
}

//For testing purpose only
function sleep(seconds)
{
  var e = new Date().getTime() + (seconds * 1000);
  while (new Date().getTime() <= e) {}
}

// Mock objects for testing purpose
/*var FolioPageFragment = {

    setHorizontalPageCount : function(pageCount) {
        console.warn("-> Mock call to FolioPageFragment.setHorizontalPageCount(" + pageCount + ")");
    },

    storeFirstVisibleSpan : function(usingId, value) {
        console.warn("-> Mock call to FolioPageFragment.storeFirstVisibleSpan(" + usingId + ", " + value + ")");
    },

    getDirection : function() {
        //var direction = "VERTICAL";
        var direction = "HORIZONTAL";
        console.warn("-> Mock call to FolioPageFragment.getDirection(), return " + direction);
        return direction;
    }
};

var FolioWebView = {

    setCompatMode : function(compatMode) {
        console.warn("-> Mock call to FolioWebView.setCompatMode(" + compatMode + ")");
    }
};

var WebViewPager = {

    setCurrentPage : function(pageIndex) {
        console.warn("-> Mock call to WebViewPager.setCurrentPage(" + pageIndex + ")");
    },

    setPageToLast : function() {
        console.warn("-> Mock call to WebViewPager.setPageToLast()");
    },

    setPageToFirst : function() {
        console.warn("-> Mock call to WebViewPager.setPageToFirst()");
    }
};

var LoadingView = {

    show : function() {
        console.warn("-> Mock call to LoadingView.show()");
    },

    hide : function() {
        console.warn("-> Mock call to LoadingView.hide()");
    },

    visible : function() {
        console.warn("-> Mock call to LoadingView.visible()");
    },

    invisible : function() {
        console.warn("-> Mock call to LoadingView.invisible()");
    }
};*/

// Testing purpose calls
function test() {

    getCompatMode();
    wrappingSentencesWithinPTags();

    if (FolioPageFragment.getDirection() == "HORIZONTAL")
        initHorizontalDirection();
}

function scrollToLast() {
    console.log("-> scrollToLast");

    var direction = FolioPageFragment.getDirection();
    var scrollingElement = bodyOrHtml();

    if (direction == "VERTICAL") {
        scrollingElement.scrollTop =
        scrollingElement.scrollHeight - document.documentElement.clientHeight;

    } else if (direction == "HORIZONTAL") {
        scrollingElement.scrollLeft =
        scrollingElement.scrollWidth - document.documentElement.clientWidth;
        WebViewPager.setPageToLast();
    }

    FolioPageFragment.hideLoading();
}

function scrollToFirst() {
    console.log("-> scrollToFirst");

    var direction = FolioPageFragment.getDirection();
    var scrollingElement = bodyOrHtml();

    if (direction == "VERTICAL") {
        scrollingElement.scrollTop = 0;

    } else if (direction == "HORIZONTAL") {
        scrollingElement.scrollLeft = 0;
        WebViewPager.setPageToFirst();
    }

    FolioPageFragment.hideLoading();
}

function getCompatMode() {
    console.log("-> getCompatMode")
    FolioWebView.setCompatMode(document.compatMode);
}

var scrollWidth;
var horizontalInterval;
var horizontalIntervalPeriod = 1000;
var horizontalIntervalCounter = 0;
var horizontalIntervalLimit = 3000;

function horizontalRecheck() {
    horizontalIntervalCounter += horizontalIntervalPeriod;

    if (window.scrollWidth != document.documentElement.scrollWidth) {
        // Rare condition
        // This might happen when document.documentElement.scrollWidth gives incorrect value
        // when the webview is busy re-drawing contents.
        //console.log("-> horizontalIntervalCounter = " + horizontalIntervalCounter);
        console.warn("-> scrollWidth changed from " + window.scrollWidth + " to " +
            document.documentElement.scrollWidth);
        postInitHorizontalDirection();
    }

    if (horizontalIntervalCounter >= horizontalIntervalLimit)
        clearInterval(horizontalInterval);
}

function initHorizontalDirection() {
    console.log("-->> initHorizontalDirection")
    preInitHorizontalDirection();
    postInitHorizontalDirection();

    horizontalInterval = setInterval(horizontalRecheck, horizontalIntervalPeriod);
}

function preInitHorizontalDirection() {

    //console.log(window);
    //console.log("-> " + document.getElementsByTagName('title')[0].innerText);
    var htmlElement = document.getElementsByTagName('html')[0];
    var bodyElement = document.getElementsByTagName('body')[0];

    // Required when initHorizontalDirection() is called multiple times.
    // Currently it is called only once per page.
    htmlElement.style.width = null;
    bodyElement.style.width = null;
    htmlElement.style.height = null;
    bodyElement.style.height = null;

    var bodyStyle = bodyElement.currentStyle || window.getComputedStyle(bodyElement);
    var paddingTop = parseInt(bodyStyle.paddingTop, 10);
    var paddingRight = parseInt(bodyStyle.paddingRight, 10);
    var paddingBottom = parseInt(bodyStyle.paddingBottom, 10);
    var paddingLeft = parseInt(bodyStyle.paddingLeft, 10);
    //console.log("-> padding = " + paddingTop + ", " + paddingRight + ", " + paddingBottom + ", " + paddingLeft);

    //document.documentElement.clientWidth is window.innerWidth excluding x scrollbar width
    var pageWidth = document.documentElement.clientWidth - (paddingLeft + paddingRight);
    //document.documentElement.clientHeight is window.innerHeight excluding y scrollbar height
    var pageHeight = document.documentElement.clientHeight - (paddingTop + paddingBottom);

    bodyElement.style.webkitColumnGap = (paddingLeft + paddingRight) + 'px';
    bodyElement.style.webkitColumnWidth = pageWidth + 'px';

    //console.log("-> window.innerWidth = " + window.innerWidth);
    //console.log("-> window.innerHeight = " + window.innerHeight);
    //console.log("-> clientWidth = " + document.documentElement.clientWidth);
    //console.log("-> clientHeight = " + document.documentElement.clientHeight);
    //console.log("-> bodyElement.offsetWidth = " + bodyElement.offsetWidth);
    //console.log("-> bodyElement.offsetHeight = " + bodyElement.offsetHeight);
    //console.log("-> pageWidth = " + pageWidth);
    //console.log("-> pageHeight = " + pageHeight);

    htmlElement.style.height = (pageHeight + (paddingTop + paddingBottom)) + 'px';
    bodyElement.style.height = pageHeight + 'px';
}

function postInitHorizontalDirection() {
    var htmlElement = document.getElementsByTagName('html')[0];
    var bodyElement = document.getElementsByTagName('body')[0];
    var bodyStyle = bodyElement.currentStyle || window.getComputedStyle(bodyElement);
    var paddingTop = parseInt(bodyStyle.paddingTop, 10);
    var paddingRight = parseInt(bodyStyle.paddingRight, 10);
    var paddingBottom = parseInt(bodyStyle.paddingBottom, 10);
    var paddingLeft = parseInt(bodyStyle.paddingLeft, 10);
    var clientWidth = document.documentElement.clientWidth;

    var scrollWidth = document.documentElement.scrollWidth;
    //console.log("-> document.documentElement.offsetWidth = " + document.documentElement.offsetWidth);
    if (scrollWidth > clientWidth
        && scrollWidth > document.documentElement.offsetWidth) {
        scrollWidth += paddingRight;
    }
    var newBodyWidth = scrollWidth - (paddingLeft + paddingRight);
    window.scrollWidth = scrollWidth;

    htmlElement.style.width = scrollWidth + 'px';
    bodyElement.style.width = newBodyWidth + 'px';

    // pageCount deliberately rounded instead of ceiling to avoid any unexpected error
    var pageCount = Math.round(scrollWidth / clientWidth);
    var pageCountFloat = scrollWidth / clientWidth;

    if (pageCount != pageCountFloat) {
        console.warn("-> pageCount = " + pageCount + ", pageCountFloat = " + pageCountFloat
            + ", Something wrong in pageCount calculation");
    }

    console.log("-> scrollWidth = " + scrollWidth);
    //console.log("-> newBodyWidth = " + newBodyWidth);
    //console.log("-> pageCount = " + pageCount);

    FolioPageFragment.setHorizontalPageCount(pageCount);

    // add dummy class for table to re-style the page
    console.log("this is run re-style");
    var tableEleList = document.getElementsByTagName('table');
        if(tableEleList.length > 0) {
            for(var i of tableEleList) {
                i.setAttribute('style', 'border-spacing: 1px !important');
                console.log("this is table re-style");
            }
        }

}

/**
 Audio Mark ID - marks an element with an ID with the given class and scrolls to it
 */
function audioMarkID(className, id) {
    if (audioMarkClass)
        removeAllClasses(audioMarkClass);

    audioMarkClass = className
    var el = document.getElementById(id);

    goToElement(el);
    el.classList.add(className)
}

function setMediaOverlayStyle(style){
    document.documentElement.classList.remove("mediaOverlayStyle0", "mediaOverlayStyle1", "mediaOverlayStyle2")
    document.documentElement.classList.add(style)
}

function setMediaOverlayStyleColors(color, colorHighlight) {
    var stylesheet = document.styleSheets[document.styleSheets.length-1];
//    stylesheet.insertRule(".mediaOverlayStyle0 span.epub-media-overlay-playing { background: "+colorHighlight+" !important }")
//    stylesheet.insertRule(".mediaOverlayStyle1 span.epub-media-overlay-playing { border-color: "+color+" !important }")
//    stylesheet.insertRule(".mediaOverlayStyle2 span.epub-media-overlay-playing { color: "+color+" !important }")
}

var currentIndex = -1;


function findSentenceWithIDInView(els) {
    // @NOTE: is `span` too limiting?
    for(indx in els) {
        var element = els[indx];

        // Horizontal scroll
        if (document.body.scrollTop == 0) {
            var elLeft = document.body.clientWidth * Math.floor(element.offsetTop / window.innerHeight);
            // document.body.scrollLeft = elLeft;

            if (elLeft == document.body.scrollLeft) {
                currentIndex = indx;
                return element;
            }

        // Vertical
        } else if(element.offsetTop > document.body.scrollTop) {
            currentIndex = indx;
            return element;
        }
    }

    return null
}

function findNextSentenceInArray(els) {
    if(currentIndex >= 0) {
        currentIndex ++;
        return els[currentIndex];
    }

    return null
}

function resetCurrentSentenceIndex() {
    currentIndex = -1;
}

function rewindCurrentIndex() {
    currentIndex = currentIndex-1;
}

function getSentenceWithIndex(className) {
    var sentence;
    var sel = getSelection();
    var node = null;
    var elements = document.querySelectorAll("span.sentence");

    // Check for a selected text, if found start reading from it
    if (sel.toString() != "") {
        console.log(sel.anchorNode.parentNode);
        node = sel.anchorNode.parentNode;

        if (node.className == "sentence") {
            sentence = node

            for(var i = 0, len = elements.length; i < len; i++) {
                if (elements[i] === sentence) {
                    currentIndex = i;
                    break;
                }
            }
        } else {
            sentence = findSentenceWithIDInView(elements);
        }
    } else if (currentIndex < 0) {
        sentence = findSentenceWithIDInView(elements);
    } else {
        sentence = findNextSentenceInArray(elements);
    }

    var text = sentence.innerText || sentence.textContent;

    goToElement(sentence);

    if (audioMarkClass){
        removeAllClasses(audioMarkClass);
    }

    audioMarkClass = className;
    sentence.classList.add(className)
    return text;
}

function wrappingSentencesWithinPTags(){
    currentIndex = -1;
    "use strict";

    var rxOpen = new RegExp("<[^\\/].+?>"),
    rxClose = new RegExp("<\\/.+?>"),
    rxSupStart = new RegExp("^<sup\\b[^>]*>"),
    rxSupEnd = new RegExp("<\/sup>"),
    sentenceEnd = [],
    rxIndex;

    sentenceEnd.push(new RegExp("[^\\d][\\.!\\?]+"));
    sentenceEnd.push(new RegExp("(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*?$)"));
    sentenceEnd.push(new RegExp("(?![^\\(]*?\\))"));
    sentenceEnd.push(new RegExp("(?![^\\[]*?\\])"));
    sentenceEnd.push(new RegExp("(?![^\\{]*?\\})"));
    sentenceEnd.push(new RegExp("(?![^\\|]*?\\|)"));
    sentenceEnd.push(new RegExp("(?![^\\\\]*?\\\\)"));
    //sentenceEnd.push(new RegExp("(?![^\\/.]*\\/)")); // all could be a problem, but this one is problematic

    rxIndex = new RegExp(sentenceEnd.reduce(function (previousValue, currentValue) {
                                            return previousValue + currentValue.source;
                                            }, ""));

    function indexSentenceEnd(html) {
        var index = html.search(rxIndex);

        if (index !== -1) {
            index += html.match(rxIndex)[0].length - 1;
        }

        return index;
    }

    function pushSpan(array, className, string, classNameOpt) {
        if (!string.match('[a-zA-Z0-9]+')) {
            array.push(string);
        } else {
            array.push('<span class="' + className + '">' + string + '</span>');
        }
    }

    function addSupToPrevious(html, array) {
        var sup = html.search(rxSupStart),
        end = 0,
        last;

        if (sup !== -1) {
            end = html.search(rxSupEnd);
            if (end !== -1) {
                last = array.pop();
                end = end + 6;
                array.push(last.slice(0, -7) + html.slice(0, end) + last.slice(-7));
            }
        }

        return html.slice(end);
    }

    function paragraphIsSentence(html, array) {
        var index = indexSentenceEnd(html);

        if (index === -1 || index === html.length) {
            pushSpan(array, "sentence", html, "paragraphIsSentence");
            html = "";
        }

        return html;
    }

    function paragraphNoMarkup(html, array) {
        var open = html.search(rxOpen),
        index = 0;

        if (open === -1) {
            index = indexSentenceEnd(html);
            if (index === -1) {
                index = html.length;
            }

            pushSpan(array, "sentence", html.slice(0, index += 1), "paragraphNoMarkup");
        }

        return html.slice(index);
    }

    function sentenceUncontained(html, array) {
        var open = html.search(rxOpen),
        index = 0,
        close;

        if (open !== -1) {
            index = indexSentenceEnd(html);
            if (index === -1) {
                index = html.length;
            }

            close = html.search(rxClose);
            if (index < open || index > close) {
                pushSpan(array, "sentence", html.slice(0, index += 1), "sentenceUncontained");
            } else {
                index = 0;
            }
        }

        return html.slice(index);
    }

    function sentenceContained(html, array) {
        var open = html.search(rxOpen),
        index = 0,
        close,
        count;

        if (open !== -1) {
            index = indexSentenceEnd(html);
            if (index === -1) {
                index = html.length;
            }

            close = html.search(rxClose);
            if (index > open && index < close) {
                count = html.match(rxClose)[0].length;
                pushSpan(array, "sentence", html.slice(0, close + count), "sentenceContained");
                index = close + count;
            } else {
                index = 0;
            }
        }

        return html.slice(index);
    }

    function anythingElse(html, array) {
        pushSpan(array, "sentence", html, "anythingElse");

        return "";
    }

    function guessSenetences() {
        var paragraphs = document.getElementsByTagName("p");

        Array.prototype.forEach.call(paragraphs, function (paragraph) {
            var html = paragraph.innerHTML,
                length = html.length,
                array = [],
                safety = 100;

            while (length && safety) {
                html = addSupToPrevious(html, array);
                if (html.length === length) {
                    if (html.length === length) {
                        html = paragraphIsSentence(html, array);
                        if (html.length === length) {
                            html = paragraphNoMarkup(html, array);
                            if (html.length === length) {
                                html = sentenceUncontained(html, array);
                                if (html.length === length) {
                                    html = sentenceContained(html, array);
                                    if (html.length === length) {
                                        html = anythingElse(html, array);
                                    }
                                }
                            }
                        }
                    }
                }

                length = html.length;
                safety -= 1;
            }

            paragraph.innerHTML = array.join("");
        });
    }

    guessSenetences();
}

function isElementVisible(element, isHorizontal) {

    var rect = element.getBoundingClientRect();

    if(isHorizontal)
        return rect.left > 0;
    else
        return rect.top > 0;
}

/**
Gets the first visible span from the displayed chapter and if it has id then usingId is true with
value as span id else usingId is false with value as span index. usingId and value is forwarded to
FolioPageFragment#storeFirstVisibleSpan(boolean, String) JavascriptInterface.

@param {boolean} isHorizontal - scrolling type of DirectionalViewpager#mDirection
*/
function getFirstVisibleSpan(isHorizontal) {

    var spanCollection = document.getElementsByTagName("span");

    if (spanCollection.length == 0) {
        FolioPageFragment.storeFirstVisibleSpan(false, 0);
        return;
    }

    var spanIndex = 0;
    var spanElement;

    for (var i = 0 ; i < spanCollection.length ; i++) {
        if (isElementVisible(spanCollection[i], isHorizontal)) {
            spanIndex = i;
            spanElement = spanCollection[i];
            break;
        }
    }

    var usingId = spanElement.id ? true : false;
    console.log("usingId = " + usingId);
    console.log("spanElement.id = " + spanElement.id);
    console.log("spanIndex = " + spanIndex);
    var value = usingId ? spanElement.id : spanIndex;
    FolioPageFragment.storeFirstVisibleSpan(usingId, value);
}

/**
Scrolls the web page to particular span using id or index

@param {boolean} usingId - if span tag has id then true or else false
@param {number} value - if usingId true then span id else span index
*/
function scrollToSpan(usingId, value) {
    console.log(">>>scrollToSpan1");
    var usingIdType = typeof usingId;

    console.log(">>>usingId type = " + usingIdType);
    console.log(">>>usingId " + usingId);

    if(usingId == "false") {
        usingId = false;
    }

    if (usingId) {
        var spanElement = document.getElementById(value);
        if (spanElement)
            goToElement(spanElement);
    } else {
        var spanCollection = document.getElementsByTagName("span");
        console.log("spanCollection = " + spanCollection.length);
        console.log("value = " + value);
        if (spanCollection.length == 0 || value < 0 || value >= spanCollection.length
            || value == null) {
            console.log("bug case >>>>>>>>>>");
                FolioPageFragment.hideLoading();
            return;
        }
        console.log(">>>scrollToSpan2");
        goToElement(spanCollection[value]);
    }

    
        FolioPageFragment.hideLoading();
}

// Class based onClick listener

function addClassBasedOnClickListener(schemeName, querySelector, attributeName, selectAll) {
    if (selectAll) {
        // Get all elements with the given query selector
        var elements = document.querySelectorAll(querySelector);
        for (elementIndex = 0; elementIndex < elements.length; elementIndex++) {
            var element = elements[elementIndex];
            addClassBasedOnClickListenerToElement(element, schemeName, attributeName);
        }
    } else {
        // Get the first element with the given query selector
        var element = document.querySelector(querySelector);
        addClassBasedOnClickListenerToElement(element, schemeName, attributeName);
    }
}

function addClassBasedOnClickListenerToElement(element, schemeName, attributeName) {
    // Get the content from the given attribute name
    var attributeContent = element.getAttribute(attributeName);
    // Add the on click logic
    element.setAttribute("onclick", "onClassBasedListenerClick(\"" + schemeName + "\", \"" + encodeURIComponent(attributeContent) + "\");");
}

var onClassBasedListenerClick = function(schemeName, attributeContent) {
    // Prevent the browser from performing the default on click behavior
    event.preventDefault();
    // Don't pass the click event to other elemtents
    event.stopPropagation();
    // Create parameters containing the click position inside the web view.
    var positionParameterString = "/clientX=" + event.clientX + "&clientY=" + event.clientY;
    // Set the custom link URL to the event
    window.location = schemeName + "://" + attributeContent + positionParameterString;
}

function getHighlightString(style) {
    var range = window.getSelection().getRangeAt(0);
    var selectionContents = range.extractContents();
    var elm = document.createElement("highlight");
    var id = guid();

    elm.appendChild(selectionContents);
    elm.setAttribute("id", id);
    elm.setAttribute("onclick","callHighlightURL(this);");
    elm.setAttribute("class", style);

    range.insertNode(elm);
    thisHighlight = elm;

    var params = [];
    params.push({id: id, rect: getRectForSelectedText(elm)});
    Highlight.getHighlightJson(JSON.stringify(params));
}

function goToHighlight(highlightId){
    var element = document.getElementById(highlightId.toString());
    if (element) {
    console.log("goToHighlight : element = " + element.id);
        goToElement(element);
    }

    else
        console.log(">>>>>> highlight element " + highlightId + " was not found !");
    FolioPageFragment.hideLoading();
}

function goToAnchor(anchorId) {
    var element = document.getElementById(anchorId);
    console.log("=====>element = " + element)
    if (element)
        goToElement(element);
    FolioPageFragment.hideLoading();
}

 $(window.document).ready(function(){
    window.ssReader = Class({
    $singleton: true,

    init: function() {
      rangy.init();

      this.highlighter = rangy.createHighlighter();

      this.highlighter.addClassApplier(rangy.createClassApplier("highlight_yellow", {
        ignoreWhiteSpace: true,
        tagNames: ["span", "a"]
      }));

      this.highlighter.addClassApplier(rangy.createClassApplier("highlight_green", {
        ignoreWhiteSpace: true,
        tagNames: ["span", "a"]
      }));

      this.highlighter.addClassApplier(rangy.createClassApplier("highlight_blue", {
        ignoreWhiteSpace: true,
        tagNames: ["span", "a"]
      }));

      this.highlighter.addClassApplier(rangy.createClassApplier("highlight_pink", {
        ignoreWhiteSpace: true,
        tagNames: ["span", "a"]
      }));

      this.highlighter.addClassApplier(rangy.createClassApplier("highlight_underline", {
        ignoreWhiteSpace: true,
        tagNames: ["span", "a"]
      }));

    },

    setFontAndada: function(){
      this.setFont("andada");
    },

    setFontLato: function(){
      this.setFont("lato");
    },

    setFontPtSerif: function(){
      this.setFont("pt-serif");
    },

    setFontPtSans: function(){
      this.setFont("pt-sans");
    },

    base64encode: function(str){
      return btoa(unescape(encodeURIComponent(str)));
    },

    base64decode: function(str){
      return decodeURIComponent(escape(atob(str)));
    },

    clearSelection: function(){
      if (window.getSelection) {
        if (window.getSelection().empty) {  // Chrome
          window.getSelection().empty();
        } else if (window.getSelection().removeAllRanges) {  // Firefox
          window.getSelection().removeAllRanges();
        }
      } else if (document.selection) {  // IE?
        document.selection.empty();
      }
    },

    // Public methods

    setFont: function(fontName){
      $("#ss-wrapper-font").removeClass().addClass("ss-wrapper-"+fontName);
    },

    setSize: function(size){
      $("#ss-wrapper-size").removeClass().addClass("ss-wrapper-"+size);
    },

    setTheme: function(theme){
      $("body, #ss-wrapper-theme").removeClass().addClass("ss-wrapper-"+theme);
    },

    setComment: function(comment, inputId){
      $("#"+inputId).val(ssReader.base64decode(comment));
      $("#"+inputId).trigger("input", ["true"]);
    },

    highlightSelection: function(color){
      try {

        this.highlighter.highlightSelection("highlight_" + color, null);
        var range = window.getSelection().toString();
        var params = {content: range,rangy: this.getHighlights(),color: color};
        this.clearSelection();
        Highlight.onReceiveHighlights(JSON.stringify(params));
      } catch(err){
        console.log("highlightSelection : " + err);
      }
    },

    unHighlightSelection: function(){
      try {
        this.highlighter.unhighlightSelection();
        Highlight.onReceiveHighlights(this.getHighlights());
      } catch(err){}
    },

    getHighlights: function(){
      try {
        return this.highlighter.serialize();
      } catch(err){}
    },

    setHighlights: function(serializedHighlight){
      try {
        this.highlighter.removeAllHighlights();
        this.highlighter.deserialize(serializedHighlight);
      } catch(err){}
    },

    removeAll: function(){
      try {
        this.highlighter.removeAllHighlights();
      } catch(err){}
    },

    copy: function(){
      SSBridge.onCopy(window.getSelection().toString());
      this.clearSelection();
    },

    share: function(){
      SSBridge.onShare(window.getSelection().toString());
      this.clearSelection();
    },

    search: function(){
      SSBridge.onSearch(window.getSelection().toString());
      this.clearSelection();
    }
  });

    if(typeof window.ssReader !== "undefined"){
      window.ssReader.init();
    }
    
    $(".verse").click(function(){
      SSBridge.onVerseClick(window.ssReader.base64encode($(this).attr("verse")));
    });
    
    $("code").each(function(i){
      var textarea = $("<textarea class='textarea'/>").attr("id", "input-"+i).on("input propertychange", function(event, isInit) {
        $(this).css({'height': 'auto', 'overflow-y': 'hidden'}).height(this.scrollHeight);
        $(this).next().css({'height': 'auto', 'overflow-y': 'hidden'}).height(this.scrollHeight);

        if (!isInit) {
          var that = this;
          if (timeout !== null) {
            clearTimeout(timeout);
          }
          timeout = setTimeout(function () {
            SSBridge.onCommentsClick(
                ssReader.base64encode($(that).val()),
                $(that).attr("id")
            );
          }, 1000);
        }
      });
      var border = $("<div class='textarea-border' />");
      var container = $("<div class='textarea-container' />");

      $(textarea).appendTo(container);
      $(border).appendTo(container);

      $(this).after(container);
    });
});

function array_diff(array1, array2){
    var difference = $.grep(array1, function(el) { return $.inArray(el,array2) < 0});
    return difference.concat($.grep(array2, function(el) { return $.inArray(el,array1) < 0}));;
}
