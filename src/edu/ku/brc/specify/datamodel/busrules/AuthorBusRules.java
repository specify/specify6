/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.specify.datamodel.Author;
import edu.ku.brc.specify.datamodel.ReferenceWork;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 23, 2008
 *
 */
public class AuthorBusRules extends BaseBusRules
{

    /**
     * 
     */
    public AuthorBusRules()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object parentDataObj, final Object dataObj, final boolean isExistingObject)
    {
        reasonList.clear();
        
        // isEdit is false when the data object is new, true when editing an existing object.
        if (isExistingObject &&
            parentDataObj instanceof ReferenceWork &&
            dataObj instanceof Author)
        {
            ReferenceWork ce = (ReferenceWork)parentDataObj;
            Author       col = (Author)dataObj;
            
            for (Author Author : ce.getAuthors())
            {
               if (Author.getAgent().getAgentId().equals(col.getAgent().getAgentId())) 
               {
                   reasonList.add(String.format(getResourceString("RW_DUPLICATE_AUTHORS"), col.getIdentityTitle()));
                   return STATUS.Error;
               }
            }
        }
        
        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
    }

}
