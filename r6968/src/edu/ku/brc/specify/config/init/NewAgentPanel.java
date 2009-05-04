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

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * This is the configuration window for create a new user and new database.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
public class NewAgentPanel extends BaseSetupPanel
{
    protected JTextField firstNameTxt;
    protected JTextField lastNameTxt;
    protected JTextField emailTxt;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public NewAgentPanel(final JButton nextBtn)
    {
        super("agent", null, nextBtn, null);
        
        CellConstraints cc = new CellConstraints();

        String header = "Fill in your information:"; // I1bN

        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,5px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 3)+",p:g"), this);
        int row = 1;
        
        builder.add(createLabel(header), cc.xywh(1,row,3,1));row += 2;

        firstNameTxt    = createField(builder, "First Name", true, row);row += 2;
        lastNameTxt     = createField(builder, "Last Name",  true, row);row += 2;
        emailTxt        = createField(builder, "EMail",      true, row);row += 2;
        
        if (DO_DEBUG) // XXX Debug RELEASE
        {
            firstNameTxt.setText("Rod");
            lastNameTxt.setText("Spears");
            emailTxt.setText("rods@ku.edu");
        }
        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties props)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Checks all the textfeilds to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isUIValid());
        }
    }

    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        JTextField[] txtFields = {firstNameTxt, lastNameTxt, emailTxt};
        for (JTextField tf : txtFields)
        {
            if (StringUtils.isEmpty(tf.getText()))
            {
                return false;
            }
        }
        return true;
    }

    // Getters 
    public String getEmail()
    {
        return emailTxt.getText();
    }

    public String getFirstName()
    {
        return firstNameTxt.getText();
    }

    public String getLastName()
    {
        return lastNameTxt.getText();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        return null;
    }
    
    
}


