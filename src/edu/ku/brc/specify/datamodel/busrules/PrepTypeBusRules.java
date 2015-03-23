/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

/**
 * Business Rules for PrepTypes.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 19, 2006
 *
 */
public class PrepTypeBusRules extends BaseBusRules
{

    /**
     * Constructor.
     */
    public PrepTypeBusRules()
    {
        super(PrepType.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getDeleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof PrepType)
        {
            return getLocalizedMessage("PREPTYPE_DELETED", ((PrepType)dataObj).getName());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getWarningsAndErrors()
     */
    public List<String> getWarningsAndErrors()
    {
        // TODO Auto-generated method stub
        return reasonList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToEnableDelete(Object dataObj)
    {
        reasonList.clear();
        
        PrepType prepType = (PrepType)dataObj;
        if (prepType.getId() == null)
        {
            return true;
        }
        
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from preparation where preparation.PrepTypeID = "+prepType.getId());
            return rs.next() && rs.getInt(1) == 0;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PrepTypeBusRules.class, ex);
            ex.printStackTrace();
            
        } finally
        {
            try 
            {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PrepTypeBusRules.class, ex);
                ex.printStackTrace();
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusinessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj == null || !(dataObj instanceof PrepType))
        {
            return STATUS.Error;
        } 
        
        if (dataObj instanceof PrepType)
        {
            PrepType prepType = (PrepType)dataObj;
            
            if (prepType.getId() == null)
            {
                return isCheckDuplicateNumberOK("name", 
                                                (FormDataObjIFace)dataObj, 
                                                PrepType.class, 
                                                "prepTypeId");
            }
        }
        
        return STATUS.OK;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.forms.DraggableRecordIdentifier)
     */
    public void setObjectIdentity(Object dataObj, DraggableRecordIdentifier draggableIcon)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        PrepType prepType = (PrepType)newDataObj;
        
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);//
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            collection = session.getData(Collection.class, "collectionId", collection.getId(), DataProviderSessionIFace.CompareType.Equals);
            collection.addReference(prepType, "prepTypes");
            prepType.setCollection(collection);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PrepTypeBusRules.class, ex);
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    
    /**
     * Notifies the PickList Cache (Factory) that the PickList has changed.
     * @param pickList the pickList that has changed
     */
    private void dispatchChangeNotification(final String pickListName)
    {
        CommandDispatcher.dispatch(new CommandAction("PICKLIST", "CLEAR",pickListName));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterDeleteCommit(java.lang.Object)
     */
    @Override
    public void afterDeleteCommit(Object dataObj)
    {
        super.afterDeleteCommit(dataObj);
        dispatchChangeNotification(dataObj.getClass().getSimpleName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        dispatchChangeNotification(dataObj.getClass().getSimpleName());
        return super.afterSaveCommit(dataObj, session);
    }
}
