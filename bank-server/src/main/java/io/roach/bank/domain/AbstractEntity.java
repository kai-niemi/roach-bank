package io.roach.bank.domain;

import java.io.Serializable;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;

import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractEntity<T extends Serializable> implements Persistable<T> {
    @Transient
    private boolean isNew = true;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
        onCreate();
    }

    protected void onCreate() {

    }
}
