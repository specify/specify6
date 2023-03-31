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
package edu.ku.brc.af.core;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.JTiledPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;

/**
 * A singleton that manages a list of NavBoxIFace items. The NavBoxIFace are layed out using a manager 
 * which typically lays them out in a vertical fashion. A Taskable object vends one or more NavBoxes.
 *
 * @code_status Complete
 * 
  * @author rods
 *
*/
@SuppressWarnings("serial") //$NON-NLS-1$
public class NavBoxMgr extends JTiledPanel implements CommandListener
{
    private static final String PREFS = "Preferences";
    
    // Static Data Members
    private static final NavBoxMgr instance = new NavBoxMgr();
    
    private static Trash trash;
    
    // Data Members
    private List<NavBoxIFace>   list   = Collections.synchronizedList(new ArrayList<NavBoxIFace>());
    private JSplitPane          splitPane;
    private boolean             pauseLayout = false;
    
    /**
     * Protected Default Constructor for the singleton
     *
     */
    protected NavBoxMgr()
    {
       //setLayout(layout);
       setBackground(getBGColor()); // XXX PREF ??
       
       CommandDispatcher.register(PREFS, this);
       
       trash = Trash.getInstance();
       
       MouseListener mouseListener = new MouseAdapter() 
       {
             private boolean showIfPopupTrigger(MouseEvent mouseEvent) 
             {
                 Taskable currTask = ContextMgr.getCurrentContext();
                 if (currTask != null)
                 {
                     if (mouseEvent.isPopupTrigger() && currTask.isConfigurable())
                     {
                         JPopupMenu popupMenu = currTask.getPopupMenu();
                         if (popupMenu != null && popupMenu.getComponentCount() > 0) 
                         {
                             popupMenu.show(mouseEvent.getComponent(),
                                     mouseEvent.getX(),
                                     mouseEvent.getY());
                             return true;
                         }
                     }
                 }
                 return false;
             }
             @Override
             public void mousePressed(MouseEvent mouseEvent) 
             {
                 showIfPopupTrigger(mouseEvent);
             }
             @Override
             public void mouseReleased(MouseEvent mouseEvent) 
             {
                 showIfPopupTrigger(mouseEvent);
             }
       };
       addMouseListener(mouseListener);
       
       SkinItem skinItem = SkinsMgr.getSkinItem("NavBoxMgr");
       if (skinItem != null)
       {
           skinItem.setupPanel(this);
       } else
       {
           setOpaque(true);
       }
    }
    
    /**
     * @return the background color for the Side bar
     */
    public static Color getBGColor()
    {
        return Color.WHITE;//new Color(247,247,247);
    }
    
    /**
     * Sets the JSplitPane.
     * @param splitPane the JSplitPane
     */
    public void setSplitPane(final JSplitPane splitPane)
    {
        this.splitPane = splitPane;
    }
    
    /**
     * Returns instance of MavBoxMgr.
     * @return returns a NavBoxMgr singleton instance
     */
    public static NavBoxMgr getInstance()
    {
        return instance;
    }
    
    /**
     * Registers a Task's NavBoxes into the Manager.
     * @param task a task to be managed, this means we ask the task for the list of NavBoxes and then does a layout
     */
    public static void register(final Taskable task)
    {
        List<NavBoxIFace> list = task.getNavBoxes();
        if (list != null)
        {
            if (instance.getComponentCount() == 0 && list.size() > 0)
            {
                //instance.add(trash);
                ((GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE)).add((GhostActionable)trash); // assumes trash implements GhostActionable (and why not?)
            }
            
            instance.pauseLayout = true;
            for (NavBoxIFace box : list)
            {
                instance.addBox(box); // Adds them to ther GhostGlassPane
            }
            instance.pauseLayout = false;
            instance.reAddItems();
        }
        instance.doLayout();
        instance.repaint();
        instance.adjustSplitter();
    }
    
    /**
     * Adjust the split for when things are added (or removed).
     *
     */
    public void adjustSplitter()
    {
        if (splitPane != null)
        {
            if (this.getComponentCount() > 0)
            {
                splitPane.setDividerLocation(getPreferredSize().width);
            } else
            {
                splitPane.setDividerLocation(0);
            }
        }        
    }
    
    public void closeSplitter()
    {
        if (splitPane != null)
        {
            splitPane.setDividerLocation(0.0);
        }
    }
    
    /**
     * Unregisters a Task's NavBoxes into the Manager.
     */
    public static void unregister()
    {
        ((GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE)).clearActionableList();
        
        for (NavBoxIFace box : instance.list)
        {
            box.setIsManaged(false);
        }
        
        // for now just clear everything
        //instance.layout.removeAll();
        //instance.layout = new BoxLayout(instance, BoxLayout.Y_AXIS);
        instance.removeAll();
        instance.list.clear();

        //instance.add(trash);
        instance.repaint();
    }
    
