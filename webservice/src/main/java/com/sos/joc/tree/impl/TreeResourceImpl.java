package com.sos.joc.tree.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryProcessClass;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobchains.JobChainPermanent;
import com.sos.joc.classes.jobs.JobPermanent;
import com.sos.joc.classes.locks.LockPermanent;
import com.sos.joc.classes.orders.OrderPermanent;
import com.sos.joc.classes.processclasses.ProcessClassPermanent;
import com.sos.joc.classes.schedule.SchedulePermanent;
import com.sos.joc.classes.tree.TreePermanent;
import com.sos.joc.db.inventory.jobchains.InventoryJobChainsDBLayer;
import com.sos.joc.db.inventory.jobs.InventoryJobsDBLayer;
import com.sos.joc.db.inventory.locks.InventoryLocksDBLayer;
import com.sos.joc.db.inventory.orders.InventoryOrdersDBLayer;
import com.sos.joc.db.inventory.processclasses.InventoryProcessClassesDBLayer;
import com.sos.joc.db.inventory.schedules.InventorySchedulesDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.job.JobP;
import com.sos.joc.model.jobChain.JobChainP;
import com.sos.joc.model.lock.LockP;
import com.sos.joc.model.order.OrderP;
import com.sos.joc.model.processClass.ProcessClassP;
import com.sos.joc.model.schedule.ScheduleP;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.tree.TreeFilter;
import com.sos.joc.model.tree.TreeView;
import com.sos.joc.tree.resource.ITreeResource;

