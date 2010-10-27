/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.event.DocumentEvent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.ui.containers.ContainerTreePanel;
import edu.ku.brc.ui.DocumentAdaptor;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 7, 2010
 *
 */
public class ContainerListPlugin extends UIPluginBase
{
    protected ContainerTreePanel treePanel;
    
    /**
     * 
     */
    public ContainerListPlugin()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(final Properties propertiesArg, final boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        treePanel = new ContainerTreePanel(isViewModeArg, null, null);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g",  "p,4px,f:p:g"), this);
        
        pb.addSeparator("Container Hierarchy",  cc.xy(1, 1));
        pb.add(treePanel,                       cc.xy(1, 3));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (value instanceof Container)
        {
            treePanel.set((Container)value, null);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
        treePanel.setFVO(parent);
        
        Component comp = parent.getCompById("nm");
        if (comp instanceof ValTextField)
        {
            final ValTextField nameTF = parent.getCompById("nm");
            nameTF.getDocument().addDocumentListener(new DocumentAdaptor() {
                @Override
                protected void changed(DocumentEvent e)
                {
                    treePanel.nameFieldChanged(nameTF.getText());
                }
            });
            
            final ValComboBox typeCBX = parent.getCompById("typ");
            typeCBX.getComboBox().addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    PickListItemIFace pli = (PickListItemIFace)typeCBX.getComboBox().getSelectedItem();
                    treePanel.typeChanged(pli == null ? -1 : Integer.parseInt(pli.getValue()));
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"collectionObject"};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#shutdown()
     */
    @Override
    public void shutdown()
    {
        treePanel.cleanUp();
    }

}
