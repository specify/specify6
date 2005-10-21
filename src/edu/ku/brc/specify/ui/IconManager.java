/* Filename:    $RCSfile: IconManager.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.2 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import java.awt.Image;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.Specify;
/**
 * @author Rod Spears
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IconManager
{
    private static Log log = LogFactory.getLog(IconManager.class);
    
    public enum ICON_SIZE {ICON_NORMAL32, ICON_NORMAL_FADE32, ICON_NORMAL16, ICON_NORMAL_FADE16};
    
    protected static String      relativePath = "images/";
    private   static IconManager iconMgr      = new IconManager();
    
    protected Hashtable<String, ImageIcon> icons       = new Hashtable<String, ImageIcon>();
    //protected Hashtable<ICON_SIZE, String>   postfixName = new Hashtable<ICON_SIZE, String>();
    
  
    /**
     * 
     * @return
     */
    public static IconManager getInstance()
    {
         return iconMgr;
    }
    
    
    private IconManager()
    {
    }
    
    /**
     * 
     * @param aName
     * @param aId
     * @return
     */
    public ImageIcon getIcon(String aName, ICON_SIZE aId)
    {
        return icons.get(aName + aId.toString());
    }

    /**
     * 
     * @param aName
     * @return
     */
    public ImageIcon getIcon(String aName)
    {
        return icons.get(aName);
    }

    /**
     * 
     * @param aIconName
     * @param aFileName
     * @param aId
     */
    public Icon createAndPutIcon(String aIconName, String aFileName, ICON_SIZE aId)
    {
        
        String name = aIconName + aId.toString();
        ImageIcon icon = icons.get(name);
        if (icon == null)
        {
            icon = new ImageIcon(Specify.class.getResource(relativePath+aFileName));
            if (icon != null)
            {
                icons.put(name + aId.toString(), icon);
            } else
            {
                log.error("Can't load icon ["+aIconName+"]["+aFileName+"]");
            }
        } else
        {
            
        }
        return icon;
    }
    
    /**
     * 
     * @param aIconName
     * @param aFileName
     */
    public Icon createAndPutIcon(String aIconName, String aFileName)
    {
        String name = aIconName + ICON_SIZE.ICON_NORMAL32.toString();
        ImageIcon icon = icons.get(name);
        if (icon == null)
        {
            icon = new ImageIcon(Specify.class.getResource(relativePath+aFileName));
            if (icon != null)
            {
                icons.put(name, icon);
            } else
            {
                log.error("Can't load icon ["+aIconName+"]["+aFileName+"]");
            }
        } else
        {
            return icon;
        }
        return icon;
    }
    
    public Icon createAndPutIconAndScale(String aIconName, String aFileName)
    {
        String name = aIconName + ICON_SIZE.ICON_NORMAL32.toString();
        ImageIcon icon = icons.get(name);
        if (icon == null)
        {
            icon = new ImageIcon(Specify.class.getResource(relativePath+aFileName));
            if (icon != null)
            {
                icons.put(name, icon);
                Image image = icon.getImage().getScaledInstance(24,24, Image.SCALE_SMOOTH);
                icons.put(aIconName + ICON_SIZE.ICON_NORMAL16.toString(), new ImageIcon(image));
            } else
            {
                log.error("Can't load icon ["+aIconName+"]["+aFileName+"]");
            }
            
        } else
        {
            return icon;
        }
        return icon;
    }

}
