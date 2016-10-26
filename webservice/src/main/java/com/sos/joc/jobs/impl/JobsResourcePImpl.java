package com.sos.joc.jobs.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobs.JobPermanent;
import com.sos.joc.db.inventory.jobs.InventoryJobsDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResourceP;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.JobP;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.JobsP;

@Path("jobs")
public class JobsResourcePImpl extends JOCResourceImpl implements IJobsResourceP {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsResourcePImpl.class);
    private static final String API_CALL = "./jobs/p";
    private String regex;
    private List<Folder> folders;
    private List<JobPath> jobs;
    private Boolean isOrderJob;

    @Override
    public JOCDefaultResponse postJobsP(String accessToken, JobsFilter jobsFilterSchema) throws Exception {
        LOGGER.debug(API_CALL);
        try {
            Globals.beginTransaction();
            JOCDefaultResponse jocDefaultResponse = init(jobsFilterSchema.getJobschedulerId(), getPermissons(accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Boolean compact = jobsFilterSchema.getCompact();
            regex = jobsFilterSchema.getRegex();
            folders = jobsFilterSchema.getFolders();
            jobs = jobsFilterSchema.getJobs();
            isOrderJob = jobsFilterSchema.getIsOrderJob();
            List<JobP> listJobs = new ArrayList<JobP>();
            InventoryJobsDBLayer dbLayer = new InventoryJobsDBLayer(Globals.sosHibernateConnection);
            Long instanceId = dbItemInventoryInstance.getId();
            List<DBItemInventoryJob> listOfJobs = processFilters(dbLayer);
            for (DBItemInventoryJob inventoryJob : listOfJobs) {
                JobP job = JobPermanent.getJob(inventoryJob, dbLayer, compact, instanceId);
                if (job != null) {
                    listJobs.add(job);
                }
            }
            JobsP entity = new JobsP();
            entity.setJobs(listJobs);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getMetaInfo(API_CALL, jobsFilterSchema));
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            JocError err = new JocError();
            err.addMetaInfoOnTop(getMetaInfo(API_CALL, jobsFilterSchema));
            return JOCDefaultResponse.responseStatusJSError(e, err);
        }
        finally{
            Globals.rollback();
        }
    }
    
    private List<DBItemInventoryJob> filterByRegex(List<DBItemInventoryJob> unfilteredJobs, String regex) throws Exception {
        List<DBItemInventoryJob> filteredJobs = new ArrayList<DBItemInventoryJob>();
        for (DBItemInventoryJob unfilteredJob : unfilteredJobs) {
            Matcher regExMatcher = Pattern.compile(regex).matcher(unfilteredJob.getName());
            if (regExMatcher.find()) {
                filteredJobs.add(unfilteredJob); 
            }
        }
        if(!filteredJobs.isEmpty()) {
            return filteredJobs;
        } else {
            return null;
        }
    }
    
    private List<DBItemInventoryJob> processFilters(InventoryJobsDBLayer dbLayer) throws Exception {
        List<DBItemInventoryJob> listOfJobs = null;
        if (jobs != null && !jobs.isEmpty()) {
            listOfJobs = new ArrayList<DBItemInventoryJob>();
            List<DBItemInventoryJob> filteredJobs = null;
            for (JobPath jobPathFilter : jobs) {
                if (isOrderJob != null) {
                    filteredJobs = 
                            dbLayer.getInventoryJobsFilteredByJobPath(jobPathFilter.getJob(), isOrderJob, dbItemInventoryInstance.getId());
                } else {
                    filteredJobs = dbLayer.getInventoryJobsFilteredByJobPath(jobPathFilter.getJob(), null, dbItemInventoryInstance.getId());
                }
                if (filteredJobs != null) {
                    listOfJobs.addAll(filteredJobs);
                }
            }
        } else if (folders != null && !folders.isEmpty()) {
            listOfJobs = new ArrayList<DBItemInventoryJob>();
            List<DBItemInventoryJob> filteredJobs = null;
            for (Folder folderFilter : folders) {
                filteredJobs = dbLayer.getInventoryJobsFilteredByFolder(folderFilter.getFolder(), isOrderJob, folderFilter.getRecursive(),
                        dbItemInventoryInstance.getId());
                if (filteredJobs != null && !filteredJobs.isEmpty()) {
                    if (regex != null && !regex.isEmpty()) {
                        List<DBItemInventoryJob> jobsFilteredByRegex = filterByRegex(filteredJobs, regex);
                        if (jobsFilteredByRegex != null) {
                            listOfJobs.addAll(jobsFilteredByRegex);
                        }
                    } else {
                        listOfJobs.addAll(filteredJobs);
                    }
                }
            }
        } else {
            listOfJobs = new ArrayList<DBItemInventoryJob>();
            List<DBItemInventoryJob> unfilteredJobs = null;
            if (isOrderJob != null) {
                unfilteredJobs = dbLayer.getInventoryJobs(isOrderJob, dbItemInventoryInstance.getId());
            } else {
                unfilteredJobs = dbLayer.getInventoryJobs(dbItemInventoryInstance.getId());
            }
            if (unfilteredJobs != null && !unfilteredJobs.isEmpty()) {
                if (regex != null && !regex.isEmpty()) {
                    List<DBItemInventoryJob> jobsFilteredByRegex = filterByRegex(unfilteredJobs, regex);
                    if (jobsFilteredByRegex != null) {
                        listOfJobs.addAll(jobsFilteredByRegex);
                    }
                } else {
                    listOfJobs.addAll(unfilteredJobs);
                }
            }
        }
        return listOfJobs;
    }

}