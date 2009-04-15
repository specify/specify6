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

import static edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr.createAutoNumber;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFormatterEditorDlg;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 18, 2008
 *
 */
public class FormatterPickerPanel extends BaseSetupPanel
{
    protected JCheckBox isNumericChk    = UIHelper.createCheckBox(getResourceString("IS_NUM_CHK"));
    protected JComboBox formatterCBX    = createComboBox(new DefaultComboBoxModel());
    protected JLabel    isNumericLbl    = createLabel(" ");
    protected JLabel    patternLbl      = createLabel(" ");
    protected JLabel    autoIncLbl      = createLabel(" ");
    
    protected boolean                     doingCatNums;
    protected List<UIFieldFormatterIFace> fmtList;
    protected UIFieldFormatterIFace       newFormatter = null;
    protected int                         newFmtInx    = 0;
    protected boolean                     wasUsed      = false;
    
    /**
     * @param nextBtn
     * @param doingCatNums
     */
    public FormatterPickerPanel(final String panelName, 
                                final String helpContext,
                                final JButton nextBtn, 
                                final JButton prevBtn, 
                                final boolean doingCatNums)
    {
        super(panelName, helpContext, nextBtn, prevBtn);
        
        this.doingCatNums = doingCatNums;
        
        formatterCBX.addActionListener(createFrmCBXAL());
        
        loadFormatCbx(null);

        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,4px,p,2px,p,f:p:g", "p,10px,p,4px,p,2px,p,2px,p"), this);
        
        int y = 1;
        String label = getResourceString(doingCatNums ? "CHOOSE_FMT_CAT" : "CHOOSE_FMT_ACC");
        pb.add(createLabel(label, SwingConstants.CENTER), cc.xywh(1, y, 6, 1)); 
        y +=2;
        
        JLabel lbl = createI18NFormLabel("FORMAT", SwingConstants.RIGHT);
        lbl.setFont(bold);
        pb.add(lbl, cc.xy(1, y));
        pb.add(formatterCBX, cc.xy(3, y));
        y +=2;

        if (doingCatNums)
        {
            pb.add(createI18NFormLabel("IS_NUM", SwingConstants.RIGHT), cc.xy(1, y));
            pb.add(isNumericLbl, cc.xy(3, y));
            y +=2;
        }
        
        pb.add(createI18NFormLabel("PATTERN", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(patternLbl, cc.xy(3, y));
        y +=2;

        pb.add(createI18NFormLabel("IS_AUTO_INC", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(autoIncLbl, cc.xy(3, y));
        y +=2;

        nextBtn.setEnabled(false);

    }
    
    /**
     * Launches the Field Formatter editor.
     */
    protected void addFieldFormatter()
    {
        DBFieldInfo fieldInfo;
        if (doingCatNums)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
            fieldInfo = ti.getFieldByColumnName("CatalogNumber");
        } else
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(Accession.getClassTableId());
            fieldInfo = ti.getFieldByColumnName("AccessionNumber");
        }
        
        newFormatter = new UIFieldFormatter();
        UIFormatterEditorDlg dlg = new UIFormatterEditorDlg(null, fieldInfo, newFormatter, true, false, UIFieldFormatterMgr.getInstance());
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            if (!newFormatter.isUserInputNeeded())
            {
                AutoNumberIFace autoNum = doingCatNums ? createAutoNumber("edu.ku.brc.specify.dbsupport.CollectionAutoNumber", "edu.ku.brc.specify.datamodel.CollectionObject", "catalogNumber") :
                                                         createAutoNumber("edu.ku.brc.af.core.db.AutoNumberGeneric", "edu.ku.brc.specify.datamodel.Accession", "accessionNumber");
                newFormatter.setAutoNumber(autoNum);
            }
        } else
        {
            newFormatter = null;
        }
        loadFormatCbx(newFormatter);
    }
    
    /**
     * @return
     */
    protected ActionListener createFrmCBXAL()
    {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = formatterCBX.getSelectedIndex();
                if (index > (doingCatNums ? 0 : 1))
                {
                    UIFieldFormatterIFace fmt = fmtList.get(index - (doingCatNums ? 1 : 2));
                    if (fmt != null)
                    {
                        isNumericLbl.setText(getResourceString(fmt.isNumeric() ? "YES" : "NO"));
                        patternLbl.setText(fmt.toPattern());
                        autoIncLbl.setText(getResourceString(fmt.isIncrementer() ? "YES" : "NO"));
                    }
                }
                
                if (formatterCBX.getSelectedIndex() == newFmtInx)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            addFieldFormatter();
                            updateBtnUI();
                        }
                    });
                }
                updateBtnUI();
            }
        };
    }
    
    /**
     * 
     */
    protected void loadFormatCbx(final UIFieldFormatterIFace selectedFmt)
    {
        ((DefaultComboBoxModel)formatterCBX.getModel()).removeAllElements();
        
        //Vector<ActionListener> alList = new Vector<ActionListener>();
        //alList.addAll(formatterCBX.getActionListeners());
        
        //Collections.addAll(alList, formatterCBX.getActionListeners());
        
        for (ActionListener al : formatterCBX.getActionListeners())
        {
            formatterCBX.removeActionListener(al);
        }
        
        fmtList = new Vector<UIFieldFormatterIFace>(UIFieldFormatterMgr.getInstance().getFormatterList(doingCatNums ? CollectionObject.class : Accession.class));
        if (newFormatter != null)
        {
            fmtList.add(newFormatter);
        }
        
        Collections.sort(fmtList, new Comparator<UIFieldFormatterIFace>() {
            @Override
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        if (!doingCatNums)
        {
            ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(getResourceString("NONE"));
            newFmtInx = 1;
        }
        ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(getResourceString("CREATE"));

        for (UIFieldFormatterIFace fmt : fmtList)
        {
            ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(fmt.getName());
        }

        if (selectedFmt != null)
        {
            formatterCBX.setSelectedItem(selectedFmt.getName());
            nextBtn.setEnabled(true);
        } else
        {
            formatterCBX.setSelectedIndex(-1);
            nextBtn.setEnabled(false);
        }
        
        formatterCBX.addActionListener(createFrmCBXAL());
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    public void setVisible(final boolean vis)
    {
        super.setVisible(vis);
        wasUsed = true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        if (wasUsed)
        {
            if (doingCatNums)
            {
                props.put("catnumfmt", newFormatter != null ? newFormatter : formatterCBX.getSelectedItem());
                
            } else if (formatterCBX.getSelectedIndex() > 1)
            {
                props.put("accnumfmt", newFormatter != null ? newFormatter : formatterCBX.getSelectedItem());
            } else
            {
                props.remove("accnumfmt");
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return !doingCatNums || formatterCBX.getSelectedIndex() > 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
        int     inx     = formatterCBX.getSelectedIndex();
        boolean enable;
        if (doingCatNums)
        {
            enable = inx > 0;
        } else
        {
            enable = inx != 1;
        }
        nextBtn.setEnabled(enable);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        updateBtnUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingPrev()
     */
    @Override
    public void doingPrev()
    {
        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        String lbl   = getResourceString(doingCatNums ? "CollectionObject" : "Accession") + " " + getResourceString("Formatter");
        String value = formatterCBX.getSelectedIndex() == -1 ? "" : formatterCBX.getSelectedItem().toString(); 
        list.add(new Pair<String, String>(lbl, value));
        return list;
    }
}
