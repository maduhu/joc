package com.sos.joc.tasks.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.job.JobsFilter;

public interface ITasksResourceHistory {

    @POST
    @Path("history")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTasksHistory(@HeaderParam("X-Access-Token") String xAccessToken,@HeaderParam("access_token") String accessToken, JobsFilter jobsFilter) throws Exception;

}
