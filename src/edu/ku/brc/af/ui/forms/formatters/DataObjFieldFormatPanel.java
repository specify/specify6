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

package edu.ku.brc.af.ui.forms.formatters;

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
                                   final ChangeListener                       listener) 
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