package com.sos.joc.classes.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCXmlCommand;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.model.common.FoldersSchema;
import com.sos.joc.model.job.JobFilterSchema;
import com.sos.joc.model.job.JobsFilterSchema;
import com.sos.joc.model.job.Lock_;
import com.sos.joc.model.job.Lock__;
import com.sos.joc.model.job.Order;
import com.sos.joc.model.job.RunningTask;
import com.sos.joc.model.job.State_;
import com.sos.joc.model.job.State__;
import com.sos.joc.model.job.State___;
import com.sos.joc.model.job.TaskQueue;
import com.sos.scheduler.model.commands.JSCmdShowJob;
import com.sos.scheduler.model.commands.JSCmdShowState;

public class JobsUtils {

    public static Boolean getBoolValue(final String value) {
        if (WebserviceConstants.YES.equalsIgnoreCase(value)) {
            return true;
        } else if (WebserviceConstants.NO.equalsIgnoreCase(value)) {
            return false;
        }
        return null;
    }

    public static String createJobsPostCommand(final JobsFilterSchema body) {
        boolean compact = body.getCompact();
        StringBuilder strb = new StringBuilder();
        strb.append("<commands>");
        // JSCmdShowState only works with one path(folder)
        // create one command per folder
        if (!body.getFolders().isEmpty()) {
            for (FoldersSchema folder : body.getFolders()) {
                JSCmdShowState showStateCommand = Globals.schedulerObjectFactory.createShowState();
                showStateCommand.setSubsystems("job folder");
                showStateCommand.setPath(folder.getFolder());
                if (!compact) {
                    if (!folder.getRecursive()) {
                        showStateCommand.setWhat("job_orders folders task_queue job_params no_subfolders");
                    } else {
                        showStateCommand.setWhat("job_orders folders task_queue job_params");
                    }
                } else {
                    showStateCommand.setWhat("job_orders folders");
                }
                strb.append(Globals.schedulerObjectFactory.toXMLString(showStateCommand));
            }
        } else {
            JSCmdShowState showStateCommand = Globals.schedulerObjectFactory.createShowState();
            showStateCommand.setSubsystems("job");
            showStateCommand.setPath("/");
            if (!compact) {
                showStateCommand.setWhat("job_orders task_queue job_params");
            } else {
                showStateCommand.setWhat("job_orders task_queue");
            }
            strb.append(Globals.schedulerObjectFactory.toXMLString(showStateCommand));
        }
        strb.append("</commands>");
        return strb.toString();
    }

    public static String createJobPostCommand(final JobFilterSchema body) {
        boolean compact = body.getCompact();
        JSCmdShowJob showJob = Globals.schedulerObjectFactory.createShowJob();
        if (!body.getJob().isEmpty()) {
            if (!compact) {
                showJob.setWhat("job_params task_queue");
            }
            showJob.setJob(body.getJob());
        }
        return Globals.schedulerObjectFactory.toXMLString(showJob);
    }
    
    private static String overwriteStateValue(String stateValue, Node jobNode) throws Exception {
        Boolean inPeriod = getBoolValue(((Element)jobNode).getAttribute(WebserviceConstants.IN_PERIOD));
        if(inPeriod != null && !inPeriod) {
            return State_.Text.NOT_IN_PERIOD.toString();
        }
        Boolean waitingForAgent = getBoolValue(((Element)jobNode).getAttribute(WebserviceConstants.WAITING_FOR_AGENT));
        if(waitingForAgent != null && waitingForAgent) {
            return State_.Text.WAITING_FOR_AGENT.toString();
        }
        Boolean waitingForProcess = getBoolValue(((Element)jobNode).getAttribute(WebserviceConstants.WAITING_FOR_PROCESS));
        if(waitingForProcess != null && waitingForProcess) {
            return State_.Text.WAITING_FOR_PROCESS.toString();
        }
        CachedXPathAPI xPath = new CachedXPathAPI();
        NodeList locks = xPath.selectNodeList((Element)jobNode, "lock.requestor/lock.use[@is_available='no']");
        boolean waitingForLock = locks.getLength() > 0;
        if(waitingForLock) {
            return State_.Text.WAITING_FOR_LOCK.toString();
        }
        // TODO: WaitingForTask
        return stateValue;
    }

    public static State_ getOutputState_(String stateValue, Node jobNode) throws Exception {
        stateValue = overwriteStateValue(stateValue, jobNode);
        State_ state = new State_();
        state.setSeverity(JobsUtils.getSeverityFromStateText(stateValue));
        state.setText(State_.Text.valueOf(stateValue.toUpperCase()));
        return state;
    }
    
