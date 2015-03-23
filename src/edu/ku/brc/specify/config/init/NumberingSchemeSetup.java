/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 30, 2008
 *
 */
public class NumberingSchemeSetup extends JPanel implements SetupPanelIFace
{
    protected JComboBox            cbx;
    protected JButton              nextBtn;
    protected JLabel               numSchemeLbl;
    protected AutoNumberingScheme  numScheme = null;
    protected JTextField           divisionTxt;
    protected JTextField           disciplineTxt;
    protected Properties           props      = null;
    

    /*
      Localization:
            DB_NAME=Database Name
            HOST_NAME=Host Name
            DSP_TYPE=DisciplineType
            DRIVER=Driver
    */
    /**
     * 
     */
    public NumberingSchemeSetup(final JButton nextBtn)
    {
        super();
        this.nextBtn = nextBtn;
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        cbx = new JComboBox(model);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder bldr = new PanelBuilder(new FormLayout("p,2px,p", "p,4px,p,10px,p,4px,p"), this);
        
        int y = 1;
        bldr.add(UIHelper.createI18NFormLabel("DIVISION"), cc.xy(1, y));
        bldr.add(divisionTxt = UIHelper.createTextField(""), cc.xy(3, y));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel("DISCILPINE"), cc.xy(1, y));
        bldr.add(disciplineTxt = UIHelper.createTextField(), cc.xy(3, y));
        y += 2;

        bldr.add(UIHelper.createI18NFormLabel("SEL_NUM_SCHEME"), cc.xy(1, y));
        bldr.add(cbx, cc.xy(3, y));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel("AutoNumberingScheme"), cc.xy(1, y));
        bldr.add(numSchemeLbl = UIHelper.createLabel(""), cc.xy(3, y));
        y += 2;
        
        ViewFactory.changeTextFieldUIForDisplay(divisionTxt, false);
        ViewFactory.changeTextFieldUIForDisplay(disciplineTxt, false);
        
        load(model);
        
        cbx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                numScheme = null;
                nextBtn.setEnabled(false);
                
                if (cbx.getSelectedIndex() == 1)
                {
                    createNewAutoNumberingScheme();
                    
                } else if (cbx.getSelectedIndex() > 1)
                {
                    numScheme = (AutoNumberingScheme)cbx.getSelectedItem();
                    numSchemeLbl.setText(numScheme.getSchemeName());
                    nextBtn.setEnabled(true);
                    props.put("numScheme", numScheme);
                } else
                {
                    props.remove("numScheme");
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#aboutToLeave(java.beans.PropertyChangeListener)
     */
    @Override
    public void aboutToLeave()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#enablePreviousBtn()
     */
    @Override
    public boolean enablePreviousBtn()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return null;
    }

    /**
     * @param model
     */
    @SuppressWarnings("unchecked")
    protected void load(final DefaultComboBoxModel model)
    {
        model.addElement(UIRegistry.getResourceString("NONE"));
        model.addElement("CREATE_NEW_NUM_SCHEME");
        DataProviderSessionIFace localSession = null;
        
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            
            List<AutoNumberingScheme> numSchemes = (List<AutoNumberingScheme>)localSession.getDataList("FROM AutoNumberingScheme");
            if (numSchemes != null)
            {
                for (AutoNumberingScheme d : numSchemes)
                {
                    model.addElement(d);
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NumberingSchemeSetup.class, ex);
            ex.printStackTrace();
        } finally
        {
            if (localSession != null)
            {
                localSession.close();
            }
        }
    }
    
    /**
     * 
     */
    protected void createNewAutoNumberingScheme()
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(AutoNumberingScheme.getClassTableId());
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)UIRegistry.getMostRecentWindow(),
                null,
                "CatAutoNumberingScheme",
                null,
                tableInfo.getTitle(),
                null,
                AutoNumberingScheme.class.getName(),
                "id",
                true,
                MultiView.HIDE_SAVE_BTN);
        
        numScheme = new AutoNumberingScheme();
        numScheme.initialize();
        
        dlg.setData(numScheme);
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            dlg.getMultiView().getDataFromUI();
            numSchemeLbl.setText(numScheme.getSchemeName());
            nextBtn.setEnabled(true);
            props.put("numScheme", numScheme);
            
        } else
        {
            numSchemeLbl.setText("");
            cbx.setSelectedIndex(0);
            nextBtn.setEnabled(false);
            props.remove("numScheme");
        }
    }
    
    /**
     * @param d1
     * @param d2
     * @return
     */
    protected boolean isSameDiscipline(final Discipline d1, final Discipline d2)
    {
        return d1 != null && 
               d1.getId() != null && 
               d2 != null && 
               d2.getId() != null && 
               d1.getId().equals(d2.getId());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingNext()
     */
    @Override
    public void doingNext()
    {
        Division div = (Division)props.get("division");
        divisionTxt.setText(div != null ? div.toString() : "");
        
        Discipline dsp = (Discipline)props.get("discipline");
        disciplineTxt.setText(div != null ? dsp.toString() : "");
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingPrev()
     */
    @Override
    public void doingPrev()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getPanelName()
     */
    @Override
    public String getPanelName()
    {
        return "AutoNumberingSchemeSetup";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties propsArg)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return numScheme != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#setValues(java.util.Properties)
     */
    @Override
    public void setValues(Properties values)
    {
        this.props = values;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
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
