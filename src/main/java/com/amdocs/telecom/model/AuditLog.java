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
}