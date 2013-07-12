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

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.ToggleButtonChooserPanel;

@SuppressWarnings("serial")
public class UploadMatchSettingsBasicPanel extends JPanel implements ActionListener
{   
    protected static final Logger log = Logger.getLogger(UploadMatchSettingsBasicPanel.class);

    ToggleButtonChooserPanel<String> modePanel;
    JCheckBox rememberCheck;
    JCheckBox matchBlanksCheck;
    JButton advancedBtn;
    
    public void actionPerformed(ActionEvent action)
    {
        if (action.getSource() == advancedBtn)
        {
            log.debug("advanced");
        }
    }

    public UploadMatchSettingsBasicPanel()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 5dlu", "2dlu, f:p:g, 8dlu, c:p, 2dlu, c:p, 3dlu"));
        CellConstraints cc = new CellConstraints();
        
        modePanel = new ToggleButtonChooserPanel<String>(UploadMatchSetting.getModeTexts(), getResourceString("WB_MATCH_MODE_CAPTION"), 
                ToggleButtonChooserPanel.Type.RadioButton);
        modePanel.createUI();
        modePanel.setOkBtn(createButton("")); //needs an ok button for setSelectedObj() etc to work??
        pb.add(modePanel, cc.xy(2,2));
        
        rememberCheck = createCheckBox(getResourceString("WB_UPLOAD_MATCH_REMEMBER_CAPTION"));
        pb.add(rememberCheck, cc.xy(2,4));
        
        matchBlanksCheck = createCheckBox(getResourceString("WB_UPLOAD_MATCH_BLANKS_CAPTION"));
        pb.add(matchBlanksCheck, cc.xy(2,6));
        
        //advancedBtn = createButton(getResourceString("WB_UPLOAD_ADVANCED_BTN"));
        //add(advancedBtn);

        add(pb.getPanel());
    }
    
    public void showSetting(final UploadTable uploadTable)
    {
        modePanel.setSelectedObj(UploadMatchSetting.getModeText(uploadTable.getMatchSetting().getMode()));
        rememberCheck.setSelected(uploadTable.getMatchSetting().isRemember());
        matchBlanksCheck.setSelected(uploadTable.getMatchSetting().isMatchEmptyValues());        
    }
    
    public void applySetting(final UploadTable uploadTable)
    {
        uploadTable.getMatchSetting().setMode(UploadMatchSetting.getMode(modePanel.getSelectedObject()));
        uploadTable.getMatchSetting().setRemember(rememberCheck.isSelected());
        uploadTable.getMatchSetting().setMatchEmptyValues(matchBlanksCheck.isSelected());       
    }
    
    public void applySettingToAll(final List<UploadTable> uploadTables)
    {
        for (UploadTable ut : uploadTables)
        {
            applySetting(ut);
        }
    }
}
