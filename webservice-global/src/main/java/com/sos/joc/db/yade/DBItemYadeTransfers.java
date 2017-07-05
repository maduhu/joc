package com.sos.joc.db.yade;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.reporting.db.DBLayer;


@Entity
@Table(name = "YADE_TRANSFERS")
@SequenceGenerator(name = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE, sequenceName = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE, allocationSize = 1)
public class DBItemYadeTransfers extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    /** Primary key */
    private Long id;
    /** Others */
    private Long sourceProtocolId;
    private Long targetProtocolId;
    private Long jumpProtocolId;
    private String mandator;
    private Integer operation;
    private Date start;
    private Date end;
    private Integer state;
    private String errorMessage;
    private String log;
    private String configuration;
    private String jobschedulerId;
    private String job;
    private String jobChain;
    private String orderId;
    private Long auditLogId;
    private Boolean hasIntervention;
    private Long parentTransferId;
    private Long numOfFiles;
    private String profileName;
    
    
    public DBItemYadeTransfers() {
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name = "`SOURCE_PROTOCOL_ID`", nullable = false)
    public Long getSourceProtocolId() {
        return sourceProtocolId;
    }
    
    @Column(name = "`SOURCE_PROTOCOL_ID`", nullable = false)
    public void setSourceProtocolId(Long sourceProtocolId) {
        this.sourceProtocolId = sourceProtocolId;
    }
    
    @Column(name = "`TARGET_PROTOCOL_ID`", nullable = true)
    public Long getTargetProtocolId() {
        return targetProtocolId;
    }
    
    @Column(name = "`TARGET_PROTOCOL_ID`", nullable = true)
    public void setTargetProtocolId(Long targetProtocolId) {
        this.targetProtocolId = targetProtocolId;
    }
    
    @Column(name = "`JUMP_PROTOCOL_ID`", nullable = true)
    public Long getJumpProtocolId() {
        return jumpProtocolId;
    }
    
    @Column(name = "`JUMP_PROTOCOL_ID`", nullable = true)
    public void setJumpProtocolId(Long jumpProtocolId) {
        this.jumpProtocolId = jumpProtocolId;
    }
    
    @Column(name = "`MANDATOR`", nullable = true)
    public String getMandator() {
        return mandator;
    }
    
    @Column(name = "`MANDATOR`", nullable = true)
    public void setMandator(String mandator) {
        this.mandator = mandator;
    }
    
    @Column(name = "`OPERATION`", nullable = false)
    public Integer getOperation() {
        return operation;
    }
    
    @Column(name = "`OPERATION`", nullable = false)
    public void setOperation(Integer operation) {
        this.operation = operation;
    }
    
    @Column(name = "`START`", nullable = false)
    public Date getStart() {
        return start;
    }
    
    @Column(name = "`START`", nullable = false)
    public void setStart(Date start) {
        this.start = start;
    }
    
    @Column(name = "`END`", nullable = false)
    public Date getEnd() {
        return end;
    }
    
    @Column(name = "`END`", nullable = false)
    public void setEnd(Date end) {
        this.end = end;
    }
    
    @Column(name = "`STATE`", nullable = false)
    public Integer getState() {
        return state;
    }
    
    @Column(name = "`STATE`", nullable = false)
    public void setState(Integer state) {
        this.state = state;
    }
    
    @Column(name = "`ERROR_MESSAGE`", nullable = true)
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Column(name = "`ERROR_MESSAGE`", nullable = true)
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Column(name = "`LOG`", nullable = true)
    public String getLog() {
        return log;
    }
    
    @Column(name = "`LOG`", nullable = true)
    public void setLog(String log) {
        this.log = log;
    }
    
    @Column(name = "`CONFIGURATION`", nullable = true)
    public String getConfiguration() {
        return configuration;
    }
    
    @Column(name = "`CONFIGURATION`", nullable = true)
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
    
    @Column(name = "`JOBSCHEDULER_ID`", nullable = true)
    public String getJobschedulerId() {
        return jobschedulerId;
    }
    
    @Column(name = "`JOBSCHEDULER_ID`", nullable = true)
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }
    
    @Column(name = "`JOB`", nullable = true)
    public String getJob() {
        return job;
    }
    
    @Column(name = "`JOB`", nullable = true)
    public void setJob(String job) {
        this.job = job;
    }
    
    @Column(name = "`JOB_CHAIN`", nullable = true)
    public String getJobChain() {
        return jobChain;
    }
    
    @Column(name = "`JOB_CHAIN`", nullable = true)
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }
    
    @Column(name = "`ORDER_ID`", nullable = true)
    public String getOrderId() {
        return orderId;
    }
    
    @Column(name = "`ORDER_ID`", nullable = true)
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    @Column(name = "`AUDIT_LOG_ID`", nullable = true)
    public Long getAuditLogId() {
        return auditLogId;
    }
    
    @Column(name = "`AUDIT_LOG_ID`", nullable = true)
    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }
    
    @Column(name = "`HAS_INTERVENTION`", nullable = true)
    @Type(type = "numeric_boolean")
    public Boolean getHasIntervention() {
        return hasIntervention;
    }
    
    @Column(name = "`HAS_INTERVENTION`", nullable = true)
    @Type(type = "numeric_boolean")
    public void setHasIntervention(Boolean hasIntervention) {
        this.hasIntervention = hasIntervention;
    }
    
    @Column(name = "`PARENT_TRANSFER_ID`", nullable = true)
    public Long getParentTransferId() {
        return parentTransferId;
    }
    
    @Column(name = "`PARENT_TRANSFER_ID`", nullable = true)
    public void setParentTransferId(Long parentTransferId) {
        this.parentTransferId = parentTransferId;
    }
    
    @Column(name = "`NUM_OF_FILES`", nullable = true)
    public Long getNumOfFiles() {
        return numOfFiles;
    }
    
    @Column(name = "`NUM_OF_FILES`", nullable = true)
    public void setNumOfFiles(Long numOfFiles) {
        this.numOfFiles = numOfFiles;
    }
    
    @Column(name = "`PROFILE_NAME`", nullable = true)
    public String getProfileName() {
        return profileName;
    }
    
    @Column(name = "`PROFILE_NAME`", nullable = true)
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    
}