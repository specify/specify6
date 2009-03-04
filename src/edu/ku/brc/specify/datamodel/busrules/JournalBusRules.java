/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 4, 2009
 *
 */
public class JournalBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public JournalBusRules()
    {
        super(Journal.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(Object dataObj,
                           DataProviderSessionIFace session,
                           BusinessRulesOkDeleteIFace deletable)
    {
        Journal journal = (Journal)dataObj;
        
        if (journal.getId() != null)
        {
            String sql = "SELECT count(*) FROM referencework r WHERE JournalID = " + journal.getId();
            Integer cnt = BasicSQLUtils.getCount(sql);
            if (cnt != null && cnt > 0)
            {
                UIRegistry.showLocalizedError("JN_NO_DEL");
                return;
            }
        }
        super.okToDelete(dataObj, session, deletable);
    }

}
