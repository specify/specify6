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
package edu.ku.brc.specify.prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.GenericLSIDGeneratorFactory.CATEGORY_TYPE;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 7, 2009
 *
 */
public class LSIDPrefsPanel extends GenericPrefsPanel
{
    protected Hashtable<CATEGORY_TYPE, ValCheckBox> checkBoxes = new Hashtable<CATEGORY_TYPE, ValCheckBox>();
    protected ValCheckBox   useVersioning;
    protected FormValidator validator     = new FormValidator(null);
    /**
     * 
     */
    public LSIDPrefsPanel()
    {
        super();
        
        title    = "LSIDPrefsPanel";
        name     = "LSIDPrefsPanel";
        hContext = "LSIDPrefs";
        
        validator.setName("LSID Validator");
        validator.setNewObj(true);
        
        createUI();
    }

    /**
     * 
     */
    protected void createUI()
    {
        ArrayList<String> list = new ArrayList<String>(CATEGORY_TYPE.values().length);
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            list.add(cat.toString());
        }
        Collections.sort(list);
        
        CellConstraints cc = new CellConstraints();
        
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", CATEGORY_TYPE.values().length+2);
        PanelBuilder pb = new PanelBuilder(new FormLayout("p", rowDef), this);
        pb.addSeparator("Automatic LSID Generation");
        int y = 3;
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            ValCheckBox vcb = new ValCheckBox(cat.toString(), false, false);
            checkBoxes.put(cat, vcb);
            pb.add(vcb, cc.xy(1, y));
            DataChangeNotifier dcn = validator.createDataChangeNotifer(cat.toString(), vcb, null);
            vcb.addActionListener(dcn);
            y += 2;
        }
        useVersioning = new ValCheckBox("Use Versioning", false, false); // I18N
        DataChangeNotifier dcn = validator.createDataChangeNotifer("UV", useVersioning, null);
        useVersioning.addActionListener(dcn);
        pb.add(useVersioning, cc.xy(1, y));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#isFormValid()
     */
    @Override
    public boolean isFormValid()
    {
        return super.isFormValid();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        // get data from UI
        
        
    }

}