    public static State__ getOutputState__(String stateValue, Node jobNode) throws Exception {
        stateValue = overwriteStateValue(stateValue, jobNode);
        State__ state = new State__();
        state.setSeverity(JobsUtils.getSeverityFromStateText(stateValue));
        state.setText(State__.Text.valueOf(stateValue.toUpperCase()));
        return state;
    }
    
    public static Integer getSeverityFromStateText(String stateValue) {
        switch (stateValue.toUpperCase()) {
        case "RUNNING":
            return 0;
        case "PENDING":
            return 1;
        case "NOT_INITIALIZED":
        case "WAITING_FOR_AGENT":
        case "STOPPING":
        case "STOPPED":
        case "REMOVED":
            return 2;
        case "INITIALIZED":
        case "LOADED":
        case "WAITING_FOR_PROCESS":
        case "WAITING_FOR_LOCK":
        case "WAITING_FOR_TASK":
        case "NOT_IN_PERIOD":
            return 3;
        case "DISABLED":
            return 4;
        }
        return null;
    }

    public static Integer getSeverityFromProcessingStateText(String stateValue) {
        switch (stateValue.toUpperCase()) {
        case "RUNNING":
            return 0;
        case "PENDING":
            return 1;
        case "WAITING_FOR_AGENT":
        case "JOB_CHAIN_STOPPED":
        case "NODE_STOPPED":
        case "JOB_STOPPED":
            return 2;
        case "JOB_NOT_IN_PERIOD":
        case "NODE_DELAY":
        case "WAITING_FOR_PROCESS":
        case "WAITING_FOR_LOCK":
        case "WAITING_FOR_TASK":
        case "NOT_IN_PERIOD":
            return 3;
        case "SETBACK":
        case "SUSPENDED":
            return 5;
        }
        return null;
    }

    public static List<Lock_> getLocks_(NodeList lockList) {
        if (lockList != null && lockList.getLength() > 0) {
            List<Lock_> listOfLocks = new ArrayList<Lock_>();
            for (int j = 0; j < lockList.getLength(); j++) {
                Node lockNode = lockList.item(j);
                Lock_ lock = new Lock_();
                Element lockElement = (Element) lockNode;
                if (lockElement.getAttribute(WebserviceConstants.EXCLUSIVE) != null) {
                    lock.setExclusive(JobsUtils.getBoolValue(lockElement.getAttribute(WebserviceConstants.EXCLUSIVE)));
                }
                if (lockElement.getAttribute(WebserviceConstants.IS_AVAILABLE) != null) {
                    lock.setAvailable(JobsUtils.getBoolValue(lockElement.getAttribute(WebserviceConstants.IS_AVAILABLE)));
                }
                if (lockElement.getAttribute(WebserviceConstants.LOCK) != null) {
                    lock.setPath(lockElement.getAttribute(WebserviceConstants.LOCK));
                }
                listOfLocks.add(lock);
            }
            return listOfLocks;
        }
        return null;
    }

    public static List<Lock__> getLocks__(NodeList lockList) {
        if (lockList != null && lockList.getLength() > 0) {
            List<Lock__> listOfLocks = new ArrayList<Lock__>();
            for (int j = 0; j < lockList.getLength(); j++) {
                Node lockNode = lockList.item(j);
                Lock__ lock = new Lock__();
                Element lockElement = (Element) lockNode;
                if (lockElement.getAttribute(WebserviceConstants.EXCLUSIVE) != null) {
                    lock.setExclusive(JobsUtils.getBoolValue(lockElement.getAttribute(WebserviceConstants.EXCLUSIVE)));
                }
                if (lockElement.getAttribute(WebserviceConstants.IS_AVAILABLE) != null) {
                    lock.setAvailable(JobsUtils.getBoolValue(lockElement.getAttribute(WebserviceConstants.IS_AVAILABLE)));
                }
                if (lockElement.getAttribute(WebserviceConstants.LOCK) != null) {
                    lock.setPath(lockElement.getAttribute(WebserviceConstants.LOCK));
                }
                listOfLocks.add(lock);
            }
            return listOfLocks;
        }
        return null;
    }

    public static List<TaskQueue> getQueuedTasks(NodeList queuedTasksList) throws Exception {
        List<TaskQueue> queuedTasks = new ArrayList<TaskQueue>();
        if (queuedTasksList != null && queuedTasksList.getLength() > 0) {
            for (int queuedTasksCount = 0; queuedTasksCount < queuedTasksList.getLength(); queuedTasksCount++) {
                TaskQueue taskQueue = new TaskQueue();
                Element taskQueueElement = (Element) queuedTasksList.item(queuedTasksCount);
                taskQueue.setTaskId(Integer.parseInt(taskQueueElement.getAttribute(WebserviceConstants.ID)));
                taskQueue.setEnqueued(JobSchedulerDate.getDate(taskQueueElement.getAttribute(WebserviceConstants.ENQUEUED)));
            }
            return queuedTasks;
        } else {
            return null;
        }
    }

