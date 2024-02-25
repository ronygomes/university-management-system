package me.ronygomes.ums.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    private static final int UUID_LENGTH = 36;

    @Column(nullable = false, updatable = false, unique = true, length = UUID_LENGTH)
    private String uuid = UUID.randomUUID().toString();

    @Version
    private Integer version;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntity other)) {
            return false;
        }

        if (uuid == null) return false;

        return uuid.equals(other.getUuid());
    }

    public int hashCode() {
        if (uuid != null) {
            return uuid.hashCode();
        } else {
            return super.hashCode();
        }
    }

    public boolean isNew() {
        return Objects.isNull(getId());
    }

    public abstract Long getId();
}
