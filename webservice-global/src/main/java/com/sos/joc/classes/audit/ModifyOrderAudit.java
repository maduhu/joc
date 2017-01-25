package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.order.ModifyOrder;
import com.sos.joc.model.order.ModifyOrders;


public class ModifyOrderAudit extends ModifyOrders implements IAuditLog {
    
    @JsonIgnore
    private String comment;
    @JsonIgnore
    private String folder;
    @JsonIgnore
    private String job;
    @JsonIgnore
    private String jobChain;
    @JsonIgnore
    private String orderId;

    public ModifyOrderAudit(ModifyOrder modifyOrder, String jobschedulerId) {
        Path p = Paths.get(modifyOrder.getJobChain());
        this.comment = modifyOrder.getComment();
        this.folder = p.getParent().toString().replace('\\', '/');
        this.job = null;
        this.jobChain = p.getFileName().toString();
        this.orderId = modifyOrder.getOrderId();
        modifyOrder.setComment(null);
        getOrders().add(modifyOrder);
        setJobschedulerId(jobschedulerId);
    }

    @Override
    @JsonIgnore
    public String getComment() {
        return comment;
    }

    @Override
    @JsonIgnore
    public String getFolder() {
        return folder;
    }

    @Override
    @JsonIgnore
    public String getJob() {
        return job;
    }

    @Override
    @JsonIgnore
    public String getJobChain() {
        return jobChain;
    }

    @Override
    @JsonIgnore
    public String getOrderId() {
        return orderId;
    }
}