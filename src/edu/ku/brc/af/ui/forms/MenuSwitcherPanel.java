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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.DropDownMenuInfo;
import edu.ku.brc.ui.IconManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 16, 2006
 *
 */
public class MenuSwitcherPanel extends JPanel
{
    protected CardLayout                                cardLayout      = null;
    protected Hashtable<String, Vector<AltViewIFace>>   selectorValHash = null;
    protected Hashtable<String, DropDownButtonStateful> switcherHash    = null;
    protected boolean                                   isSelector;

    /**
     * @param mvParentArg
     * @param viewArg
     * @param altViewArg
     * @param altViewsListArg
     */
    public MenuSwitcherPanel(final MultiView            mvParent, 
                             final AltViewIFace         altView, 
                             final Vector<AltViewIFace> altViewsList)
    {
        super();
        setOpaque(false);
        
        switcherHash    = new Hashtable<String, DropDownButtonStateful>();
        selectorValHash = new Hashtable<String, Vector<AltViewIFace>>();
        
        isSelector = StringUtils.isNotEmpty(altView.getSelectorName());
        
        if (isSelector)
        {
            setLayout(cardLayout = new CardLayout());
            for (AltViewIFace av : altViewsList)
            {
                Vector<AltViewIFace> avList = selectorValHash.get(av.getSelectorValue());
                if (avList == null)
                {
                    avList = new Vector<AltViewIFace>();
                    selectorValHash.put(av.getSelectorValue(), avList);
                }
                avList.add(av);
            }
            
            for (String selectorVal : selectorValHash.keySet())
            {
                Vector<AltViewIFace> avList = selectorValHash.get(selectorVal);
                DropDownButtonStateful switcherUI = createSwitcher(mvParent, avList);
                switcherUI.setOpaque(false);
                add(switcherUI, selectorVal);
                switcherHash.put(selectorVal, switcherUI);
            }
            
        } else
        {
            setLayout(new BorderLayout());
            DropDownButtonStateful switcherUI = createSwitcher(mvParent, altViewsList);
            switcherUI.setOpaque(false);
            add(switcherUI, BorderLayout.CENTER);
            switcherHash.put("0", switcherUI);
            selectorValHash.put("0", altViewsList);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        for (DropDownButtonStateful ddbs : switcherHash.values())
        {
            ddbs.setEnabled(enabled);
        }
    }
    
    /**
     * @param value
     */
    public void set(final AltViewIFace altView)
    {
        if (cardLayout != null)
        {
            cardLayout.show(this, altView.getSelectorValue());
        }
        
        String value = isSelector ? altView.getSelectorValue() : "0";
        Vector<AltViewIFace>        avList = selectorValHash.get(value);
        DropDownButtonStateful dds    = switcherHash.get(value);
        
        if (dds != null && avList != null)
        {
            dds.setCurrentIndex(avList.indexOf(altView));
        }
    }
    
    /**
     * Creates a special drop "switcher UI" component for switching between the Viewables in the MultiView.
     * @param mvParentArg the MultiView Parent
     * @param altViewsListArg the Vector of AltViewIFace that will contains the ones in the Drop Down
     * @return the special combobox
     */
    protected DropDownButtonStateful createSwitcher(final MultiView       mvParentArg, 
                                                    final Vector<AltViewIFace> altViewsListArg)
    {
        DropDownButtonStateful switcher = null;
        List<DropDownMenuInfo> items    = new ArrayList<DropDownMenuInfo>(altViewsListArg.size());
        
        
        class SwitcherAL implements ActionListener
        {
            protected DropDownButtonStateful switcherComp;
            public SwitcherAL(final DropDownButtonStateful switcherComp)
            {
                this.switcherComp = switcherComp;
            }
            public void actionPerformed(ActionEvent ae)
            {
                //log.info("Index: "+switcherComp.getCurrentIndex());
                
                mvParentArg.showView(altViewsListArg.get(switcherComp.getCurrentIndex()));
            }
        }
        
        // If we have AltViewIFace then we need to build information for the Switcher Control
        if (altViewsListArg.size() > 0)
        {
            for (AltViewIFace av : altViewsListArg)
            {
                String    label   = null;
                ImageIcon imgIcon = null;
                String    toolTip = null;

                // TODO This is Sort of Temporary until I get it all figured out
                // But somehow we need to externalize this, possible have the AltViewIFace Definition
                // define its own icon
                
                ViewDefIFace viewDef = av.getViewDef();
                boolean      isEdit  = av.getMode() == AltViewIFace.CreationMode.EDIT;
                
                if (viewDef != null)
                {
                    if (viewDef.getType() == ViewDefIFace.ViewType.form)
                    {
                        label   = getResourceString("Form");
                        imgIcon = IconManager.getImage(isEdit ? "EditForm" : "ViewForm", IconManager.IconSize.Std16);
                        toolTip = getResourceString(isEdit ? "ShowEditViewTT" : "ShowViewTT");
    
                    } else if (viewDef.getType() == ViewDefIFace.ViewType.table ||
                               viewDef.getType() == ViewDefIFace.ViewType.formtable)
                    {
                        label   = getResourceString("Grid");
                        imgIcon = IconManager.getImage(isEdit ? "SpreadsheetEdit" : "Spreadsheet", IconManager.IconSize.Std16);
                        toolTip = getResourceString("ShowSpreadsheetTT");
    
                    } else
                    {
                        label   = getResourceString("Icon");
                        imgIcon = IconManager.getImage("image", IconManager.IconSize.Std16);
                        toolTip = getResourceString("ShowViewTT");
                    }
                }
                
                // Override when Top Level Form
                if (mvParentArg.isTopLevel())
                {
                    label = getResourceString(isEdit ? "Edit" : "View");
                }

                items.add(new DropDownMenuInfo(label, imgIcon, toolTip));
            }
            

            switcher = new DropDownButtonStateful(items);
            switcher.setToolTipText(getResourceString("SwitchViewsTT"));
            switcher.addActionListener(new SwitcherAL(switcher));
            switcher.validate();
            switcher.doLayout();
            switcher.setOpaque(false);
            
        }
        return switcher;
    }
}
