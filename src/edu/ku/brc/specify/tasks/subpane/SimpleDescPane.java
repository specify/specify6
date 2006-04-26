/* Filename:    $RCSfile: SimpleDescPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.tasks.subpane;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.ku.brc.specify.core.Taskable;

/**
 * A default pane for display a simple label telling what it is suppose to do
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class SimpleDescPane extends BaseSubPane
{
    //private static Log log = LogFactory.getLog(SimpleDescPane.class);
    
    /**
     * 
     *
     */
    public SimpleDescPane(final String name, 
                          final Taskable task,
                          final String desc)
    {
        super(name, task);
        
        setBackground(Color.WHITE);
        
        JLabel label = new JLabel(desc, SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
       
    }
    
    /**
     * 
     *
     */
    public SimpleDescPane(final String name, 
                          final Taskable task,
                          final JPanel panel)
    {
        super(name, task);
        
        /*
        CellConstraints cc      = new CellConstraints();
        PanelBuilder builder    = new PanelBuilder(new FormLayout("F:P:G", "F:P:G"), this);
        builder.add(panel, cc.xy(1,1));
*/
        setBackground(Color.WHITE);
        add(panel, BorderLayout.CENTER);
       
    }
    
    
    
}
