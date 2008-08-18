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

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 11, 2008
 *
 */
public class CollectionBusRules extends BaseBusRules
{

    /**
     * Constructor.
     */
    public CollectionBusRules()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(final Object dataObj)
    {
        boolean isOK =  okToDelete("collectionobject", "CollectionID", ((FormDataObjIFace)dataObj).getId());
        if (!isOK)
        {
            return false;
        }
        
        Collection collection = (Collection)dataObj;
        
        String colMemName = "CollectionMemberID";
        
        Vector<String> tableList = new Vector<String>();
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            for (DBFieldInfo fi : ti.getFields())
            {
                String colName = fi.getColumn();
                if (StringUtils.isNotEmpty(colName) && colName.equals(colMemName))
                {
                    tableList.add(ti.getName());
                    break;
                }
            }
        }
        
        int inx = 0;
        String[] tableFieldNamePairs = new String[tableList.size() * 2];
        for (String tableName : tableList)
        {
            tableFieldNamePairs[inx++] = tableName;
            tableFieldNamePairs[inx++] = colMemName;
        }
        isOK = okToDelete(tableFieldNamePairs, collection.getId());
        
        return isOK;
    }
    
}
