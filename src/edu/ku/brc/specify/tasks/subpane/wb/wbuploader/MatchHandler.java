/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

public class MatchHandler 
{
    protected static final Logger log = Logger.getLogger(MatchHandler.class);
 
    protected final UploadTable uploadTable;
    protected CustomDialog matchDlg;
    protected CustomDialog settingDlg;
    protected JList matchesList;
    protected JButton viewBtn;

    public MatchHandler(final UploadTable uploadTable)
    {
        this.uploadTable = uploadTable;
    }
    /**
     * @param matches
     * @return selected match
     */
    public DataModelObjBase dealWithMultipleMatches(final List<DataModelObjBase> matches, final Vector<UploadTable.MatchRestriction> restrictedVals, int recNum) throws UploaderException
    {
        if (uploadTable.getMatchSetting().getMode() == UploadMatchSetting.PICK_FIRST_MODE)
        {
            return matches.get(0);
        }
        if (uploadTable.getMatchSetting().getMode() == UploadMatchSetting.SKIP_ROW_MODE)
        {
            throw new UploaderMatchSkipException(UploaderMatchSkipException.makeMsg(restrictedVals, uploadTable.getUploadFields().get(recNum).size(), Uploader.getCurrentUpload().getRow()), 
                    matches, Uploader.getCurrentUpload().getRow(), uploadTable);
        }
        if (uploadTable.getMatchSetting().isRemember() || uploadTable.getMatchSetting().getMode() == UploadMatchSetting.ADD_NEW_MODE)
        {
            Object key = uploadTable.getMatchSetting().doLookup(restrictedVals);
            if (uploadTable.getMatchSetting().getMode() == UploadMatchSetting.ADD_NEW_MODE && key == null)
            {
                uploadTable.onAddNewMatch(restrictedVals);                 
                return null;
            }
            if (key != null)
            {
                for (DataModelObjBase match : matches)
                {
                    if (key.equals(match.getId()))
                    {
                        return match;
                    }
                }
                log.error("WB values matched previous selection, but no matching database object was found.");
            }
        }
        
        JPanel pane = new JPanel(new BorderLayout());

        JPanel matchedPane = new JPanel(new BorderLayout());
        
        JLabel matchedLbl = createLabel(getResourceString("WB_UPLOAD_MATCHED_CELLS"));
        matchedLbl.setFont(matchedLbl.getFont().deriveFont(Font.BOLD));        
        matchedLbl.setBorder(new EmptyBorder(3, 1, 3, 0));
        matchedPane.add(matchedLbl, BorderLayout.NORTH);
        
        //Horizontal layout
        Vector<String> headers = new Vector<String>(restrictedVals.size());
        for (UploadTable.MatchRestriction val : restrictedVals)
        {
            headers.add(val.getFieldName());
        }
        Vector<Vector<String>> matchedVec = new Vector<Vector<String>>(1);
        Vector<String> row = new Vector<String>(restrictedVals.size());
        for (int v = 0; v < restrictedVals.size(); v++)
        {
            String val = restrictedVals.get(v).getRestriction();
            if (val.equals(uploadTable.getNullRestrictionText()))
            {
                val = "";
            }
            row.add(val);
        }
        matchedVec.add(row);
        
        JTable matchedTbl = new JTable(matchedVec, headers);
        JScrollPane scrollPane = new JScrollPane(matchedTbl);
        //JLabel corner = createLabel(getResourceString("WB_ROW"));
        //corner.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel corner = new JPanel(); 
        scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);
        
        //following stuff is stolen from SpreadSheet.buildSpreadSheet().
        //Hopefully most of the stuff from SpreadSheet thats not necessary here has been removed.
        
        //matchedTbl.getModel().fireTableStructureChanged();
        
        TableColumn       column   = matchedTbl.getColumnModel().getColumn(0);
        TableCellRenderer renderer = matchedTbl.getTableHeader().getDefaultRenderer();
        if (renderer == null)
        {
            column = matchedTbl.getColumnModel().getColumn(0);
            renderer = column.getHeaderRenderer();
        }
        
        // Calculate Row Height
        Component   cellRenderComp = renderer.getTableCellRendererComponent(matchedTbl, column.getHeaderValue(), false, false, -1, 0);
        Font        cellFont       = cellRenderComp.getFont();
        Border      cellBorder     = (Border)UIManager.getDefaults().get("TableHeader.cellBorder");
        Insets      insets         = cellBorder.getBorderInsets(matchedTbl.getTableHeader());
        FontMetrics metrics        = matchedTbl.getFontMetrics(cellFont);

