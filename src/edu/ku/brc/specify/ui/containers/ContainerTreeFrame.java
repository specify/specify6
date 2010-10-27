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
package edu.ku.brc.specify.ui.containers;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.ui.CustomFrame;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 3, 2010
 *
 */
public class ContainerTreeFrame extends CustomFrame
{
    protected Container          rootContainer;
    protected CollectionObject   rootColObj;
    protected ContainerTreePanel treePanel;
    
    
    /**
     * @param title
     * @param contentPanel
     * @throws HeadlessException
     */
    public ContainerTreeFrame(final String title, 
                              final Container rootContainer, 
                              final CollectionObject rootColObj) throws HeadlessException
    {
        super(title, null);
        this.rootContainer = rootContainer;
        this.rootColObj    = rootColObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomFrame#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        treePanel = new ContainerTreePanel(true, rootContainer, rootColObj);
        
        mainPanel.add(treePanel, BorderLayout.CENTER);
    }
}
