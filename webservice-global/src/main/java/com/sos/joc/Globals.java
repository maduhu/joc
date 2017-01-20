package com.sos.joc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.hibernate.HibernateException;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSShiroCurrentUsersList;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class Globals {

    private static final String HIBERNATE_CONFIGURATION_FILE = "hibernate_configuration_file";
    private static final Logger LOGGER = LoggerFactory.getLogger(Globals.class);
    public static final String SESSION_KEY_FOR_USED_HTTP_CLIENTS_BY_EVENTS = "event_http_clients";
    public static final String DEFAULT_SHIRO_INI_PATH = "classpath:shiro.ini";
    public static SOSShiroCurrentUsersList currentUsersList;
    public static SOSHibernateConnection sosHibernateConnection;
    public static Map<String, SOSHibernateConnection> sosSchedulerHibernateConnections;
    public static JocCockpitProperties sosShiroProperties;
    public static Map<String, DBItemInventoryInstance> urlFromJobSchedulerId = new HashMap<String, DBItemInventoryInstance>();
    public static Map<String, Boolean> jobSchedulerIsRunning = new HashMap<String, Boolean>();
    public static int httpConnectionTimeout = 2000;
    public static int httpSocketTimeout = 2000;
    public static boolean withHostnameVerification = false;
    
    public static SOSHibernateConnection getConnection() throws JocException {
        if (sosHibernateConnection == null) {
            try {
                String confFile = getConfFile(null);
                sosHibernateConnection = new SOSHibernateConnection(confFile);
                sosHibernateConnection.addClassMapping(DBLayer.getInventoryClassMapping());
                sosHibernateConnection.addClassMapping(DBLayer.getReportingClassMapping());
                sosHibernateConnection.setAutoCommit(true);
                sosHibernateConnection.setIgnoreAutoCommitTransactions(true);
                sosHibernateConnection.setUseOpenStatelessSession(true);
                sosHibernateConnection.connect();
            } catch (JocException e) {
                throw e;
            } catch (Exception e) {
                throw new DBConnectionRefusedException(e);
            }
        }
        return sosHibernateConnection;
    }

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
                sosHibernateConnection.setAutoCommit(true);
                sosHibernateConnection.setIgnoreAutoCommitTransactions(true);
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

    public static void checkConnection() throws JocException {
        checkConnection(null);
    }

    public static void checkConnection(String schedulerId) throws JocException {
        SOSHibernateConnection connection = null;
        if (schedulerId == null) {
            connection = getConnection();
        } else {
            connection = getConnection(schedulerId);
        }
        try {
            if ((connection.getSessionFactory() != null && connection.getSessionFactory().isClosed()) || (!connection
                    .isUseOpenStatelessSession() && !((org.hibernate.Session) connection.getCurrentSession()).isOpen())) {
                LOGGER.info("Database session is closed. Retry connect...");
                connection.reconnect();
            }
        } catch (Exception e) {
            throw new DBConnectionRefusedException(e);
        }
    }

    public static void trySelect(String schedulerId) throws JocException {
        SOSHibernateConnection connection = null;
        if (schedulerId == null) {
            connection = getConnection();
        } else {
            connection = getConnection(schedulerId);
        }
        try {
            
            String sql = " from DailyPlanDBItem where 1=0";
            StatelessSession session = connection.createStatelessSession();
            connection.createQuery(sql,session).list();

        } catch (HibernateException ex) {
            try {
                LOGGER.info("Database session is invalid. Retry connect...");
                connection.reconnect();
            } catch (Exception e) {
                throw new DBConnectionRefusedException(e);
            }

        } catch (Exception e) {
            throw new DBConnectionRefusedException(e);
        }
    }

    
    public static String getShiroIniInClassPath() {
        if (sosShiroProperties != null) {
            return "classpath:" + sosShiroProperties.getPropertiesFileClassPathParent() + "shiro.ini";
        }
        return DEFAULT_SHIRO_INI_PATH;
    }
    
    public static void setProperties() throws JocException {
        setJobSchedulerConnectionTimeout();
        setJobSchedulerSocketTimeout();
        setHostnameVerification();
        setTrustStore();
    }
    
    public static void beginTransaction() {
        try {
            if (sosHibernateConnection != null) {
                sosHibernateConnection.beginTransaction();
            }
        } catch (Exception e) {
        }
    }

    public static void rollback() {
        try {
            if (sosHibernateConnection != null) {
                sosHibernateConnection.rollback();
            }
        } catch (Exception e) {
        }
    }

    public static void forceRollback() {
        if (sosHibernateConnection != null) {
            try {
                sosHibernateConnection.setIgnoreAutoCommitTransactions(false);
                sosHibernateConnection.rollback();
            } catch (Exception e) {
            } finally {
                sosHibernateConnection.setIgnoreAutoCommitTransactions(true);
            }
        }
    }

    public static void commit() {
        try {
            if (sosHibernateConnection != null) {
                sosHibernateConnection.commit();
            }
        } catch (Exception e) {
        }
    }

    public static void forceClosingHttpClients(Session session) {
        forceClosingHttpClients(session, SESSION_KEY_FOR_USED_HTTP_CLIENTS_BY_EVENTS);
    }
    
    @SuppressWarnings("unchecked")
    public static void forceClosingHttpClients(Session session, String sessionKey) {
        try {
            if (session != null && session.getAttribute(sessionKey) != null) {
                try {
                    for (JOCJsonCommand command : (List<JOCJsonCommand>) session.getAttribute(sessionKey)) {
                        command.forcedClosingHttpClient();
                    }
                    session.removeAttribute(sessionKey);
                } catch (Exception e) {
                }
            }
        } catch (InvalidSessionException e) {
        }
    }
    
    private static String getConfFile(String schedulerId) throws JocException {
        String confFile = null;
        JocError error = new JocError();
        String propertyKey = null;
        if (schedulerId != null) {
            propertyKey = HIBERNATE_CONFIGURATION_FILE + "_" + schedulerId;
            confFile = sosShiroProperties.getProperty(propertyKey);
            error.setCode("JOC-310");
        }
        if (confFile == null) {
            propertyKey = HIBERNATE_CONFIGURATION_FILE;
            confFile = sosShiroProperties.getProperty(propertyKey, "./hibernate.cfg.xml");
            if (confFile == null) {
                error.setMessage(String.format("Could find value for %1$s in joc_properties file", propertyKey));
                throw new JocException(error);
            }
        }
        Path p = sosShiroProperties.resolvePath(confFile);
        if (p != null) {
            if (!Files.exists(p)) {
                error.setMessage(String.format("hibernate configuration (%1$s) is set but file (%2$s) not found.", confFile, p.toString()));
                throw new JocException(error);
            } else {
                confFile = p.toString().replace('\\', '/');
            }
        }
        return confFile;
    }
    
    private static void setJobSchedulerConnectionTimeout() {
        int defaultSeconds = 2;
        if (sosShiroProperties != null) {
            int seconds = sosShiroProperties.getProperty("jobscheduler_connection_timeout", defaultSeconds);
            httpConnectionTimeout = seconds*1000;
            LOGGER.info("HTTP(S) connection timeout = " + seconds );
        }
    }
    
    private static void setJobSchedulerSocketTimeout() {
        int defaultSeconds = 2;
        if (sosShiroProperties != null) {
            int seconds = sosShiroProperties.getProperty("jobscheduler_socket_timeout", defaultSeconds);
            httpSocketTimeout = seconds*1000;
            LOGGER.info("HTTP(S) socket timeout = " + seconds );
        }
    }
    
    private static void setHostnameVerification() {
        boolean defaultVerification = false;
        if (sosShiroProperties != null) {
            withHostnameVerification = sosShiroProperties.getProperty("https_with_hostname_verification", defaultVerification);
            LOGGER.info("HTTPS with hostname verification in certicate = " + withHostnameVerification );
        }
    }
    
    private static void setTrustStore() throws JocException {
        if (sosShiroProperties != null) {
            JocError error = new JocError();
            error.setCode("JOC-311");
            String truststore = sosShiroProperties.getProperty("truststore_path", "");
            if (truststore != null && !truststore.isEmpty()) {
                Path p = sosShiroProperties.resolvePath(truststore);
                if (p != null) {
                    if (!Files.exists(p)) {
                        error.setMessage(String.format("truststore path (%1$s) is set but file (%2$s) not found.", truststore, p.toString()));
                        throw new JocException(error);
                    } else {
                        truststore = p.toString();
                        System.setProperty("javax.net.ssl.trustStore", truststore);
                    }
                }
            }
        }
    }
}
