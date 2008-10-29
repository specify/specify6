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
import edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator;

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
     * @param doAddDefaultPermissions
     */
    public abstract void updateData(SpPrincipal principalArg, 
                                    SpPrincipal overrulingPrincipal, 
                                    boolean     doAddDefaultPermissions);
    
    /**
     * 
     */
    public abstract void savePermissions(DataProviderSessionIFace session) throws Exception;
    
    /**
     * @return the enumerator used to display the permissions
     */
    public abstract PermissionEnumerator  getPermissionEnumerator();
}
