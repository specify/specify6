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
package edu.ku.brc.stats;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.specify.tasks.StatsTask;
import edu.ku.brc.ui.CurvedBorder;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.SortableJTable;
import edu.ku.brc.ui.SortableTableModel;

/**
 * Class to manage an entire group of StatItems where each StatItem gets its own data from a unique query.
 * This class represents a logical 'group' of statistics. The statistic items do not effect each other.
 * This is a simple class that is mostly used for layoing out the group in a vertial fashion.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatGroupTable extends JPanel
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



    /**
     * Constructor with the localized name of the Group
     * @param name name of the group (already been localized)
     */
    public StatGroupTable(final String name, final String[] columnNames)
    {
        this.name = name;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), BorderFactory.createEmptyBorder(15, 2, 2, 2)));
        setBackground(Color.WHITE);

        if (progressIcon == null)
        {
            progressIcon = IconManager.getIcon("Progress", IconManager.IconSize.Std16);
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

        if (progressIcon == null)
        {
            progressIcon = IconManager.getIcon("Progress", IconManager.IconSize.Std16);
        }
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        model = new StatGroupTableModel(this, columnNames);
        table = numRows > SCROLLPANE_THRESOLD ? (new SortableJTable(new SortableTableModel(model))) : (new JTable(model));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);

        table.addMouseMotionListener(new TableMouseMotion());

        table.addMouseListener(new LinkListener());

        table.getColumnModel().getColumn(0).setCellRenderer(new MyTableCellRenderer(JLabel.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(new MyTableCellRenderer(JLabel.RIGHT));


        //table.setRowSelectionAllowed(true);

        if (numRows > SCROLLPANE_THRESOLD)
        {
            scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            if (table instanceof SortableJTable)
            {
                ((SortableJTable)table).installColumnHeaderListeners();
            }
        }

        if (useSeparator)
        {
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            CellConstraints cc = new CellConstraints();

            builder.addSeparator(name, cc.xy(1,1));

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
     * Requests that all the data be reloaded (Not implemented yet)
     */
    public void reloadData()
    {

    }

    /**
     * Get the StatDataItem at a given row
     * @param tableModel the table model
     * @param rowIndex the row index in question
     * @return the StatDataItem at the rowIndex
     */
    protected StatDataItem getStatDataItem(final JTable table, final int rowIndex)
    {
        int rowInx = rowIndex;

        TableModel model;
        if (table instanceof SortableJTable)
        {
            SortableJTable     sTable        = (SortableJTable)table;
            SortableTableModel sortableModel = sTable.getSortableTableModel();

            rowInx = sortableModel.getDelegatedRow(rowInx);
            model  = sTable.getModel();

        } else
        {
            model = table.getModel();
        }

        return (StatDataItem)((StatGroupTableModel)model).getDataItem(rowInx);

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
    public void addDataItem(StatDataItem item)
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
                column.setPreferredWidth(width + margin); // <1.3: without margin
            else
                ; // ???

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
                    table.setCursor(sdi != null && StringUtils.isNotEmpty(sdi.getLink()) ? handCursor : defCursor);

                } else
                {
                    table.setCursor(defCursor);
                }
            } else
            {
                table.setCursor(defCursor);
            }
        }
    };

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
                    if (sdi != null && StringUtils.isNotEmpty(sdi.getLink()))
                    {
                        StatsTask statTask = (StatsTask)ContextMgr.getTaskByName(StatsTask.STATISTICS);
                        statTask.createStatPane(sdi.getLink());
                    }
                }

            }
        }
    };

    // This renderer extends a component. It is used each time a
    // cell must be displayed.
    class MyTableCellRenderer extends JLabel implements TableCellRenderer
    {
        @SuppressWarnings("unchecked")
        public MyTableCellRenderer(final int alignment)
        {
            super("", alignment);

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

        public void paint(Graphics g)
        {
            super.paint(g);
            /*
            Dimension size = getSize();

            Insets insets = new Insets(0,0,0,0);
            this.getInsets(insets);
            FontMetrics fm = getFontMetrics(getFont());
            g.setColor(getForeground());
            int strWidth = fm.stringWidth(getText());

            int x;
            int y = insets.top + fm.getAscent()+1;
            if (this.getHorizontalAlignment() == JLabel.LEFT)
            {
                x = insets.left;

            } else if (this.getHorizontalAlignment() == JLabel.RIGHT)
            {
                x = size.width-insets.left-strWidth;

            } else
            {
                x = (size.width - strWidth) / 2;
            }
            g.drawLine(x,y, x + strWidth, y);
*/
        }

        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
        {
            StatDataItem sdi = getStatDataItem(table, rowIndex);
            if (sdi != null)
            {

                /*if (sdi.isUseProgress())
                {
                    setIcon(progressIcon);
                    setText("");

                } else
                {*/
                    setIcon(null);

                    String desc = sdi.getDescription();
                    Object val  = sdi.getValue();

                    setForeground(StringUtils.isNotEmpty(sdi.getLink()) ? Color.BLUE : Color.BLACK);

                    String valStr = val != null ? val.toString() : "";
                    // Configure the component with the specified value
                    setText(vColIndex == 0 ? desc : valStr);

                    // Set tool tip if desired
                    setToolTipText(desc + " " + valStr);
                //}

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
