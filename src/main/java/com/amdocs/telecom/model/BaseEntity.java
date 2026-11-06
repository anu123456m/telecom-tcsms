package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public abstract class BaseEntity {
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}