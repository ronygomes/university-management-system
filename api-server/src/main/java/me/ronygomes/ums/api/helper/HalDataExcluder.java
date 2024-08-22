package me.ronygomes.ums.api.helper;

import java.io.Serializable;

public interface HalDataExcluder extends Serializable {

    String FILTER_NAME = "hal-content-filter";

    enum HalDataOutputType {
        FULL, EMBEDDED
    }

    HalDataOutputType displayType();

    boolean include(HalDataOutputType type, String propertyName);
}
