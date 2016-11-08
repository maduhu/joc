package com.sos.joc.classes.jobchains;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCXmlCommand;
import com.sos.joc.classes.filters.FilterAfterResponse;
import com.sos.joc.classes.orders.OrdersSummaryCallable;
import com.sos.joc.classes.orders.OrdersVCallable;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.jobChain.JobChainPath;
import com.sos.joc.model.jobChain.JobChainV;
import com.sos.joc.model.jobChain.JobChainsFilter;
import com.sos.joc.model.order.OrderV;
import com.sos.scheduler.model.commands.JSCmdCommands;
import com.sos.scheduler.model.commands.JSCmdShowJobChain;
import com.sos.scheduler.model.commands.JSCmdShowState;


public class JOCXmlJobChainCommand extends JOCXmlCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCXmlJobChainCommand.class);
    private String jsonUrl = null;
    private Set<String> nestedJobChains = new HashSet<String>();
    private String accessToken;
    
    public JOCXmlJobChainCommand(String url, String accessToken) {
        super(url);
        this.jsonUrl = url;
        this.accessToken = accessToken;
    }
    
    public JobChainV getJobChain(String jobChain, Boolean compact) throws Exception {
        JSCmdShowJobChain j = createShowJobChainPostCommand(jobChain, compact);
        executePostWithThrowBadRequest(j.toXMLString(), accessToken);
        Element jobElem = (Element) getSosxml().selectSingleNode("/spooler/answer/job_chain");
        JobChainVolatile jobChainV = new JobChainVolatile(jobElem, this);
        jobChainV.setFields(compact);
        nestedJobChains.addAll(jobChainV.getNestedJobChains());
        jobChainV.setOrdersSummary(new OrdersSummaryCallable(jobChainV, setUriForOrdersSummaryJsonCommand(), accessToken).getOrdersSummary());
        return jobChainV;
    }
    
    public List<JobChainV> getNestedJobChains() throws Exception {
        if (nestedJobChains.size() == 0) {
            return null;
        }
        JobChainsFilter jobChainsFilter = new JobChainsFilter();
        JSCmdCommands commands = Globals.schedulerObjectFactory.createCmdCommands();
        for (String jobChainPath : nestedJobChains) {
            if (jobChainPath != null) {
                JSCmdShowJobChain j = createShowJobChainPostCommand(jobChainPath,jobChainsFilter.getCompact());
                commands.getAddJobsOrAddOrderOrCheckFolders().add(j);
            }
        }
        return getJobChains(commands.toXMLString(), jobChainsFilter, "/spooler/answer/job_chain");
    }
    
    public List<JobChainV> getJobChainsFromShowJobChain(List<JobChainPath> jobChains, JobChainsFilter jobChainsFilter) throws Exception {
        JSCmdCommands commands = Globals.schedulerObjectFactory.createCmdCommands();
        for (JobChainPath jobChain : jobChains) {
            if (jobChain.getJobChain() == null || jobChain.getJobChain().isEmpty()) {
                throw new JocMissingRequiredParameterException("undefined jobChain");
            }
            JSCmdShowJobChain j = createShowJobChainPostCommand(jobChain.getJobChain(),jobChainsFilter.getCompact());
            commands.getAddJobsOrAddOrderOrCheckFolders().add(j);
        }
        return getJobChains(commands.toXMLString(), jobChainsFilter, "/spooler/answer/job_chain");
    }
    
    public List<JobChainV> getJobChainsFromShowState(List<Folder> folders, JobChainsFilter jobChainsFilter) throws Exception {
        JSCmdCommands commands = Globals.schedulerObjectFactory.createCmdCommands();
        for (Folder folder : folders) {
            if (folder.getFolder() == null || folder.getFolder().isEmpty()) {
                throw new JocMissingRequiredParameterException("undefined folder");
            }
            JSCmdShowState s = createShowStatePostCommand(folder.getFolder(), folder.getRecursive(), jobChainsFilter.getCompact());
            commands.getAddJobsOrAddOrderOrCheckFolders().add(s);
        }
        return getJobChains(commands.toXMLString(), jobChainsFilter);
    }
    
    public List<JobChainV> getJobChainsFromShowState(JobChainsFilter jobChainsFilter) throws Exception {
        JSCmdShowState s = createShowStatePostCommand("/", true, jobChainsFilter.getCompact()); 
        return getJobChains(s.toXMLString(), jobChainsFilter);
    }

    private JSCmdShowState createShowStatePostCommand(String folder, Boolean recursive, Boolean compact) {
        JSCmdShowState showState = Globals.schedulerObjectFactory.createShowState();
        showState.setSubsystems("folder order");
        showState.setWhat("job_chains folders order_source_files blacklist");
        if (compact) {
            showState.setMaxOrders(BigInteger.valueOf(0));
        } else {
            showState.setWhat("job_chain_jobs " + showState.getWhat());
        }
        if (!recursive) {
            showState.setWhat("no_subfolders " + showState.getWhat());
        }
        if (folder != null) {
            showState.setPath(("/"+folder).replaceAll("//+", "/").replaceFirst("/$", ""));
        }
        showState.setMaxOrderHistory(BigInteger.valueOf(0));
        return showState;
    }

    private JSCmdShowJobChain createShowJobChainPostCommand(String jobChain, boolean compact) {
        JSCmdShowJobChain showJobChain = Globals.schedulerObjectFactory.createShowJobChain();
        showJobChain.setWhat("order_source_files blacklist"); //blacklisted orders are not counted in <order_queue length="..."/>
        if (compact) {
            showJobChain.setMaxOrders(BigInteger.valueOf(0));
        } else {
            showJobChain.setWhat("job_chain_jobs " + showJobChain.getWhat());
        }
        showJobChain.setJobChain(jobChain);
        showJobChain.setMaxOrderHistory(BigInteger.valueOf(0));
        return showJobChain;
    }
    
    private List<JobChainV> getJobChains(String command, JobChainsFilter jobChainsFilter) throws Exception {
        return getJobChains(command, jobChainsFilter, "/spooler/answer//job_chains/job_chain");
    }
    
    private List<JobChainV> getJobChains(String command, JobChainsFilter jobChainsFilter, String xPath) throws Exception {
        executePostWithThrowBadRequest(command, accessToken);
        StringBuilder x = new StringBuilder();
        x.append(xPath);
        NodeList jobChainNodes = getSosxml().selectNodeList(x.toString());
        LOGGER.debug("..." + jobChainNodes.getLength() + " jobChains found");
        
        Map<String, JobChainVolatile> jobChainMap = new HashMap<String, JobChainVolatile>();
        List<OrdersSummaryCallable> summaryTasks = new ArrayList<OrdersSummaryCallable>();
        List<OrdersVCallable> orderTasks = new ArrayList<OrdersVCallable>();
        URI uriForJsonSummaryCommand = setUriForOrdersSummaryJsonCommand();
        URI uriForJsonOrdersCommand = setUriForOrdersJsonCommand();
        
        for (int i= 0; i < jobChainNodes.getLength(); i++) {
           Element jobChainElem = (Element) jobChainNodes.item(i);
           JobChainVolatile jobChainV = new JobChainVolatile(jobChainElem, this);
           jobChainV.setPath();
           if (!FilterAfterResponse.matchRegex(jobChainsFilter.getRegex(), jobChainV.getPath())) {
               LOGGER.debug("...processing skipped caused by 'regex=" + jobChainsFilter.getRegex() + "'");
               continue; 
           }
           jobChainV.setState();
           if (!FilterAfterResponse.filterStateHasState(jobChainsFilter.getStates(), jobChainV.getState().get_text())) {
               LOGGER.debug(String.format("...processing skipped because jobChain's state '%1$s' doesn't contain in state filter '%2$s'", jobChainV.getState().get_text().name(),jobChainsFilter.getStates().toString()));
               continue; 
           }
           jobChainV.setFields(jobChainsFilter.getCompact());
           nestedJobChains.addAll(jobChainV.getNestedJobChains());
           summaryTasks.add(new OrdersSummaryCallable(jobChainV, uriForJsonSummaryCommand, accessToken));
           if (!jobChainsFilter.getCompact() && jobChainV.hasJobNodes()) {
               orderTasks.add(new OrdersVCallable(jobChainV, false, uriForJsonOrdersCommand, accessToken)); 
           }
        }
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (Future<Map<String, JobChainVolatile>> result : executorService.invokeAll(summaryTasks)) {
            try {
                jobChainMap.putAll(result.get());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof JocException) {
                    throw (JocException) e.getCause();
                } else {
                    throw (Exception) e.getCause();
                }
            }
        }
        if (!jobChainsFilter.getCompact()) {
            for (Future<Map<String, OrderV>> result : executorService.invokeAll(orderTasks)) {
                try {
                    Map<String, OrderV> orders = result.get();
                    if (orders.size() > 0) {
                        List<OrderV> o = new ArrayList<OrderV>(orders.values());
                        JobChainVolatile j = jobChainMap.get(o.get(0).getJobChain());
                        j.setOrders(o);
                        // jobChainMap.put(jobChain, j);
                    }
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof JocException) {
                        throw (JocException) e.getCause();
                    } else {
                        throw (Exception) e.getCause();
                    }
                }
            }
        }
        //LOGGER.debug("..." + jobChainMap.size() + " jobChains processed");
        return new ArrayList<JobChainV>(jobChainMap.values());
    }
    
    private URI setUriForOrdersSummaryJsonCommand() {
        JOCJsonCommand jsonSummaryCommand = new JOCJsonCommand();
        jsonSummaryCommand.setUriBuilderForOrders(jsonUrl);
        jsonSummaryCommand.addOrderStatisticsQuery();
        return jsonSummaryCommand.getURI();
    }
    
    private URI setUriForOrdersJsonCommand() {
        JOCJsonCommand jsonOrdersCommand = new JOCJsonCommand();
        jsonOrdersCommand.setUriBuilderForOrders(jsonUrl);
        jsonOrdersCommand.addOrderCompactQuery(false);
        return jsonOrdersCommand.getURI();
    }
}
