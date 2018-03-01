package edu.ku.brc.specify.tasks;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.dbsupport.cleanuptools.LocalityDuplicateRemover;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.tmanfe.RHCellMouseAdapter;
import edu.ku.brc.ui.tmanfe.RHCellOwner;
import edu.ku.brc.ui.tmanfe.RowHeaderLabel;
import edu.ku.brc.util.Pair;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

public class DuplicateSetDisplay extends JPanel implements RHCellOwner {
    private static final Logger log = Logger.getLogger(DuplicateSetDisplay.class);
    final private DBTableInfo tblInfo;
    final private List<Object[]> dups;
    final private AtomicReferenceArray<List<Object>> sets;
    protected final static int STATUS_COL_IDX = 0;
    protected final static int DESC_COL_IDX = 1;

    protected JXTable tbl;
    protected JPanel btmPnl;
    protected JLabel statusLbl;
    protected JButton leBtn;

    protected boolean rowSelectionStarted = false;

    public class DupCellRenderer extends DefaultTableCellRenderer {
        private String doneStatus = getResourceString("DuplicateSetDisplay.RowDeDuped");
        private String mergingStatus = getResourceString("DuplicateSetDisplay.RowMerging");
        private String errorStatus = getResourceString("DuplicateSetDisplay.RowError");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
            Object val = column == STATUS_COL_IDX ? obj : table.getValueAt(row, STATUS_COL_IDX);
            if (doneStatus.equals(val))
                cell.setBackground(Color.GREEN);
            else if (mergingStatus.equals(val))
                cell.setBackground(Color.YELLOW);
            else if (errorStatus.equals(val))
                cell.setBackground(Color.RED);

