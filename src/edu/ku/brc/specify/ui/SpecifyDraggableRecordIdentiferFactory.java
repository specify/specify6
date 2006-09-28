/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory;

/**
 * Specify's implementation of the factory.
 * This factory creates DraggableRecordIdentifier objects.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class SpecifyDraggableRecordIdentiferFactory extends DraggableRecordIdentifierFactory
{
    /**
     * Default Constructor.
     */
    public SpecifyDraggableRecordIdentiferFactory()
    {
        // Default Constructor
    }
    
    /**
     * Creates an instance of a DraggableRecordIdentifier.
     * @param icon an icon
     * @return the Specify specific new instance
     */
    public DraggableRecordIdentifier createDraggableRecordIdentifier(final ImageIcon icon)
    {
        return new SpecifyDraggableRecordIdentifier(icon); 
    }
    
}
