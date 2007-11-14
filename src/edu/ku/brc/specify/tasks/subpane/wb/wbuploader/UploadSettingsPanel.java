/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

import javax.swing.JPanel;

public class UploadSettingsPanel extends JPanel
{
    UploadMatchSettingsPanel matchPane;
    
    public void buildUI(final MissingDataResolver resolver, final Vector<UploadTable> tables, final boolean readOnly)
    {
        matchPane = new UploadMatchSettingsPanel(tables, readOnly);
        add(matchPane);
        add(resolver.getUI(readOnly));
    }
}
