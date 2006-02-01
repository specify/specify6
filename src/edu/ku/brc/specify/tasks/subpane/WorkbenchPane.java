/* Filename:    $RCSfile: WorkbenchPane,v $
 * Author:      $Author: megkumin $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/02/01  $
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
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.ui.db.WorkbenchTableModel;

public class WorkbenchPane  extends BaseSubPane {
    private static Log log = LogFactory.getLog(WorkbenchPane.class);

    private JTable                table;

	public WorkbenchPane(final String name, 
            final Taskable task) {
		super(name, task);
        setPreferredSize(new Dimension(600,600));

        
        table = new JTable();
        table.setModel(new WorkbenchTableModel());
		// TODO Auto-generated constructor stub
        
        FormLayout      formLayout = new FormLayout("p,2dlu,100dlu:g,2dlu,p", "center:p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();   
        
        add(new JScrollPane(table), BorderLayout.CENTER);   
        //enableUI(true);
	}


}
