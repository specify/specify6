/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 30, 2008
 *
 */
public class NumberingSchemeSetupDlg extends CustomDialog
{
    protected JComboBox            cbx;
    protected JTextField           numSchemeTxt;
    protected AutoNumberingScheme  numScheme = null;
    protected JTextField           divisionTxt;
    protected JTextField           disciplineTxt;
    protected Division             division; 
    protected Discipline           discipline;
    protected Collection           collection;
    
    protected String               ansTitle;
    
    /**
     * 
     */
    public NumberingSchemeSetupDlg(final Division   division, 
                                   final Discipline discipline,
                                   final Collection collection)
    {
        super((Frame)UIRegistry.getMostRecentWindow(), "", true, OKHELP, null);
        
        this.division   = division;
        this.discipline = discipline;
        this.collection = collection;
        
        okLabel = UIRegistry.getResourceString("CLOSE");
        
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        DBTableInfo divTblInfo = DBTableIdMgr.getInstance().getInfoById(Division.getClassTableId());
        DBTableInfo dspTblInfo = DBTableIdMgr.getInstance().getInfoById(Discipline.getClassTableId());
        
        ansTitle = DBTableIdMgr.getInstance().getInfoById(AutoNumberingScheme.getClassTableId()).getTitle();
        setTitle(ansTitle);
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        cbx = new JComboBox(model);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder bldr = new PanelBuilder(new FormLayout("p,2px,p", "p,4px,p,10px,p,4px,p,4px,p:g,20px,p"));
        
        int y = 1;
        bldr.add(UIHelper.createI18NFormLabel(divTblInfo.getTitle()), cc.xy(1, y));
        bldr.add(divisionTxt = UIHelper.createTextField(""), cc.xy(3, y));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel(dspTblInfo.getTitle()), cc.xy(1, y));
        bldr.add(disciplineTxt = UIHelper.createTextField(), cc.xy(3, y));
        y += 2;

        bldr.add(UIHelper.createI18NFormLabel("SEL_NUM_SCHEME"), cc.xy(1, y));
        bldr.add(cbx, cc.xy(3, y));
        y += 2;
        
        bldr.addSeparator(" ", cc.xyw(1, y, 3));
        y += 2;
        
        bldr.add(UIHelper.createI18NLabel("NUM_SCHEME_DESC", SwingConstants.LEFT), cc.xyw(1, y, 3));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel(ansTitle), cc.xy(1, y));
        bldr.add(numSchemeTxt = UIHelper.createTextField(""), cc.xy(3, y));
        y += 2;
        
        ViewFactory.changeTextFieldUIForDisplay(divisionTxt, false);
        ViewFactory.changeTextFieldUIForDisplay(disciplineTxt, false);
        ViewFactory.changeTextFieldUIForDisplay(numSchemeTxt, false);
        
        divisionTxt.setText(division.toString());
        disciplineTxt.setText(discipline.toString());
        
        load(model);
        
        cbx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                numScheme = null;
                okBtn.setEnabled(false);
                
                if (cbx.getSelectedIndex() == 1)
                {
                    createNewAutoNumberingScheme();
                    
                } else if (cbx.getSelectedIndex() > 1)
                {
                    numScheme = (AutoNumberingScheme)cbx.getSelectedItem();
                    numSchemeTxt.setText(numScheme.getSchemeName());
                    okBtn.setEnabled(true);
                }
            }
        });
        
        bldr.setDefaultDialogBorder();
        contentPanel = bldr.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        okBtn.setEnabled(false);
        
        pack();
    }
    
    /**
     * @param model
     */
    @SuppressWarnings("unchecked")
    protected void load(final DefaultComboBoxModel model)
    {
        model.addElement(UIRegistry.getResourceString("NONE"));
        model.addElement(UIRegistry.getLocalizedMessage("NewRecordTT", ansTitle));
        
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
            numSchemeTxt.setText(numScheme.getSchemeName());
            okBtn.setEnabled(true);
            
        } else
        {
            numSchemeTxt.setText("");
            cbx.setSelectedIndex(0);
            okBtn.setEnabled(false);
            numScheme = null;
        }
    }
    /**
     * @return the numScheme
     */
    public AutoNumberingScheme getNumScheme()
    {
        return numScheme;
    }
    
}
