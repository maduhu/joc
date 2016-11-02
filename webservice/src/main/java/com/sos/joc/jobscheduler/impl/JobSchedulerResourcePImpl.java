package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.hibernate.HibernateException;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.JobSchedulerPermanent;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceP;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.JobSchedulerP200;

@Path("jobscheduler")
public class JobSchedulerResourcePImpl extends JOCResourceImpl implements IJobSchedulerResourceP {

    private static final String API_CALL = "./jobscheduler/p";

    @Override
    public JOCDefaultResponse postJobschedulerP(String accessToken, JobSchedulerId jobSchedulerId) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(accessToken, jobSchedulerId.getJobschedulerId(), getPermissons(accessToken)
                    .getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Globals.beginTransaction();
            JobSchedulerP200 entity = new JobSchedulerP200();
            entity.setJobscheduler(JobSchedulerPermanent.getJobScheduler(dbItemInventoryInstance, false));
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getMetaInfo(API_CALL, jobSchedulerId));
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            JocError err = new JocError();
            err.addMetaInfoOnTop(getMetaInfo(API_CALL, jobSchedulerId));
            return JOCDefaultResponse.responseStatusJSError(e, err);
        } finally {
            Globals.rollback();
        }
    }
    
}