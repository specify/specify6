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
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;

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
    protected DatabasePanel                    dbPanel;
    protected Hashtable<String, Integer>       rankHash;
    
    /**
     * 
     */
    public TaxonTreeDefPanel(final JButton nextBtn, 
                             final DatabasePanel dbPanel)
    {
        super("TaxonTreeDef", nextBtn);
        setLayout(new BorderLayout());
        
        rankHash = new Hashtable<String, Integer>();
        
        this.dbPanel = dbPanel;
        
        add(UIHelper.createI18NLabel("TAXONTREEDEF"), BorderLayout.NORTH);
        
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
                    String  name = XMLHelper.getAttr(level, "name", null);
                    int     rank = XMLHelper.getAttr(level, "rank", -1);
                    if (rank > -1)
                    {
                        names.addElement(name);
                        rankHash.put(name, rank);
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            list = new ToggleButtonChooserPanel<String>(names, ToggleButtonChooserPanel.Type.Checkbox);
            list.setUseScrollPane(true);
            list.setAddSelectAll(true);
            list.createUI();
            
            for (JToggleButton btn : list.getButtons())
            {
                int rank = rankHash.get(btn.getText());
                boolean isStd = TaxonTreeDef.isStdRequiredLevel(rank) || rank == 0;
                btn.setSelected(isStd);
                btn.setEnabled(!isStd);
            }

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
        StringBuilder sb = new StringBuilder();
        for (JToggleButton btn : list.getButtons())
        {
            if (btn.isSelected())
            {
                sb.append(btn.getText());
                sb.append(',');
                sb.append(rankHash.get(btn.getText()));
                sb.append(',');
            }
        }
        // chomp
        props.put("taxontreedefs", StringUtils.chomp(sb.toString()));
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
