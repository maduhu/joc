// $Id: popup_builder.js,v 1.2 2004/07/21 08:20:36 jz Exp $

//------------------------------------------------------------------------------------Popup_builder

function Popup_builder()
{
    this._html_array = new Array();
    this._html_array.push( "<html><head>" );
  //this._html_array.push( "<style type='text/css'>" );
  //this._html_array.push( "    td { background-color: menu; color: menutext; font-family: Sans-Serif; font-size: 9pt }" );
  //this._html_array.push( "</style>" );
    this._html_array.push( "</head><body>" );
    this._html_array.push( "<table cellpadding='0' cellspacing='0' width='100%' style='padding-left=3px; padding-right=3px; border: thin outset' >" );
    this._finished = false;
}

//--------------------------------------------------------------------------Popup_builder.add_entry

Popup_builder.prototype.add_entry = function( html_entry, call, is_active )
{
    if( is_active == undefined )  is_active = true;
    
    html =  "<tr>";
    html += "<td style='";
    html +=         "background-color: menu;";
    html +=         "color: " + ( is_active? "menutext" : "gray" ) + ";";
    html +=         "font-family: Sans-Serif; font-size: 9pt; cursor: default; white-space: nowrap'";

    if( is_active )
    {
        html +=   " onmouseover='this.style.backgroundColor=\"highlight\"; this.style.color=\"highlighttext\"'";
        html +=   " onmouseout='this.style.backgroundColor=\"menu\"; this.style.color=\"menutext\"'";
        html +=   " onclick=\"" + call.replace( "\"", "\\\"" ) + "\"";
    }
    
    html +=  ">";
    html +=  html_entry;
    html += "</td>";
    html += "</tr>";
                         
    this._html_array.push( html );
}

//-------------------------------------------------------------------------------Popup_builder.html

Popup_builder.prototype.html = function()
{
    if( !this._finished )
    {
        this._html_array.push( "</table></body></html>" );
        this._finished = true;
    }
    
    return this._html_array.join( "" );
}

//-----------------------------------------------------------------------Popup_builder.create_popup

Popup_builder.prototype.create_popup = function()
{
    if( !this._popup )
    {
        this._popup = window.createPopup();
        //alert( this._html_array.join( "\n" ) );
        this._popup.document.body.innerHTML = this.html();
    }
    
    return this._popup;
}

//---------------------------------------------------------------------------------------show_popup

Popup_builder.prototype.show_popup = function()
{
    this.create_popup();
    
    this._popup.show( 0, 0, 0, 0 );
    var width  = this._popup.document.body.scrollWidth;
    var height = this._popup.document.body.scrollHeight;
    this._popup.hide();
    this._popup.show( 0, 15, width, height, event.srcElement );
    
    return this._popup;
}

//-------------------------------------------------------------------------------------------------
