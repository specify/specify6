/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

public class MatchHandler 
{
    protected static final Logger log = Logger.getLogger(MatchHandler.class);
 
    protected final UploadTable uploadTable;
    protected CustomDialog matchDlg;
    protected CustomDialog settingDlg;
    protected JList matchesList;

    public MatchHandler(final UploadTable uploadTable)
    {
        this.uploadTable = uploadTable;
    }
    /**
     * @param matches
     * @return selected match
     */
    public DataModelObjBase dealWithMultipleMatches(final List<DataModelObjBase> matches, final Vector<Pair<String,String>> restrictedVals, int recNum) throws UploaderException
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
        
        JLabel matchedLbl = new JLabel(getResourceString("WB_UPLOAD_MATCHED_CELLS"));
        matchedLbl.setFont(matchedLbl.getFont().deriveFont(Font.BOLD));
        matchedPane.add(matchedLbl, BorderLayout.NORTH);

        Vector<String> headers = new Vector<String>(2);
        headers.add(getResourceString("WB_UPLOAD_CELL_HEADER"));
        headers.add(getResourceString("WB_UPLOAD_CELL_VALUE"));
        Vector<Vector<String>> matchedVec = new Vector<Vector<String>>(restrictedVals.size());
        for (int v = 0; v < restrictedVals.size() && v < uploadTable.getUploadFields().get(recNum).size(); v++)
        {
            Pair<String, String> val = restrictedVals.get(v);
            Vector<String> row = new Vector<String>(2);
            row.add(val.getFirst());
            row.add(val.getSecond());
            matchedVec.add(row);
        }
        JTable matchedTbl = new JTable(matchedVec, headers);
        matchedTbl.setPreferredScrollableViewportSize(new Dimension(matchedTbl.getWidth(), matchedTbl.getRowCount()*matchedTbl.getRowHeight()));
        matchedPane.add(new JScrollPane(matchedTbl), BorderLayout.CENTER);
        
        pane.add(matchedPane, BorderLayout.NORTH);

        JPanel matchesPane = new JPanel(new BorderLayout());
        JLabel matchesLbl = new JLabel(getResourceString("WB_UPLOAD_DB_MATCHES"));
        matchesLbl.setFont(matchedLbl.getFont().deriveFont(Font.BOLD));
        matchesPane.add(matchesLbl, BorderLayout.NORTH); 
        
        Vector<Object> matchVec = new Vector<Object>();

        matchVec.addAll(matches);
        matchesList = new JList(matchVec);
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
        
        JButton settingsBtn = new JButton(getResourceString("WB_UPLOAD_MATCH_SETTINGS_BTN"));
        settingsBtn.setActionCommand("SETTINGS");
        settingsBtn.setToolTipText(getResourceString("WB_UPLOAD_MATCH_SETTINGS_BTN_HINT"));
        settingsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                showSettings();
            }
        });
        JPanel btnPane = new JPanel(new BorderLayout());
        btnPane.add(settingsBtn, BorderLayout.EAST);
        pane.add(btnPane, BorderLayout.SOUTH);
        
        String title = null;
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(uploadTable.getWriteTable().getName());        
        if (ti != null)
        {
            title = ti.getTitle();
        }

        matchDlg = new CustomDialog(null, title, true, CustomDialog.OKCANCELAPPLYHELP, pane, CustomDialog.OK_BTN);
        matchDlg.createUI();
        matchDlg.setOkLabel(getResourceString("WB_UPLOAD_MATCH_SELECT_BTN"));
        matchDlg.getOkBtn().setToolTipText(getResourceString("WB_UPLOAD_MATCH_SELECT_BTN_HINT"));
        matchDlg.setCancelLabel(getResourceString("WB_UPLOAD_MATCH_ADD_BTN"));
        matchDlg.getCancelBtn().setToolTipText(getResourceString("WB_UPLOAD_MATCH_ADD_BTN_HINT"));
        matchDlg.setApplyLabel(getResourceString("WB_UPLOAD_MATCH_SKIPROW_BTN"));
        matchDlg.getApplyBtn().setToolTipText(getResourceString("WB_UPLOAD_MATCH_SKIPROW_BTN_HINT"));
        matchDlg.getApplyBtn().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                matchDlg.setVisible(false);
            }
        });
        updateMatchUIState();
        UIHelper.centerAndShow(matchDlg);
        try
        {
            if (matchDlg.isCancelled()) { return null; }
            if (matchDlg.getBtnPressed() == CustomDialog.APPLY_BTN) { throw new UploaderMatchSkipException(
                    UploaderMatchSkipException.makeMsg(restrictedVals, uploadTable.getUploadFields().get(recNum).size(),
                            Uploader.getCurrentUpload().getRow()), matches, Uploader
                            .getCurrentUpload().getRow(), uploadTable); }
            return (DataModelObjBase) matchesList.getSelectedValue(); 
        }
        finally
        {
            matchDlg.dispose();
            matchDlg = null;
        }
    }
    
    protected void updateMatchUIState()
    {
        matchDlg.getOkBtn().setEnabled(matchesList.getSelectedValue() != null);
    }

    protected void showSettings()
    {
        UploadMatchSettingsBasicPanel umsbp = new UploadMatchSettingsBasicPanel();
        umsbp.showSetting(uploadTable);
        
        try
        {
            settingDlg = new CustomDialog(null, getResourceString("WB_UPLOAD_SETTINGS"), true,
                    CustomDialog.OKCANCELAPPLYHELP, umsbp, CustomDialog.OK_BTN);
            settingDlg.setApplyLabel(getResourceString("Apply to All")); // i18n
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
    
    protected void applyToAll()
    {
        if (settingDlg != null)
        {
            settingDlg.setVisible(false);
        }        
    }
}
