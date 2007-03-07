package edu.ku.brc.ui.tmanfe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import edu.ku.brc.ui.UIHelper;

/***************************************************************************************************
 * 
 * This class implements a basic spreadsheet using a JTable. It also provides a main() method to be
 * run as an application.
 * 
 * @version 1.0 July-2002
 * @author Thierry Manf, Rod Spears
 * 
 **************************************************************************************************/
public class SpreadSheet extends JTable
{

    /**
     * Set this field to true and recompile to get debug traces
     */
    public static final boolean DEBUG = true;

    private JScrollPane         scrollPane;
    private CellMenu            popupMenu;

    //private int                 _editedModelRow;
    //private int                 _editedModelCol;


    // Cells selected.
    private Object[]            _selection;

    /**
     * Build SpreadSheet of numCol columns and numRow rows.
     * 
     * @param cells[numRow][numColumn] If not null, the cells to be used in the spreadsheet. It must be a two dimensional
     *  rectangular array. If null, the cells are automatically created.
     * @param numRow The number of rows
     * @param numCol The number of columns
     * 
     */
    public SpreadSheet(final TableModel model)
    {
        super(model);
        buildSpreadsheet(model);
    }

    protected void buildSpreadsheet(final TableModel model)
    {
        this.setShowGrid(true);


        int numRows = model.getRowCount();

        // Create the JScrollPane that includes the Table
        scrollPane = new JScrollPane(this);
        
        setModel(model);
        
        //setRowHeight(new JTextField().getPreferredSize().height+5);

        /*
         * Tune the selection mode
         */

        // Allows row and collumn selections to exit at the same time
        setCellSelectionEnabled(true);

        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent ev)
            {

                int selRow[] = getSelectedRows();
                int selCol[] = getSelectedColumns();

                _selection = new Object[selRow.length * selCol.length];

                int indice = 0;
                for (int r = 0; r < selRow.length; r++)
                {
                    for (int c = 0; c < selCol.length; c++)
                    {
                        //int rr = selRow[r];
                        //int cc = convertColumnIndexToModel(selCol[c]);
                        _selection[indice] = indice;//_model.cells[selRow[r]][convertColumnIndexToModel(selCol[c])];
                        indice++;
                    }
                }

            }
        });

        // Create a row-header to display row numbers.
        // This row-header is made of labels whose Borders,
        // Foregrounds, Backgrounds, and Fonts must be
        // the one used for the table column headers.
        // Also ensure that the row-header labels and the table
        // rows have the same height.
        TableColumn       aColumn   = getColumnModel().getColumn(0);
        TableCellRenderer aRenderer = getTableHeader().getDefaultRenderer();
        if (aRenderer == null)
        {
            aColumn = getColumnModel().getColumn(0);
            aRenderer = aColumn.getHeaderRenderer();
            if (aRenderer == null)
            {
                throw new RuntimeException("Can'r get default renderer!");
            }
        }
        Component aComponent = aRenderer.getTableCellRendererComponent(this, aColumn.getHeaderValue(), false, false, -1, 0);
        Font aFont = aComponent.getFont();
        Color aBackground = aComponent.getBackground();
        Color aForeground = aComponent.getForeground();

        Border      border  = (Border)UIManager.getDefaults().get("TableHeader.cellBorder");
        Insets      insets  = border.getBorderInsets(tableHeader);
        FontMetrics metrics = getFontMetrics(aComponent.getFont());
        rowHeight = insets.bottom + metrics.getHeight() + insets.top;

        
        /*
         * Creating a panel to be used as the row header.
         * 
         * Since I'm not using any LayoutManager, a call to setPreferredSize().
         */
        JPanel pnl = new JPanel((LayoutManager)null);
        Dimension dim = new Dimension(metrics.stringWidth("9999") + insets.right + insets.left, rowHeight * numRows);
        pnl.setPreferredSize(dim);
        
        class MyNumLabel extends JComponent
        {
            protected String rowNum;
            protected Font font;
            
            public MyNumLabel(int rowNum, final Font font)
            {
                this.rowNum = Integer.toString(rowNum);
                this.font = font;
            }

            /* (non-Javadoc)
             * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
             */
            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                g.setFont(font);
                //Insets insets = getInsets();
                Dimension size = this.getSize();
                FontMetrics fm = getFontMetrics(font);
                int width = fm.stringWidth(rowNum);
                int y = size.height - ((size.height - fm.getAscent()) / 2);// - insets.bottom;
                //System.out.println(fm.getAscent() + " " + Integer.toString((size.width - width) / 2) + " " + Integer.toString(y) + " " + size.height);
                g.drawString(rowNum, (size.width - width) / 2, y);
            }
            
        }

        // Adding the row header labels
        dim.height = rowHeight;
        boolean isMac = UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX;
        for (int ii = 0; ii < numRows; ii++)
        {
            if (isMac)
            {
                JLabel lbl = new JLabel(Integer.toString(ii + 1), SwingConstants.CENTER);
                lbl.setFont(aFont);
                lbl.setBackground(aBackground);
                lbl.setOpaque(true);
                lbl.setForeground(aForeground);
                lbl.setBorder(border);
                lbl.setBounds(0, ii * dim.height, dim.width, dim.height);
                
            } else
            {
                MyNumLabel lbl = new MyNumLabel(ii+1, aComponent.getFont());
                lbl.setBounds(0, ii * dim.height, dim.width, dim.height);
                pnl.add(lbl);
            }
        }

        JViewport vp = new JViewport();
        dim.height = rowHeight * numRows;
        vp.setViewSize(dim);
        vp.setView(pnl);
        scrollPane.setRowHeader(vp);

        resizeAndRepaint();
    }

    /**
     * Invoked when a cell edition starts. This method overrides and calls that of its super class.
     * 
     * @param int The row to be edited
     * @param int The column to be edited
     * @param EventObject The firing event
     * @return boolean false if for any reason the cell cannot be edited.
     */
    public boolean editCellAt(int row, int column, EventObject ev)
    {

        //if (_editedModelRow != -1)
        //    _model.setDisplayMode(_editedModelRow, _editedModelCol);

        //_editedModelRow = row;
        //_editedModelCol = convertColumnIndexToModel(column);

        //_model.setEditMode(row, convertColumnIndexToModel(column));
        return super.editCellAt(row, column, ev);

    }

    /**
     * Invoked by the cell editor when a cell edition stops. This method override and calls that of
     * its super class.
     * 
     */
    public void editingStopped(ChangeEvent ev)
    {
        //_model.setDisplayMode(_editedModelRow, _editedModelCol);
        super.editingStopped(ev);
    }

    /**
     * Invoked by the cell editor when a cell edition is cancelled. This method override and calls
     * that of its super class.
     * 
     */
    public void editingCanceled(ChangeEvent ev)
    {
        //_model.setDisplayMode(_editedModelRow, _editedModelCol);
        super.editingCanceled(ev);
    }

    public JScrollPane getScrollPane()
    {
        return scrollPane;
    }

    public void processMouseEvent(MouseEvent ev)
    {

        int type = ev.getID();
        int modifiers = ev.getModifiers();

        if ((type == MouseEvent.MOUSE_RELEASED) && (modifiers == InputEvent.BUTTON3_MASK))
        {

            if (_selection != null)
            {
                if (popupMenu == null)
                    popupMenu = new CellMenu(this);

                if (popupMenu.isVisible())
                    popupMenu.setVisible(false);
                else
                {
                    popupMenu.setTargetCells(_selection);
                    Point p = getLocationOnScreen();
                    popupMenu.setLocation(p.x + ev.getX() + 1, p.y + ev.getY() + 1);
                    popupMenu.setVisible(true);
                }
            }

        }
        super.processMouseEvent(ev);
    }


    public void setVisible(boolean flag)
    {
        scrollPane.setVisible(flag);
    }

    /*
     * This class is used to customize the cells rendering.
     */
    public class CellRenderer extends JLabel implements TableCellRenderer
    {

        private Border      _selectBorder;
        private EmptyBorder _emptyBorder;
        //private Dimension   _dim;

        public CellRenderer()
        {
            super();
            _emptyBorder  = new EmptyBorder(2, 2, 2, 2);
            _selectBorder = new LineBorder(Color.BLUE);
            //_selectBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            //_dim = new Dimension();
            //_dim.height = 22;
            //_dim.width = 100;
            //setSize(_dim);
        };

        /**
         *
         * Method defining the renderer to be used 
         * when drawing the cells.
         *
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            setBorder(isSelected ? _selectBorder : _emptyBorder);
            setText(value.toString());

            return this;

        }

    }

}
