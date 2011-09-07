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
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import jexifviewer.JSettings;
import jexifviewer.Main;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import shared.files.JPathHelper;
import edu.ku.brc.sgr.Match;
import edu.ku.brc.sgr.MatchResults;
import edu.ku.brc.sgr.SGRRecord;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
@SuppressWarnings("serial")
public class SGRResultsDisplay extends JPanel implements TableColumnModelListener
{
    private static final int    ROW_HEIGHT = 22;

    public SGRResultsDisplay(int width, MatchResults results)
    {
        if (Main.m_settings == null)
        {
            Main.m_settings = new JSettings(true);
            Main.m_settings.load(
                    JPathHelper.addSeparator(
                            System.getProperty("user.home")) + JSettings.SETTINGS_FILE, "1.8");
            
            Main.m_sysLocale = Locale.getDefault();
            if ( Main.m_settings.getLocale().length() > 0)
            {
                Locale.setDefault(new Locale( Main.m_settings.getLocale()));
            }
        }
        
        SGRColumnOrdering columns = SGRColumnOrdering.getInstance();
        String [] fields = columns.getFields();
        
        DefaultTableModel resultsTableModel = new DefaultTableModel(columns.getHeadings(), 0);
        
        final List<List<Color>>rowColors = new LinkedList<List<Color>>();
        
        for (Match result : results)
        {
            SGRRecord match = result.match;
            float score = result.score;
            float maxScore = 22.0f; //results.maxScore;
            
            List<Color> cellColors = new LinkedList<Color>();

            List<String> data = new LinkedList<String>();            
            for (String field : fields)
            {
                if (field.equals("id"))
                {
                    data.add(match.id);
                    cellColors.add(SGRColors.colorForScore(score, maxScore));                    
                }
                else if (field.equals("score"))
                {
                    data.add(((Float)score).toString());
                    cellColors.add(SGRColors.colorForScore(score, maxScore));
                }
                else
                {
                    data.add(StringUtils.join(match.getFieldValues(field).toArray(), ';'));
                
                    Float fieldContribution = result.fieldScoreContributions().get(field);
                    Color color = SGRColors.colorForScore(score, maxScore, fieldContribution);
                    cellColors.add(color);
                }
            }
            resultsTableModel.addRow(data.toArray());
            rowColors.add(cellColors);
        }
        
        JTable table = createTable(resultsTableModel, rowColors);
        Dimension preferredSize = table.getPreferredSize();
        preferredSize.width = Math.min((int)(0.9 * width), preferredSize.width);
        table.setPreferredScrollableViewportSize(preferredSize);
        table.getColumnModel().addColumnModelListener(this);
        
        add(new JScrollPane(table));
    }
    

    private JTable createTable(DefaultTableModel resultsTableModel, final List<List<Color>> rowColors)
    {
        JTable table = new JTable(resultsTableModel)
        {
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex)
            {
                Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
                
                final Color bgColor = isCellSelected(rowIndex, colIndex) ?
                        getSelectionBackground()
                        :
                        rowColors.get(rowIndex).get(colIndex); 

                c.setBackground(bgColor);
                return c;
            }
        };
        
        
        DefaultTableCellRenderer tcr = getTableCellRenderer();//model.getRowInfoList());
        for (int i=0;i<resultsTableModel.getColumnCount();i++)
        {
            if (resultsTableModel.getColumnClass(i) != Boolean.class)
            {
                table.setDefaultRenderer(resultsTableModel.getColumnClass(i), tcr);
            }
        }
        
        table.setCellSelectionEnabled(true);
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        UIHelper.makeTableHeadersCentered(table, false);
        table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
        table.setRowHeight(ROW_HEIGHT);

        autoResizeColWidth(table, resultsTableModel);
        return table;
    }

    /**
     * @return
     */
    private DefaultTableCellRenderer getTableCellRenderer()
    {
        return new DefaultTableCellRenderer() {
            @SuppressWarnings("unchecked")
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column)
            {
//                DataObjTableModelRowInfo rowInfo = rowInfoList.get(row);
                
                boolean doCenter = false;
                Object  val      = value;
                if (value instanceof Pair<?, ?>)
                {
                    Pair<Object, Object> pair = (Pair<Object, Object>)value;
                    val      = pair.first;
                    doCenter = true;
                }
                
                JLabel lbl = 
                    (JLabel)super.getTableCellRendererComponent(
                            table, val, isSelected, hasFocus, row, column);
//                if (rowInfo.isMainRecord())
//                {
//                    lbl.setForeground(Color.WHITE);
//                    lbl.setBackground(Color.BLUE.brighter().brighter());
//                    
//                } else
//                {
//                    lbl.setForeground(model.isSame(column) ? sameColor : Color.BLACK);
//                    lbl.setBackground(Color.WHITE);
//                }
                lbl.setHorizontalTextPosition(doCenter ? SwingConstants.CENTER : SwingConstants.LEFT);
                
                return lbl;
            }
        };
    }

    /**
     * From http://www.pikopong.com/blog/2008/08/13/auto-resize-jtable-column-width/
     * 
     * @param table
     * @param model
     * @return
     */
    private JTable autoResizeColWidth(JTable table, DefaultTableModel model) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(model);

        int margin = 5;

        for (int i = 0; i < table.getColumnCount(); i++) {
            int                     vColIndex = i;
            DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn             col       = colModel.getColumn(vColIndex);
            int                     width     = 0;

            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();

            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }

            Component comp = 
                renderer.getTableCellRendererComponent(
                        table, col.getHeaderValue(), false, false, 0, 0);

            width = comp.getPreferredSize().width;

            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, vColIndex);
                comp = renderer.getTableCellRendererComponent(
                        table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }

            // Add margin
            width += 2 * margin;

            // Set the width
            col.setPreferredWidth(width);
        }

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).
            setHorizontalAlignment(SwingConstants.LEFT);

        // table.setAutoCreateRowSorter(true);
        //table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    @Override
    public void columnMoved(TableColumnModelEvent e)
    {
        int from = e.getFromIndex();
        int to = e.getToIndex();
        if (from == to) return;
        System.out.println(e.getSource() + " " + from + " " + to);
        SGRColumnOrdering.getInstance().moveColumn(from, to);
    }


    @Override public void columnAdded(TableColumnModelEvent arg0) {}
    @Override public void columnMarginChanged(ChangeEvent arg0) {}
    @Override public void columnRemoved(TableColumnModelEvent arg0) {}
    @Override public void columnSelectionChanged(ListSelectionEvent arg0) {}   
}
