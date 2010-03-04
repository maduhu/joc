/********************************************************* begin of preamble
**
** Copyright (C) 2003-2010 Software- und Organisations-Service GmbH. 
** All rights reserved.
**
** This file may be used under the terms of either the 
**
**   GNU General Public License version 2.0 (GPL)
**
**   as published by the Free Software Foundation
**   http://www.gnu.org/licenses/gpl-2.0.txt and appearing in the file
**   LICENSE.GPL included in the packaging of this file. 
**
** or the
**  
**   Agreement for Purchase and Licensing
**
**   as offered by Software- und Organisations-Service GmbH
**   in the respective terms of supply that ship with this file.
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
** IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
** THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
** PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
** BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
** CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
** SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
** INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
** CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
** ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
** POSSIBILITY OF SUCH DAMAGE.
********************************************************** end of preamble*/

//----------------------------Initialising of global vars which are set in the custom.js file
var _update_periodically        = false;
var _update_seconds             = 5;
var _update_incl_hot_folders    = false;
var _show_card                  = 'jobs';
var _checkbox_states            = new Object();
var _radio_states               = new Object();
var _select_states              = new Object();
var _max_orders                 = 10;
var _max_last_activities        = 30;
var _max_order_history          = 50;
var _max_task_history           = 10;
var _description_lang           = 'en';
var _terminate_timeout          = 60;
var _start_at_default_is_now    = true;
var _debug_level                = 0;
var _extra_items                = new Object();
_extra_items.monitor            = true;
_extra_items.configuration      = true;
var _extra_urls                 = new Object();
var _url_aliase                 = new Object();
var _sos_lang                   = '';
var _job_filter                 = new Object();
var _job_chain_filter           = new Object();
var _order_filter               = new Object();
var _project_filter             = new Object();
var _active_job_filter          = -1;
var _active_job_chain_filter    = -1;
var _active_order_filter        = -1;
var _active_project_filter      = -1;


//--------------------------------------------------------------------------custom.js per hash  
var _server_settings            = location.hash && location.hash.substr(1);
document.writeln('<script type="text/javascript" src="custom.js"></sc'+'ript>');
if( _server_settings && location.hash.substr(1) != 'custom' ) {
  document.writeln('<script type="text/javascript" src="'+location.hash.substr(1)+'.js"></sc'+'ript>');
}


//------------------------------------------------------------------------------getTranslation
var _lang_file_exists           = false;
var _translations               = new Object();

function getTranslation( s, args )
{   
    if(!_lang_file_exists && typeof args != 'object' ) return s;
    if( _lang_file_exists && typeof _translations[s] == 'string' ) s = _translations[s];
    if( typeof args == 'object' )  {
      for( var entry in args ) {
        s = s.replace(new RegExp('\\$'+entry,'g'),args[entry]);
      }
    }
    return s;
}

//------------------------------------------------------------------------------------check_browser
var ie        = 0;   // Microsoft Internet Explorer
var firefox   = 0;   // Mozilla Firefox
var chrome    = 0;   // Google Chrome
var seamonkey = 0;   // SeaMonkey

function check_browser()
{   
    if( window != undefined )
    {                
        if( window.navigator != undefined  &&  window.navigator.appName )
        {   
            if( window.navigator.appName == "Microsoft Internet Explorer" )
            {
                var match = window.navigator.appVersion.match( /MSIE (\d+\.\d+);/ );
                if( match )  ie = 1 * RegExp.$1;
            }
            else
            if( window.navigator.appName == "Netscape" )
            {
                if( window.navigator.vendor == "Firefox" || window.navigator.userAgent.indexOf( "Firefox" ) > -1 )
                {
                    firefox = 0.1;
                    if( window.navigator.productSub >= 20041108 )  firefox = 1;
                }
                else
                if( window.navigator.vendor == "Google Inc." || window.navigator.userAgent.indexOf( "Chrome" ) > -1 )
                {
                    var match = window.navigator.appVersion.match( /Chrome\/(\d+\.\d+)/ );
                    if( match )  chrome = 1 * RegExp.$1;
                }
                else
                if( window.navigator.userAgent.indexOf("SeaMonkey") > -1 )
                {
                    var match = window.navigator.userAgent.match( /SeaMonkey\/(\d+\.\d+)/ );
                    if( match )  seamonkey = 1 * RegExp.$1;
                }
            }
        }
    }
    
    if( ie < 6 && firefox < 1 && chrome < 0.2 && seamonkey < 2.0 )
    {
        var allBrowser = ie+firefox+chrome+seamonkey;
        var msg = "The page may not work with this browser.\n\n";
        if( allBrowser == 0 ) 
        {
          msg += "Please use\n";
          msg += "  - Microsoft Internet Explorer\n";
          msg += "  - Mozilla Firefox\n";
          msg += "  - Google Chrome\n";
          msg += "  - SeaMonkey";
        } 
        else 
        { 
          if( allBrowser-ie        == 0 ) msg += "The Microsoft Internet Explorer version should be at least 6.0";
          if( allBrowser-firefox   == 0 ) msg += "The Mozilla Firefox version should be at least 1.0";
          if( allBrowser-chrome    == 0 ) msg += "The Google Chrome version should be at least 0.2";
          if( allBrowser-seamonkey == 0 ) msg += "The SeaMonkey version should be at least 2.0";
        }

        if( window.navigator != undefined )
        {
            msg += "\n\n\n";
            msg += "userAgent="       + window.navigator.userAgent          + "\n";
            msg += "appName="         + window.navigator.appName            + "\n";
            msg += "appVersion="      + window.navigator.appVersion         + "\n";
            msg += "appMinorVersion=" + window.navigator.appMinorVersion    + "\n";
            msg += "vendor="          + window.navigator.vendor             + "\n";
            msg += "product="         + window.navigator.product            + "\n";
            msg += "productSub="      + window.navigator.productSub         + "\n";
        }

        alert( msg );
    }
}