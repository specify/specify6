/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * This is a PermissionEnumerator that wraps one or more actual heterogeneous enumerators into a 
 * single enumerator.
 * 
 * @author Ricardo
 *
 */
public class CompositePermissionEnumerator extends PermissionEnumerator {

    protected List<PermissionEnumerator> enumerators = new ArrayList<PermissionEnumerator>();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getPermissions(edu.ku.brc.specify.datamodel.SpPrincipal, java.util.Hashtable, java.util.Hashtable, java.lang.String)
     */
    @Override
    public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
                                                         final Hashtable<String, SpPermission> existingUserPerms,
                                                         final Hashtable<String, SpPermission> existingGroupPerms,
                                                         final String                          userType) 
    {
        List<PermissionEditorRowIFace> allPerms = new ArrayList<PermissionEditorRowIFace>();
        for (PermissionEnumerator currEnumerator : enumerators)
        {
            allPerms.addAll(currEnumerator.getPermissions(principal, existingUserPerms, existingGroupPerms, userType));
        }
        return allPerms;
    }
    
    public void clear()
    {
        enumerators.clear();
    }
    
    public void addEnumerator(PermissionEnumerator enumerator)
    {
        enumerators.add(enumerator);
    }
}
