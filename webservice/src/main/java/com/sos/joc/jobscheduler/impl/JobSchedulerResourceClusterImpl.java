package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JOCXmlCommand;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceCluster;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.Cluster;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.Clusters;
import com.sos.scheduler.model.commands.JSCmdShowState;

@Path("jobscheduler")
public class JobSchedulerResourceClusterImpl extends JOCResourceImpl implements IJobSchedulerResourceCluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerResourceClusterImpl.class);
    private static final String API_CALL = "API-CALL: ./jobscheduler/cluster";

    @Override
    public JOCDefaultResponse postJobschedulerCluster(String accessToken, JobSchedulerId jobSchedulerFilterSchema) {
        LOGGER.debug(API_CALL);
        try {
            JOCDefaultResponse jocDefaultResponse = init(jobSchedulerFilterSchema.getJobschedulerId(),getPermissons(accessToken).getJobschedulerMasterCluster().getView().isClusterStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JOCXmlCommand jocXmlCommand = new JOCXmlCommand(dbItemInventoryInstance.getUrl());
            jocXmlCommand.executePost(getXMLCommand());
            jocXmlCommand.throwJobSchedulerError();
            
            Cluster cluster = new Cluster();
            cluster.setJobschedulerId(jobSchedulerFilterSchema.getJobschedulerId());
            cluster.setSurveyDate(jocXmlCommand.getSurveyDate());
            Element clusterElem = (Element) jocXmlCommand.getSosxml().selectSingleNode("/spooler/answer/state/cluster");
            if (clusterElem == null) {
                cluster.set_type(ClusterType.STANDALONE);
            } else {
                NodeList clusterMembers = jocXmlCommand.getSosxml().selectNodeList(clusterElem,"cluster_member[@distributed_orders='yes']");
                if (clusterMembers != null && clusterMembers.getLength() > 0) {
                    cluster.set_type(ClusterType.ACTIVE);
                } else {
                    cluster.set_type(ClusterType.PASSIVE);
                }
            }
            Clusters entity = new Clusters();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setCluster(cluster);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(API_CALL, "USER: "+getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        }

    }
    
    private String getXMLCommand() {
        JSCmdShowState jsCmdShowState = Globals.schedulerObjectFactory.createShowState();
        jsCmdShowState.setWhat("folders no_subfolders cluster");
        jsCmdShowState.setPath("/does/not/exist");
        jsCmdShowState.setSubsystems("folder");
        return jsCmdShowState.toXMLString();
    }
}
