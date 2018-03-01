/**
 * 
 */
package org.rta.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 *
 */
public enum Status {
	
	FRESH(1, "FRESH"), PENDING(2, "PENDING"), APPROVED(3, "APPROVED"), REJECTED(4, "REJECTED"),
    CLOSED(5,"CLOSED");

    private static final Map<Integer, String> lookup = new HashMap<Integer, String>();
    private static final Map<String, Status> labelToStatus = new HashMap<String, Status>();
    private static final Map<Integer, Status> valueToStatus = new HashMap<Integer, Status>();

    static {
        for (Status status : EnumSet.allOf(Status.class)) {
            lookup.put(status.getValue(), status.getLabel());
        }
        for (Status status : EnumSet.allOf(Status.class)) {
            labelToStatus.put(status.getLabel(), status); 
        }
        for (Status status : EnumSet.allOf(Status.class)) {
            valueToStatus.put(status.getValue(), status); 
        }
    }

    private Integer value;
    private String label;

    private Status(Integer valuesd, String label) {
        this.value = valuesd;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static String getLabel(Integer value) {
        return lookup.get(value);
    }
    
    public static Status getStatus(String label) {
        return (label == null) ? null : labelToStatus.get(label.toUpperCase());
    }
    
    public static Status getStatus(Integer value) {
        return valueToStatus.get(value);
    }
}
