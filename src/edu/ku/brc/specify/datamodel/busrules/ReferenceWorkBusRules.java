/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author Administrator
 *
 *Added in case of need to manage ContainingReferenceWork relationship.
 */
public class ReferenceWorkBusRules extends BaseBusRules
{

	/**
	 * 
	 */
	public ReferenceWorkBusRules()
	{
	    super(ReferenceWork.class);
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(Object dataObj,
                           DataProviderSessionIFace session,
                           BusinessRulesOkDeleteIFace deletable)
    {
        ReferenceWork rw = (ReferenceWork)dataObj;
        
        if (rw.getId() != null)
        {
            String sql = "SELECT count(*) FROM referencework WHERE JournalID is NULL AND ReferenceWorkID = " + rw.getId();
            Integer cnt = BasicSQLUtils.getCount(sql);
            if (cnt == 0)
            {
                UIRegistry.showLocalizedError("RW_NO_DEL");
                return;
            }
        }
        super.okToDelete(dataObj, session, deletable);
    }
	
	
}
