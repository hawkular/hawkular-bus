package org.hawkular.bus.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * A message that contains a complex object, which gets serialized into JSON.
 *
 * Use this class to send and receive ad-hoc objects - that is, ones that do not extend from {@link BasicMessage}.
 *
 * @author Heiko W. Rupp
 * @author John Mazzitelli
 */
public class ObjectMessage extends BasicMessage {
    @Expose
    private String message; // the object in JSON form
    private Class<?> objectClass; // the ad-hoc class that this object message represents

    public ObjectMessage() {
        // the owner of the object will have to tell us the object class later
    }

    public ObjectMessage(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }
        setObjectClass(object.getClass());

        final Gson gson = new GsonBuilder().create();
        final String msg = gson.toJson(object);
        setMessage(msg);
    }

    public ObjectMessage(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        setObjectClass(clazz);
    }

    @Override
    public String toJSON() {
        return message; // we override the superclass JSON encoding - our message *is* the JSON string
    }

    /**
     * The simple JSON representation of the object.
     *
     * @return message string as a JSON string
     */
    public String getMessage() {
        return message;
    }

    protected void setMessage(String msg) {
        this.message = msg;
    }

    public Class<?> getObjectClass() {
        return this.objectClass;
    }

    public void setObjectClass(Class<?> objectClass) {
        this.objectClass = objectClass;
    }

    public Object getObject() {
        Class<?> clazz = getObjectClass();
        if (clazz == null) {
            throw new IllegalStateException("Do not know what the class is that represents the JSON data");
        }

        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(getMessage(), clazz);
    }
}
