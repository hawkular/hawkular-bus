package org.hawkular.bus.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * A simple message that is sent over the message bus.
 */
public class SimpleBasicMessage extends BasicMessage {
    // the basic message body - it will be exposed to the JSON output
    @Expose
    private String message;

    // some optional additional details about the basic message
    @Expose
    private Map<String, String> details;

    protected SimpleBasicMessage() {
        ; // Intentionally left blank
    }

    public SimpleBasicMessage(String message) {
        this(message, null);
    }

    public SimpleBasicMessage(String message, Map<String, String> details) {
        this.message = message;

        // make our own copy of the details data
        if (details != null && !details.isEmpty()) {
            this.details = new HashMap<String, String>(details);
        } else {
            this.details = null;
        }
    }

    /**
     * The simple message string of this message.
     * 
     * @return message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Allow subclasses to set the message
     */
    protected void setMessage(String msg) {
        this.message = msg;
    }

    /**
     * Optional additional details about this message. This could be null if there are no additional details associated
     * with this message.
     *
     * @return the details of this message or null. This is an unmodifiable, read-only map of details.
     */
    public Map<String, String> getDetails() {
        if (details == null) {
            return null;
        }
        return Collections.unmodifiableMap(details);
    }
}
