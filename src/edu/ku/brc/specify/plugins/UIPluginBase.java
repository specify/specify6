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
package edu.ku.brc.specify.plugins;

import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIPluginable;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 18, 2007
 *
 */
public class UIPluginBase extends JPanel implements GetSetValueIFace, UIPluginable
{
    protected Object         data          = null;
    protected ChangeListener listener      = null;
    protected String         cellName      = null;
    protected boolean        isDisplayOnly = true;
    protected boolean        isViewMode    = true;
    protected Properties     properties    = null;
    
    public UIPluginBase()
    {
        // no op
    }

    //-----------------------------------------
    // GetSetValueIFace
    //-----------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        data = value == null ? defaultValue : value;
    }
    
    //-----------------------------------------
    // UIPluginable
    //-----------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        this.properties = propertiesArg;
        this.isViewMode = isViewModeArg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(final ChangeListener listener)
    {
        this.listener = listener;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setIsDisplayOnly(boolean)
     */
    public void setIsDisplayOnly(boolean isDisplayOnly)
    {
        this.isDisplayOnly = isDisplayOnly;
    }

}
