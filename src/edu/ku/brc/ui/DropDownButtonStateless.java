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
package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.common.collect.ImmutableList;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Oct 4, 2011
 *
 */
public class DropDownButtonStateless extends DropDownButton
{
    public final MenuGenerator menuGenerator;
    private List<MenuInfo>     menuInfos;
    private String             initialLabel;
  
    public DropDownButtonStateless(String label, ImageIcon icon, String tooltip, 
                                   MenuGenerator menuGenerator)
    {
        this.menuGenerator = menuGenerator;
        this.initialLabel = label;
        
        fillinMenus();
        init(initialLabel, icon, tooltip, true);
    }
    
    public List<MenuInfo> getMenuInfos()
    {
        return menuInfos;
    }

    public void setSelected(MenuInfo mi)
    {
        mainBtn.setText(mi.getLabel());
        mi.selected();        
    }
    
    public void reset()
    {
        mainBtn.setText(initialLabel);
    }
    
    public static interface MenuGenerator
    {
        List<MenuInfo> getItems();
    }
    
    public static class MenuInfo extends DropDownMenuInfo
    {
        public MenuInfo(String label, ImageIcon imageIcon, String tooltip)
        {
            super(label, imageIcon, tooltip);
        }

        public void selected() {}
    }
    
    private void fillinMenus()
    {
        menuInfos = ImmutableList.copyOf(menuGenerator.getItems());
        menus = new LinkedList<JComponent>();
        
        for (final MenuInfo mi : menuInfos)
        {
            JMenuItem menuItem = new JMenuItem(mi.getLabel(), mi.getImageIcon());
            
            menuItem.addActionListener(new ActionListener()
            { 
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    mainBtn.setText(mi.getLabel());
                    mi.selected();
                }
            });
            menus.add(menuItem);
        }
    }
    
    @Override
    protected JPopupMenu getPopupMenu()
    {
        fillinMenus();
        return super.getPopupMenu();
    }
}
