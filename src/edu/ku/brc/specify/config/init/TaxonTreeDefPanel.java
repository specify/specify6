/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.ui.ToggleButtonChooserPanel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 7, 2009
 *
 */
public class TaxonTreeDefPanel extends BaseSetupPanel implements SetupPanelIFace
{
    protected ToggleButtonChooserPanel<String> list;
    protected DatabasePanel dbPanel;
    
    /**
     * 
     */
    public TaxonTreeDefPanel(final JButton nextBtn, 
                             final DatabasePanel dbPanel)
    {
        super("TaxonTreeDef", nextBtn);
        setLayout(new BorderLayout());
        
        this.dbPanel = dbPanel;
        
        DisciplineType  dType  = DisciplineType.getDiscipline(dbPanel.getDisciplineType().getDisciplineType());
        File            file   = XMLHelper.getConfigDir(dType.getName()+ File.separator + "taxon_init.xml");
        if (file.exists())
        {
            Vector<String> names = new Vector<String>();
            try
            {
                Element root = XMLHelper.readFileToDOM4J(file);
                for (Object levelObj : root.selectNodes("/tree/treedef/level"))
                {
                    Element level = (Element)levelObj;
                    String name = XMLHelper.getAttr(level, "name", null);
                    names.addElement(name);
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            
            list = new ToggleButtonChooserPanel<String>(names, ToggleButtonChooserPanel.Type.Checkbox);
            list.setUseScrollPane(true);
            list.setAddSelectAll(true);
            list.createUI();

            list.setChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    updateBtnUI();
                }
            });
            add(list, BorderLayout.CENTER);
        }
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
        return "Taxon";
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
    public void getValues(Properties props)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return list.hasSelection();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#setValues(java.util.Properties)
     */
    @Override
    public void setValues(Properties values)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
        nextBtn.setEnabled(list.hasSelection());
    }

}
