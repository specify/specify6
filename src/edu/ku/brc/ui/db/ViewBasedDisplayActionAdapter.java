/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.ui.db;

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

    public ViewBasedDisplayActionAdapter()
    {
        // no op
    }
    
    public void okPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd)
    {
        // no op
    }
    
    public void cancelPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd)
    {
        // no op
    }
    
    public void applyPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd)
    {
        
    }
    
    public void helpPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd)
    {
        // no op
    }
    
}
