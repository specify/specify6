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
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.DraggableRecordIdentifierFactory;

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
