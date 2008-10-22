/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.auth;

import java.awt.Component;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
public interface PermissionPanelContainerIFace
{
    /**
     * @return
     */
    public abstract String getPanelName();
    
    /**
     * @return
     */
    public abstract Component getUIComponent();
    
    /**
     * @param principalArg
     * @param overrulingPrincipal
     */
    public abstract void updateData(SpPrincipal principalArg, SpPrincipal overrulingPrincipal);
    
    /**
     * 
     */
    public abstract void savePermissions(DataProviderSessionIFace session) throws Exception;
}