        int rowHeight = insets.bottom + metrics.getHeight() + insets.top;

        int rowLabelWidth  = metrics.stringWidth("9999") + insets.right + insets.left;
        
        Dimension dim  = new Dimension(rowLabelWidth, rowHeight);
          
        JLabel lbl = createLabel(Integer.toString(Uploader.getCurrentUpload().rowUploading+1));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setPreferredSize(dim); 
        lbl.setBorder(cellBorder);
        lbl.setSize(dim);

        JViewport viewPort = new JViewport();
        dim.height = rowHeight;
        viewPort.setViewSize(dim);
        viewPort.setView(lbl);
        scrollPane.setRowHeader(viewPort);
        //end stolen stuff
        
        matchedTbl.setPreferredScrollableViewportSize(new Dimension(matchedTbl.getWidth(), matchedTbl.getRowCount()*matchedTbl.getRowHeight()));
        
        matchedPane.add(scrollPane, BorderLayout.CENTER);
        
        pane.add(matchedPane, BorderLayout.NORTH);

        JPanel matchesPane = new JPanel(new BorderLayout());
        JLabel matchesLbl = createLabel(getResourceString("WB_UPLOAD_DB_MATCHES"));
        matchesLbl.setFont(matchedLbl.getFont().deriveFont(Font.BOLD));
        matchesLbl.setBorder(new EmptyBorder(8, 1, 3, 0));
        matchesPane.add(matchesLbl, BorderLayout.NORTH); 

        Vector<Object> matchVec = new Vector<Object>();

