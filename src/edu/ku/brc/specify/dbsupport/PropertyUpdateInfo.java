package edu.ku.brc.specify.dbsupport;

public class PropertyUpdateInfo {
    protected String name;
    protected Object oldValue;
    protected Object newValue;

    public PropertyUpdateInfo(String name, Object oldValue, Object newValue) {
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public PropertyUpdateInfo(String name) {
        this.name = name;
        this.oldValue = null;
        this.newValue = null;
    }

    public String getName() {
        return name;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
};