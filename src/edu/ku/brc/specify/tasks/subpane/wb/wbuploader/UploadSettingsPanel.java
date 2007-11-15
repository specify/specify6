/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

public class UploadSettingsPanel extends JPanel
{
    UploadMatchSettingsPanel matchPane;
    JTabbedPane mainPane;
    
    public void buildUI(final MissingDataResolver resolver, final Vector<UploadTable> tables, final boolean readOnly)
    {
        matchPane = new UploadMatchSettingsPanel(tables, readOnly);
        setLayout(new BorderLayout());
        mainPane = new JTabbedPane();
        mainPane.addTab(getResourceString("WB_UPLOAD_MATCH_SETTINGS"), matchPane);
        mainPane.addTab(getResourceString("WB_UPLOAD_MISSING_DATA"), resolver.getUI(readOnly));
        add(mainPane, BorderLayout.CENTER);
    }
}
