package com.sos.joc.locks.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSServicePermissionShiro;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.joc.model.lock.LocksFilter;
import com.sos.joc.model.lock.LocksP;

public class LocksResourcePImplTest {
    private static final String LDAP_PASSWORD = "secret";
    private static final String LDAP_USER = "root";
    private static final Logger LOGGER = LoggerFactory.getLogger(LocksResourcePImplTest.class);
    private static final String SCHEDULER_ID = "scheduler_4444";

    @Test
    public void postLocksTest() throws Exception {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.loginPost("", LDAP_USER, LDAP_PASSWORD).getEntity();
        LocksFilter locksFilterSchema = new LocksFilter();
        locksFilterSchema.setJobschedulerId(SCHEDULER_ID);
        LocksResourcePImpl locksResourcePImpl = new LocksResourcePImpl();
        JOCDefaultResponse jobsResponse = locksResourcePImpl.postLocksP(sosShiroCurrentUserAnswer.getAccessToken(), locksFilterSchema);
        LocksP locksVSchema = (LocksP) jobsResponse.getEntity();
        assertEquals("postLocksTest", "myName", locksVSchema.getLocks().get(0).getName());
        LOGGER.info(jobsResponse.toString());
    }

}
