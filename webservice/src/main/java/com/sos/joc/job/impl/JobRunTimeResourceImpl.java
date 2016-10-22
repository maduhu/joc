package com.sos.joc.job.impl;

import java.math.BigInteger;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JOCXmlCommand;
import com.sos.joc.classes.runtime.RunTime;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.job.resource.IJobRunTimeResource;
import com.sos.joc.model.common.RunTime200;
import com.sos.joc.model.job.JobFilter;
import com.sos.scheduler.model.commands.JSCmdShowJob;

@Path("job")
public class JobRunTimeResourceImpl extends JOCResourceImpl implements IJobRunTimeResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunTimeResourceImpl.class);

    @Override
    public JOCDefaultResponse postJobRunTime(String accessToken, JobFilter jobFilter) throws Exception {
        LOGGER.debug("init job/run_time");
        try {
            JOCDefaultResponse jocDefaultResponse = init(jobFilter.getJobschedulerId(), getPermissons(accessToken).getJob().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            RunTime200 runTimeAnswer = new RunTime200();
            JOCXmlCommand jocXmlCommand = new JOCXmlCommand(dbItemInventoryInstance.getUrl());
            if (checkRequiredParameter("job", jobFilter.getJob())) {
                runTimeAnswer = RunTime.set(jocXmlCommand, createJobRuntimePostCommand(jobFilter), "//job/run_time");
            }
            return JOCDefaultResponse.responseStatus200(runTimeAnswer);
        } catch (JocException e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        }

    }

    private String createJobRuntimePostCommand(JobFilter body) {

        JSCmdShowJob showJob = Globals.schedulerObjectFactory.createShowJob();
        showJob.setWhat("run_time");
        showJob.setJob(("/" + body.getJob()).replaceAll("//+", "/"));
        showJob.setMaxOrders(BigInteger.valueOf(0));
        showJob.setMaxTaskHistory(BigInteger.valueOf(0));
        return Globals.schedulerObjectFactory.toXMLString(showJob);
    }

}
