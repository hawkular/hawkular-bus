package org.hawkular.bus.common;

/**
 * Identifies a message that has been sent over the message bus.
 */
public class MessageId {
    private final String id;

    public MessageId(String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MessageId)) {
            return false;
        }
        MessageId other = (MessageId) obj;
        return id.equals(other.id);
    }
}
