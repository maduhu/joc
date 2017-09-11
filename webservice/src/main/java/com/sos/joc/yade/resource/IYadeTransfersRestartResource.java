package com.sos.joc.yade.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.yade.ModifyTransfers;


public interface IYadeTransfersRestartResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postYadeTransfersRestart(@HeaderParam("access_token") String accessToken, ModifyTransfers filterBody)
            throws Exception;
}
