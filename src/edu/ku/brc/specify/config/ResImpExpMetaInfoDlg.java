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
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 4, 2009
 *
 */
@SuppressWarnings("serial")
public class ResImpExpMetaInfoDlg extends CustomDialog
{
    protected JTextField    nameTxt;
    protected JComboBox     mimeTypeCBX;  // text/xml, jrxml/label, jrxml/report, jrxml/subreport
    protected JTextField    descTxt;      // text
    
    // Meta Info
    protected JComboBox     tableIdCBX;    // List of all the tables
    protected JCheckBox     reqsRecSetChk; // whether a recordset is required
    
    protected JComboBox     rptTypeCBX;    // Invoice, WorkBench, Report
    
    protected JTextField    subReportsTxt; // list of subreports
    protected JTextField    actionsTxt;
    
    protected ArrayList<JLabel> labels = new ArrayList<JLabel>();
    
    protected SpAppResource appRes   = null;
    protected String        fileName;
    
    /**
     * @param appRes
     * @param fileName
     */
    public ResImpExpMetaInfoDlg(final SpAppResource appRes,
                                final String fileName)
    {
        super((Frame)UIRegistry.getTopWindow(), UIRegistry.getResourceString("ResImpExpMetaInfoDlg.DlgTitle"), true, OKCANCELHELP, null); //$NON-NLS-1$   // I18N
        
        this.appRes   = appRes;
        this.fileName = fileName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        //HelpMgr.registerComponent(helpBtn, "ResImpExpMetaInfoDlg");
        
        DefaultComboBoxModel mimeModel = new DefaultComboBoxModel(new String[] {"XML", "Label", "Report", "Subreport"});                  // I18N
        DefaultComboBoxModel typeModel = new DefaultComboBoxModel(new String[] {"Report", "Invoice", "WorkBench", "CollectionObject"});   // I18N
        
        DefaultComboBoxModel tblModel = new DefaultComboBoxModel(DBTableIdMgr.getInstance().getTablesForUserDisplay());
        
        mimeTypeCBX = createComboBox(mimeModel);
        
        nameTxt = createTextField();
        descTxt = createTextField();
        
        CellConstraints cc = new CellConstraints();
        
        // Meta Info
        tableIdCBX    = createComboBox(tblModel);
        reqsRecSetChk = createCheckBox("Is Record Set Required?");
        
        rptTypeCBX    = createComboBox(typeModel);
        
        subReportsTxt = createTextField();
        actionsTxt    = createTextField();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p,4px,p,4px,p,4px,p,4px,p,4px")); //$NON-NLS-1$ //$NON-NLS-2$

        int y = 1;
        JLabel lbl = createI18NFormLabel("Mime Type");              // I18N
        pb.add(lbl,         cc.xy(1, y));
        pb.add(mimeTypeCBX, cc.xy(3, y)); y += 2;
        
        lbl = createI18NFormLabel("Name");              // I18N
        pb.add(lbl,         cc.xy(1, y));
        pb.add(nameTxt,     cc.xy(3, y)); y += 2;
        
        lbl = createI18NFormLabel("Description");              // I18N
        pb.add(lbl,         cc.xy(1, y));
        pb.add(descTxt,     cc.xyw(3, y, 2)); y += 2;
        
        lbl = createI18NFormLabel("Table");              // I18N
        labels.add(lbl);
        pb.add(lbl,         cc.xy(1, y));
        pb.add(tableIdCBX,  cc.xy(3, y)); y += 2;
        
        pb.add(reqsRecSetChk, cc.xy(3, y)); y += 2;
        
        lbl = createI18NFormLabel("Report Type");              // I18N
        labels.add(lbl);
        pb.add(lbl,         cc.xy(1, y));
        pb.add(rptTypeCBX,  cc.xy(3, y)); y += 2;
        
        lbl = createI18NFormLabel("Sub-Reports");              // I18N
        labels.add(lbl);
        pb.add(lbl,           cc.xy(1, y));
        pb.add(subReportsTxt, cc.xyw(3, y, 2)); y += 2;
        
        lbl = createI18NFormLabel("Action");              // I18N
        labels.add(lbl);
        pb.add(lbl,        cc.xy(1, y));
        pb.add(actionsTxt, cc.xyw(3, y, 2)); y += 2;
            
        pb.setDefaultDialogBorder();
        
        nameTxt.setText(FilenameUtils.getBaseName(fileName));
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
        
        updateUI();
        
        mimeTypeCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateUI();
            }
        });
    }
    
    /**
     * 
     */
    private void updateUI()
    {
        boolean isRep = mimeTypeCBX.getSelectedIndex() > 0;
        tableIdCBX.setEnabled(isRep);
        reqsRecSetChk.setEnabled(isRep);
        rptTypeCBX.setEnabled(isRep);
        subReportsTxt.setEnabled(isRep);
        actionsTxt.setEnabled(isRep);
        for (JLabel lbl : labels)
        {
            lbl.setEnabled(isRep);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        String mimeType = (String)mimeTypeCBX.getSelectedItem();
        appRes.setMimeType((mimeTypeCBX.getSelectedIndex() == 0 ? "text/" : "jrxml/") + mimeType.toLowerCase());
        
        appRes.setName(nameTxt.getText());
        appRes.setDescription(descTxt.getText());
        
        if (mimeTypeCBX.getSelectedIndex() > 0 && mimeTypeCBX.getSelectedIndex() < 3)
        {
            StringBuilder metaData = new StringBuilder();
            if (tableIdCBX.getSelectedItem() != null)
            {
                DBTableInfo ti = (DBTableInfo)tableIdCBX.getSelectedItem();
                metaData.append("tableid=");
                metaData.append(ti.getTableId());
            }
            
            metaData.append("reqrs=");
            metaData.append(reqsRecSetChk.isSelected());
            
            String type = null;
            String rptTyp = (String)rptTypeCBX.getSelectedItem();
            if (rptTyp != null)
            {
                metaData.append("reporttype=");
                metaData.append(rptTyp);
                if (rptTyp.equals("Label"))
                {
                    type = "Labels";
                } else
                {
                    type = "Report";
                }
            }
            metaData.append("type=");
            metaData.append(type);
            
            String actionStr = actionsTxt.getText();
            if (StringUtils.isNotEmpty(actionStr))
            {
                metaData.append("action=");
                metaData.append(type);
            }
            
            String subRpt = subReportsTxt.getText();
            if (StringUtils.isNotEmpty(subRpt))
            {
                metaData.append("subreports=");
                metaData.append(subRpt);
            }
            
            appRes.setMetaData(metaData.toString());
        }
        
        super.okButtonPressed();
    }
}
