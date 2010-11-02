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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import sun.swing.table.DefaultTableCellHeaderRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.tasks.BaseTask.ASK_TYPE;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.FormViewObj.FVOFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.ColObjSourceHelper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 27, 2010
 *
 */
public class BatchReidentifyPanel extends JPanel
{
    private static final String LOOKUP_SQL = "SELECT co.CollectionObjectID, co.CatalogNumber, d.DeterminedDate,  d.TypeStatusName, t.FullName, d.DeterminationID " +
    	                                     "FROM collectionobject co INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
    	                                     "INNER JOIN taxon t ON d.TaxonID = t.TaxonID WHERE co.CollectionObjectID %s ORDER BY co.CatalogNumber";
    
    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");

    private ColObjTaxaTableModel  model;
    private JTable                coTable;
    private RecordSetIFace        recordSet = null;
    private Determination         determination;
    private Vector<ColObjTaxa>    items      = new Vector<ColObjTaxa>();
    private EditDeleteAddPanel    edaPanel;
    private UIFieldFormatterIFace fmtr      = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "catalogNumber");
    
    /**
     * 
     */
    public BatchReidentifyPanel()
    {
        super();
    }

    /**
     * 
     */
    protected void createUI()
    {
        ActionListener editAL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                viewCO();
            }
        };
        ActionListener delAL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeCO();
            }
        };
        ActionListener addAL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                searchCO();
            }
        };
        edaPanel = new EditDeleteAddPanel(editAL, delAL, addAL);
        edaPanel.getEditBtn().setIcon(IconManager.getIcon("ViewForm", IconManager.STD_ICON_SIZE.Std16));
        edaPanel.getAddBtn().setIcon(IconManager.getIcon("Search", IconManager.STD_ICON_SIZE.Std16));
        
        loadColObjInfo();
        
        model   = new ColObjTaxaTableModel();
        coTable = new JTable(model);
        
        DefaultTableCellRenderer       cr   = new DefaultTableCellRenderer();
        DefaultTableCellHeaderRenderer tchr = new DefaultTableCellHeaderRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        tchr.setHorizontalAlignment(SwingConstants.CENTER);
        
        coTable.setDefaultRenderer(String.class, cr);
        coTable.getTableHeader().setDefaultRenderer(tchr);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,8px,p,4px,f:350px:g,2px,p"), this);
        
        ViewBasedDisplayPanel vbPanel = new ViewBasedDisplayPanel(null, "Determination", Determination.class.getName(), true, MultiView.HIDE_SAVE_BTN);
        pb.add(vbPanel, cc.xy(1, 1));
        
        pb.addSeparator(DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()), cc.xy(1,3));
        pb.add(UIHelper.createScrollPane(coTable), cc.xy(1, 5));
        pb.add(edaPanel, cc.xy(1, 7));
        
        determination = new Determination();
        determination.initialize();
        determination.setIsCurrent(true);
        vbPanel.getMultiView().setData(determination);
        FormViewObj fvo = vbPanel.getMultiView().getCurrentViewAsFormViewObj();
        if (fvo != null)
        {
            FVOFieldInfo fi = fvo.getFieldInfoForName("isCurrent");
            if (fi != null)
            {
                ((ValCheckBox)fi.getComp()).setValue(true, null);
                fi.getComp().setEnabled(false);
            }
        }

        pb.setDefaultDialogBorder();
        
        updateBtnUI();
        
        coTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateBtnUI();
            }
        });
    }
    
    /**
     * 
     */
    private void updateBtnUI()
    {
        boolean isRowSelected = coTable.getSelectedRow() > -1;
        edaPanel.getEditBtn().setEnabled(isRowSelected);
        edaPanel.getDelBtn().setEnabled(isRowSelected);
        edaPanel.getAddBtn().setEnabled(true);
    }
    
    /**
     * 
     */
    private void viewCO()
    {
        if (coTable.getSelectedRow() > -1)
        {
            ColObjTaxa cotx = (ColObjTaxa)items.get(coTable.getSelectedRow());
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                CollectionObject co = session.get(CollectionObject.class, cotx.getColObjId());
                co.forceLoad();
                for (Determination d : co.getDeterminations())
                {
                    d.forceLoad();
                }
                String title = DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId());
                ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                        null,
                        "CollectionObject",
                        null,
                        title,
                        "Close",
                        null,
                        null,
                        false,
                        MultiView.HIDE_SAVE_BTN);
                dlg.setWhichBtns(CustomDialog.OK_BTN);
                dlg.getMultiView().getCurrentViewAsFormViewObj().setDataObj(co);
                
                session.close();
                session = null;
                UIHelper.centerAndShow(dlg);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BatchReidentifyPanel.class, ex);
    
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
    }
    
    /**
     * 
     */
    private void searchCO()
    {
        try
        {
            String title = "CollectionObjectSearch";
            
            ViewBasedSearchDialogIFace srchDlg = getViewbasedFactory().createSearchDialog(null, title); //$NON-NLS-1$
            if (srchDlg != null)
            {
                //srchDlg.setTitle(title);
                srchDlg.getDialog().setVisible(true);
                if (!srchDlg.isCancelled())
                {
                    CollectionObject co = (CollectionObject)srchDlg.getSelectedObject();
                    String sql = String.format(LOOKUP_SQL, String.format(" = %d", co.getId()));
                    if (addItems(sql) > 0)
                    {
                        Collections.sort(items);
                        model.fireDataChanged();
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            // it's ok 
            // we get when it can't find the search dialog
            // xxx error dialog "Unable to retrieve default search dialog"
        }    
    }
    
    /**
     * 
     */
    private void removeCO()
    {
        if (coTable.getSelectedRow() > -1)
        {
            int[] indexes = coTable.getSelectedRows();
            for (int inx : indexes)
            {
                items.remove(inx);
            }
            model.fireDataChanged();
        }
    }
    
    /**
     * @return
     */
    public boolean askForColObjs()
    {
        ColObjSourceHelper colObjSrcHelper = new ColObjSourceHelper(ColObjSourceHelper.TypeOfRS.eColObjs);
        recordSet = colObjSrcHelper.getRecordSet(null);
        if (recordSet != null && colObjSrcHelper.getAskTypeRV() != ASK_TYPE.Cancel)
        {
            createUI();
            return true;
        }
        return false;
    }
    
    /**
     * @return list of CatalogNumber to be changed
     */
    protected void loadColObjInfo()
    {
        if (recordSet != null)
        {
            if (recordSet.getRecordSetId() != null)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    RecordSet rs = session.get(RecordSet.class, recordSet.getRecordSetId());
                    rs.getItems().size();
                    recordSet = rs;
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BatchReidentifyPanel.class, ex);
    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
            
            items = new Vector<ColObjTaxa>(recordSet.getNumItems());
            if (fmtr != null)
            {
                String sql = String.format(LOOKUP_SQL, DBTableIdMgr.getInstance().getInClause(recordSet));
                addItems(sql);
            }
        }
    }
    
    /**
     * @param sql
     */
    private int addItems(final String sql)
    {
        int cnt = 0;
        System.out.println(sql);
        for (Object[] cols : BasicSQLUtils.query(sql))
        {
            String catNumStr  = (String)fmtr.formatToUI(cols[1]);
            String detDateStr = scrDateFormat.format((Date)cols[2]);
            String typeStatus = (String)cols[3];
            String taxonName  = (String)cols[4];
            items.add(new ColObjTaxa(catNumStr, detDateStr, typeStatus, taxonName, (Integer)cols[0]));
            cnt++;
        }
        return cnt;
    }
    
    /**
     * 
     */
    public void doReIdentify()
    {
        final String GLASSKEY = "REIDENTIFY";
        
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                int cnt = 0;
                try
                {
                    for (ColObjTaxa cot : items)
                    {
                        DataProviderSessionIFace session = null;
                        try
                        {
                            session = DataProviderFactory.getInstance().createSession();
                            CollectionObject co = session.get(CollectionObject.class, cot.getColObjId());
                            Thread.sleep(300);
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        } finally 
                        {
                            if (session != null) session.close();
                        }
                        cnt++;
                        firePropertyChange(GLASSKEY, 0, cnt);
                    }
                    firePropertyChange(GLASSKEY, 0, items.size());
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    //UIRegistry.showLocalizedError("MySQLBackupService.EXCP_BK");
                    
                } finally
                {
                    
                }
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.clearSimpleGlassPaneMsg();
            }
        };
        
        final SimpleGlassPane gp = UIRegistry.writeSimpleGlassPaneMsg(UIRegistry.getResourceString("DET_BTCH_REIDENT_MENU"), 24);
        gp.setProgress(0);
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (GLASSKEY.equals(evt.getPropertyName())) 
                        {
                            double value   = (double)((Integer)evt.getNewValue()).intValue();
                            int    percent = (int)(value / ((double)items.size()) * 100.0);
                            gp.setProgress(percent);
                        }
                    }
                });
        worker.execute();
    }
    
    //-------------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------------
    class ColObjTaxaTableModel extends DefaultTableModel
    {
        private String[] header = new String[] {"catalogNumber", "determinedDate", "typeStatusName", ""};
        private int[]    tblIds = new int[]    {CollectionObject.getClassTableId(), Determination.getClassTableId(), Determination.getClassTableId()};
        
        /**
         * 
         */
        public ColObjTaxaTableModel()
        {
            super();
            
            for (int i=0;i<tblIds.length;i++)
            {
                header[i] = DBTableIdMgr.getInstance().getTitleForField(tblIds[i], header[i]);
            }
            header[tblIds.length] = DBTableIdMgr.getInstance().getTitleForId(Taxon.getClassTableId());
        }

        /**
         * 
         */
        public void fireDataChanged()
        {
            fireTableDataChanged();
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return header != null ? header.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return header[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            ColObjTaxa cot = items.get(row);
            switch (column)
            {
                case 0 : return cot.getCatalogNumber();
                case 1 : return cot.getDetDate();
                case 2 : return cot.getTypeStatus();
                case 3 : return cot.getTaxa();
                default: return "";
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return items.size();
        }
        
    }
    
    //-------------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------------
    class ColObjTaxa implements Comparable<ColObjTaxa>
    {
        protected String  catalogNumber;
        protected String  detDate;
        protected String  typeStatus;
        protected String  taxa;
        protected Integer colObjId;
        
        /**
         * @param catalogNumber
         * @param taxa
         * @param colObjId
         */
        public ColObjTaxa(String catalogNumber, 
                          String detDate,
                          String typeStatus,
                          String taxa, 
                          Integer colObjId)
        {
            super();
            this.catalogNumber = catalogNumber;
            this.taxa = taxa;
            this.detDate = detDate;
            this.typeStatus = typeStatus;
            this.colObjId = colObjId;
        }

        /**
         * @return the catalogNumber
         */
        public String getCatalogNumber()
        {
            return catalogNumber;
        }

        /**
         * @return the taxa
         */
        public String getTaxa()
        {
            return taxa;
        }

        /**
         * @return the detDate
         */
        public String getDetDate()
        {
            return detDate;
        }

        /**
         * @return the typeStatus
         */
        public String getTypeStatus()
        {
            return typeStatus;
        }

        /**
         * @return the colObjId
         */
        public Integer getColObjId()
        {
            return colObjId;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(ColObjTaxa o)
        {
            return catalogNumber.compareTo(o.catalogNumber);
        }

    }
}
