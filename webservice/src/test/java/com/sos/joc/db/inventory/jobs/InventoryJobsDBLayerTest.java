package com.sos.joc.db.inventory.jobs;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.sos.auth.rest.SOSServicePermissionShiro;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.instances.InventoryInstancesDBLayer;

public class InventoryJobsDBLayerTest {
    private static final String LDAP_PASSWORD = "sos01";
    private static final String LDAP_USER = "SOS01";

    @Test
    public void getJobSchedulerJobs() throws Exception {
        InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(Globals.createSosHibernateStatelessConnection("InventoryInstancesDBLayer"));
        DBItemInventoryInstance instance = instanceLayer.getInventoryInstanceBySchedulerId("scheduler_current", getAccessToken());
        InventoryJobsDBLayer dbLayer = new InventoryJobsDBLayer(Globals.createSosHibernateStatelessConnection("InventoryJobsDBLayer"));
        
        List<DBItemInventoryJob>  listOfJobs = dbLayer.getInventoryJobs(instance.getId());
        
        assertEquals("getJobSchedulerJobs", "batch_install_universal_agent/PerformInstall", listOfJobs.get(0).getName());

    }
    
    
    @Test
    public void getJobSchedulerJob() throws Exception {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        sosServicePermissionShiro.loginPost("", LDAP_USER, LDAP_PASSWORD).getEntity();
        InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(Globals.createSosHibernateStatelessConnection("InventoryInstancesDBLayer"));
        DBItemInventoryInstance instance = instanceLayer.getInventoryInstanceBySchedulerId("scheduler_current", getAccessToken());
        InventoryJobsDBLayer dbLayer = new InventoryJobsDBLayer(Globals.createSosHibernateStatelessConnection("InventoryJobsDBLayer"));
        
        DBItemInventoryJob job = dbLayer.getInventoryJobByName("batch_install_universal_agent/PerformInstall", instance.getId());
        
        assertEquals("getJobSchedulerJob", "PerformInstall", job.getBaseName());

    }

    private String getAccessToken() {
        return UUID.randomUUID().toString();
    }

}