        for (DataModelObjBase match : matches)
        {
            matchVec.add(new MatchedRecord(match));
        }
        matchesList = new JList(matchVec);
        matchesList.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        matchesList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateMatchUIState();
                }
            }
        });
        matchesPane.add(new JScrollPane(matchesList), BorderLayout.CENTER);
        
        pane.add(matchesPane, BorderLayout.CENTER);
        
        JButton settingsBtn = createButton(getResourceString("WB_UPLOAD_MATCH_SETTINGS_BTN"));
        settingsBtn.setToolTipText(getResourceString("WB_UPLOAD_MATCH_SETTINGS_BTN_HINT"));
        settingsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                showSettings();
            }
        });
        JPanel btnPane = new JPanel(new BorderLayout());
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p, 1dlu, f:min", "f:p"));
        viewBtn = createButton(getResourceString("WB_UPLOAD_VIEW_MATCH_BTN"));
        viewBtn.setToolTipText(getResourceString("WB_UPLOAD_VIEW_MATCH_BTN_HINT"));
        viewBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                viewSelectedMatch();
            }
        });
        CellConstraints cc = new CellConstraints();
        pb.add(viewBtn, cc.xy(1, 1));
        pb.add(settingsBtn, cc.xy(3, 1));
        
        btnPane.add(pb.getPanel(), BorderLayout.EAST);
        
        pane.add(btnPane, BorderLayout.SOUTH);
        
        String tblTitle = DBTableIdMgr.getInstance().getByShortClassName(uploadTable.getTblClass().getSimpleName()).getTitle();
        matchDlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), getResourceString("WB_UPLOAD_CHOOSE_MATCH") + " - " + tblTitle, true, 
                CustomDialog.OKCANCELAPPLYHELP, pane, CustomDialog.OK_BTN);
        matchDlg.setOkLabel(getResourceString("WB_UPLOAD_MATCH_SELECT_BTN"));
        matchDlg.setCancelLabel(getResourceString("WB_UPLOAD_MATCH_ADD_BTN"));
        matchDlg.setApplyLabel(getResourceString("WB_UPLOAD_MATCH_SKIPROW_BTN"));
        matchDlg.createUI();
        matchDlg.getOkBtn().setToolTipText(getResourceString("WB_UPLOAD_MATCH_SELECT_BTN_HINT"));
        matchDlg.getCancelBtn().setToolTipText(getResourceString("WB_UPLOAD_MATCH_ADD_BTN_HINT"));
        matchDlg.getApplyBtn().setToolTipText(getResourceString("WB_UPLOAD_MATCH_SKIPROW_BTN_HINT"));
        matchDlg.getApplyBtn().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                matchDlg.setVisible(false);
            }
        });
        updateMatchUIState();
        try
        {
            while (true) //scary...
            {
                UIHelper.centerAndShow(matchDlg);
                if (matchDlg.isCancelled() && matchDlg.getBtnPressed() == CustomDialog.CANCEL_BTN) 
                { 
                    return null; 
                }
                else if (!matchDlg.isCancelled())
                {
                    if (matchDlg.getBtnPressed() == CustomDialog.APPLY_BTN) 
                    { 
                        throw new UploaderMatchSkipException(
                            UploaderMatchSkipException.makeMsg(restrictedVals, uploadTable.getUploadFields().get(recNum).size(),
                            Uploader.getCurrentUpload().getRow()), matches, Uploader
                            .getCurrentUpload().getRow(), uploadTable); 
                    }
                    return ((MatchedRecord )matchesList.getSelectedValue()).getDataObj();
                }
            }
        }
        finally
        {
            matchDlg.dispose();
            matchDlg = null;
        }
    }
    
    /**
     * Uppdate UI to reflect selection changes.
     */
    protected void updateMatchUIState()
    {
        matchDlg.getOkBtn().setEnabled(matchesList.getSelectedValue() != null);
        viewBtn.setEnabled(matchesList.getSelectedValue() != null);
    }

    /**
     * Show match settings dialog.
     */
    protected void showSettings()
    {
        UploadMatchSettingsBasicPanel umsbp = new UploadMatchSettingsBasicPanel();
        umsbp.showSetting(uploadTable);
        
        try
        {
            settingDlg = new CustomDialog(null, getResourceString("WB_UPLOAD_SETTINGS"), true,
                    CustomDialog.OKCANCELAPPLYHELP, umsbp, CustomDialog.OK_BTN);
            settingDlg.setApplyLabel(getResourceString("WB_UPLOAD_APPLY_TO_ALL"));
            settingDlg.createUI();
            settingDlg.getApplyBtn().addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    applyToAll();
                }
            });

            //settingDlg.pack();
            UIHelper.centerAndShow(settingDlg);
            if (!settingDlg.isCancelled())
            {
                if (settingDlg.getBtnPressed() == CustomDialog.OK_BTN)
                {
                    umsbp.applySetting(uploadTable);
                }
                if (settingDlg.getBtnPressed() == CustomDialog.APPLY_BTN)
                {
                    System.out.println("applyToAll??");
                }
            }
        }
        finally
        {
            settingDlg.dispose();
            settingDlg = null;
        }
    }
    
    /**
     * Apply settings for current table to all tables.
     */
    protected void applyToAll()
    {
        //XXX Implement This!
        if (settingDlg != null)
        {
            settingDlg.setVisible(false);
        }        
    }

    /**
     * Views the match currently selected in the match list in a data form.
     * 
     * @return true if the match is selected. (Currently not implemented. Always returns false).
     */
    protected boolean viewSelectedMatch()
    {
        MatchedRecord selection = (MatchedRecord )matchesList.getSelectedValue();
        
        System.out.println("Viewing " + matchesList.getSelectedValue());

        String      viewName      = selection.getDataObj().getClass().getSimpleName();
        Frame       parentFrame   = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String      displayName   = selection.getTblInfo().getTitle();
        String      closeBtnText  = getResourceString("CLOSE");
        String      className     = selection.getDataObj().getClass().getName();
        String      idFieldName   = selection.getTblInfo().getIdFieldName();
        int         options       = MultiView.HIDE_SAVE_BTN;
        
        // create the form dialog
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, viewName, displayName, displayName, 
                                                                   closeBtnText, className, idFieldName, false, options);
        dialog.setModal(true);
        dialog.setData(selection.getDataObj());

        //dialog.getMultiView().getCurrentView().getValidator().validateForm();
        
        dialog.pack();
        // show the dialog (which allows all user edits to happen)
        dialog.setVisible(true);

        return false;
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     * 
     * Packages matched objects with their DBTableInfo
     *
     */
    private class MatchedRecord
    {
        protected final DataModelObjBase dataObj;
        protected final DBTableInfo      tblInfo;
        
        /**
         * @param matchedObj
         */
        public MatchedRecord(final DataModelObjBase matchedObj)
        {
            this.dataObj = matchedObj;
            int tblId = DBTableIdMgr.getInstance().getIdByClassName(dataObj.getClass().getName());
            tblInfo = DBTableIdMgr.getInstance().getInfoById(tblId);
        }

        /**
         * @return the dataObj
         */
        public DataModelObjBase getDataObj()
        {
            return dataObj;
        }

        /**
         * @return the tblInfo
         */
        public DBTableInfo getTblInfo()
        {
            return tblInfo;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            String value = DataObjFieldFormatMgr.getInstance().format(dataObj, dataObj.getClass());
            if (value != null)
            {
                return value;
            }
            //else
            log.error("no formatter found for " + dataObj.getClass().toString());
            return dataObj.toString();
        }
        
        
    }
}

