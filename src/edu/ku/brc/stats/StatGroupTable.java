/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.stats;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CurvedBorder;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JTiledPanel;
import edu.ku.brc.ui.SortableJTable;
import edu.ku.brc.ui.SortableTableModel;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;

/**
 * Class to manage an entire group of StatItems where each StatItem gets its own data from a unique query.
 * This class represents a logical 'group' of statistics. The statistic items do not effect each other.
 * This is a simple class that is mostly used for layoing out the group in a vertial fashion.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatGroupTable extends JTiledPanel
{
    protected static final int    SCROLLPANE_THRESOLD = 10;
    protected static final Cursor handCursor   = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor    = new Cursor(Cursor.DEFAULT_CURSOR);
    protected static ImageIcon    progressIcon = null;

    protected static int          visibleRows  = 8;      // XXX This needs to be moved to the "box" element of the XML

    protected String              name;
    protected PanelBuilder        builder      = new PanelBuilder(new FormLayout("p:g", "p,p:g"));
    protected JTable              table;
    protected StatGroupTableModel model;
    protected JScrollPane         scrollPane   = null;
    protected boolean             useSeparator = true;
    
    protected int                 colId        = -1;
    protected CommandAction       cmdAction    = null;
    protected SkinItem            skinItem;

    /**
     * Constructor with the localized name of the Group
     * @param name name of the group (already been localized)
     */
    public StatGroupTable(final String name, @SuppressWarnings("unused")final String[] columnNames)
    {
        this.name     = name;
        this.skinItem = SkinsMgr.getSkinItem("StatGroup");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), BorderFactory.createEmptyBorder(15, 2, 2, 2)));
        //setBackground(Color.WHITE);
        
        setOpaque(SkinsMgr.shouldBeOpaque(skinItem));
        
        if (progressIcon == null)
        {
            progressIcon = IconManager.getIcon("Progress", IconManager.IconSize.Std16);
        }
        
        if (this.skinItem != null)
        {
            this.skinItem.setupPanel(this);
        } else
        {
            setOpaque(true);
        }
    }

    /**
     * Constructor with the localized name of the Group
     * @param name name of the group (already been localized)
     * @param useSeparator use non-border separator titles
     */
    public StatGroupTable(final String name, final String[] columnNames, final boolean useSeparator, final int numRows)
    {
        this.name         = name;
        this.useSeparator = useSeparator;
        this.skinItem     = SkinsMgr.getSkinItem("StatGroup");

        if (progressIcon == null)
        {
            progressIcon = IconManager.getIcon("Progress", IconManager.IconSize.Std16);
        }
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        model = new StatGroupTableModel(this, columnNames);
        //table = numRows > SCROLLPANE_THRESOLD ? (new SortableJTable(new SortableTableModel(model))) : (new JTable(model));
        if (numRows > SCROLLPANE_THRESOLD)
        {
            table = new SortableJTable(new SortableTableModel(model))
            {
                protected void configureEnclosingScrollPane() {
                    Container p = getParent();
                    if (p instanceof JViewport) {
                        Container gp = p.getParent();
                        if (gp instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane)gp;
                            // Make certain we are the viewPort's view and not, for
                            // example, the rowHeaderView of the scrollPane -
                            // an implementor of fixed columns might do this.
                            JViewport viewport = scrollPane.getViewport();
                            if (viewport == null || viewport.getView() != this) {
                                return;
                            }
//                            scrollPane.setColumnHeaderView(getTableHeader());
                            //scrollPane.getViewport().setBackingStoreEnabled(true);
                            scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
                        }
                    }
                }
            };
        } else
        {
            table = new JTable(model)
            {
                protected void configureEnclosingScrollPane() {
                    Container p = getParent();
                    if (p instanceof JViewport) {
                        Container gp = p.getParent();
                        if (gp instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane)gp;
                            // Make certain we are the viewPort's view and not, for
                            // example, the rowHeaderView of the scrollPane -
                            // an implementor of fixed columns might do this.
                            JViewport viewport = scrollPane.getViewport();
                            if (viewport == null || viewport.getView() != this) {
                                return;
                            }
//                            scrollPane.setColumnHeaderView(getTableHeader());
                            //scrollPane.getViewport().setBackingStoreEnabled(true);
                            scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
                        }
                    }
                }
            };
        }
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        
        if (SkinsMgr.shouldBeOpaque(skinItem))
        {
            table.setOpaque(false);
            setOpaque(false);
        } else
        {
            table.setOpaque(true);
            setOpaque(true);
        }

        table.addMouseMotionListener(new TableMouseMotion());
        table.addMouseListener(new LinkListener());

        if (table.getColumnModel().getColumnCount() == 1)
        {
            table.getColumnModel().getColumn(0).setCellRenderer(new StatGroupTableCellRenderer(SwingConstants.CENTER, 1));
            
        } else
        {
            table.getColumnModel().getColumn(0).setCellRenderer(new StatGroupTableCellRenderer(SwingConstants.LEFT, 2));
            table.getColumnModel().getColumn(1).setCellRenderer(new StatGroupTableCellRenderer(SwingConstants.RIGHT, 2));
        }

        //table.setRowSelectionAllowed(true);

        if (numRows > SCROLLPANE_THRESOLD)
        {
            scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            if (table instanceof SortableJTable)
            {
                ((SortableJTable)table).installColumnHeaderListeners();
            }
            
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            //scrollPane.getViewport().setBorder(BorderFactory.createEmptyBorder());
        }

        if (useSeparator)
        {
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            CellConstraints cc = new CellConstraints();

            if (StringUtils.isNotEmpty(name))
            {
                builder.addSeparator(name, cc.xy(1,1));
            }

            builder.add(scrollPane != null ? scrollPane : table, cc.xy(1,2));
            builder.getPanel().setOpaque(false);
            add(builder.getPanel());


        } else
        {
            setBorder(BorderFactory.createEmptyBorder(15, 2, 2, 2));
            setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));

            add(scrollPane != null ? scrollPane : table, BorderLayout.CENTER);
         }
    }
    

    /**
     * Sets info needed to send commands
     * @param commandAction the command to be cloned and sent
     * @param colId the column of the id which is used to build the link
     */
    public void setCommandAction(final CommandAction commandAction, 
                                 final int           colId)
    {
        this.cmdAction = commandAction;
        this.colId     = colId;
    }
    
    /**
     * Clones and sets up the CommandAction
     * @param colIdObj the data from the column marked as colid
     * @return null or a new cloned CommandAction
     */
    protected CommandAction createCommandAction(final Object colIdObj)
    {
        CommandAction commandAction = null;
        if (cmdAction != null)
        {
            try
            {
                commandAction = (CommandAction)cmdAction.clone();
                
                if (colIdObj instanceof Integer && ((Integer)colIdObj).intValue() != -1)
                {
                    commandAction.setProperty("colid", colIdObj);
                }
                
                Object cmdData = commandAction.getData();
                if (cmdData instanceof Class<?>)
                {
                    Class<?>    clazz   = (Class<?>)commandAction.getData();
                    DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(clazz.getName());
                    if (tblInfo != null)
                    {
                        RecordSetIFace rs = RecordSetFactory.getInstance().createRecordSet();
                        rs.setName("");
                        rs.setDbTableId(tblInfo.getTableId());
                        rs.addItem((Integer)colIdObj);
                        commandAction.setData(rs);
                        
                    } else
                    {
                        commandAction = null;
                    }
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatGroupTable.class, ex);
                ex.printStackTrace();
            }
        }
        return commandAction;
    }

    /**
     * Requests that all the data be reloaded (Not implemented yet)
     */
    public void reloadData()
    {

    }

    /**
     * Get the StatDataItem at a given row.
     * @param tableArg the table 
     * @param rowIndex the row index in question
     * @return the StatDataItem at the rowIndex
     */
    protected static StatDataItem getStatDataItem(final JTable tableArg, final int rowIndex)
    {
        int rowInx = rowIndex;

        TableModel tblModel;
        if (tableArg instanceof SortableJTable)
        {
            SortableJTable     sTable        = (SortableJTable)tableArg;
            SortableTableModel sortableModel = sTable.getSortableTableModel();

            rowInx = sortableModel.getDelegatedRow(rowInx);
            tblModel  = sTable.getModel();

        } else
        {
            tblModel = tableArg.getModel();
        }

        return ((StatGroupTableModel)tblModel).getDataItem(rowInx);

    }

    /**
     * Enables Threaded objects to ask for a relayout of the UI
     *
     */
    public synchronized void relayout()
    {
        validate();
        doLayout();
        repaint();
    }

    /**
     * Adds StatItem to group
     * @param item the item to be added
     */
    public void addDataItem(final StatDataItem item)
    {
        model.addDataItem(item);
    }

    /**
     * Overrides paint to draw in name at top with separator AND the Label
     */
    public void paint(Graphics g)
    {
        super.paint(g);

        if (!useSeparator)
        {
            Dimension dim = getSize();

            FontMetrics fm = g.getFontMetrics();
            int strW = fm.stringWidth(name);

            int x = (dim.width - strW) / 2;
            Insets ins = getBorder().getBorderInsets(this);
            int y = 2 + fm.getAscent();

            int lineW = dim.width - ins.left - ins.right;
            g.setColor(Color.BLUE.darker());
            g.drawString(name, x, y);
            x = ins.left;
            y += fm.getDescent() + fm.getLeading();

            g.setColor(Color.LIGHT_GRAY.brighter());
            g.drawLine(x, y,   x+lineW, y);
            y++;
            x++;
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(x, y,   x+lineW, y);
        }
    }



    /**
     * Notifies the TableModel that the underlying data has changed
     */
    public void fireNewData()
    {
        calcColumnWidths(table);
    }

    /**
     * Calculates and sets the each column to it preferred size
     * @param table the table to fix ups
     */
    public static void calcColumnWidths(JTable table)
    {
        JTableHeader header = table.getTableHeader();

        TableCellRenderer defaultHeaderRenderer = null;

        if (header != null)
        {
            defaultHeaderRenderer = header.getDefaultRenderer();
        }

        TableColumnModel columns = table.getColumnModel();
        TableModel data = table.getModel();

        int margin = columns.getColumnMargin(); // only JDK1.3

        int rowCount = data.getRowCount();

        int totalWidth = 0;

        for (int i = columns.getColumnCount() - 1; i >= 0; --i)
        {
            TableColumn column = columns.getColumn(i);

            int columnIndex = column.getModelIndex();

            int width = -1;

            TableCellRenderer h = column.getHeaderRenderer();

            if (h == null)
                h = defaultHeaderRenderer;

            if (h != null) // Not explicitly impossible
            {
                Component c = h.getTableCellRendererComponent
                       (table, column.getHeaderValue(),
                        false, false, -1, i);

                width = c.getPreferredSize().width;
            }

            for (int row = rowCount - 1; row >= 0; --row)
            {
                TableCellRenderer r = table.getCellRenderer(row, i);

                Component c = r.getTableCellRendererComponent
                   (table,
                    data.getValueAt(row, columnIndex),
                    false, false, row, i);

                    width = Math.max(width, c.getPreferredSize().width+10); // adding an arbitray 10 pixels to make it look nicer
            }

            if (width >= 0)
            {
                column.setPreferredWidth(width + margin); // <1.3: without margin
            }
            

            totalWidth += column.getPreferredWidth();
        }

        // If you like; This does not make sense for two many columns!
        Dimension size = table.getPreferredScrollableViewportSize();
        //if (totalWidth > size.width)
        {
            size.height = Math.min(size.height, table.getRowHeight()*visibleRows);
            size.width  = totalWidth;
            table.setPreferredScrollableViewportSize(size);
        }

    }

    //-------------------------------------------------------------------------------------------
    //-- Inner Classes
    //-------------------------------------------------------------------------------------------

    class TableMouseMotion extends MouseMotionAdapter
    {
        public void mouseMoved(MouseEvent e)
        {
            int colInx = table.columnAtPoint(e.getPoint());
            if (colInx > -1)
            {
                int rowInx = table.rowAtPoint(e.getPoint());
                if (rowInx > -1)
                {
                    StatDataItem sdi = getStatDataItem(table, rowInx);
                    table.setCursor(sdi != null && sdi.shouldShowLinkCursor() ? handCursor : defCursor);

                } else
                {
                    table.setCursor(defCursor);
                }
            } else
            {
                table.setCursor(defCursor);
            }
        }
    }

    //-------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------
    class LinkListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            int colInx = table.columnAtPoint(e.getPoint());
            if (colInx > -1)
            {
                int rowInx = table.rowAtPoint(e.getPoint());
                if (rowInx > -1)
                {
                    StatDataItem sdi = getStatDataItem(table, rowInx);
                    if (sdi != null && sdi.getCmdAction() != null)
                    {
                        CommandDispatcher.dispatch(sdi.getCmdAction());
                    }
                }

            }
        }
    }

    //-------------------------------------------------------------------------
    // This renderer extends a component. It is used each time a
    // cell must be displayed.
    //-------------------------------------------------------------------------
    class StatGroupTableCellRenderer extends JLabel implements TableCellRenderer
    {
        protected  int numCols;
        
        @SuppressWarnings("unchecked")
        public StatGroupTableCellRenderer(final int alignment, final int numCols)
        {
            super("", alignment);
            
            this.numCols = numCols;

            /*
            //Map map = getFont().getAttributes();
            Map map = (new Font("Arial", Font.PLAIN, 12)).getAttributes();
            map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
            Font newFont = new Font(map);
            setFont(newFont);
            setForeground(Color.blue);
            */
        }

        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent(JTable renderTable, 
                                                       Object value,
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int rowIndex, 
                                                       int vColIndex)
        {
            StatDataItem sdi = getStatDataItem(renderTable, rowIndex);
            if (sdi != null)
            {
                setIcon(null);

                setForeground(sdi.shouldShowLinkCursor() ? Color.BLUE : Color.BLACK);

                if (numCols == 1)
                {
                    Object val  = sdi.getValue();
                    String valStr = val != null ? val.toString() : "";
                    
                    // Configure the component with the specified value
                    setText(valStr);

                    // Set tool tip if desired
                    setToolTipText(valStr);    
                    
                } else
                {
                    String desc = sdi.getDescription();
                    Object val  = sdi.getValue();
                    String valStr = val != null ? val.toString() : "";
                    
                    // Configure the component with the specified value
                    setText(vColIndex == 0 ? desc : valStr);

                    // Set tool tip if desired
                    setToolTipText(desc + " " + valStr);                }


            } else
            {
                setText(getResourceString("NoneAvail"));
            }
            // Since the renderer is a component, return itself
            return this;
        }

        // The following methods override the defaults for performance reasons
        public void validate() {}
        public void revalidate() {}
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }

}
