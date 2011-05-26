/* Copyright (C) 2005-2011, University of Kansas Center for Research
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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForInts;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.showLocalizedError;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 16, 2010
 *
 */
public class ContainerBusRules extends BaseBusRules
{
    private static final String CONTAINER_CO_USED = "CONTAINER_CO_USED";
    private static final String CONTAINER_CO_BAD  = "CONTAINER_CO_BAD";
    	
    private static final String GET_PARENT_SQL = "SELECT ParentID FROM container WHERE ContainerID = ";
    
    private ValComboBoxFromQuery parentQCBX = null;
    
    /**
     * 
     */
    public ContainerBusRules()
    {
        super(ContainerBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (parentQCBX == null && formViewObj != null)
        {
            Component comp = formViewObj.getCompById("parent");
            if (comp != null && comp instanceof ValComboBoxFromQuery)
            {
                parentQCBX = (ValComboBoxFromQuery)comp;
                parentQCBX.addListSelectionListener(new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        parentSelected();
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    private void parentSelected()
    {
        if (parentQCBX != null)
        {
            final Container parent = (Container)parentQCBX.getValue();
            if (parent != null)
            {
                final Container currContainer = (Container)formViewObj.getDataObj();
                if (currContainer.getId() != null)
                {
                    if (!searchContainerChildren(parent.getId(), currContainer.getId()))
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String sql = "SELECT Name FROM container WHERE ContainerID = ";
                                final String currName      = querySingleObj(sql + currContainer.getId());
                                final String newParentName = querySingleObj(sql + parent.getId());

                                showLocalizedError("CONTAINER_BAD_PARENT", newParentName, currName);
                                parentQCBX.setValue(null, null);
                            }
                        });
                    }
                }
            }
        }
    }
    
    /**
     * Searches for pId in the children of currParentId
     * @param pId
     * @param currParentId
     * @return true if not a child, false is bad
     */
    private boolean searchContainerChildren(final Integer pId, final Integer currParentId)
    {
        String sql = "SELECT ContainerID FROM container WHERE ParentID = " + currParentId;
        Vector<Integer> childrenIds = queryForInts(sql);
        for (Integer id : childrenIds)
        {
            if (pId.equals(id))
            {
                return false;
            }
            if (!searchContainerChildren(pId, id))
            {
                return false;
            }
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj instanceof Container)
        {
        	UIFieldFormatterIFace catNumFmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "CatalogNumber");
        	
            Container container = (Container)dataObj;
            Container parent    = container.getParent();
            
            if (container.getId() != null)
            {
                for (Container child : container.getChildren())
                {
                    if (parent != null && parent.getId().equals(child.getId()))
                    {
                        reasonList.add(getLocalizedMessage("CONTAINER_PARENT_CHILD", child.getName(), container.getName()));
                        return STATUS.Error;
                    }
                    
                    if (container.getId().equals(child.getId()))
                    {
                        reasonList.add(getLocalizedMessage("CONTAINER_SELF_CHILD", container.getName()));
                        return STATUS.Error;
                    }
                    
                    // Look for cycles XXX -> ZZZ -> YYY -> XXX
                    Integer id = container.getId();
                    do
                    {
                        String  sql      = GET_PARENT_SQL + id;
                        Integer parentId = BasicSQLUtils.getCount(sql);
                        if (parentId == null)
                        {
                            break;
                        }
                        
                        if (parentId.equals(child.getId()))
                        {
                            reasonList.add(getLocalizedMessage("CONTAINER_CYCLE", child.getName()));
                            return STATUS.Error;
                        }
                        id = parentId;
                    } while (true);
                }
            }
            
            ArrayList<String> catNumsInError = new ArrayList<String>();
        	HashMap<Integer, String> colObjIdsHash = new HashMap<Integer, String>(); // needed later
            for (CollectionObject coKid : container.getCollectionObjectKids())
            {
                String  sql = "SELECT ContainerID, ContainerOwnerID FROM collectionobject WHERE (ContainerID IS NOT NULL OR ContainerOwnerID IS NOT NULL) AND CollectionObjectID = " + coKid.getId();
                //System.err.println(sql);
                Vector<Object[]> rows = BasicSQLUtils.query(sql);
                if (rows.size() > 0)
                {
                	Integer cId = container.getId();
                	for (Object[] row : rows)
                	{
                		Integer containerId = (Integer)row[0];
                		if (containerId != null && (cId == null || !containerId.equals(cId)))
                		{
                			catNumsInError.add(coKid.getCatalogNumber());
                		}
                		Integer containerOwnerId = (Integer)row[1];
                		if (containerOwnerId != null && (cId == null || !containerOwnerId.equals(cId)))
                		{
                			catNumsInError.add(coKid.getCatalogNumber());
                		}
                	}
                }
                
                colObjIdsHash.put(coKid.getId(), coKid.getCatalogNumber()); // remember for later
            }
            
            if (catNumsInError.size() > 0)
            {
                for (String catNum : catNumsInError)
                {
                    reasonList.add(getLocalizedMessage(CONTAINER_CO_USED, catNumFmt != null ? catNumFmt.formatToUI(catNum) : catNum));
                }
                
                return STATUS.Error;
            }

            // Check to see if theContainer is being cataloged
            // by checking the colObj data member.
            if (container.getId() != null && container.getCollectionObject() != null)
            {
            	// Now check to see if it previously has one, if not this is new
            	// and just being set and therefore not being caught by the code above.
            	String  sql      = "SELECT CollectionObjectID FROM collectionobject WHERE ContainerID IS NOT NULL AND ContainerID = " + container.getId();
            	//System.err.println(sql);
            	Integer colObjId = BasicSQLUtils.getCount(sql);
                if (colObjId == null)
                {
                	// Ok, it's new, so now check to see if 
                	// this is one of the already used children
                	String catNum = colObjIdsHash.get(container.getCollectionObject().getId());
                	if (catNum != null)
                	{
                		reasonList.add(getLocalizedMessage(CONTAINER_CO_BAD, catNumFmt != null ? catNumFmt.formatToUI(catNum) : catNum));
                		return STATUS.Error;
                	}
                }
            }

        } else
        {
            reasonList.add("Object is of wrong Class.");
            return STATUS.Error;
        }
        
        STATUS nameStatus = isCheckDuplicateNumberOK("name",
                                                    (FormDataObjIFace)dataObj, 
                                                    Container.class, 
                                                    "containerId");
        
        return nameStatus != STATUS.OK ? STATUS.Error : STATUS.OK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#aboutToShutdown()
     */
    @Override
    public void aboutToShutdown()
    {
        super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
    }
}
