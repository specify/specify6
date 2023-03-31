/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.db;

import java.util.Collections;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.specify.datamodel.PickList;
/**
 * This implements the PickListDBAdapterIFaceand enables a PickList to have it's contents
 * come from a Pref with a scomma separated list of values, instead of from the database.
 * This is certainly a candidate for be pulled out and made a "full class".
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 10, 2006
 *
 */
public class PropertiesPickListAdapter implements PickListDBAdapterIFace
{
    // Static Data Memebers
    protected static final Logger log                = Logger.getLogger(PropertiesPickListAdapter.class);
    protected static PickListItemIFace searchablePLI = null;
    
    // Data Memebers        
    protected String                    prefName;
    protected String                    prefSelectedName;
    protected ValComboBox               comboBox;
    protected boolean                   savePickList = true;
    
    protected Vector<PickListItemIFace> items    = new Vector<PickListItemIFace>(); // Make this Vector because the combobox can use it directly
    protected PickListIFace             pickList = null;
    protected boolean                   doAutoSaveOnAdd = true;
    protected Vector<ChangeListener>    changeListeners = new Vector<ChangeListener>();

    /**
     * Default Constructor.
     */
    public PropertiesPickListAdapter(final String prefName)
    {
        super();

        this.prefName = prefName;

        this.prefSelectedName = prefName + "_selected"; //$NON-NLS-1$

        searchablePLI = PickListDBAdapterFactory.getInstance().createPickListItem(); // used for binary searches
        
        pickList = PickListDBAdapterFactory.getInstance().createPickList();
        
        if (savePickList)
        {
            readData();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#setAutoSaveOnAdd(boolean)
     */
    @Override
    public void setAutoSaveOnAdd(boolean doAutoSave)
    {
        doAutoSaveOnAdd = doAutoSave;
    }
    
    /**
     * Sets the combobox
     * @param comboBox the conboxbox being operated on
     */
    public void setComboBox(final ValComboBox comboBox)
    {
        this.comboBox = comboBox;
    }

    /**
     * Sets whether the picklist should be save or not
     * @param savePickList true - save, false don't save
     */
    public void setSavePickList(boolean savePickList)
    {
        this.savePickList = savePickList;
    }

    /**
     * Builds the PickList from a prefs string
     */
    protected void readData()
    {
        String valuesStr = AppPreferences.getLocalPrefs().get(prefName, ""); //$NON-NLS-1$
        //log.debug("["+prefName+"]["+valuesStr+"]");

        if (StringUtils.isNotEmpty(valuesStr))
        {
            String[] strs = StringUtils.split(valuesStr, ","); //$NON-NLS-1$
            if (strs.length > 0)
            {
                for (int i=0;i<strs.length;i++)
                {
                    PickListItemIFace pli = pickList.addItem(strs[i], strs[i]);
                    items.add(pli);
                }
            }
            // Always keep the list sorted
            Collections.sort(items);
        }
    }

    
    /**
     * Gets the PickList Item from the Database.
     * @param name the name of the picklist to get
     * @return the picklist
     */
    protected PickList getPickList(@SuppressWarnings("unused") final String nameArg) //$NON-NLS-1$
    {
        throw new RuntimeException("Don't call this!"); //$NON-NLS-1$
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getPickList()
     */
    public PickListIFace getPickList()
    {
        return pickList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getList()
     */
    public Vector<PickListItemIFace> getList()
    {
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getItem(int)
     */
    public PickListItemIFace getItem(final int index)
    {
        return items.get(index);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#addItem(java.lang.String, java.lang.String)
     */
    public PickListItemIFace addItem(final String title, final String value)
    {
        // this should never happen!
        if (pickList.getReadOnly())
        {
            throw new RuntimeException("Trying to add an item to a readonly picklist ["+pickList.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        int     sizeLimit = 50; // arbitrary size could be a pref (XXX PREF)
        Integer sizeLimitInt = pickList.getSizeLimit();
        if (sizeLimitInt != null)
        {
            sizeLimit = sizeLimitInt.intValue();
        }
        
        searchablePLI.setTitle(title);
        int index = Collections.binarySearch(items, searchablePLI);
        if (index < 0)
        {
            // find oldest item and remove it
            if (items.size() >= sizeLimit) 
            {
                PickListItemIFace oldest = null;
                for (PickListItemIFace pli : items)
                {
                    if (oldest == null || pli.getTimestampCreated().getTime() < oldest.getTimestampCreated().getTime())
                    {
                        oldest = pli;
                    }
                }
                items.remove(oldest);
                pickList.removeItem(oldest);
            }
            
            PickListItemIFace item = PickListDBAdapterFactory.getInstance().createPickListItem();
            item.setTitle(title);
            item.setValue(value);
            items.add(item);
            
            if (pickList != null)
            {
                pickList.addItem(item);
                item.setPickList(pickList);
                pickList.reorder();
            }
            Collections.sort(items);
            
            final int newItemInx = items.indexOf(item);

            if (doAutoSaveOnAdd)
            {
                save();
            }
            
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    for (ChangeListener cl : changeListeners)
                    {
                        cl.stateChanged(new ChangeEvent(PropertiesPickListAdapter.this));
                    }
                    PropertiesPickListAdapter.this.comboBox.getComboBox().setSelectedIndex(newItemInx);
                }
            });
            
            return item;
            
        }
        // else
        return items.elementAt(index);
    }

    
    /**
     * Sets the proper index from the pref
     */
    public void setSelectedIndex()
    {
        String selectStr = AppPreferences.getLocalPrefs().get(prefSelectedName, ""); //$NON-NLS-1$
        //log.debug("["+prefSelectedName+"]["+selectStr+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        int selectedIndex = -1;
        int i = 0;
        for (PickListItemIFace item : items)
        {
            if (StringUtils.isNotEmpty(selectStr) && item.getValue().equals(selectStr))
            {
                selectedIndex = i;
            }
            i++;
        }
        comboBox.getComboBox().setSelectedIndex(selectedIndex);
    }

    /**
     * @param pickListArg the picklist which is the model
     * @return a string representing the model
     */
    protected String convertModelToStr(final PickListIFace pickListArg)
    {
        StringBuilder strBuf = new StringBuilder();
        for (PickListItemIFace item : pickListArg.getItems())
        {
            if (strBuf.length() > 0) strBuf.append(","); //$NON-NLS-1$
            strBuf.append(item.getValue());
        }
        return strBuf.toString();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapter#save()
     */
    public void save()
    {
        log.debug("Saving PickList"); //$NON-NLS-1$
        if (savePickList)
        {
            AppPreferences.getLocalPrefs().put(prefName, convertModelToStr(pickList));
            log.debug("["+prefName+"]["+convertModelToStr(pickList)+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        Object selectedItem = comboBox.getModel().getSelectedItem();
        if (selectedItem == null && comboBox.getTextField() != null)
        {
            selectedItem = comboBox.getTextField().getText();
        }

        if (selectedItem != null)
        {
        	AppPreferences.getLocalPrefs().put(prefSelectedName, selectedItem.toString());
            log.debug("["+prefSelectedName+"]["+selectedItem.toString()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isTabledBased()
     */
    public boolean isTabledBased()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getType()
     */
    public PickListDBAdapterIFace.Type getType()
    {
        return PickListDBAdapterIFace.Type.Item;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener l)
    {
        if (l != null) changeListeners.add(l);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener(ChangeListener l)
    {
        if (l != null) changeListeners.remove(l);
    }


}
