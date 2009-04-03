/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.ExchangeOut;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 3, 2009
 *
 */
public class ExchangeOutBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public ExchangeOutBusRules()
    {
        super(ExchangeOut.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(Object newDataObj)
    {
        ((ExchangeOut)newDataObj).setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
    }

}
