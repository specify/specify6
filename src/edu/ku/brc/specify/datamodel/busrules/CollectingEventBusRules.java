/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.awt.Component;

import javax.swing.JButton;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PaleoContext;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod 
 *
 * (original author was JDS)
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class CollectingEventBusRules extends AttachmentOwnerBaseBusRules
{
    private Component	     paleoContextCmp  = null;
    private PaleoContext     cachedPalCon     = null;
    
    /**
     * 
     */
    public CollectingEventBusRules()
    {
        super(CollectingEvent.class);
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	public void beforeMerge(Object dataObj, DataProviderSessionIFace session) {
		super.beforeMerge(dataObj, session);
        if (AppContextMgr.getInstance().getClassObject(Discipline.class).getIsPaleoContextEmbedded())
        {
            CollectingEvent ceObj = CollectingEvent.class.cast(dataObj);
        	cachedPalCon = ceObj != null ? ceObj.getPaleoContext() : null;
            if (ceObj != null && cachedPalCon != null)
            {
                ceObj.setPaleoContext(null);
                try
                {
                    cachedPalCon.getCollectingEvents().clear();
                } catch (org.hibernate.LazyInitializationException ex)
                {
                    //ex.printStackTrace();
                } catch (Exception ex)
                {
                    //ex.printStackTrace();
                }
            }
        }
	}

	
	

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	public void beforeSave(Object dataObj, DataProviderSessionIFace session) {
		super.beforeSave(dataObj, session);
        if (AppContextMgr.getInstance().getClassObject(Discipline.class).getIsPaleoContextEmbedded())
        {
            if (cachedPalCon != null)
            {
                try
                {
                    if (cachedPalCon != null && cachedPalCon.getId() != null)
                    {
                    	cachedPalCon = session.merge(cachedPalCon);
                    } else
                    {
                        session.save(cachedPalCon);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectingEventBusRules.class, ex);
                }
            }
            
            // Hook back up
            CollectingEvent ceObj = CollectingEvent.class.cast(dataObj);
            if (cachedPalCon != null && ceObj != null)
            {
                ceObj.setPaleoContext(cachedPalCon);
                cachedPalCon.getCollectingEvents().add(ceObj);
                cachedPalCon = null;
            } else
            {
                log.error("The PC "+cachedPalCon+" was null or the CE "+ceObj+" was null");
            }     
        }
	}

	

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	public boolean beforeDeleteCommit(Object dataObj,
			DataProviderSessionIFace session) throws Exception {
		boolean result = super.beforeDeleteCommit(dataObj, session);
		if (result) {
			if (dataObj != null) {
				Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
				if (discipline != null && discipline.getIsPaleoContextEmbedded()) {
					CollectingEvent ceObj = CollectingEvent.class.cast(dataObj);
					PaleoContext pc = ceObj.getPaleoContext();
					if (pc != null) {
						try {
							session.delete(pc);
							result = true;
						} catch (Exception ex) {
							edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
							edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectingEventBusRules.class, ex);
							ex.printStackTrace();
							result = false;
						}
					}
				}
			}
		}
		return result;
	}


	/* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            JButton newBtn = getNewBtn();
            if (newBtn != null)
            {
                newBtn.setVisible(false);
            }
            JButton delBtn = getDelBtn();
            if (delBtn != null)
            {
                delBtn.setVisible(false);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object                     dataObj,
                           final DataProviderSessionIFace   session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = false;
        if (deletable != null)
        {
            CollectingEvent ce = (CollectingEvent)dataObj;
            
            Integer id = ce.getId();
            if (id == null)
            {
                isOK = true;
                
            } else
            {
                Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                int        count      = collection.getIsEmbeddedCollectingEvent() ? 1 : 0;
                isOK = okToDelete(count, new String[] {"collectionobject", "CollectingEventID"}, ce.getId());
            }
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj != null)
        {
            Pair<Component, FormViewObj> paleoContext = formViewObj.getControlWithFormViewObjByName("paleoContext");
            if (paleoContext != null && paleoContextCmp == null && paleoContext.getSecond() == this.formViewObj) {
            	paleoContextCmp = paleoContext.getFirst();
            	Discipline disc = (AppContextMgr.getInstance().getClassObject(Discipline.class));
            	if (!"collectingevent".equalsIgnoreCase(disc.getPaleoContextChildTable())) {
            		UIRegistry.showLocalizedMsg("CollectingEventBusRules.PaleoRelationshipDisabled");
            		paleoContextCmp.setEnabled(false);
            	} else {
                 		UsageTracker.incrUsageCount("CollectionEventBusRules.AfterFillForm.PaleoRelationshipDisplayed");
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCloneField(java.lang.String)
     */
    @Override
    public boolean shouldCloneField(String fieldName)
    {
        if (fieldName.equals("collectingEventAttribute"))
        {
            return true;
        }

        Discipline disc = AppContextMgr.getInstance().getClassObject(Discipline.class);        
        if (fieldName.equals("paleoContext") && disc.getIsPaleoContextEmbedded()
        		&& disc.getPaleoContextChildTable().equalsIgnoreCase("collectingevent"))
        {
        	DisciplineType dt = DisciplineType.getByName(disc.getType());
            if (dt != null && dt.isPaleo())
            {
                return true;
            }
            return false;
        }

        return false;
    }
}
