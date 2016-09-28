package com.sos.joc.orders.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.order.ModifyOrdersSchema;

public interface IOrdersResourceCommandAddOrder {

    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrdersAdd(@HeaderParam("access_token") String accessToken, ModifyOrdersSchema modifyOrdersSchema) throws Exception;

}