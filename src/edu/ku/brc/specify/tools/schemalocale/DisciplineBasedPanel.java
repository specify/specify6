/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 2, 2007
 *
 */
public class DisciplineBasedPanel extends JPanel implements LocalizableIOIFace, PropertyChangeListener
{
    protected JComboBox disciplineCBX = new JComboBox();
    protected JButton   addBtn;
    protected JButton   delBtn;
    
    protected final SchemaLocalizerPanel    schemaPanel;
    protected FieldItemPanel                fieldPanel;
    
    protected Vector<SpLocaleContainerItem> items                    = new Vector<SpLocaleContainerItem>();
    protected Vector<LocalizableJListItem>  listItems                = new Vector<LocalizableJListItem>();
    
    protected DisciplineBasedContainer      currentBaselineContainer = null;
    protected LocalizableJListItem          jlistContainer           = null;
    protected SpLocaleContainerItem         currentBaselineItem      = null;
    protected SpLocaleContainerItem         currentItem              = null;
    protected Discipline                    discipline               = null;
    
    /**
     * 
     */
    public DisciplineBasedPanel(final SchemaLocalizerPanel schemaPanel)
    {
        this.schemaPanel = schemaPanel;
        
        for (Discipline disp : Discipline.getDisciplineList())
        {
            if (disp.getType() == 0)
            {
                disciplineCBX.addItem(disp);
            }
        }
        
        disciplineCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                getDataFromUI();
                
                discipline = (Discipline)disciplineCBX.getSelectedItem();
                
                fillWithDisciplineItems();
                
                if (fieldPanel != null)
                {
                    fieldPanel.setContainer(currentBaselineContainer, jlistContainer);
                }
                
                updateBtnUI();
            }
        });
        
        disciplineCBX.setSelectedIndex(0);
        
        createUI();
    }
    
    /**
     * 
     */
    public void getDataFromUI()
    {
        if (currentBaselineContainer != null)
        {
            Set<SpLocaleContainerItem> cItems = currentBaselineContainer.getDisciplineItems(discipline.getName());
            cItems.clear();
            cItems.addAll(items);
        }
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        CellConstraints cc = new CellConstraints();
        
        addBtn = UIHelper.createIconBtn("DownArrow", "", new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                addItem();
            }
            
        });
        
        delBtn = UIHelper.createIconBtn("UpArrow", "", new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                delItem();
            }
            
        });
        
        fieldPanel = new FieldItemPanel(schemaPanel, true, true, true, null);
        fieldPanel.setLocalizableIO(this);
        fieldPanel.setStatusBar(schemaPanel.getStatusBar());
        
        PanelBuilder arrowPanel = new PanelBuilder(new FormLayout("f:p:g, p, 2px, p, f:p:g", "p"));
        arrowPanel.add(addBtn, cc.xy(2, 1));
        arrowPanel.add(delBtn, cc.xy(4, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p, 2px, f:p:g", "p,2px,p,2px,p,2px,p"), this);
        pb.add(arrowPanel.getPanel(),  cc.xy(1, 1));
        pb.addSeparator("Disciplines", cc.xywh(1, 3, 3, 1));
        pb.add(disciplineCBX,          cc.xy(1, 5));
        pb.add(fieldPanel,             cc.xywh(1, 7, 3, 1));

    }
    

    public void set(final DisciplineBasedContainer container,
                    final LocalizableJListItem     jListContainerItem)
    {
        if (currentBaselineContainer != null)
        {
            getDataFromUI();
        }
        
        currentBaselineContainer = container;
        currentBaselineItem      = null;
        jlistContainer           = jListContainerItem;
        
        fillWithDisciplineItems();
        
        fieldPanel.setContainer(container, jListContainerItem);
        
        updateBtnUI();
        
    }
    
    public void setItem(final SpLocaleContainerItem currBaselineItem)
    {
        currentBaselineItem = currBaselineItem;
        
        updateBtnUI();
    }
    
    /**
     * 
     */
    protected void fillWithDisciplineItems()
    {
        items.clear();
        listItems.clear();
        
        if (currentBaselineContainer != null && currentBaselineContainer.hasDiscipline(discipline.getName()))
        {
            items.addAll(currentBaselineContainer.getDisciplineItems(discipline.getName()));
            
            Collections.sort(items);
            
            for (SpLocaleContainerItem item : items)
            {
                listItems.add(new LocalizableJListItem(item.getName(), item.getId(), null));
            }
        }
    }
    
    /**
     * 
     */
    protected void updateBtnUI()
    {
        if (addBtn != null)
        {
            boolean inListAlready = false;
            if (currentBaselineItem != null)
            {
                for (SpLocaleContainerItem item : items)
                {
                    if (item.getName().equals(currentBaselineItem.getName()))
                    {
                        inListAlready = true;
                        break;
                    }
                }
            }
            addBtn.setEnabled(currentBaselineItem != null && !inListAlready);
            delBtn.setEnabled(currentItem  != null);
        }
    }
    
    protected void copyItems(Set<SpLocaleItemStr> fromItems, Set<SpLocaleItemStr> toItems)
    {
        for (SpLocaleItemStr str : fromItems)
        {
            SpLocaleItemStr newStr = new SpLocaleItemStr();
            newStr.initialize();
            newStr.setCountry(str.getCountry());
            newStr.setLanguage(str.getLanguage());
            newStr.setVariant(str.getVariant());
            newStr.setText(str.getText());
            toItems.add(newStr);
        }
    }
    
    /**
     * 
     */
    protected void addItem()
    {
        SpLocaleContainerItem item = new SpLocaleContainerItem();
        item.initialize();
        
        item.setName(currentBaselineItem.getName());
        item.setType(currentBaselineItem.getType());
        
        copyItems(currentBaselineItem.getNames(), item.getNames());
        copyItems(currentBaselineItem.getDescs(), item.getDescs());
        
        items.add(item);
        Collections.sort(items);
        listItems.insertElementAt(new LocalizableJListItem(item.getName(), item.getId(), null), items.indexOf(item));
        
        fieldPanel.setContainer(currentBaselineContainer, jlistContainer);
    }
    
    /**
     * 
     */
    protected void delItem()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#copyLocale(java.util.Locale, java.util.Locale)
     */
    public void copyLocale(Locale src, Locale dst)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#createResourceFiles()
     */
    public boolean createResourceFiles()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    public boolean didModelChangeDuringLoad()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#exportToDirectory(java.io.File)
     */
    public boolean exportToDirectory(File expportFile)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public LocalizableContainerIFace getContainer(LocalizableJListItem item)
    {
        return currentBaselineContainer;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem container)
    {
        return listItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace, edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public LocalizableItemIFace getItem(LocalizableContainerIFace container, LocalizableJListItem item)
    {
        return items.get(listItems.indexOf(item));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getLocalesInUse()
     */
    public Vector<Locale> getLocalesInUse()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#isLocaleInUse(java.util.Locale)
     */
    public boolean isLocaleInUse(Locale locale)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#load()
     */
    public boolean load()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#realize(edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace)
     */
    public LocalizableItemIFace realize(LocalizableItemIFace item)
    {
        return item;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    public boolean save()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        
    }
}
