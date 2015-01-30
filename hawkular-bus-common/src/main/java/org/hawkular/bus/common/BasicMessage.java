package org.hawkular.bus.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Basic information that is sent over the message bus.
 *
 * The {@link #getMessageId() message ID} is assigned by the messaging framework and so typically is not explicitly set.
 *
 * The {@link #getCorrelationId() correlation ID} is a message ID of another message that was sent previously. This is
 * usually left unset unless this message needs to be correlated with another. As an example, when a process is stopped,
 * you can correlate the "Stopped" event with the "Stopping" event so you can later determine how long it took for the
 * process to stop.
 */
public abstract class BasicMessage {
    // these are passed out-of-band of the message body - these attributes will therefore not be JSON encoded
    private MessageId messageId;
    private MessageId correlationId;

    /**
     * Convenience static method that converts a JSON string to a particular message object.
     *
     * @param json
     *            the JSON string
     * @param clazz
     *            the class whose instance is represented by the JSON string
     *
     * @return the message object that was represented by the JSON string
     */
    public static <T extends BasicMessage> T fromJSON(String json, Class<T> clazz) {
        final Gson gson = createGsonBuilder();
        return gson.fromJson(json, clazz);
    }

    /**
     * Converts this message to its JSON string representation.
     *
     * @return JSON encoded data that represents this message.
     */
    public String toJSON() {
        final Gson gson = createGsonBuilder();
        return gson.toJson(this);
    }

    protected BasicMessage() {
        // Intentionally left blank
    }

    /**
     * Returns the message ID that was assigned to this message by the messaging infrastructure. This could be null if
     * the message has not been sent yet.
     *
     * @return message ID assigned to this message by the messaging framework
     */
    public MessageId getMessageId() {
        return messageId;
    }

    public void setMessageId(MessageId messageId) {
        this.messageId = messageId;
    }

    /**
     * If this message is correlated with another message, this will be that other message's ID. This could be null if
     * the message is not correlated with another message.
     *
     * @return the message ID of the correlated message
     */
    public MessageId getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(MessageId correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.getClass().getSimpleName() + ": [");
        str.append("message-id=");
        str.append(getMessageId());
        str.append(", correlation-id=");
        str.append(getCorrelationId());
        str.append(", json-body=[");
        str.append(toJSON());
        str.append("]]");
        return str.toString();
    }

    protected static Gson createGsonBuilder() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
