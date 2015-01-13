package org.hawkular.bus.common.test;

import java.util.Map;

import org.hawkular.bus.common.SimpleBasicMessage;

import com.google.gson.annotations.Expose;

/**
 * Test subclass of BasicMessage.
 */
public class SpecificMessage extends SimpleBasicMessage {
    @Expose
    private final String specific;

    public SpecificMessage(String message, Map<String, String> details, String specific) {
        super(message, details);
        if (specific == null) {
            throw new NullPointerException("specific string cannot be null");
        }
        this.specific = specific;
    }

    public String getSpecific() {
        return specific;
    }
}
