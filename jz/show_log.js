// $Id$

// Javascript-Code f�r HTTP Show log des Schedulers.
// Der Scheduler liefert �ber HTTP die Ausgaben eines Protokolls bis dieses geschlossen wird.


//-------------------------------------------------------------------------------------------------

var debug_window;

/*
if(false)
{
    debug_window = window.open( "", "",      "width="  + ( window.screen.availWidth - 7 )+
                                           ", height=" + ( Math.floor( window.screen.availHeight * 0.2 ) - 30 ) +
                                           ", left=0"  +
                                           ", top="    +  Math.floor( window.screen.availHeight * 0.8 ) +
                                           ", location=no, menubar=no, toolbar=no, status=no, scrollbars=yes, resizable=yes" );
}
*/
//----------------------------------------------------------------------------------------------var

var timer;
var program_is_scrolling = true;
var error                = false;   // Nach Fehler kein Timer-Intervall mehr

//-------------------------------------------------------------------------------------------------

start_timer();
modify_title();

document.onreadystatechange = document__onreadystatechange;
window.onscroll             = window__onscroll;

if( window.navigator.vendor == "Firefox" )      // Firefox ruft onscroll nur auf, wenn der Knopf in der Bildlaufleiste mit der Maus verschoben wird.
{                                               // Nicht beim Mausklick auf die Leiste oder bei Page-Up.
    window.onkeydown            = stop_timer;
    window.onmousedown          = stop_timer;
    window.onkeyup              = window__onscroll;
    window.onmouseup            = window__onscroll;
}

//--------------------------------------------------------------------------------------start_timer

function start_timer()
{
    stop_timer();
    
    if( !error )  timer = window.setInterval( "scroll_down()", 100, "JavaScript" );
}

//---------------------------------------------------------------------------------------stop_timer

function stop_timer()
{
    if( timer != undefined )
    {
        window.clearInterval( timer );
        timer = undefined;
    }
}

//--------------------------------------------------------------------------------------scroll_down

function scroll_down()
{
    try
    {
        program_is_scrolling = true;
        window.scrollTo( document.body.scrollLeft, document.body.scrollHeight );
    }
    catch( x )
    {
        error = true;
        alert( x.message );
        window.clearInterval( timer );
        timer = undefined;
    }
}

//---------------------------------------------------------------------------------window__onscroll

function window__onscroll()
{
    // Bei Reload scrollt der Browser, bevor der Timer gesetzt ist.
    
    if( debug_window )  debug_window.document.write( "window__onscroll()  timer=" + timer + " program_is_scrolling=" + program_is_scrolling );
    
    if( !program_is_scrolling ) 
    {
        if( document.body.scrollTop + document.body.clientHeight == document.body.scrollHeight )
        {
            if( timer == undefined )  start_timer();
        }
        else
            stop_timer(); 
    }

    program_is_scrolling = false;
}

//-------------------------------------------------------------------------------------modify_title
// document.title += " (running)" oder " (terminated)"

function modify_title()
{
    if( title != undefined )  document.title = "Scheduler - " + title;

    var title_state;
    
    if( document.readyState == "interactive" )  title_state = "(running)";
    else
    if( document.readyState == "complete" )     title_state = "(terminated)";

    if( title_state )  document.title = document.title.replace( / *(\(.*\))|$/, " " + title_state );
}

//---------------------------------------------------------------------document__onreadystatechange

function document__onreadystatechange()
{
    if( document.readyState == "complete" )
    {
        modify_title();
    }
}

//-------------------------------------------------------------------------------------------------
