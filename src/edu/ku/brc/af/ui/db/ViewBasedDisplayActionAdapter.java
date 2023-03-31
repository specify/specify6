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
package edu.ku.brc.af.ui.db;

/**
 * This Listener/Adaptor is used for those who need to perform a specific task for non-model Dialogs and
 * for Frames. Otherwise, the work can be done after the setVisible.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 18, 2007
 *
 */
public class ViewBasedDisplayActionAdapter
{
    /**
     * 
     */
    public ViewBasedDisplayActionAdapter()
    {
        // no op
    }
    
    /**
     * @param vbd
     * @return
     */
    public boolean okPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd) //$NON-NLS-1$
    {
        return true;
    }
    
    /**
     * @param vbd
     * @return
     */
    public boolean cancelPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd) //$NON-NLS-1$
    {
        return true;
    }
    
    /**
     * @param vbd
     * @return
     */
    public boolean applyPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd) //$NON-NLS-1$
    {
        return true;
    }
    
    /**
     * @param vbd
     * @return
     */
    public boolean helpPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd) //$NON-NLS-1$
    {
        return true;
    }
    
}
