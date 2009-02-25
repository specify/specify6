/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr.createAutoNumber;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    protected JCheckBox isNumericChk    = createCheckBox(getResourceString("IS_NUM_CHK"));
    protected JComboBox formatterCBX    = createComboBox(new DefaultComboBoxModel());
    protected JLabel    isNumericLbl    = createLabel("");
    
    protected boolean                     doingCatNums;
    protected List<UIFieldFormatterIFace> fmtList;
    protected UIFieldFormatterIFace       newFormatter = null;
    protected int                         newFmtInx    = 0;
    
    /**
     * @param nextBtn
     * @param doingCatNums
     */
    public FormatterPickerPanel(final String panelName, 
                                final JButton nextBtn, 
                                final boolean doingCatNums)
    {
        super(panelName, nextBtn);
        
        this.doingCatNums = doingCatNums;
        
        formatterCBX.addActionListener(new ActionListener() {
            
            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = formatterCBX.getSelectedIndex();
                if (index > 0)
                {
                    UIFieldFormatterIFace fmt = fmtList.get(index-1);
                    if (fmt != null)
                    {
                        isNumericLbl.setText(getResourceString(fmt.isNumeric() ? "YES" : "NO"));
                    }
                }
            }
        });
        
        loadFormatCbx(null);

        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,4px,p,2px,p,f:p:g", "p,10px,p,4px,p"), this);
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
            AutoNumberIFace autoNum = doingCatNums ? createAutoNumber("edu.ku.brc.specify.dbsupport.CollectionAutoNumber", "edu.ku.brc.specify.datamodel.CollectionObject", "catalogNumber") :
                                                     createAutoNumber("edu.ku.brc.af.core.db.AutoNumberGeneric", "edu.ku.brc.specify.datamodel.Accession", "accessionNumber");
            newFormatter.setAutoNumber(autoNum);
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
                if (formatterCBX.getSelectedIndex() == newFmtInx)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            addFieldFormatter();
                        }
                    });
                }
            }
        };
    }
    
    /**
     * 
     */
    protected void loadFormatCbx(final UIFieldFormatterIFace selectedFmt)
    {
        ((DefaultComboBoxModel)formatterCBX.getModel()).removeAllElements();
        
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
            newFmtInx++;
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
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        if (doingCatNums)
        {
            props.put("catnumfmt", newFormatter != null ? newFormatter : formatterCBX.getSelectedItem());
            
        } else if (formatterCBX.getSelectedIndex() > 0)
        {
            props.put("accnumfmt", newFormatter != null ? newFormatter : formatterCBX.getSelectedItem());
        } else
        {
            props.remove("accnumfmt");
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
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
    }
}
