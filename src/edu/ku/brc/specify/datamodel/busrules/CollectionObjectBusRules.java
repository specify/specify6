/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionType;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.forms.Viewable;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 24, 2007
 *
 */
public class CollectionObjectBusRules extends AttachmentOwnerBaseBusRules
{
    private static final Logger  log = Logger.getLogger(CollectionObjectBusRules.class);
    
    /**
     * Constructor.
     */
    public CollectionObjectBusRules()
    {
        super(CollectionObject.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (!(dataObj instanceof CollectionObject))
        {
            return false;
        }
        
        /*
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        HibernateDataProviderSession hibSession = (HibernateDataProviderSession)session;

        //hibSession.getSession().getSessionFactory().
        SessionFactory.
        session.attach(dataObj);

        CollectionObject colObj = (CollectionObject)dataObj;
        for (Preparation prep : colObj.getPreparations())
        {
            if (prep.getLoanPreparations().size() > 0)
            {
                session.close();
                return false;
            }
        }
        session.close();
        */
        
        return true;
    }
    
    /**
     * @param discipline
     * @return
     */
    protected PrepType getDefaultPrepType()
    {

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(PrepType.getClassTableId());
        if (tableInfo != null)
        {
            
            String sqlStr = QueryAdjusterForDomain.getInstance().adjustSQL("FROM PrepType WHERE collectionTypeId = COLTYPID");
            log.debug(sqlStr);
            if (StringUtils.isNotEmpty(sqlStr))
            {
                try
                {
                    List<?> dataList = session.getDataList(sqlStr);
                    if (dataList != null && !dataList.isEmpty())
                    {
                        // XXX for now we just get the First one
                        return (PrepType)dataList.iterator().next();
                        
                    } else
                    {
                        // No Data Error
                    }
        
                } catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            } else
            {
                log.error("Query String is empty for tableId["+tableInfo.getTableId()+"]");
            }
        } else
        {
            throw new RuntimeException("Error looking up PickLIst's Table Name PrepType");
        }
        return null;
    }
    
    /**
     * @return the default preparer
     */
    protected Agent getDefaultPreparedByAgent()
    {
        return Agent.getUserAgent();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        CollectionObject co              = (CollectionObject)newDataObj;
        CollectionType   ct              = CollectionType.getCurrentCollectionType();
        Discipline       discipline      = Discipline.getDiscipline(ct.getDiscipline());
        //Discipline       plantDiscipline = Discipline.getDiscipline("plant");
        if (discipline != null)
        {
            //if (discipline == plantDiscipline || ct.getName().equals("Plant")) // RELEASE (remove Plant)
            {
                CollectingEvent ce = new CollectingEvent();
                ce.initialize();
                co.addReference(ce, "collectingEvent");
                
                Preparation prep = new Preparation();
                prep.initialize();
                prep.setCount(1);
                prep.setPrepType(getDefaultPrepType());
                prep.setPreparedDate(Calendar.getInstance());
                co.addReference(prep, "preparations");
                prep.setPreparedByAgent(getDefaultPreparedByAgent());
            }
            
        } else
        {
            log.error("Unknown discipline ["+ct.getDiscipline()+"]");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeFormFill(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void beforeFormFill(Viewable viewable)
    {
        super.beforeFormFill(viewable);
    }
    
    
}
