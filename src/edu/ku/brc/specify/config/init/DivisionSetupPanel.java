/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
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
public class DivisionSetupPanel extends JPanel implements SetupPanelIFace
{
    protected JComboBox cbx;
    protected JButton   nextBtn;
    protected JLabel    divisionLbl;
    protected Division  division       = null;
    protected JList     disciplineList = null;
    protected Properties props         = null;

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public DivisionSetupPanel(final JButton nextBtn)
    {
        super();
        this.nextBtn = nextBtn;
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        cbx = new JComboBox(model);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder bldr = new PanelBuilder(new FormLayout("p,2px,p", "p,10px,p,4px,p"), this);
        bldr.add(UIHelper.createI18NFormLabel("Select a Division"), cc.xy(1, 1)); // I18N
        bldr.add(cbx, cc.xy(3, 1));
        
        bldr.add(UIHelper.createI18NFormLabel("Division"), cc.xy(1, 3));
        bldr.add(divisionLbl = UIHelper.createLabel(""), cc.xy(3, 3));
        
        disciplineList = new JList(new DefaultListModel());
        JScrollPane sp = UIHelper.createScrollPane(disciplineList);
        bldr.add(UIHelper.createI18NFormLabel("Disciplines"), cc.xy(1, 5));
        bldr.add(sp, cc.xy(3, 5));
    
        
        model.addElement("None");
        model.addElement("Create New Division");
        
        DataProviderSessionIFace localSession = null;
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            
            List<Division> divisions = (List<Division>)localSession.getDataList("FROM Division");
            if (divisions != null)
            {
                for (Division d : divisions)
                {
                    model.addElement(d);
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DivisionSetupPanel.class, ex);
            ex.printStackTrace();
        } finally
        {
            if (localSession != null)
            {
                localSession.close();
            }
        }
        
        cbx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                division = null;
                nextBtn.setEnabled(false);
                
                if (cbx.getSelectedIndex() == 1)
                {
                    createNewDivision();
                    
                } else if (cbx.getSelectedIndex() > 1)
                {
                    division = (Division)cbx.getSelectedItem();
                    divisionLbl.setText(division.getName());
                    nextBtn.setEnabled(true);
                }
                fillDisciplineList();
            }
        });
    }
    
    /**
     * Fills the JList with the Disciplines that the Division has.
     */
    protected void fillDisciplineList()
    {
        DefaultListModel model = (DefaultListModel)disciplineList.getModel();
        model.clear();
        
        if (division != null)
        {
            for (String title : division.getDisciplineList())
            {
                model.addElement(title);
            }
            props.put("division", division);
        } else
        {
            props.remove("division");
        }
    }
    
    /**
     * 
     */
    protected void createNewDivision()
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(Division.getClassTableId());
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)UIRegistry.getMostRecentWindow(),
                null,
                "Division",
                null,
                tableInfo.getTitle(),
                null,
                Division.class.getName(),
                "id",
                true,
                MultiView.HIDE_SAVE_BTN);
        
        division = new Division();
        division.initialize();
        
        dlg.setData(division);
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            dlg.getMultiView().getDataFromUI();
            divisionLbl.setText(division.getName());
            nextBtn.setEnabled(true);
            
        } else
        {
            divisionLbl.setText("");
            cbx.setSelectedIndex(0);
            nextBtn.setEnabled(false);
        }
        fillDisciplineList();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingNext()
     */
    @Override
    public void doingNext()
    {
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
        return "DivisionSetup";
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
    public void getValues(@SuppressWarnings("hiding") Properties props)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return division != null;
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
