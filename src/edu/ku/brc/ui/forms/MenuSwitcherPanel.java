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
package edu.ku.brc.ui.forms;

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

import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.DropDownMenuInfo;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.ViewDef;

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
    protected Hashtable<String, Vector<AltView>>        selectorValHash = null;
    protected Hashtable<String, DropDownButtonStateful> switcherHash    = null;
    protected boolean                                   isSelector;

    /**
     * @param mvParentArg
     * @param viewArg
     * @param altViewArg
     * @param altViewsListArg
     */
    public MenuSwitcherPanel(final MultiView       mvParent, 
                             final AltView         altView, 
                             final Vector<AltView> altViewsList)
    {
        super();
        
        switcherHash    = new Hashtable<String, DropDownButtonStateful>();
        selectorValHash = new Hashtable<String, Vector<AltView>>();
        
        isSelector = StringUtils.isNotEmpty(altView.getSelectorName());
        
        if (isSelector)
        {
            setLayout(cardLayout = new CardLayout());
            for (AltView av : altViewsList)
            {
                Vector<AltView> avList = selectorValHash.get(av.getSelectorValue());
                if (avList == null)
                {
                    avList = new Vector<AltView>();
                    selectorValHash.put(av.getSelectorValue(), avList);
                }
                avList.add(av);
            }
            
            for (String selectorVal : selectorValHash.keySet())
            {
                Vector<AltView> avList = selectorValHash.get(selectorVal);
                DropDownButtonStateful switcherUI = createSwitcher(mvParent, avList);
                add(switcherUI, selectorVal);
                switcherHash.put(selectorVal, switcherUI);
            }
            
        } else
        {
            setLayout(new BorderLayout());
            DropDownButtonStateful switcherUI = createSwitcher(mvParent, altViewsList);
            add(switcherUI, BorderLayout.CENTER);
            switcherHash.put("0", switcherUI);
            selectorValHash.put("0", altViewsList);
        }
    }
    
    /**
     * @param value
     */
    public void set(final AltView altView)
    {
        if (cardLayout != null)
        {
            cardLayout.show(this, altView.getSelectorValue());
        }
        
        String value = isSelector ? altView.getSelectorValue() : "0";
        Vector<AltView>        avList = selectorValHash.get(value);
        DropDownButtonStateful dds    = switcherHash.get(value);
        
        if (dds != null && avList != null)
        {
            dds.setCurrentIndex(avList.indexOf(altView));
        }
    }
    
    /**
     * Creates a special drop "switcher UI" component for switching between the Viewables in the MultiView.
     * @param mvParentArg the MultiView Parent
     * @param altViewsListArg the Vector of AltView that will contains the ones in the Drop Down
     * @return the special combobox
     */
    protected DropDownButtonStateful createSwitcher(final MultiView       mvParentArg, 
                                                    final Vector<AltView> altViewsListArg)
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
        
        // If we have AltView then we need to build information for the Switcher Control
        if (altViewsListArg.size() > 0)
        {
            for (AltView av : altViewsListArg)
            {
                String    label   = av.getLabel();
                ImageIcon imgIcon = null;
                String    toolTip = null;

                // TODO This is Sort of Temporary until I get it all figured out
                // But somehow we need to externalize this, possible have the AltView Definition
                // define its own icon
                if (av.getMode() == AltView.CreationMode.Edit)
                {
                    imgIcon = IconManager.getImage("EditForm", IconManager.IconSize.Std16);
                    toolTip = getResourceString("ShowEditViewTT");

                } else if (av.getViewDef().getType() == ViewDef.ViewType.table ||
                           av.getViewDef().getType() == ViewDef.ViewType.formtable)
                {
                    imgIcon = IconManager.getImage("Spreadsheet", IconManager.IconSize.Std16);
                    toolTip = getResourceString("ShowSpreadsheetTT");

                } else
                {
                    imgIcon = IconManager.getImage("ViewForm", IconManager.IconSize.Std16);
                    toolTip = getResourceString("ShowViewTT");
                }

                items.add(new DropDownMenuInfo(label, imgIcon, toolTip));
            }


            switcher = new DropDownButtonStateful(items);
            switcher.setToolTipText(getResourceString("SwitchViewsTT"));
            switcher.addActionListener(new SwitcherAL(switcher));
            switcher.validate();
            switcher.doLayout();

        }
        return switcher;
    }
}
