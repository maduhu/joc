
package com.sos.joc.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.order.OrderFilter;

public interface IOrderCalendarsResource {

    @POST
    @Path("calendars")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderCalendars(@HeaderParam("X-Access-Token") String xAccessToken, OrderFilter orderFilter) throws Exception;

}