@Path("tree")
public class TreeResourceImpl extends JOCResourceImpl implements ITreeResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeResourceImpl.class);
    private static final String API_CALL = "./tree";

    @Override
    public JOCDefaultResponse postTree(String accessToken, TreeFilter treeBody) throws Exception {
        LOGGER.debug(API_CALL);

        try {
            List<JobSchedulerObjectType> types = null;
            boolean permission = false;
            SOSPermissionJocCockpit sosPermission = getPermissons(accessToken);
            if (treeBody.getTypes() == null || treeBody.getTypes().isEmpty()) {
                permission = true;
            } else {
                types = TreePermanent.getAllowedTypes(treeBody, sosPermission);
                treeBody.setTypes(types);
                permission = types.size() > 0;
            }
            JOCDefaultResponse jocDefaultResponse = init(treeBody.getJobschedulerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Boolean compact = treeBody.getCompact();
            List<String> folders = TreePermanent.initFoldersFromBody(treeBody, dbItemInventoryInstance.getId());
            Set<String> folderSet = new HashSet<String>();
            if(folders != null && !folders.isEmpty()) {
                folderSet.addAll(folders);
            }
            Tree root = new Tree();
            root.setPath("/");
            root.setName("");
            TreePermanent.getTree(root, folderSet);
            TreePermanent.mergeTreeDuplications(root);
            
            TreeView entity = new TreeView();
            entity.getFolders().add(root);
            if(types == null || types.isEmpty()) {
                entity.setJobChains(null);
                entity.setJobs(null);
                entity.setOrders(null);
                entity.setLocks(null);
                entity.setProcessClasses(null);
                entity.setSchedules(null);
            } else {
              List<JobP> outputJobs = new ArrayList<JobP>();
              List<JobChainP> outputJobChains = new ArrayList<JobChainP>();
              List<OrderP> outputOrders = new ArrayList<OrderP>();
              List<ProcessClassP> outputProcessClasses = new ArrayList<ProcessClassP>();
              List<ScheduleP> outputSchedules = new ArrayList<ScheduleP>();
              List<LockP> outputLocks = new ArrayList<LockP>();
              for(JobSchedulerObjectType type : types) {
                  if(type.equals(JobSchedulerObjectType.JOB)) {
                      InventoryJobsDBLayer jobsLayer = new InventoryJobsDBLayer(Globals.sosHibernateConnection);
                      List<DBItemInventoryJob> jobsFromDb = new ArrayList<DBItemInventoryJob>();
                      for(String folder : folderSet) {
                          List<DBItemInventoryJob> jobResults =
                                  jobsLayer.getInventoryJobsFilteredByFolder(folder, null, true, dbItemInventoryInstance.getId());
                          if (jobResults != null && !jobResults.isEmpty()) {
                              jobsFromDb.addAll(jobResults);
                          }
                      }
                      for(DBItemInventoryJob jobFromDb : jobsFromDb) {
                          JobP job = JobPermanent.getJob(jobFromDb, jobsLayer, compact, dbItemInventoryInstance.getId());
                          if(job != null) {
                              outputJobs.add(job);
                          }
                      }
                  } else if(type.equals(JobSchedulerObjectType.JOBCHAIN)) {
                      InventoryJobChainsDBLayer jobChainsLayer = new InventoryJobChainsDBLayer(Globals.sosHibernateConnection);
                      List<DBItemInventoryJobChain> jobChainsFromDb = new ArrayList<DBItemInventoryJobChain>();
                      for(String folder : folderSet) {
                          List<DBItemInventoryJobChain> jobChainResults = jobChainsLayer.getJobChainsByFolder(folder, true, dbItemInventoryInstance.getId());
                          if (jobChainResults != null && !jobChainResults.isEmpty()) {
                              jobChainsFromDb.addAll(jobChainResults);
                          }
                      }
                      for (DBItemInventoryJobChain jobChainFromDb : jobChainsFromDb) {
                          JobChainP jobChain = 
                                  JobChainPermanent.initJobChainP(jobChainsLayer, jobChainFromDb, compact, dbItemInventoryInstance.getId());
                          if (jobChain != null) {
                             outputJobChains.add(jobChain); 
                          }
                      }
                  } else if(type.equals(JobSchedulerObjectType.ORDER)) {
                      InventoryOrdersDBLayer ordersLayer = new InventoryOrdersDBLayer(Globals.sosHibernateConnection);
                      List<DBItemInventoryOrder> ordersFromDb = new ArrayList<DBItemInventoryOrder>();
                      for(String folder : folderSet) {
                          List<DBItemInventoryOrder> orderResults = 
                                  ordersLayer.getInventoryOrdersFilteredByFolders(folder, true, dbItemInventoryInstance.getId());
                          if(orderResults != null && !orderResults.isEmpty()) {
                              ordersFromDb.addAll(orderResults);
                          }
                      }
                      outputOrders.addAll(OrderPermanent.fillOutputOrders(ordersFromDb, ordersLayer, compact));
                  } else if(type.equals(JobSchedulerObjectType.PROCESSCLASS)) {
                      InventoryProcessClassesDBLayer pcLayer = new InventoryProcessClassesDBLayer(Globals.sosHibernateConnection);
                      List<DBItemInventoryProcessClass> pcsFromDb = new ArrayList<DBItemInventoryProcessClass>();
                      for(String folder : folderSet) {
                          List<DBItemInventoryProcessClass> pcResults = 
                                  pcLayer.getProcessClassesByFolders(folder, dbItemInventoryInstance.getId(), true);
                          if (pcResults != null && !pcResults.isEmpty()) {
                              pcsFromDb.addAll(pcResults);
                          }
                      }
                      outputProcessClasses.addAll(ProcessClassPermanent.getProcessClassesToAdd(pcLayer, pcsFromDb, null));
                  } else if(type.equals(JobSchedulerObjectType.SCHEDULE)) {
                      InventorySchedulesDBLayer schedulesLayer = new InventorySchedulesDBLayer(Globals.sosHibernateConnection);
                      List<DBItemInventorySchedule> schedulesFromDb = new ArrayList<DBItemInventorySchedule>();
                      for(String folder : folderSet) {
                          List<DBItemInventorySchedule> scheduleResults = 
                                  schedulesLayer.getSchedulesByFolders(folder, dbItemInventoryInstance.getId(), true);
                          if (scheduleResults != null && !scheduleResults.isEmpty()) {
                              schedulesFromDb.addAll(scheduleResults);
                          }
                      }
                      for(DBItemInventorySchedule scheduleFromDb : schedulesFromDb) {
                          ScheduleP schedule = SchedulePermanent.initSchedule(schedulesLayer, scheduleFromDb, dbItemInventoryInstance);
                          if (schedule != null) {
                             outputSchedules.add(schedule);
                          }
                      }                      
                  } else if(type.equals(JobSchedulerObjectType.LOCK)) {
                      InventoryLocksDBLayer locksLayer = new InventoryLocksDBLayer(Globals.sosHibernateConnection);
                      List<DBItemInventoryLock> locksFromDb = new ArrayList<DBItemInventoryLock>();
                      for(String folder : folderSet) {
                          List<DBItemInventoryLock> lockResults = locksLayer.getLocksByFolders(folder, dbItemInventoryInstance.getId(), true);
                          if (lockResults != null && !lockResults.isEmpty()) {
                              locksFromDb.addAll(lockResults);
                          }
                      }
                      outputLocks.addAll(LockPermanent.getListOfLocksToAdd(locksLayer, locksFromDb, null));
                  }
              }
              if (outputJobs != null && !outputJobs.isEmpty()) {
                  entity.setJobs(outputJobs);
              } else {
                  entity.setJobs(null);
              }
              if (outputJobChains != null && !outputJobChains.isEmpty()) {
                  entity.setJobChains(outputJobChains);
              } else {
                  entity.setJobChains(null);
              }
              if (outputOrders != null && !outputOrders.isEmpty()) {
                  entity.setOrders(outputOrders);
              } else {
                  entity.setOrders(null);
              }
              if (outputProcessClasses != null && !outputProcessClasses.isEmpty()) {
                  entity.setProcessClasses(outputProcessClasses);
              } else {
                  entity.setProcessClasses(null);
              }
              if (outputSchedules != null && !outputSchedules.isEmpty()) {
                  entity.setSchedules(outputSchedules);
              } else {
                  entity.setSchedules(null);
              }
              if (outputLocks != null && !outputLocks.isEmpty()) {
                  entity.setLocks(outputLocks);
              } else {
                  entity.setLocks(null);
              }
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getMetaInfo(API_CALL, treeBody));
           return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            JocError err = new JocError();
            err.addMetaInfoOnTop(getMetaInfo(API_CALL, treeBody));
            return JOCDefaultResponse.responseStatusJSError(e, err);
        }
    }

}