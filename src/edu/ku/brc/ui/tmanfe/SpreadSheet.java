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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

import edu.ku.brc.ui.SearchableJXTable;
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
public class SpreadSheet  extends SearchableJXTable
{

    /**
     * Set this field to true and recompile to get debug traces
     */
    public static final boolean DEBUG = true;

    protected SpreadSheetModel   model;
    protected JScrollPane        scrollPane;
    protected JPopupMenu         popupMenu;
    
    protected boolean            useRowScrolling = false;

    // Members needed for the RowHeader    
    protected int                rowLabelWidth       = 0;     // the width of the each row's label
    protected JPanel             rowHeaderPanel;
    protected RHCellMouseAdapter rhCellMouseAdapter;
    protected Border             cellBorder          = null;
    protected Font               cellFont;
    
    // Cell Selection
    protected boolean            mouseDown           = false;
    private boolean              rowSelectionStarted = false;
    private int                  rowAnchor           = 0;
    

    /**
     * Constructor for Spreadsheet from model
     * @param model
     */
    public SpreadSheet(final SpreadSheetModel model)
    {
        super(model);
        
        this.model = model;
        
        buildSpreadsheet();
    }

    /**
     * @param model
     */
    protected void buildSpreadsheet()
    {
       
        this.setShowGrid(true);

        int numRows = model.getRowCount();
        
        scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Allows row and collumn selections to exit at the same time
        setCellSelectionEnabled(true);

        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Create a row-header to display row numbers.
        // This row-header is made of labels whose Borders,
        // Foregrounds, Backgrounds, and Fonts must be
        // the one used for the table column headers.
        // Also ensure that the row-header labels and the table
        // rows have the same height.
        TableColumn       column   = getColumnModel().getColumn(0);
        TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
        if (renderer == null)
        {
            column = getColumnModel().getColumn(0);
            renderer = column.getHeaderRenderer();
        }
        
        // Calculate Row Height
        Component   cellRenderComp = renderer.getTableCellRendererComponent(this, column.getHeaderValue(), false, false, -1, 0);
        cellFont                   = cellRenderComp.getFont();
        cellBorder                 = (Border)UIManager.getDefaults().get("TableHeader.cellBorder");
        Insets      insets         = cellBorder.getBorderInsets(tableHeader);
        FontMetrics metrics        = getFontMetrics(cellFont);

        rowHeight = insets.bottom + metrics.getHeight() + insets.top;

        /*
         * Create the Row Header Panel
         */
        rowHeaderPanel = new JPanel((LayoutManager)null);
        rowLabelWidth  = metrics.stringWidth("9999") + insets.right + insets.left;
        
        Dimension dim  = new Dimension(rowLabelWidth, rowHeight * numRows);
        rowHeaderPanel.setPreferredSize(dim); // need to call this when no layout manager is used.

        //final JTable table = this;
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                /*
                ListSelectionModel selModel = table.getSelectionModel();
                int anchor = selModel.getAnchorSelectionIndex();
                int lead   = selModel.getLeadSelectionIndex();
                
                System.out.println("anchor: "+anchor);
                System.out.println("lead:   "+lead);
                 */
            }
        });
        rhCellMouseAdapter = new RHCellMouseAdapter(this);
        
        // Adding the row header labels
        for (int ii = 0; ii < numRows; ii++)
        {
            addRow(ii, ii+1, false);
        }

        JViewport viewPort = new JViewport();
        dim.height = rowHeight * numRows;
        viewPort.setViewSize(dim);
        viewPort.setView(rowHeaderPanel);
        scrollPane.setRowHeader(viewPort);

        resizeAndRepaint();
    }
    
    /**
     * Appends a new Row onto the spreadsheet.
     * @param rowInx the last index
     * @param adjustPanelSize whether to resize the header panel
     */
    protected void addRow(final int rowInx, final int rowNum, final boolean adjustPanelSize)
    {
        RowHeaderLabel lbl = new RowHeaderLabel(rowNum, cellFont);
        lbl.setBounds(0, rowInx * rowHeight, rowLabelWidth, rowHeight);
        //System.out.println(rowNum+"  "+lbl.getBounds());
        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            lbl.setBorder(cellBorder);
        }
        lbl.addMouseListener(rhCellMouseAdapter);
        //lbl.addMouseMotionListener(rhCellMouseAdapter);
        rowHeaderPanel.add(lbl);
        
        if (adjustPanelSize)
        {
            Dimension dim = new Dimension(rowLabelWidth, rowHeight * (rowInx+1));
            rowHeaderPanel.setPreferredSize(dim);
            rowHeaderPanel.setSize(dim);
            resizeAndRepaint();
        }
    }
    
    /**
     * Appends a new row onto the Spreadsheet. 
     */
    public void addRow()
    {
        addRow(getModel().getRowCount()-1, getModel().getRowCount(), true);
        
    }
    
    /**
     * Must be called AFTER the model has been adjusted.
     * @param rowInx the row index that was removed
     */
    public void removeRow(final int rowInx)
    {
        int rowCount = getModel().getRowCount();
        
        Component comp = rowHeaderPanel.getComponent(rowCount);
        remove(comp);

        Dimension dim = new Dimension(rowLabelWidth, rowHeight * (rowCount));
        rowHeaderPanel.setPreferredSize(dim);
        rowHeaderPanel.setSize(dim);
        
        resizeAndRepaint();
        
        if (rowCount > -1)
        {
            if (rowInx >= rowCount)
            {
                setRowSelectionInterval(rowCount-1, rowCount-1);
            } else
            {
                setRowSelectionInterval(rowInx, rowInx);
            }
            setColumnSelectionInterval(0, model.getColumnCount()-1);
        }
    }

    /**
     * Scrolls to a specified row.
     * @param row the row to scroll to (zero-based)
     */
    public void scrollToRow(final int row)
    {
        Rectangle r = getCellRect(row, 0, true);
        scrollRectToVisible( r );
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
        return mouseDown ? false : super.editCellAt(row, column, ev);
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

    /**
     * @return the scroll pane.
     */
    public JScrollPane getScrollPane()
    {
        return scrollPane;
    }
    
    protected JPopupMenu createMenuForSelection(final Point pnt)
    {
        final int row = rowAtPoint(pnt);
        //int col = columnAtPoint(pnt);
        
        JPopupMenu pMenu = new JPopupMenu();
        
        if (getSelectedColumnCount() == 1)
        {
            final int[] rows = getSelectedRows();
            if (row == rows[0])
            {
                JMenuItem mi = pMenu.add(new JMenuItem("Fill Down"));
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        model.fill(getSelectedColumn(), row, rows);
                        popupMenu.setVisible(false);
                    }
                });
            } else if (row == rows[rows.length-1])
            {
                JMenuItem mi = pMenu.add(new JMenuItem("Fill Up"));
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        model.fill(getSelectedColumn(), row, rows);
                        popupMenu.setVisible(false);
                    }
                });
            }
        }
        
        JMenuItem mi = pMenu.add(new JMenuItem("Clear Cell(s)"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                model.clearCells(getSelectedRows(), getSelectedColumns());
                popupMenu.setVisible(false);
            }
        });
        
        mi = pMenu.add(new JMenuItem("Delete Row(s)"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                model.deleteRows(getSelectedRows());
                popupMenu.setVisible(false);
            }
        });
        return pMenu;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#processMouseEvent(java.awt.event.MouseEvent)
     */
    public void processMouseEvent(MouseEvent ev)
    {
        int type = ev.getID();
        int modifiers = ev.getModifiers();
        
        mouseDown = type == MouseEvent.MOUSE_PRESSED;

        if ((type == MouseEvent.MOUSE_RELEASED) && (modifiers == InputEvent.BUTTON3_MASK))
        {
            
            if (getSelectedRowCount() > 0)
            {
                if (popupMenu != null)
                {
                    popupMenu.setVisible(false);
                }

                popupMenu = createMenuForSelection(ev.getPoint());

                if (popupMenu.isVisible())
                {
                    popupMenu.setVisible(false);
                    
                } else
                {
                    //popupMenu.setTargetCells(_selection);
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
    
    /**
     * @return
     */
    public int getMaxUnitIncrement()
    {
        if (getModel() == null)
        {
            return 0;
        }
        int cols = getModel().getColumnCount();
        if (cols > 0)
        {
            cols--;
        }
        double unit = getPreferredSize().width / cols;
        return (int)unit;
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if (useRowScrolling)
        {
            if (orientation == SwingConstants.HORIZONTAL)
            {
                return visibleRect.width - getMaxUnitIncrement();
            }
            return visibleRect.height - getMaxUnitIncrement();
        }
        
        return super.getScrollableBlockIncrement(visibleRect, orientation, direction);

    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return useRowScrolling ? false : super.getScrollableTracksViewportHeight();
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return useRowScrolling ? false : super.getScrollableTracksViewportWidth();
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if (useRowScrolling)
        {
            // Get the current position.
            int currentPosition = 0;
            if (orientation == SwingConstants.HORIZONTAL)
            {
                currentPosition = visibleRect.x;
            }
            else 
            {
                currentPosition = visibleRect.y;
            }

            // Return the number of pixels between currentPosition
            // and the nearest tick mark in the indicated direction.
            if (direction < 0)
            {
                int newPosition = currentPosition - (currentPosition / getMaxUnitIncrement()) * getMaxUnitIncrement();
                return (newPosition == 0) ? getMaxUnitIncrement() : newPosition;
            } else
            {
                return ((currentPosition / getMaxUnitIncrement()) + 1) * getMaxUnitIncrement() - currentPosition;
            }
        }
        return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
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

    
    //------------------------------------------------------------------------------
    //-- Inner Classes
    //------------------------------------------------------------------------------
    class RowHeaderLabel extends JComponent
    {
        protected String rowNumStr;
        protected int    rowNum;
        protected Font   font;
   
        protected int    labelWidth  = Integer.MAX_VALUE;     
        protected int    labelheight = Integer.MAX_VALUE;     
        
        public RowHeaderLabel(int rowNum, final Font font)
        {
            this.rowNum    = rowNum;
            this.rowNumStr = Integer.toString(rowNum);
            this.font      = font;
        }

        public int getRowNum()
        {
            return rowNum;
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            g.setFont(font);
            
            if (labelWidth == Integer.MAX_VALUE)
            {
                FontMetrics fm = getFontMetrics(font);
                labelheight = fm.getAscent();
                labelWidth  = fm.stringWidth(rowNumStr);
            }
            
            Insets    ins  = getInsets();
            Dimension size = this.getSize();
            int y = size.height - ((size.height - labelheight) / 2) - ins.bottom;
            
            g.drawString(rowNumStr, (size.width - labelWidth) / 2, y);
        }
    }
    
    /**
     * MouseAdaptter for selecting rows by clicking and dragging on the Row Headers.
     */
    class RHCellMouseAdapter extends MouseAdapter
    {
        protected JTable table;
        protected Hashtable<Integer, Boolean> selectionHash = new Hashtable<Integer, Boolean>();
        protected int selAnchor = -1;
        protected int selLead   = -1;
        
        public RHCellMouseAdapter(final JTable table)
        {
            this.table = table;
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e) 
        { 
            RowHeaderLabel lbl = (RowHeaderLabel)e.getSource();
            int            row = lbl.getRowNum()-1;
            
            // ZZZ System.out.println("\nPressed: ["+row+"] A["+selAnchor+"]  L["+selLead+"]");
            
            if (e.isControlDown())
            {
                ListSelectionModel selModel = table.getSelectionModel();
                boolean wasSelected = false;
                for (int inx : table.getSelectedRows())
                {
                    if (row == inx)
                    {
                        wasSelected = true;
                        break;
                    }
                }
                 // ZZZ System.out.printlnm.out.println("wasSelected: "+wasSelected);
                if (wasSelected)
                {
                    selModel.removeSelectionInterval(row, row);
                } else
                {
                    selModel.addSelectionInterval(row, row);
                }
                
                
                
            } else if (e.isShiftDown())
            {
                ListSelectionModel selModel = table.getSelectionModel();
                int anchor = selModel.getAnchorSelectionIndex();
                int lead   = selModel.getLeadSelectionIndex();
                
                // ZZZ System.out.println("anchor: "+anchor);
                // ZZZ System.out.println("lead:   "+lead);

                if (lead == anchor || (row < lead && lead < anchor) || (row > lead && lead > anchor))
                {
                    selModel.addSelectionInterval(lead, row);
                    // ZZZ System.out.println("*Adding  ["+lead+"]["+row+"]");
                    
                } else
                {
                    int[] selectedRows = table.getSelectedRows();
                    selectionHash.clear();
                    for (int inx : selectedRows)
                    {
                        selectionHash.put(inx, true);
                        //System.out.println("Selected["+inx+"]");
                    }

                    for (int inx=anchor;inx<=lead;inx++)
                    {
                        if (selectionHash.get(inx) != null)
                        {
                            selModel.removeSelectionInterval(inx, inx);
                            //System.out.println("Removing["+inx+"]");
                        } else
                        {
                            selModel.addSelectionInterval(inx, inx);
                            //System.out.println("Adding  ["+inx+"]");
                        }
                    }
                    // ZZZ System.out.println("Adding  ["+row+"]["+(anchor-1)+"]");
                    selModel.addSelectionInterval(row, anchor-1);
                }
                
                //selModel.getAnchorSelectionIndex();
                //selModel.getLeadSelectionIndex();
                //System.out.println("isShiftDown Anchor: "+selModel.getAnchorSelectionIndex());
                //System.out.println("isShiftDown Lead  : "+selModel.getLeadSelectionIndex());

                
            } else
            {
                table.setRowSelectionInterval(row, row);
                table.setColumnSelectionInterval(0, model.getColumnCount()-1);
            }
            
            rowSelectionStarted = true;
            table.getSelectionModel().setValueIsAdjusting(true);
            rowAnchor = row;
            selAnchor = row;
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseReleased(MouseEvent e) 
        {
            rowSelectionStarted = false;
            table.getSelectionModel().setValueIsAdjusting(false);
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseEntered(MouseEvent e) 
        {
            if (rowSelectionStarted)
            {
                RowHeaderLabel lbl = (RowHeaderLabel)e.getSource();
                int row    = lbl.getRowNum()-1;
                rowSelectionStarted = true;
                table.setRowSelectionInterval(rowAnchor, row);
                table.setColumnSelectionInterval(0, model.getColumnCount()-1);
                
                selLead = row;

            }
        }

    }
}
