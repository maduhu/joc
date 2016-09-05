package com.sos.joc.jobscheduler.impl;
 
import static org.junit.Assert.*;
 
import org.junit.Test;
import com.sos.auth.rest.SOSServicePermissionShiro;
import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.jobscheduler.impl.JobSchedulerResourceClusterImpl;
import com.sos.joc.model.common.JobSchedulerFilterSchema;
import com.sos.joc.model.jobscheduler.ClusterSchema;

public class JobSchedulerResourceClusterImplTest {
    private static final String LDAP_PASSWORD = "secret";
    private static final String LDAP_USER = "root";
     
    @Test
    public void postjobschedulerClusterTest() throws Exception   {
         
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.loginGet("", LDAP_USER, LDAP_PASSWORD).getEntity();
        JobSchedulerFilterSchema jobSchedulerFilterSchema = new JobSchedulerFilterSchema();
        jobSchedulerFilterSchema.setJobschedulerId("scheduler_current");
        JobSchedulerResourceClusterImpl jobschedulerResourceClusterImpl = new JobSchedulerResourceClusterImpl();
        JOCDefaultResponse jobschedulerClusterResponse = jobschedulerResourceClusterImpl.postJobschedulerCluster(sosShiroCurrentUserAnswer.getAccessToken(), jobSchedulerFilterSchema);
        ClusterSchema jobscheduler200VSchema = (ClusterSchema) jobschedulerClusterResponse.getEntity();
        assertEquals("postjobschedulerClusterTest", "scheduler_current", jobscheduler200VSchema.getCluster().getJobschedulerId());
     }

}

