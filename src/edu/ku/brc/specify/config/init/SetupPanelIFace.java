/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import java.awt.Component;
import java.util.List;
import java.util.Properties;

import edu.ku.brc.util.Pair;

public interface SetupPanelIFace
{

    /**
     * @return the panelName
     */
    public abstract String getPanelName();

    public abstract void getValues(Properties props);

    public abstract void setValues(Properties values);

    public abstract boolean isUIValid();

    public abstract void updateBtnUI();
    
    public abstract Component getUIComponent();
    
    public abstract void doingPrev();
    
    public abstract void doingNext();
    
    public abstract List<Pair<String, String>> getSummary();

}