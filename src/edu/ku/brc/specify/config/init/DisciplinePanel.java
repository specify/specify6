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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This is the configuration window for create a new discipline.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2009
 *
 */
public class DisciplinePanel extends BaseSetupPanel
{
    protected JTextField         disciplineName;
    protected JComboBox          disciplines;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public DisciplinePanel(final String helpContext, final JButton nextBtn, final JButton prevBtn)
    {
        super("DISCIPLINE", helpContext, nextBtn, prevBtn);
        
        String header = getResourceString("DISP_INFO") + ":";

        CellConstraints cc = new CellConstraints();
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,6px,p,2px,p"), this);
        int row = 1;
        
        builder.add(createLabel(header, SwingConstants.CENTER), cc.xywh(1, row, 3, 1)); row += 2;
        
        Vector<DisciplineType> dispList = new Vector<DisciplineType>();
        for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
        {
            if (disciplineType.getType() == 0)
            {
                dispList.add(disciplineType);
            }
        }
        
        Collections.sort(dispList);
        
        disciplines = createComboBox(dispList);
        
        disciplines.setSelectedIndex(-1);
        
        // Discipline 
        JLabel lbl = createI18NFormLabel("DSP_TYPE", SwingConstants.RIGHT);
        lbl.setFont(bold);
        builder.add(lbl,         cc.xy(1, row));
        builder.add(disciplines, cc.xy(3, row));
        row += 2;
        
        makeStretchy = true;
        disciplineName = createField(builder, "DISP_NAME",  true, row, 64); row += 2;
        
        updateBtnUI();
        
        disciplines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateBtnUI();
                
                if (disciplines.getSelectedIndex() > -1)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            DisciplineType dt = (DisciplineType)disciplines.getSelectedItem();
                            disciplineName.setText(dt.getTitle());
                        }
                    });
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        props.put("dispName", disciplineName.getText());
        props.put("disciplineType", getDisciplineType());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        disciplineName.setText(values.getProperty("dispName"));
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid);
        }
    }
    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        String name = disciplineName.getText();
        if (StringUtils.isNotEmpty(name) && disciplines.getSelectedIndex() > -1)
        {
            int cnt = BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM discipline WHERE Name = '%s'", name));
            if (cnt > 0)
            {
                UIRegistry.showLocalizedError("DISPNAME_DUP", name);
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    // Getters 
    
    /**
     * @return
     */
    public String getDisciplineTitle()
    {
        return disciplineName.getText();
    }

    /**
     * @return
     */
    public DisciplineType getDisciplineType()
    {
        return (DisciplineType)disciplines.getSelectedItem();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        list.add(new Pair<String, String>(getResourceString("DSP_TYPE"), disciplines.getSelectedItem().toString()));
        list.add(new Pair<String, String>(getResourceString("DSP_NAME"), disciplineName.getText()));
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        super.doingNext();
    }
    
    
}
