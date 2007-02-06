/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.ui.forms.FormDataObjIFace;

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
    protected RecordSet     recordSet     = null;
    protected RecordSetItem recordSetItem = null;
    
    /**
     * Constructor with icon and label.
     * @param icon the icon
     * @param label the label
     */
    public SpecifyDraggableRecordIdentifier(final ImageIcon icon, final String label)
    {
        super(icon, label);
        dragFlavors.add(RecordSetTask.RECORDSET_FLAVOR);

    }

    /**
     * Constructor with icon.
     * @param icon the icon
     */
    public SpecifyDraggableRecordIdentifier(final ImageIcon icon)
    {
        super(icon);
        dragFlavors.add(RecordSetTask.RECORDSET_FLAVOR);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DraggableRecordIdentifier#setData(java.lang.Object)
     */
    @Override
    public void setData(final Object data)
    {
        
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
            }
            
            recordSet.addItem(formDataObj.getId());
            setLabel(formDataObj.getIdentityTitle());
        }

        super.setFormDataObj(formDataObj);
    }

}
