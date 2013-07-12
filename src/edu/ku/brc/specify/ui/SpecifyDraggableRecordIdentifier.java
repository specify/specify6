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
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.ui.DataFlavorTableExt;

/**
 * Dervied class that enables the "data" portion of the DraggableRecordIdentifier to be created internally as a RecordSet
 * in which it gets it's value from the FormDataObjIFace.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class SpecifyDraggableRecordIdentifier extends DraggableRecordIdentifier
{
    protected RecordSet          recordSet     = null;
    protected RecordSetItem      recordSetItem = null;
    protected DataFlavorTableExt dataFlavor    = new DataFlavorTableExt(RecordSetTask.class, "Record_Set", -1);
    
    /**
     * Constructor with icon and label.
     * @param icon the icon
     * @param label the label
     */
    public SpecifyDraggableRecordIdentifier(final ImageIcon icon, final String label)
    {
        super(icon, label);
        dragFlavors.add(dataFlavor);

    }

    /**
     * Constructor with icon.
     * @param icon the icon
     */
    public SpecifyDraggableRecordIdentifier(final ImageIcon icon)
    {
        super(icon);
        dragFlavors.add(dataFlavor);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DraggableRecordIdentifier#setData(java.lang.Object)
     */
    @Override
    public void setData(final Object data)
    {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DraggableRecordIdentifier#getData()
     */
    @Override
    public Object getData()
    {
        if (recordSetItem != null && recordSetItem.getRecordId() != null)
        {
            return recordSet;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DraggableRecordIdentifier#setFormDataObj(edu.ku.brc.ui.forms.FormDataObjIFace)
     */
    @Override
    public void setFormDataObj(final FormDataObjIFace formDataObj)
    {
        if (formDataObj == null)
        {
            recordSet     = null;
            recordSetItem = null;
            
        } else
        {
            // Create a new RecordSet if there isn't one or the forDataObj has changed
            if (recordSet == null || formDataObj != this.formDataObj)
            {
                recordSet = new RecordSet();
                recordSet.initialize();
                recordSet.setDbTableId(formDataObj.getTableId());
                dataFlavor.addTableId(formDataObj.getTableId());
            }
            
            recordSetItem = (RecordSetItem)recordSet.addItem(formDataObj.getId());
            setLabel(formDataObj.getIdentityTitle());
        }

        super.setFormDataObj(formDataObj);
    }

}
