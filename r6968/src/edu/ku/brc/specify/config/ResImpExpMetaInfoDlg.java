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
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

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
public class ResImpExpMetaInfoDlg extends CustomDialog
{
    protected JComboBox  mimeTypeCBX;  // text/xml, jrxml/label, jrxml/report, jrxml/subreport
    protected JTextField descTxt;      // text
    
    // Meta Info
    protected JComboBox  tableIdCBX;    // List of all the tables
    protected JCheckBox  reqsRecSetChk; // whether a recordset is required
    
    protected JComboBox  rptTypeCBX;    // Invoice, WorkBench, Report
    
    protected JTextField subReportsTxt; // list of subreports
    protected JTextField actionsTxt;
    
    protected SpAppResource appRes = null;
    
    /**
     * Default Constructor.
     */
    public ResImpExpMetaInfoDlg(final SpAppResource appRes)
    {
        super((Frame)UIRegistry.getTopWindow(), "Title", true, OKCANCELHELP, null); //$NON-NLS-1$
        
        this.appRes = appRes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        //HelpMgr.registerComponent(helpBtn, "ResImpExpMetaInfoDlg");
        
        DefaultComboBoxModel mimeModel = new DefaultComboBoxModel(new String[] {"XML", "Label", "Report", "Subreport"});
        DefaultComboBoxModel typeModel = new DefaultComboBoxModel(new String[] {"Report", "Invoice", "WorkBench", "CollectionObject"});
        
        DefaultComboBoxModel tblModel = new DefaultComboBoxModel(DBTableIdMgr.getInstance().getTablesForUserDisplay());
        
        mimeTypeCBX = createComboBox(mimeModel);
        
        descTxt = createTextField();
        
        // Meta Info
        tableIdCBX    = createComboBox(tblModel);
        reqsRecSetChk = createCheckBox("Is RecSet Required?");
        
        rptTypeCBX    = createComboBox(typeModel);
        
        subReportsTxt = createTextField();
        actionsTxt    = createTextField();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p,4px,p,4px,p,4px,p,4px,")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc = new CellConstraints();

        int y = 1;
        pb.add(createI18NFormLabel("Mime Type"), cc.xy(1, y));
        pb.add(mimeTypeCBX,                      cc.xy(3, y)); y += 2;
        
        pb.add(createI18NFormLabel("Description"), cc.xy(1, y));
        pb.add(descTxt,                            cc.xyw(3, y, 2)); y += 2;
        
        pb.add(createI18NFormLabel("Table"), cc.xy(1, y));
        pb.add(tableIdCBX,                   cc.xy(3, y)); y += 2;
        
        //pb.add(createI18NFormLabel("Mime Type"), cc.xy(1, y));
        pb.add(reqsRecSetChk,                      cc.xy(3, y)); y += 2;
        
        pb.add(createI18NFormLabel("Report Type"), cc.xy(1, y));
        pb.add(rptTypeCBX,                         cc.xy(3, y)); y += 2;
        
        pb.add(createI18NFormLabel("Sub-Reports"), cc.xy(1, y));
        pb.add(subReportsTxt,                      cc.xyw(3, y, 2)); y += 2;
        
        pb.add(createI18NFormLabel("Action"), cc.xy(1, y));
        pb.add(actionsTxt,                    cc.xyw(3, y, 2)); y += 2;
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        String mimeType = (String)mimeTypeCBX.getSelectedItem();
        appRes.setMimeType((mimeTypeCBX.getSelectedIndex() == 0 ? "text/" : "jrxml/") + mimeType.toLowerCase());
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