    /**
     * Returns whether a box with a unique name has already been registered.
     * @param name the name of the box
     * @return Returns whether a box with a unique name has already been registered
     */
    protected boolean exists(final String name)
    {
        for (NavBoxIFace box : list)
        {
            if (box.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     */
    private void reAddItems()
    {
        removeAll();
        
        String rowDef = "";
        if (list.size() > 0)
        {
            rowDef = UIHelper.createDuplicateJGoodiesDef("p", "1px", list.size()) + ",f:p:g,10px,p";
            
            int row = 1;
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g",  rowDef), this);
            for (int i=0;i<list.size();i++)
            {
                pb.add(list.get(i).getUIComponent(), cc.xy(1, row));
                row += 2;
            }
            PanelBuilder pbTrash = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g",  "p"));
            pbTrash.add(trash, cc.xy(2, 1));
            pb.add(pbTrash.getPanel(), cc.xy(1, row+1));
            pbTrash.setOpaque(false);
        }
    }

    
    /**
     * Adds a box to the manager (all adds are 'appends' at the moment). The ignoreAlreadyThere allows to request
     * something to be added without worrying whether it is already there.
     * @param box the box to be added
     * @param ignoreAlreadyThere ignore the fact if it is already there
     */
    protected void addBoxInternal(final NavBoxIFace box, final boolean ignoreAlreadyThere)
    {
        if (box == null)
        {
            throw new NullPointerException("Null pane when adding to NavBoxMgr"); //$NON-NLS-1$
        }
        
        if (!exists(box.getName()))
        {
            list.add(box); 
            
            if (!pauseLayout)
            {
                reAddItems();
            }
            
            adjustSplitter();
            
            box.setIsManaged(true);
            GhostGlassPane glassPane = (GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE);
            for (NavBoxItemIFace item : box.getItems())
            {
                if (item instanceof GhostActionable)
                {
                    glassPane.add((GhostActionable)item);
                }
            }
            
        } else if (ignoreAlreadyThere)
        {
            throw new ConfigurationException("Adding a new NavBox with duplicate name["+box.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Adds a box to the manager (all adds are 'appends' at the moment). The ignoreAlreadyThere allows to request
     * something to be added without worrying whether it is already there.
     * @param box the box to be added
     * @param ignoreAlreadyThere ignore the fact if it is already there
     */
    public static void addBox(final NavBoxIFace box, final boolean ignoreAlreadyThere)
    {
        instance.addBoxInternal(box, ignoreAlreadyThere);
    }
    
    /**
     * Adds a box to the manager (all adds are 'appends' at the moment).
     * @param box the box to be added
     */
    public void addBox(final NavBoxIFace box)
    {
        addBox(box, false);
    } 
    
    /**
     * Removes a box from the manager.
     * @param box the box to be remove
     * @param notify true - throws exception if it can't be found, false - ignore not found
     */
    public void removeBox(final NavBoxIFace box, boolean notify)
    {
        if (list.contains(box))
        {
            list.remove(box);
            box.setIsManaged(false);
            
            reAddItems();
            
        } else if (notify)
        {
            throw new ConfigurationException("Can't find an existing NavBox with name["+box.getName()+"] to remove."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    
    /**
     * @param appPrefs
     */
    protected void prefsChanged(final AppPreferences appPrefs)
    {
        if (appPrefs == AppPreferences.getRemote())
        {
            AppPreferences ap = AppPreferences.getLocalPrefs();
            String key      = "ui.formatting.controlSizes"; //$NON-NLS-1$
            String fontName = ap.get(key+".FN", UIRegistry.getBaseFont().getFamily());//$NON-NLS-1$
            int    size     = ap.getInt(key+".SZ", UIRegistry.getBaseFont().getSize());//$NON-NLS-1$
            
            Font font = UIRegistry.adjustPerDefaultFont(new Font(fontName, Font.PLAIN, size));
            
            for (NavBoxIFace nb : list)
            {
                boolean changed = false;
                for (NavBoxItemIFace nbi : nb.getItems())
                {
                    Font f = nbi.getUIComponent().getFont();
                    if (font.getFamily().equals(f.getFamily()) && f.getSize() == font.getSize())
                    {
                        nbi.getUIComponent().setFont(font);
                        changed = true;
                    }
                }
                if (changed)
                {
                    nb.getUIComponent().validate();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(PREFS))
        {
            prefsChanged((AppPreferences)cmdAction.getData());
        }
    }
    
    
}
