package com.sos.joc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;

import com.sos.auth.rest.SOSShiroCurrentUsersList;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.scheduler.model.SchedulerObjectFactory;

public class Globals {
    private static final String HIBERNATE_CONFIGURATION_FILE = "hibernate_configuration_file";
    public static final String SESSION_KEY_FOR_USED_HTTP_CLIENTS_BY_EVENTS = "event_http_clients";
    public static SOSShiroCurrentUsersList currentUsersList;
    public static SOSHibernateConnection sosHibernateConnection;
    public static SchedulerObjectFactory schedulerObjectFactory;
    public static Map<String, SOSHibernateConnection> sosSchedulerHibernateConnections;
    public static JocCockpitProperties sosShiroProperties;
    public static Map<String, String> UrlFromJobSchedulerId = new HashMap<String, String>();
    
    public static SOSHibernateConnection getConnection(String schedulerId) throws JocException {
        if (sosSchedulerHibernateConnections == null) {
            sosSchedulerHibernateConnections = new HashMap<String, SOSHibernateConnection>();
        }
        SOSHibernateConnection sosHibernateConnection = sosSchedulerHibernateConnections.get(schedulerId);
        
        if (sosHibernateConnection == null) {
            try {
                String confFile = getConfFile(schedulerId);
                sosHibernateConnection = new SOSHibernateConnection(confFile);
                sosHibernateConnection.addClassMapping(DBLayer.getSchedulerClassMapping());
                sosHibernateConnection.connect();
                sosSchedulerHibernateConnections.put(schedulerId, sosHibernateConnection);
            } catch (JocException e) {
                throw e;
            } catch (Exception e) {
                throw new DBConnectionRefusedException(e);
            }
        }

        return sosHibernateConnection;
    }

    private static String getConfFile(String schedulerId) throws JocException {
        String propertieKey = HIBERNATE_CONFIGURATION_FILE + "_" + schedulerId;
        String confFile = sosShiroProperties.getProperty(propertieKey);
        if (confFile == null) {
            confFile = sosShiroProperties.getProperty(HIBERNATE_CONFIGURATION_FILE);
            if (confFile == null) {
                JocError error = new JocError();
                error.setCode("JOC-310");
                error.setMessage(String.format("Could find value for %s in joc_properties file", propertieKey));
                throw new JocException(error);
            }
        }

        return confFile;

    }
    
    public static void beginTransaction(){
        try {
            sosHibernateConnection.beginTransaction();
        } catch (Exception e) {
        }
    }

    public static void rollback(){
        try {
            sosHibernateConnection.rollback();
        } catch (Exception e) {
        }
    }
    
    public static void commit(){
        try {
            sosHibernateConnection.commit();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    public static void forceClosingHttpClients(Session session) {
        try {
            if (session != null && session.getAttribute(SESSION_KEY_FOR_USED_HTTP_CLIENTS_BY_EVENTS) != null) {
                try {
                    for (JOCJsonCommand command : (List<JOCJsonCommand>) session.getAttribute(SESSION_KEY_FOR_USED_HTTP_CLIENTS_BY_EVENTS)) {
                        command.forcedClosingHttpClient();
                    }
                    session.removeAttribute(SESSION_KEY_FOR_USED_HTTP_CLIENTS_BY_EVENTS);
                } catch (Exception e) {}
            }
        } catch (InvalidSessionException e) {}
    }
}
