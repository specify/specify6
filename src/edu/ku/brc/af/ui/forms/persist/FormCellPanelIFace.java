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
package edu.ku.brc.af.ui.forms.persist;

/**
 * Presents a Panel in the UI.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
public interface FormCellPanelIFace extends FormCellIFace
{

    /**
     * @return the type of panel to create
     */
    public abstract String getPanelType();
    
    /**
     * @return the JGoodies Row Definition for panel layout
     */
    public abstract String getRowDef();
    
    /**
     * @return the JGoodies Column Definition for panel layout
     */
    public abstract String getColDef();
    
}