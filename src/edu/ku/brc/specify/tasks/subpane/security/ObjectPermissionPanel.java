/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.PermissionIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
@SuppressWarnings("serial")
public class ObjectPermissionPanel extends JPanel implements PermissionEditorIFace
{
    protected BasicPermisionPanel[] panels = new BasicPermisionPanel[3];
    
    /**
     * 
     */
    public ObjectPermissionPanel()
    {
        super();
        
        String[] keys = new String[] {"SEC_USER", "SEC_VIEW_TITLE","SEC_MOD_TITLE","SEC_ADD_TITLE","SEC_DEL_TITLE",
                                      "SEC_GROUP","SEC_VIEW_TITLE","SEC_MOD_TITLE","SEC_ADD_TITLE","SEC_DEL_TITLE",
                                      "SEC_OTHER","SEC_VIEW_TITLE","SEC_MOD_TITLE","SEC_ADD_TITLE","SEC_DEL_TITLE",};
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,f:p:g,20px,p,f:p:g", "p,10px,p"), this);
        
        int keyInx = 0;
        pb.add(panels[0] = new BasicPermisionPanel(keys[keyInx], 
                                                   keys[keyInx+1], 
                                                   keys[keyInx+2], 
                                                   keys[keyInx+3], 
                                                   keys[keyInx+4]), cc.xy(1, 1));            
        keyInx += 5;
        
        pb.add(panels[1] = new BasicPermisionPanel(keys[keyInx], 
                keys[keyInx+1], 
                keys[keyInx+2], 
                keys[keyInx+3], 
                keys[keyInx+4]), cc.xy(4, 1));            
        keyInx += 5;
        
        pb.add(panels[2] = new BasicPermisionPanel(keys[keyInx], 
                keys[keyInx+1], 
                keys[keyInx+2], 
                keys[keyInx+3], 
                keys[keyInx+4]), cc.xy(1, 3));            
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setOverrideText(int, java.lang.String, boolean)
     */
    @Override
    public void setOverrideText(int option, String text, final boolean isReadOnly)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(final ChangeListener changeListener)
    {
        for (BasicPermisionPanel p : panels)
        {
            p.addChangeListener(changeListener);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#getPermissions()
     */
    @Override
    public List<PermissionIFace> getPermissions()
    {
        ArrayList<PermissionIFace> list = new ArrayList<PermissionIFace>(3);
        
        for (BasicPermisionPanel p : panels)
        {
            List<PermissionIFace> tmpList = p.getPermissions();
            list.add(tmpList.get(0));
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#hasChanged()
     */
    @Override
    public boolean hasChanged()
    {
        for (BasicPermisionPanel p : panels)
        {
            boolean hasChgd = p.hasChanged();
            if (hasChgd)
            {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener(ChangeListener changeListener)
    {
        for (BasicPermisionPanel p : panels)
        {
            p.removeChangeListener(changeListener);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean changed)
    {
        for (BasicPermisionPanel p : panels)
        {
            p.setChanged(changed);
        } 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setPermissions(edu.ku.brc.af.auth.PermissionIFace)
     */
    @Override
    public void setPermissions(List<PermissionIFace> permissions)
    {
        if (permissions.size() == 3)
        {
            ArrayList<PermissionIFace> tmpList = new ArrayList<PermissionIFace>(1);
            
            int i = 0;
            for (PermissionIFace ps : permissions)
            {
                tmpList.clear();
                tmpList.add(ps);
                panels[i++].setPermissions(tmpList);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#getAssociatedTableIds()
     */
    @Override
    public int[] getAssociatedTableIds()
    {
        return null;
    }
}
