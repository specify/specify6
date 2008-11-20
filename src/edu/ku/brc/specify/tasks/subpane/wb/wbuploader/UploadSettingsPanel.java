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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class UploadSettingsPanel extends JPanel
{
    UploadMatchSettingsPanel matchPanel;
    protected final Vector<UploadTable> tables;
    //protected UploadMatchSettingsBasicPanel basicMatchPane; 
    //protected JTabbedPane matchPane;
    //protected JTabbedPane mainPane;
    
    public UploadSettingsPanel(final Vector<UploadTable> tables)
    {
        super();
        this.tables = tables;
        setLayout(new BorderLayout());
    }
    
    
    @SuppressWarnings("unused")
    public void buildUI(final MissingDataResolver resolver, final boolean readOnly)
    {
/*        matchPane = new JTabbedPane();
        //basicMatchPane = new UploadMatchSettingsBasicPanel();
        matchPanel = new UploadMatchSettingsPanel(tables, readOnly);
        //matchPane.add(getResourceString("WB_UPLOAD_GLOBAL"), basicMatchPane);
        matchPane.add(getResourceString("WB_UPLOAD_INDIVIDUAL"), matchPanel);
        setLayout(new BorderLayout());
        mainPane = new JTabbedPane();
        mainPane.addTab(getResourceString("WB_UPLOAD_MATCH_SETTINGS"), matchPane);
        mainPane.addTab(getResourceString("WB_UPLOAD_MISSING_DATA"), resolver.getUI(readOnly));
        add(mainPane, BorderLayout.CENTER);
*/        
        
        matchPanel = new UploadMatchSettingsPanel(tables, readOnly, false);
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        //add(matchPanel, BorderLayout.CENTER);
        pb.add(matchPanel, new CellConstraints().xy(1,1));
        add(pb.getPanel(), BorderLayout.CENTER);
    }

    /**
     * @return the matchPanel
     */
    public UploadMatchSettingsPanel getMatchPanel()
    {
        return matchPanel;
    }
}
