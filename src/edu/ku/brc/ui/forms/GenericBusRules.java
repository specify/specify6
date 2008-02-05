/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms;


/**
 * Simple placeholder BusinessRules implementation.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Feb 5, 2008
 *
 */
public class GenericBusRules extends BaseBusRules
{

    /**
     * Constructor.
     */
    public GenericBusRules()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

}
