
package com.sos.joc.jobchain.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobChain.JobChainFilter;

public interface IJobChainResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobChain(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("access_token") String accessToken,
            JobChainFilter jobChainFilter) throws Exception;
}
