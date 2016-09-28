
package com.sos.joc.order.resource;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrderLogHtmlResource {

    @GET
    @Path("log/html")
    @Produces({ MediaType.TEXT_HTML})
    public JOCDefaultResponse getOrderLogHtml(@HeaderParam("access_token") String accessToken, @QueryParam("jobschedulerId")  String jobschedulerId) throws Exception;

}

 

