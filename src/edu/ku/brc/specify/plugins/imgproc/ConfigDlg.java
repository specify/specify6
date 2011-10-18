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
package edu.ku.brc.specify.plugins.imgproc;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 17, 2011
 *
 */
public class ConfigDlg extends CustomDialog
{
    protected ValCheckBox       useNumCheckbox;
    protected ValSpinner        numPicsSpin;
    protected ValSpinner        barCodeInxSpin;
    protected ValTextField      dstTxt;
    protected ValBrowseBtnPanel destination;
    
    /**
     * @param dialog
     * @throws HeadlessException
     */
    public ConfigDlg(Dialog dialog) throws HeadlessException
    {
        super(dialog, "Configure", true, CustomDialog.OKCANCELHELP, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        useNumCheckbox = new ValCheckBox("Use Picture Count", false, false);
        numPicsSpin    = new ValSpinner(1, 10, false, false);
        barCodeInxSpin = new ValSpinner(1, 10, false, false);
        dstTxt         = new ValTextField();
        destination    = new ValBrowseBtnPanel(dstTxt, true, true);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder bldr = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p"));
        
        int y = 1;
        bldr.add(UIHelper.createLabel(""), cc.xy(1, y));
        bldr.add(useNumCheckbox, cc.xy(3, y));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel("Number of Pictures"), cc.xy(1, y));
        bldr.add(numPicsSpin, cc.xy(3, y));
        y += 2;

        bldr.add(UIHelper.createI18NFormLabel("Barcode Picture Number"), cc.xy(1, y));
        bldr.add(barCodeInxSpin, cc.xy(3, y));
        y += 2;

        bldr.add(UIHelper.createI18NFormLabel("Destination"), cc.xy(1, y));
        bldr.add(destination, cc.xyw(3, y, 2));
        y += 2;
        
        bldr.setDefaultDialogBorder();
        contentPanel = bldr.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        //okBtn.setEnabled(false);
        
        AppPreferences locPrefs = AppPreferences.getLocalPrefs();
        useNumCheckbox.setSelected(locPrefs.getBoolean("IMGWRKFLW.USE_CNT", true));
        numPicsSpin.setValue(locPrefs.getInt("IMGWRKFLW.PIC_CNT", 3));
        barCodeInxSpin.setValue(locPrefs.getInt("IMGWRKFLW.PIC_INX", 0)+1);
        dstTxt.setValue(locPrefs.get("IMGWRKFLW.PIC_DEST", "/Users/rods/Pictures/Eye-Fi/Pics"), "");
        
        pack();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        AppPreferences locPrefs = AppPreferences.getLocalPrefs();
        locPrefs.putBoolean("IMGWRKFLW.USE_CNT", useNumCheckbox.isSelected());
        locPrefs.putInt("IMGWRKFLW.PIC_CNT", numPicsSpin.getIntValue());
        locPrefs.putInt("IMGWRKFLW.PIC_INX", barCodeInxSpin.getIntValue()-1);
        
        String dstStr = dstTxt.getText();
        if (StringUtils.isNotEmpty(dstStr))
        {
            File dir = new File(dstStr);
            if (dir.exists())
            {
                locPrefs.put("IMGWRKFLW.PIC_DEST", dstStr);
            }
        }
        
        super.okButtonPressed();
    }

}
