/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UICacheManager.getLocalizedMessage;
import edu.ku.brc.specify.datamodel.Geography;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class GeographyBusRules extends BaseBusRules
{
    public GeographyBusRules()
    {
        super(Geography.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.SimpleBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("GEOGRAPHY_DELETED", ((Geography)dataObj).getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.SimpleBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
