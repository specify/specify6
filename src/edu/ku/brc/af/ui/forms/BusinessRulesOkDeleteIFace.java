/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.ui.forms;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * This interface is used to enable a database search (or lengthy search) to see if a
 * data object can be deleted. The method is called no matter what, the arg doDelete
 * tells the destination whether it can delete the object or not.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 11, 2008
 *
 */
public interface BusinessRulesOkDeleteIFace
{

    /**
     * Tell it is ok to be deleted.
     * @param dataObj the data object to be deleted
     * @param session the current session.
     * @param doDelete whether it can be deleted or not.
     */
    public void doDeleteDataObj(Object dataObj, DataProviderSessionIFace session, boolean doDelete);
    
    
}
