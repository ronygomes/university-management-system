package me.ronygomes.ums.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    private static final int UUID_LENGTH = 36;
    private static final String UUID_REGEX = "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})";
    public static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PHONE_REGEX_PATTERN = "^\\+\\d{13}$";

    @NotNull
    @JsonIgnore
    @Pattern(regexp = UUID_REGEX, message = "invalid uuid")
    @Column(nullable = false, updatable = false, unique = true, length = UUID_LENGTH)
    private String uuid = UUID.randomUUID().toString();

    @Version
    @JsonIgnore
    private Integer version;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntity other)) {
            return false;
        }

        if (uuid == null) return false;

        return uuid.equals(other.getUuid());
    }

    @Override
    public int hashCode() {
        if (uuid != null) {
            return uuid.hashCode();
        } else {
            return super.hashCode();
        }
    }

    @JsonIgnore
    public boolean isNew() {
        return Objects.isNull(getId());
    }

    @JsonIgnore
    public abstract Long getId();
}
