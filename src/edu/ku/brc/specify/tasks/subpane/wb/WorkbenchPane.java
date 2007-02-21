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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.ui.CsvTableModel;
import edu.ku.brc.ui.JustifiedTableCellRenderer;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * PLaceholder for Workbench Pane
 
 * @code_status Alpha
 **
 * @author rods
 *
 */
public class WorkbenchPane extends BaseSubPane
{
    private static final Logger log = Logger.getLogger(WorkbenchPane.class);

    protected JTable     table;
    protected Workbench  workbench;
    protected FormPane   formPane;

    public WorkbenchPane(final String name,
                         final Taskable task,
                         final Workbench workbench)
    {
        super(name, task);

        progressBarPanel.setVisible(false);
    }

}
