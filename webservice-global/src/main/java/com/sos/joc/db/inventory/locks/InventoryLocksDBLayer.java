package com.sos.joc.db.inventory.locks;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBLayer;


public class InventoryLocksDBLayer extends DBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryLocksDBLayer.class);

    public InventoryLocksDBLayer(SOSHibernateConnection connection) {
        super(connection);
    }

    @SuppressWarnings("unchecked")
    public List<DBItemInventoryLock> getLocks(Long instanceId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryLock> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result;
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }        
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryLock> getLocksByFolders(String folderPath, Long instanceId, boolean recursive) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where instanceId = :instanceId");
            if (recursive) {
                sql.append(" and name like :folderPath");
            } else {
                sql.append(" and name = :folderPath");
            }
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            if (recursive) {
                query.setParameter("folderPath", "%" + folderPath + "%");
            } else {
                query.setParameter("folderPath", folderPath);
            }
            List<DBItemInventoryLock> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result;
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }        
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryLock getLock(String lockPath, Long instanceId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and name = :lockPath");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("lockPath", lockPath);
            List<DBItemInventoryLock> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }        
    }
    
    public Date getLockConfigurationDate(Long id) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("select files.fileModified from ");
            sql.append(DBITEM_INVENTORY_FILES).append(" files, ");
            sql.append(DBITEM_INVENTORY_LOCKS).append(" locks ");
            sql.append(" where files.id = locks.fileId");
            sql.append(" and locks.id = :id");
            LOGGER.debug(sql.toString());
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("id", id);
            Object result = query.uniqueResult();
            if (result != null) {
                return (Date)result;
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
}