package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;

public class PreparationBusRules extends AttachmentOwnerBaseBusRules
{
    public PreparationBusRules()
    {
        super(Preparation.class);
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = false;
        if (deletable != null)
        {
            FormDataObjIFace dbObj = (FormDataObjIFace)dataObj;
            
            Integer id = dbObj.getId();
            if (id == null)
            {
                isOK = false;
                
            } else
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(Agent.getClassTableId());
                String[] tableFieldList = gatherTableFieldsForDelete(new String[] {"agent", "address", "agentvariant"}, tableInfo);
                isOK = okToDelete(tableFieldList, dbObj.getId());
            }
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }
}
