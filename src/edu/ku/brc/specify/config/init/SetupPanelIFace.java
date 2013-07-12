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
package edu.ku.brc.specify.config.init;

import java.awt.Component;
import java.util.List;
import java.util.Properties;

import edu.ku.brc.util.Pair;

public interface SetupPanelIFace
{

    /**
     * @return the panelName
     */
    public abstract String getPanelName();
    
    public abstract String getHelpContext();

    public abstract void getValues(Properties props);

    public abstract void setValues(Properties values);

    public abstract boolean isUIValid();

    public abstract void updateBtnUI();
    
    public abstract Component getUIComponent();
    
    public abstract void doingPrev();
    
    public abstract void aboutToLeave();
    
    public abstract void doingNext();
    
    public abstract boolean enablePreviousBtn();
    
    public abstract List<Pair<String, String>> getSummary();
    
    
}
