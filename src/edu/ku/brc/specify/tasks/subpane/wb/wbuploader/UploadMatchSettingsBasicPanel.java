/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.ToggleButtonChooserPanel;

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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        modePanel = new ToggleButtonChooserPanel<String>(UploadMatchSetting.getModeTexts(), getResourceString("WB_MATCH_MODE_CAPTION"), 
                ToggleButtonChooserPanel.Type.RadioButton);
        add(modePanel);
        modePanel.createUI();
        rememberCheck = new JCheckBox(getResourceString("WB_UPLOAD_MATCH_REMEMBER_CAPTION"));
        add(rememberCheck);
        matchBlanksCheck = new JCheckBox(getResourceString("WB_UPLOAD_MATCH_BLANKS_CAPTION"));
        add(matchBlanksCheck);
        //advancedBtn = new JButton(getResourceString("WB_UPLOAD_ADVANCED_BTN"));
        //add(advancedBtn);
    }
}
