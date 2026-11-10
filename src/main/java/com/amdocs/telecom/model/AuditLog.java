package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class AuditLog extends BaseEntity {
    private long auditId;
    private ActorType actorType;
    private Integer actorId;
    private String action;
    private String entityType;
    private Integer entityId;
    private String details;
    private LocalDateTime actionTime;

    public long getAuditId() { return auditId; }
    public void setAuditId(long auditId) { this.auditId = auditId; }
    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }
    public Integer getActorId() { return actorId; }
    public void setActorId(Integer actorId) { this.actorId = actorId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}