/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class UploadSettingsPanel extends JPanel
{
    UploadMatchSettingsPanel advancedMatchPane;
    protected final Vector<UploadTable> tables;
    protected UploadMatchSettingsBasicPanel basicMatchPane;
    protected JTabbedPane matchPane;
    protected JTabbedPane mainPane;
    
    public UploadSettingsPanel(final Vector<UploadTable> tables)
    {
        this.tables = tables;
    }
    
    public void buildUI(final MissingDataResolver resolver, final boolean readOnly)
    {
        matchPane = new JTabbedPane();
        //basicMatchPane = new UploadMatchSettingsBasicPanel();
        advancedMatchPane = new UploadMatchSettingsPanel(tables, readOnly);
        //matchPane.add(getResourceString("WB_UPLOAD_GLOBAL"), basicMatchPane);
        matchPane.add(getResourceString("WB_UPLOAD_INDIVIDUAL"), advancedMatchPane);
        setLayout(new BorderLayout());
        mainPane = new JTabbedPane();
        mainPane.addTab(getResourceString("WB_UPLOAD_MATCH_SETTINGS"), matchPane);
        mainPane.addTab(getResourceString("WB_UPLOAD_MISSING_DATA"), resolver.getUI(readOnly));
        add(mainPane, BorderLayout.CENTER);
    }
}
