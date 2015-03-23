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
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.specify.config.init.DataBuilder.createPickList;
import static edu.ku.brc.ui.UIHelper.createComboBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.datamodel.PickList;
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
public class DisciplineBasedPanel extends JPanel implements LocalizableIOIFace, 
                                                            PropertyChangeListener
{
    protected JComboBox disciplineCBX = createComboBox();
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
    protected DisciplineType                disciplineType           = null;
    protected WebLinkMgr                    webLinkMgrCache          = null;
    
    /**
     * 
     */
    public DisciplineBasedPanel(final SchemaLocalizerPanel schemaPanel,
                                final WebLinkMgr webLinkMgrCache)
    {
        this.schemaPanel = schemaPanel;
        this.webLinkMgrCache = webLinkMgrCache;
        
        for (DisciplineType disp : DisciplineType.getDisciplineList())
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
                
                disciplineType = (DisciplineType)disciplineCBX.getSelectedItem();
                
                fillWithDisciplineItems();
                
                if (fieldPanel != null)
                {
                    fieldPanel.setDisciplineType(disciplineType);
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
        if (fieldPanel != null)
        {
            fieldPanel.getAllDataFromUI();
        }
        
        if (currentBaselineContainer != null)
        {
            Set<SpLocaleContainerItem> cItems = currentBaselineContainer.getDisciplineItems(disciplineType.getName());
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
        
        fieldPanel = new FieldItemPanel(schemaPanel, webLinkMgrCache, true, true, true, null);
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
    

    /**
     * @param container
     * @param jListContainerItem
     */
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
        
        fieldPanel.setDisciplineType(disciplineType);
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
        
        if (currentBaselineContainer != null && currentBaselineContainer.hasDiscipline(disciplineType.getName()))
        {
            items.addAll(currentBaselineContainer.getDisciplineItems(disciplineType.getName()));
            
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
    
    /**
     * @param fromItems
     * @param toItems
     */
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

        fieldPanel.setDisciplineType(disciplineType);
        fieldPanel.setContainer(currentBaselineContainer, jlistContainer);
    }
    
    /**
     * 
     */
    protected void delItem()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#copyLocale(edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFaceListener, java.util.Locale, java.util.Locale, java.beans.PropertyChangeListener)
     */
    @Override
    public void copyLocale(final LocalizableIOIFaceListener lcl, final Locale src, final Locale dst, final PropertyChangeListener pcl)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#createResourceFiles()
     */
    @Override
    public boolean createResourceFiles()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    @Override
    public boolean didModelChangeDuringLoad()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#exportToDirectory(java.io.File)
     */
    @Override
    public boolean exportToDirectory(File expportFile)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem, edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFaceListener)
     */
    @Override
    public LocalizableContainerIFace getContainer(LocalizableJListItem item, final LocalizableIOIFaceListener l)
    {
        if (l != null)
        {
            l.containterRetrieved(currentBaselineContainer);
        }
        return currentBaselineContainer;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    @Override
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem container)
    {
        return listItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace, edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public LocalizableItemIFace getItem(LocalizableContainerIFace container, LocalizableJListItem item)
    {
        return items.get(listItems.indexOf(item));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getLocalesInUse()
     */
    @Override
    public Vector<Locale> getLocalesInUse()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#isLocaleInUse(java.util.Locale)
     */
    @Override
    public boolean isLocaleInUse(Locale locale)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#load(boolean)
     */
    @Override
    public boolean load(final boolean useCurrentLocaleOnly)
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#containerChanged(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace)
     */
    @Override
    public void containerChanged(LocalizableContainerIFace container)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    @Override
    public boolean save()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#hasUpdatablePickLists()
     */
    @Override
    public boolean hasUpdatablePickLists()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#shouldIncludeAppTables()
     */
    @Override
    public boolean shouldIncludeAppTables()
    {
        return true;
    }

    /**
     * Helper to get the common or discipline specific PickLists.
     * @param pickLists the list
     * @param disciplineName null or the discipline (directory) name
     */
    private void getPickLists(final List<PickList> pickLists, final String disciplineName)
    {
        List<BldrPickList> bdlrPickLists = DataBuilder.getBldrPickLists(disciplineName != null ? disciplineName : "common");
        
        for (BldrPickList pl : bdlrPickLists)
        {
            PickList pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                               pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                               pl.getSizeLimit(), pl.getIsSystem(), pl.getSortType(), null);
            for (BldrPickListItem item : pl.getItems())
            {
                pickList.addItem(item.getTitle(), item.getValue());
            }
            pickLists.add(pickList);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getPickLists(java.lang.String)
     */
    @Override
    public List<PickList> getPickLists(final String disciplineName)
    {
        List<PickList> pickLists = new Vector<PickList>();
        getPickLists(pickLists, null);
        getPickLists(pickLists, disciplineName);
        return pickLists;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#exportSingleLanguageToDirectory(java.io.File, java.util.Locale)
     */
    @Override
    public boolean exportSingleLanguageToDirectory(File expportFile, Locale locale)
    {
        throw new RuntimeException("Not Impl");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#hasChanged()
     */
    @Override
    public boolean hasChanged()
    {
        throw new RuntimeException("Not Impl");
    }
}
