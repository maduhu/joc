package com.sos.joc.classes.jobscheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.configuration.ConfigurationStatus;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.model.jobscheduler.AgentClusterState;
import com.sos.joc.model.jobscheduler.AgentClusterStateText;
import com.sos.joc.model.jobscheduler.AgentClusterV;
import com.sos.joc.model.jobscheduler.AgentV;
import com.sos.joc.model.jobscheduler.JobSchedulerStateText;
import com.sos.joc.model.jobscheduler.NumOfAgentsInCluster;
import com.sos.joc.model.processClass.Process;

public class AgentClusterVolatile extends AgentClusterV {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentClusterVolatile.class);
    private final JsonObject agent;
    private final JsonObject overview;

    public AgentClusterVolatile(JsonObject agent, Date surveyDate) {
        this.agent = agent;
        this.overview = getAgentOverview();
        setSurveyDate(surveyDate);
    }

    public void setFields(Map<String,AgentV> mapOfAgents, boolean compact) throws JobSchedulerInvalidResponseDataException {
        setAgentsListAndState(mapOfAgents, compact);
        if (compact) {
            setCompactFields(mapOfAgents);
            cleanArrays();
        } else {
            setDetailedFields(mapOfAgents);
        }
    }
    
    public void setAgentClusterPath() throws JobSchedulerInvalidResponseDataException {
        if (getPath() == null) {
            String path = overview.getString("path", null);
            if (path == null || path.isEmpty()) {
                throw new JobSchedulerInvalidResponseDataException("Invalid resonsed data: path is empty");
            }
            LOGGER.debug("...processing AgentCluster " + path);
            setPath(path);
        }
    }
    
    public Set<String> getAgentSet() {
        Set<String> agentSet = new HashSet<String>();
        JsonArray agents = agent.getJsonArray("agents");
        if (agents != null) {
            for (JsonString agent : agents.getValuesAs(JsonString.class)) {
                agentSet.add(agent.getString());
            }
        }
        return agentSet;
    }
    
    public void setAgentsListAndState(Map<String, AgentV> mapOfAgents, boolean compact) {
        if (getNumOfAgents() == null) {
            List<AgentV> agentList = new ArrayList<AgentV>();
            NumOfAgentsInCluster numOf = new NumOfAgentsInCluster();
            int numOfRunning = 0;
            JsonArray agents = agent.getJsonArray("agents");
            if (agents != null) {
                for (JsonString agent : agents.getValuesAs(JsonString.class)) {
                    AgentV a = mapOfAgents.get(agent.getString());
                    if (a != null) {
                        agentList.add(a);
                        if (a.getState().get_text() == JobSchedulerStateText.RUNNING) {
                            numOfRunning += 1;
                        }
                    }
                }
            }
            if (!compact) {
                setAgents(agentList);
            }
            numOf.setRunning(numOfRunning);
            numOf.setAny(agentList.size());
            setNumOfAgents(numOf);
            AgentClusterState state = new AgentClusterState();
            if (numOf.getAny() == numOf.getRunning()) {
                state.set_text(AgentClusterStateText.ALL_AGENTS_ARE_RUNNING);
                state.setSeverity(0);
            } else if (numOf.getRunning() == 0) {
                state.set_text(AgentClusterStateText.ALL_AGENTS_ARE_UNREACHABLE);
                state.setSeverity(2);
            } else {
                state.set_text(AgentClusterStateText.ONLY_SOME_AGENTS_ARE_RUNNING);
                state.setSeverity(1);
            }
            setState(state);
        }
    }

    private void setDetailedFields(Map<String,AgentV> mapOfAgents) throws JobSchedulerInvalidResponseDataException {
        setCompactFields(mapOfAgents);
        List<Process> processList = new ArrayList<Process>();
        JsonArray processes = agent.getJsonArray("processes");
        if (processes != null) {
            for (JsonObject processObj : processes.getValuesAs(JsonObject.class)) {
               Process process = new Process();
               process.setAgent(processObj.getString("agent", null));
               process.setJob(processObj.getString("jobPath", null));
               process.setPid(processObj.getInt("pid", 0));
               process.setRunningSince(JobSchedulerDate.getDateFromISO8601String(processObj.getString("startedAt", null)));
               process.setTaskId(processObj.getString("taskId", null));
               processList.add(process);
            }
        }
        setProcesses(processList);
    }

    private void setCompactFields(Map<String,AgentV> mapOfAgents) throws JobSchedulerInvalidResponseDataException {
        setAgentClusterPath();
        setNumOfProcesses(overview.getInt("usedProcessCount", 0));
        JsonArray obstacles = overview.getJsonArray("obstacles");
        setConfigurationStatus(ConfigurationStatus.getConfigurationStatus(obstacles));
    }
    
    private JsonObject getAgentOverview() {
        return agent.containsKey("overview") ? agent.getJsonObject("overview") : agent;
    }

    private void cleanArrays() {
        setAgents(null);
        setProcesses(null);
    }
}