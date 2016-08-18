
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.jobscheduler.post.JobSchedulerDefaultBody;
import com.sos.joc.response.JOCDefaultResponse;

public interface IJobSchedulerResourceClusterMembers {

    @POST
    @Path("cluster/members")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobschedulerClusterMembers(@HeaderParam("access_token") String accessToken,
            JobSchedulerDefaultBody jobSchedulerDefaultBody) throws Exception;

 

}
