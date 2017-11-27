package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jade.db.DBItemYadeProtocols;
import com.sos.jade.db.DBItemYadeTransfers;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.yade.Operation;
import com.sos.joc.model.yade.Protocol;
import com.sos.joc.model.yade.ProtocolFragment;
import com.sos.joc.model.yade.Transfer;
import com.sos.joc.model.yade.TransferFilter;
import com.sos.joc.model.yade.TransferStateText;
import com.sos.joc.model.yade.Transfers;
import com.sos.joc.yade.resource.IYadeTransfersResource;

@Path("/yade/transfers")
public class YadeTransfersResourceImpl extends JOCResourceImpl implements IYadeTransfersResource {

    private static final String API_CALL = "./yade/transfers";

    @Override
    public JOCDefaultResponse postYadeTransfers(String accessToken, TransferFilter filterBody) throws Exception {
        SOSHibernateSession connection = null;
        try {
            SOSPermissionJocCockpit sosPermission = getPermissonsJocCockpit(accessToken);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filterBody, accessToken, "", 
                    sosPermission.getYADE().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            //Filters
            Boolean compact = filterBody.getCompact();
            Date dateFrom = null;
            Date dateTo = null;
            String from = filterBody.getDateFrom();
            String to = filterBody.getDateTo();
            String timezone = filterBody.getTimeZone();
            if (from != null && !from.isEmpty()) {
                dateFrom = JobSchedulerDate.getDateFrom(from, timezone);
            }
            if (to != null && !to.isEmpty()) {
                dateTo = JobSchedulerDate.getDateTo(to, timezone);
            }
            List<Operation> operations = filterBody.getOperations();
            List<TransferStateText> states = filterBody.getStates();
            String mandator = filterBody.getMandator();
            List<ProtocolFragment> sources = filterBody.getSources();
            List<ProtocolFragment> targets = filterBody.getTargets();
            Boolean isIntervention = filterBody.getIsIntervention();
            Boolean hasInterventions = filterBody.getHasIntervention();
            List<String> profiles = filterBody.getProfiles();
            Integer limit = filterBody.getLimit();
            if (limit == null) {
                limit = 10000;  // default
            } else if (limit == -1) {
                limit = null;   // unlimited
            }
            JocDBLayerYade dbLayer = new JocDBLayerYade(connection);
            List<DBItemYadeTransfers> transfersFromDb = dbLayer.getFilteredTransfers(operations, states, mandator,
                    sources, targets, isIntervention, hasInterventions, profiles, limit, dateFrom, dateTo);
            Transfers entity = new Transfers();
            List<Transfer> transfers = new ArrayList<Transfer>();
            for (DBItemYadeTransfers transferFromDb : transfersFromDb) {
                Transfer transfer = new Transfer();
                transfer.set_operation(getOperationFromValue(transferFromDb.getOperation()));
                transfer.setEnd(transferFromDb.getEnd());
                Err err = new Err();
                err.setCode(transferFromDb.getErrorCode());
                err.setMessage(transferFromDb.getErrorMessage());
                transfer.setError(err);
                transfer.setHasIntervention(transferFromDb.getHasIntervention());
                transfer.setId(transferFromDb.getId());
                transfer.setJob(transferFromDb.getJob());
                transfer.setJobChain(transferFromDb.getJobChain());
                transfer.setJobschedulerId(transferFromDb.getJobschedulerId());
                if(transferFromDb.getJumpProtocolId() != null) {
                    DBItemYadeProtocols protocol = dbLayer.getProtocolById(transferFromDb.getJumpProtocolId());
                    if (protocol != null) {
                        ProtocolFragment jumpFragment = new ProtocolFragment();
                        jumpFragment.setAccount(protocol.getAccount());
                        jumpFragment.setHost(protocol.getHostname());
                        jumpFragment.setPort(protocol.getPort());
                        jumpFragment.setProtocol(getProtocolFromValue(protocol.getProtocol()));
                        transfer.setJump(jumpFragment);
                    }
                }
                transfer.setMandator(transferFromDb.getMandator());
                transfer.setNumOfFiles(transferFromDb.getNumOfFiles().intValue());
                transfer.setOrderId(transferFromDb.getOrderId());
                transfer.setParent_id(transferFromDb.getParentTransferId());
                transfer.setProfile(transferFromDb.getProfileName());
                if(transferFromDb.getSourceProtocolId() != null) {
                    DBItemYadeProtocols protocol = dbLayer.getProtocolById(transferFromDb.getSourceProtocolId());
                    if (protocol != null) {
                        ProtocolFragment sourceFragment = new ProtocolFragment();
                        sourceFragment.setAccount(protocol.getAccount());
                        sourceFragment.setHost(protocol.getHostname());
                        sourceFragment.setPort(protocol.getPort());
                        sourceFragment.setProtocol(getProtocolFromValue(protocol.getProtocol()));
                        transfer.setSource(sourceFragment);
                    }
                }
                transfer.setStart(transferFromDb.getStart());
                transfer.setSurveyDate(transferFromDb.getModified());
                if(transferFromDb.getTargetProtocolId() != null) {
                    DBItemYadeProtocols protocol = dbLayer.getProtocolById(transferFromDb.getTargetProtocolId());
                    if (protocol != null) {
                        ProtocolFragment targetFragment = new ProtocolFragment();
                        targetFragment.setAccount(protocol.getAccount());
                        targetFragment.setHost(protocol.getHostname());
                        targetFragment.setPort(protocol.getPort());
                        targetFragment.setProtocol(getProtocolFromValue(protocol.getProtocol()));
                        transfer.setTarget(targetFragment);
                    }
                }
                transfers.add(transfer);
            }
            entity.setTransfers(transfers);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Operation getOperationFromValue(Integer value) {
        switch(value) {
        case 1:
            return Operation.COPY;
        case 2:
            return Operation.MOVE;
        case 3:
            return Operation.GETLIST;
        case 4:
            return Operation.RENAME;
        default:
            return null;
        }
    }
    
    private Protocol getProtocolFromValue(Integer value) {
        switch(value) {
        case 1:
            return Protocol.LOCAL;
        case 2:
            return Protocol.FTP;
        case 3:
            return Protocol.FTPS;
        case 4:
            return Protocol.SFTP;
        case 5:
            return Protocol.HTTP;
        case 6:
            return Protocol.HTTPS;
        case 7:
            return Protocol.WEBDAV;
        case 8:
            return Protocol.WEBDAVS;
        case 9:
            return Protocol.SMB;
        default:
            return null;
        
        }
    }
}