            return cell;
        }
    }

    public DuplicateSetDisplay(final DBTableInfo tblInfo, final List<Object[]> dups) {
        super(new BorderLayout());
        this.tblInfo = tblInfo;
        this.dups = dups;
        sets = new AtomicReferenceArray<>(dups.size());
    }

    protected void getFormats() {
        SwingWorker formatter = new SwingWorker() {

            DataProviderSessionIFace session;

            @Override
            public Object doInBackground() {
                session = DataProviderFactory.getInstance().createSession();
                int row = 0;
                for (Object[] set : dups) {
                    DataModelObjBase obj = null;
                    Integer id = null;
                    try {
                        String sql = LocalityDuplicateRemover.getSqlForIdsInSetOfDups(tblInfo.getName(), set);
                        sets.set(row, BasicSQLUtils.querySingleCol(sql));
                        id = (Integer) sets.get(row).get(0);
                        obj = id != null ? (DataModelObjBase) session.get(tblInfo.getClassObj(), id) : null;
                    } catch (Exception ex) {
                        log.error(ex);
                    }
                    final int r = row++;
                    if (obj != null) {
                        obj.forceLoad();
                    }
                    String format = obj != null ?
                            DataObjFieldFormatMgr.getInstance().format(obj, tblInfo.getDataObjFormatter()) : null;
                    final String text = format != null ? format : tblInfo.getTitle() + ": " + id;
                    //System.out.println(text);
                    SwingUtilities.invokeLater(() -> tbl.getModel().setValueAt(text, r, DESC_COL_IDX));
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
                session.close();
                resizeColumnWidth(tbl, 0);
                resizeColumnWidth(tbl, 1);
                resizeColumnWidth(tbl, 2);
            }
        };
        formatter.execute();
    }

    private void resizeColumnWidth(JTable table, int col) {
        int width = 50; // Min width
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, col);
            Component comp = table.prepareRenderer(renderer, row, col);
            width = Math.max(comp.getPreferredSize().width + 1, width);
        }
        if (width > 500) {
            width = 500;
        }
        table.getColumnModel().getColumn(col).setPreferredWidth(width);
    }


    public void processProgress(List<Integer> chunks) {
        if (chunks.size() == 2) {
            if (!btmPnl.isVisible()) {
                btmPnl.setVisible(true);
                showTblCol(STATUS_COL_IDX);
            }
            if (chunks.get(0) != -1) {
                System.out.println("finished " + (chunks.get(0) + 1));
                tbl.setValueAt(getResourceString("DuplicateSetDisplay.RowDeDuped"), chunks.get(0), STATUS_COL_IDX);
            }
            if (chunks.get(1) != -1) {
                System.out.println("processing " + (chunks.get(1) + 1));
                statusLbl.setText(String.format(getResourceString("DuplicateSetDisplay.ProcessingRow"), chunks.get(1) + 1));
                tbl.setValueAt(getResourceString("DuplicateSetDisplay.RowMerging"), chunks.get(1), STATUS_COL_IDX);
                //scrollToVisible(tbl, chunks.get(1), STATUS_COL_IDX);
                tbl.scrollCellToVisible(chunks.get(1) + 1, STATUS_COL_IDX);
            }
        }

    }

    private int getDeDupedRecCnt(List<Integer> deDuped) {
        int result = 0;
        int idx = dups.get(0).length - 1;
        for (Integer r : deDuped) {
            result += (Long)dups.get(r)[idx] - 1;
        }
        return result;
    }

    public void deDupeDone(Pair<List<Integer>, List<Integer>> results) {
        List<Integer> fails = results.getFirst();
        List<Integer> done = results.getSecond();
        if (fails.size() > 0) {
            leBtn.setVisible(true);
        } else {
           statusLbl.setText(String.format(getResourceString("DuplicateSetDisplay.DupRecordsDumped"), getDeDupedRecCnt(done)));
        }
    }

    private void hideTblCol(int c) {
        TableColumn col = tbl.getColumnModel().getColumn(c);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setWidth(0);
        col.setPreferredWidth(0);
        tbl.doLayout();
    }

    private void showTblCol(int c) {
        //resizeColumnWidth(tbl, c);
        TableColumn col = tbl.getColumnModel().getColumn(c);
        col.setMinWidth(95);
        col.setMaxWidth(95);
        col.setWidth(95);
        col.setPreferredWidth(95);
        tbl.doLayout();
    }

    protected void createTbl() {
        tbl = new JXTable();
        //tbl.setCellSelectionEnabled(false);
        Vector<String> heads = new Vector<>(2);
        heads.add(getResourceString("DuplicateSetDisplay.DupStatus"));
        heads.add(getResourceString("DuplicateSetDisplay.RecordDescription"));
        heads.add(getResourceString("DuplicateSetDisplay.DupCount"));
        final Vector<Vector<Object>> cells = new Vector<>();
        for (Object[] dup : dups) {
            Vector<Object> row = new Vector<>();
            row.add("");
            row.add(getResourceString("DuplicateSetDisplay.LOADING"));
            row.add(dup[dup.length - 1]);
            cells.add(row);
        }
        tbl.setModel(new DefaultTableModel(cells, heads));
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl.setShowGrid(true);
        hideTblCol(STATUS_COL_IDX);
        JButton cornerBtn = UIHelper.createIconBtn("Blank", IconManager.IconSize.Std16,
                "SelectAll",
                new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        tbl.selectAll();
                    }
                });
        cornerBtn.setEnabled(true);
        JScrollPane sp = new JScrollPane(tbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, cornerBtn);
        /*
         * Create the Row Header Panel
         */
        JPanel rowHeaderPanel = new JPanel(null);

        Font cellFont;
        if (tbl.getColumnModel().getColumnCount() > 0) {
            TableColumn column   = tbl.getColumnModel().getColumn(0);
            TableCellRenderer renderer = tbl.getTableHeader().getDefaultRenderer();
            if (renderer == null) {
                renderer = column.getHeaderRenderer();
            }
            Component   cellRenderComp = renderer.getTableCellRendererComponent(tbl, column.getHeaderValue(), false, false, -1, 0);
            cellFont                   = cellRenderComp.getFont();
        } else {
            cellFont = (new JLabel()).getFont();
        }

        // Calculate Row Height
        Border cellBorder          = (Border)UIManager.getDefaults().get("TableHeader.cellBorder");
        Insets      insets  = cellBorder.getBorderInsets(tbl.getTableHeader());
        FontMetrics metrics = getFontMetrics(cellFont);

        int rowHeight = tbl.getRowHeight();
        int rowLabelWidth = metrics.stringWidth("9999") + insets.right + insets.left;

        Dimension dim  = new Dimension(rowLabelWidth, rowHeight * cells.size());
        rowHeaderPanel.setPreferredSize(dim); // need to call this when no layout manager is used.

        RHCellMouseAdapter rhCellMouseAdapter = new RHCellMouseAdapter(this);

        // Adding the row header labels
        for (int ii = 0; ii < cells.size(); ii++) {
            RowHeaderLabel lbl = new RowHeaderLabel(ii+1, cellFont);
            lbl.setBounds(0, ii * rowHeight, rowLabelWidth, rowHeight);
            if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX) {
                lbl.setBorder(cellBorder);
            }
            lbl.addMouseListener(rhCellMouseAdapter);
            //lbl.addMouseMotionListener(rhCellMouseAdapter);
            rowHeaderPanel.add(lbl);

            if (true) {
                dim = new Dimension(rowLabelWidth, rowHeight * (ii+1));
                rowHeaderPanel.setPreferredSize(dim);
                rowHeaderPanel.setSize(dim);
                tbl.revalidate();
            }
        }

        for (int c = 0; c < tbl.getColumnCount(); c++) {
            tbl.getColumn(c).setCellRenderer(new DupCellRenderer());
        }
        JViewport viewPort = new JViewport();
        dim.height = rowHeight * cells.size();
        viewPort.setViewSize(dim);
        viewPort.setView(rowHeaderPanel);
        sp.setRowHeader(viewPort);
        this.add(sp, BorderLayout.CENTER);
        tbl.revalidate();
        tbl.repaint();

    }
    public void createUI() {
        createTbl();
        btmPnl = new JPanel(new BorderLayout());
        leBtn = UIHelper.createButton();
        statusLbl = UIHelper.createLabel("");
        statusLbl.setHorizontalAlignment(JLabel.CENTER);
        btmPnl.add(statusLbl, BorderLayout.CENTER);
        btmPnl.add(leBtn, BorderLayout.EAST);
        leBtn.setVisible(false);
        btmPnl.setVisible(false);
        add(btmPnl, BorderLayout.SOUTH);
        getFormats();
    }

    @Override
    public void setPrevRowSelInx(int inx) {

    }

    @Override
    public void setPrevColSelInx(int inx) {

    }

    @Override
    public void setRowSelectionStarted(boolean b) {
        rowSelectionStarted = b;
    }

    @Override
    public boolean isRowSelectionStarted() {
        return rowSelectionStarted;
    }

    @Override
    public JTable getTable() {
        return tbl;
    }
}
