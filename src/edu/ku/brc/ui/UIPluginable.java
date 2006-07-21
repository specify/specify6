package edu.ku.brc.ui;

import java.util.Map;

public interface UIPluginable
{
    /**
     * Sets the property list into the plugin control
     * @param properties the map of properties
     */
    public void initialize(Map<String, String> properties);

}
