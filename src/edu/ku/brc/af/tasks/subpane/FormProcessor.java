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
package edu.ku.brc.af.tasks.subpane;

/**
 * Objects implementing this interface can be used by the FormPane to perform processing
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public interface FormProcessor
{

    /**
     * The form pane that the process will work on. This enables it to have access to the UI components.
     * @param formPane the form pane
     */
    public void setViewable(final FormPane formPane); 
    
}
