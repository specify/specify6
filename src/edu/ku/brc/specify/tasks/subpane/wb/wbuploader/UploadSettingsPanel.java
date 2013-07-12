/* Copyright (C) 2013, University of Kansas Center for Research
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
