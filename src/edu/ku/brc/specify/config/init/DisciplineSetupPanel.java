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
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
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
public class DisciplineSetupPanel extends JPanel implements SetupPanelIFace
{
    protected JComboBox  cbx;
    protected JButton    nextBtn;
    protected JTextField disciplineTxt;
    protected JTextField divisionTxt;
    
    protected Division   division   = null;
    protected Discipline discipline = null;
    protected Properties props      = null;
    protected Vector<Discipline> dList = null;

    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public DisciplineSetupPanel(final JButton nextBtn)
    {
        super();
        this.nextBtn = nextBtn;
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        cbx = new JComboBox(model);
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder bldr = new PanelBuilder(new FormLayout("p,2px,p", "p,10px,p,4px,p"), this);
        
        int y = 1;
        bldr.add(UIHelper.createI18NFormLabel("Division"), cc.xy(1, y));
        bldr.add(divisionTxt = UIHelper.createTextField(""), cc.xy(3, y));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel("Select a Discipline"), cc.xy(1, y));
        bldr.add(cbx, cc.xy(3, y));
        y += 2;
        
        bldr.add(UIHelper.createI18NFormLabel("Discipline"), cc.xy(1, y));
        bldr.add(disciplineTxt = UIHelper.createTextField(), cc.xy(3, y));
        y += 2;
        
        ViewFactory.changeTextFieldUIForDisplay(divisionTxt, false);
        ViewFactory.changeTextFieldUIForDisplay(disciplineTxt, false);
        
        cbx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                discipline = null;
                nextBtn.setEnabled(false);
                
                if (cbx.getSelectedIndex() == 1)
                {
                    createNewDiscipline();
                    
                } else if (cbx.getSelectedIndex() > 1)
                {
                    discipline = (Discipline)cbx.getSelectedItem();
                    disciplineTxt.setText(discipline.getTitle());
                    props.put("discipline", discipline);
                    nextBtn.setEnabled(true);
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    protected void load(final Division div)
    {
        if (dList == null)
        {
            dList = new Vector<Discipline>();
            if (div.getId() != null)
            {
                DataProviderSessionIFace localSession = null;
                try
                {
                    localSession = DataProviderFactory.getInstance().createSession();
                    
                    String sql = "SELECT dsp FROM Discipline dsp INNER JOIN dsp.division dv WHERE dv.id = "+div.getId();
                    List<Discipline> disciplines = (List<Discipline>)localSession.getDataList(sql);
                    if (disciplines != null)
                    {
                        for (Discipline d : disciplines)
                        {
                            dList.add(d);
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
        } else
        {
            dList = new Vector<Discipline>();
        }
    }
    
    /**
     * 
     */
    protected void createNewDiscipline()
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(Discipline.getClassTableId());
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)UIRegistry.getMostRecentWindow(),
                null,
                "Discipline",
                null,
                tableInfo.getTitle(),
                null,
                Discipline.class.getName(),
                "id",
                true,
                MultiView.HIDE_SAVE_BTN);
        
        discipline = new Discipline();
        discipline.initialize();
        
        dlg.setData(discipline);
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            dlg.getMultiView().getDataFromUI();
            disciplineTxt.setText(discipline.getTitle());
            nextBtn.setEnabled(true);
            props.put("discipline", discipline);
            
        } else
        {
            disciplineTxt.setText("");
            cbx.setSelectedIndex(0);
            nextBtn.setEnabled(false);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingNext()
     */
    @Override
    public void doingNext()
    {
        Division div = (Division)props.get("division");
        divisionTxt.setText(div != null ? div.toString() : "");
        
        if (!isSameDiv(div, division))
        {
            props.remove("discipline");
            props.remove("numScheme");
            
            DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
            cbx.removeAllItems();
            model.addElement("None");
            model.addElement("Create New Discipline");
    
            dList = null;
            load(div);
            
            if (dList != null)
            {
                for (Discipline d : dList)
                {
                    model.addElement(d);
                }
            }
            division = div;
            disciplineTxt.setText("");
        }
    }
    
    /**
     * @param d1
     * @param d2
     * @return
     */
    protected boolean isSameDiv(final Division d1, final Division d2)
    {
        return d1 != null && 
               d1.getId() != null && 
               d2 != null && 
               d2.getId() != null && 
               d1.getId().equals(d2.getId());
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
        return "DisciplineSetup";
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
        if (discipline != null)
        {
            propsArg.put("discipline", discipline);
        } else
        {
            propsArg.remove("discipline");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return discipline != null;
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

}
