package com.sos.joc.jobscheduler.impl;
 
import static org.junit.Assert.*;
 
import org.junit.Test;
import com.sos.auth.rest.SOSServicePermissionShiro;
import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.jobscheduler.impl.JobSchedulerResourceAgentClustersImpl;
import com.sos.joc.model.jobscheduler.AgentClusterFilter;
import com.sos.joc.model.jobscheduler.AgentClustersV;

public class JobSchedulerResourceAgentClustersImplTest {
    private static final String LDAP_PASSWORD = "secret";
    private static final String LDAP_USER = "root";
     
    @Test
    public void postjobschedulerAgentClustersTest() throws Exception   {
         
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.loginPost("", LDAP_USER, LDAP_PASSWORD).getEntity();
        AgentClusterFilter agentClusterFilterSchema = new AgentClusterFilter();
        agentClusterFilterSchema.setJobschedulerId("scheduler_current");
        JobSchedulerResourceAgentClustersImpl jobschedulerResourceAgentClustersImpl = new JobSchedulerResourceAgentClustersImpl();
        JOCDefaultResponse jobschedulerClusterResponse = jobschedulerResourceAgentClustersImpl.postJobschedulerAgentClusters(sosShiroCurrentUserAnswer.getAccessToken(), agentClusterFilterSchema);
        AgentClustersV agentClustersVSchema = (AgentClustersV) jobschedulerClusterResponse.getEntity();
        assertEquals("postjobschedulerClusterTest", -1, agentClustersVSchema.getAgentClusters().get(0).getNumOfAgents().getAny().intValue());
     }

}

