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
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.ui.CsvTableModel;
import edu.ku.brc.specify.ui.JustifiedTableCellRenderer;

/**
 * PLaceholder for Workbench Pane
 *
 * @author rods
 *
 */
public class WorkbenchPane extends BaseSubPane
{
    private static Log log = LogFactory.getLog(WorkbenchPane.class);

    private JTable     table;

    public WorkbenchPane(final String name,
                         final Taskable task,
                         final File csvFile)
    {
        super(name, task);
        setPreferredSize(new Dimension(600, 600));

        table = new JTable();
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);
        // table.setModel(new WorkbenchTableModel());

        // START OF CODE TO REMOVE AFTER DEMO
        // it was just for a quick demo
        log.info("This code section should be removed after the demo");
        if (csvFile != null)
        {
            try
            {
                table.setModel(new CsvTableModel(csvFile));
                table.getColumnModel().getColumn(0).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.LEFT));
                table.getColumnModel().getColumn(1).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.CENTER));
                table.getColumnModel().getColumn(2).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.CENTER));
                table.getColumnModel().getColumn(3).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.CENTER));
                table.getColumnModel().getColumn(4).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.RIGHT));
                table.getColumnModel().getColumn(5).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.RIGHT));
                table.getColumnModel().getColumn(6).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.CENTER));
                table.getColumnModel().getColumn(7).setCellRenderer(
                        new JustifiedTableCellRenderer(SwingConstants.CENTER));
            } catch (Exception e)
            {
                log.error(e);
            }
        }
        // END OF CODE TO REMOVE AFTER DEMO

        // TODO Auto-generated constructor stub

        //PanelBuilder builder = new PanelBuilder(new FormLayout("p,2dlu,100dlu:g,2dlu,p", "center:p:g"));
       // CellConstraints cc = new CellConstraints();

        add(new JScrollPane(table), BorderLayout.CENTER);
        //enableUI(true);
    }

}
