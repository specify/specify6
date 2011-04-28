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

import java.awt.Component;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.ui.UIRegistry;

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
    //private JButton containerTreeBtn = null;
    
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
                                final String currName      = BasicSQLUtils.querySingleObj(sql + currContainer.getId());
                                final String newParentName = BasicSQLUtils.querySingleObj(sql + parent.getId());

                                UIRegistry.showLocalizedError("CONTAINER_BAD_PARENT", newParentName, currName);
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
        Vector<Integer> childrenIds = BasicSQLUtils.queryForInts(sql);
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
        
        if (!(dataObj instanceof Container))
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
