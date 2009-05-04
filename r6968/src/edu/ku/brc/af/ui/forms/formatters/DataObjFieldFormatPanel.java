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
package edu.ku.brc.af.ui.forms.formatters;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;

import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 */
public abstract class DataObjFieldFormatPanel extends JPanel
{

    protected DataObjSwitchFormatterContainerIface formatContainer;
    protected PanelBuilder                         mainPanelBuilder;
    protected DBTableInfo                          tableInfo;
    protected boolean                              newFormat;

    protected DataObjFieldFormatMgr                dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr                  uiFieldFormatterMgrCache;
    
    protected boolean                              isInError   = false;
    private   boolean                              hasChanged  = false;
    protected ChangeListener                       listener;
    protected ChangeEvent                          changeEvent = new ChangeEvent(this);

    protected JButton                              okButton;
    
    /**
     * Fills the editor with the DataObjSwitchFormatter
     * @param fmt DataObjSwitchFormatter
     */
    public abstract void fillWithObjFormatter(DataObjSwitchFormatter fmt);
    
    /**
     * @param tableInfo
     * @param availableFieldsComp
     * @param formatContainer
     * @param okButton
     * @param uiFieldFormatterMgrCache
     */
    public DataObjFieldFormatPanel(final DBTableInfo                          tableInfo,
                                   final DataObjSwitchFormatterContainerIface formatContainer,
                                   final DataObjFieldFormatMgr                dataObjFieldFormatMgrCache,
                                   final UIFieldFormatterMgr                  uiFieldFormatterMgrCache,
                                   final ChangeListener                       listener,
                                   final JButton                              okButton) 
    {
        super();

        if (formatContainer == null)
        {
            throw new RuntimeException("Cannot instantiate data obj format panel builder with null format container.");
        }
        
        if (listener == null)
        {
            throw new RuntimeException("listener cannot be null!");
        }
        
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
        this.tableInfo                  = tableInfo;
        this.formatContainer            = formatContainer;
        this.listener                   = listener;
        this.newFormat                  = false;
        this.okButton                   = okButton;
        
        init();
        buildUI();
    }

    /**
     * 
     */
    protected void buildUI()
    {
    }

    /**
     * 
     */
    protected void init() 
    {
    }

    /**
     * @return
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }

    /**
     * @param hasChanged
     */
    public void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
        listener.stateChanged(changeEvent);
    }

    /**
     * @return
     */
    public boolean isNewFormat()
    {
        return newFormat;
    }
    
    /**
     * @return
     */
    public PanelBuilder getMainPanelBuilder() 
    {
        return mainPanelBuilder;
    }

    public boolean isInError()
    {
        return isInError;
    }
}
