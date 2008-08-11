package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * @author Ricardo
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class DataObjAggListEdtDlg extends CustomDialog 
{
    protected DBTableInfo               tableInfo;
    protected DataObjFieldFormatMgr     dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr       uiFieldFormatterMgrCache;
    
    protected boolean                   hasChanged = false; 
    
    // UI controls
    protected JList                     aggregatorList;
    protected DefaultListModel          listModel;
    protected DefEditDeleteAddPanel     dedaPanel;
    
    // listeners
    protected ListSelectionListener     aggregatorListSL = null;

    /**
     * @throws HeadlessException
     */
    public DataObjAggListEdtDlg(final Frame                 frame, 
                                      final DBTableInfo           tableInfo, 
                                      final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                      final UIFieldFormatterMgr   uiFieldFormatterMgrCache) 
        throws HeadlessException
    {
        super(frame, getResourceString("DOA_DLG_TITLE"), true, OKCANCELHELP, null); //I18N 
        this.tableInfo                  = tableInfo;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
       
        // table info
        PanelBuilder tblInfoPB          = new PanelBuilder(new FormLayout("p,p", "p")/*, new FormDebugPanel()*/);
        JLabel       tableTitleLbl      = createI18NFormLabel("FFE_TABLE");
        JLabel       tableTitleValueLbl = createLabel(tableInfo.getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);
        
        tblInfoPB.add(tableTitleLbl,      cc.xy(1, 1));
        tblInfoPB.add(tableTitleValueLbl, cc.xy(2, 1));
        
        // add available data object formatters
        populateAggregatorList();
        
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {   
                addAggregator();
            }
        };
        
        ActionListener delAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                deleteSelectedAggregator();
            }
        };
        
        ActionListener edtAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                editAggregator((DataObjAggregator)aggregatorList.getSelectedValue(), false);
            }
        };
        
        ActionListener defAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                setAggDefault();
            }
        };
        
        // delete button
        dedaPanel = new DefEditDeleteAddPanel(defAL, edtAL, delAL, addAL, "", "", "", "");
        dedaPanel.getAddBtn().setEnabled(true);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:max(250px;p):g,2px,p"));
        
        // lay out components on main panel        
        int y = 1; // leave first row blank 
        pb.add(tblInfoPB.getPanel(), cc.xy(1, y)); y += 2;
        
        pb.add(UIHelper.createScrollPane(aggregatorList), cc.xy(1,y)); y += 2;

        pb.add(dedaPanel, cc.xy(1,y)); y += 2;
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        updateUIEnabled();

        pack();
    }

    /**
     * @return
     */
    protected List<DataObjAggregator> populateAggregatorList() 
    {
        listModel = new DefaultListModel();
        
        List<DataObjAggregator> aggs = dataObjFieldFormatMgrCache.getAggregatorList(tableInfo.getClassObj());
        Collections.sort(aggs, new ComparatorByStringRepresentation<DataObjAggregator>()); 
        for (DataObjAggregator agg : aggs)
        {
            listModel.addElement(agg);
        }

        aggregatorList = createList(listModel);
        aggregatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        aggregatorList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateUIEnabled();
                }
            }
        });
        
        aggregatorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    editAggregator((DataObjAggregator)aggregatorList.getSelectedValue(), false);
                }
            }
        });
        
        
        return aggs;
    }
    
    /**
     * 
     */
    protected void deleteSelectedAggregator()
    {
        DataObjAggregator agg = (DataObjAggregator)aggregatorList.getSelectedValue();
        listModel.removeElement(agg);
        updateUIEnabled();
    }
    
    /**
     * 
     */
    protected void editAggregator(final DataObjAggregator agg, final boolean isNew)
    {
        try
        {
            DataObjAggregator tempCopy = isNew ? agg : (DataObjAggregator)agg.clone();
            
            DataObjAggregatorDlg dlg = new DataObjAggregatorDlg(this, tableInfo, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, tempCopy);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                if (isNew)
                {
                    dataObjFieldFormatMgrCache.addAggregator(agg);
                    listModel.addElement(agg);
                    
                } else
                {
                    listModel.removeElement(agg);
                    dataObjFieldFormatMgrCache.removeAggregator(agg);
                    dataObjFieldFormatMgrCache.addAggregator(tempCopy);
                    listModel.addElement(tempCopy);
                }
            }
            
        } catch (CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected void setAggDefault()
    {
        DataObjAggregator selected = (DataObjAggregator)aggregatorList.getSelectedValue();
        DefaultListModel model    = (DefaultListModel)aggregatorList.getModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataObjAggregator agg = (DataObjAggregator)model.get(i);
            agg.setDefault(agg == selected);
        }
        aggregatorList.repaint();
    }
    
    /**
     * 
     */
    protected void addAggregator()
    {
        DataObjAggregator agg = new DataObjAggregator();
        agg.setDataClass(tableInfo.getClassObj());
        
        editAggregator(agg, true);
    }
    
    /**
     * 
     */
    protected void updateUIEnabled()
    {
        DataObjAggregator agg = (DataObjAggregator)aggregatorList.getSelectedValue();
        
        dedaPanel.getDelBtn().setEnabled(agg != null);
        dedaPanel.getDefBtn().setEnabled(agg != null);
        dedaPanel.getEditBtn().setEnabled(agg != null);
    }
    
}