    public static List<RunningTask> getRunningTasks(NodeList runningTaskList, JOCXmlCommand jocXmlCommand) throws Exception {
        List<RunningTask> runningTasks = new ArrayList<RunningTask>();
        if (runningTaskList != null && runningTaskList.getLength() > 0) {
            for (int runningTasksCount = 0; runningTasksCount < runningTaskList.getLength(); runningTasksCount++) {
                RunningTask task = new RunningTask();
                Element taskElement = (Element) runningTaskList.item(runningTasksCount);
                task.setCause(RunningTask.Cause.valueOf(taskElement.getAttribute(WebserviceConstants.CAUSE)));
                task.setEnqueued(JobSchedulerDate.getDate(taskElement.getAttribute(WebserviceConstants.ENQUEUED)));
                task.setIdleSince(JobSchedulerDate.getDate(taskElement.getAttribute(WebserviceConstants.IDLE_SINCE)));
                if (taskElement.getAttribute(WebserviceConstants.PID) != null && !taskElement.getAttribute(WebserviceConstants.PID).isEmpty()) {
                    task.setPid(Integer.parseInt(taskElement.getAttribute(WebserviceConstants.PID)));
                }
                task.setStartedAt(JobSchedulerDate.getDate(taskElement.getAttribute(WebserviceConstants.START_AT)));
                if (taskElement.getAttribute(WebserviceConstants.STEPS) != null && !taskElement.getAttribute(WebserviceConstants.STEPS).isEmpty()) {
                    task.setSteps(Integer.parseInt(taskElement.getAttribute(WebserviceConstants.STEPS)));
                }
                if (taskElement.getAttribute(WebserviceConstants.ID) != null && !taskElement.getAttribute(WebserviceConstants.ID).isEmpty()) {
                    task.setTaskId(Integer.parseInt(taskElement.getAttribute(WebserviceConstants.ID)));
                }
                Element orderElement = (Element) jocXmlCommand.getSosxml().selectSingleNode(taskElement, WebserviceConstants.ORDER);
                if (orderElement != null) {
                    Order order = new Order();
                    order.setInProcessSince(JobSchedulerDate.getDate(orderElement.getAttribute(WebserviceConstants.IN_PROCESS_SINCE)));
                    order.setJobChain(orderElement.getAttribute(WebserviceConstants.JOB_CHAIN));
                    order.setOrderId(orderElement.getAttribute(WebserviceConstants.ID));
                    order.setPath(orderElement.getAttribute(WebserviceConstants.PATH));
                    order.setState(orderElement.getAttribute(WebserviceConstants.STATE));
                    task.setOrder(order);
                }
                runningTasks.add(task);
            }
            return runningTasks;
        } else {
            return null;
        }
    }

    public static boolean filterJobs(JobsFilterSchema filter, Element node, SOSXMLXPath sosXml) throws Exception {
        boolean isAvailable = false;
        // TODO no property to compare dateFrom and dateTo to
        // clarification needed of which Date is relevant for comparison
        Date dateFrom = JobSchedulerDate.getDate(filter.getDateFrom());
        Date dateTo = JobSchedulerDate.getDate(filter.getDateTo());
        // TODO What to do with regex
        String regex = filter.getRegex();
        // TODO What to do with timezone
        String timezone = filter.getTimeZone();
        if (dateFrom == null && dateTo == null && regex == null && timezone == null) {
            return true;
        }
        // Date runningSince = JobSchedulerDate.getDate(sosXml.selectSingleNodeValue(node, "tasks/task/@running_since"));
        // Date startTime = JobSchedulerDate.getDate(sosXml.selectSingleNodeValue(node, "tasks/task/order/@start_time"));
        // Date nextStartTime = JobSchedulerDate.getDate(node.getAttribute(NEXT_START_TIME));
        if (node.getAttribute(WebserviceConstants.STATE) != null && !node.getAttribute(WebserviceConstants.STATE).isEmpty()
                && stateAvailable(node.getAttribute(WebserviceConstants.STATE), filter.getState())) {
            isAvailable = true;
        }
        // if (dateFrom != null && runningSince != null && dateFrom.compareTo(runningSince) <= 0) {
        // isAvailable = true;
        // } else {
        // isAvailable = false;
        // }
        // if (dateTo != null && runningSince != null && runningSince.compareTo(dateTo) <= 0) {
        // isAvailable = true;
        // } else {
        // isAvailable = false;
        // }
        return isAvailable;
    }

    private static boolean stateAvailable(String stateText, List<State___> states) {
        for (State___ state : states) {
            if (state.toString().equalsIgnoreCase(stateText)) {
                return true;
            }
        }
        return false;
    }
}