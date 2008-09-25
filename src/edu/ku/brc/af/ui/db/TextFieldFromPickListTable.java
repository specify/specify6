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
package edu.ku.brc.af.ui.db;

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 15, 2006
 *
 */
public class TextFieldFromPickListTable extends JTextField implements GetSetValueIFace, AppPrefsChangeListener
{
    //protected static ColorWrapper    valtextcolor = null;
    
    protected Object                 dataObj    = null;
    protected PickListDBAdapterIFace adapter;
    protected Integer                nullIndex  = null;

    /**
     * Constructor.
     * @param adapter the PickListTableAdapter
     */
    public TextFieldFromPickListTable(final PickListDBAdapterIFace adapter, final int cols)
    {
        super(Math.max(cols, 10));
        
        setControlSize(this);

        //if (adapter.isTabledBased())
       // {
            this.adapter = adapter; 
            
        //} else
        //{
        //   throw new RuntimeException("Wrong type of picklist! Must be table based wher the type is not 0.");
        //}
        
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        //bgColor = getBackground();
        //if (valtextcolor == null)
        //{
            //valtextcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
        //}
        //AppPreferences.getRemote().addChangeListener("ui.formatting.valtextcolor", this);

    }
    
    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value;
        
        if (value != null)
        {
            if (adapter != null && adapter.isTabledBased())
            {
                String data = null;
    
                boolean                   isFormObjIFace = value instanceof FormDataObjIFace;
                Vector<PickListItemIFace> items          = adapter.getList();
                
                for (int i=0;i<items.size();i++)
                {
                    PickListItemIFace pli    = items.get(i);
                    Object       valObj = pli.getValueObject();
                    
                    if (valObj != null)
                    {
                        if (isFormObjIFace && valObj instanceof FormDataObjIFace)
                        {
                            //System.out.println(((FormDataObjIFace)value).getId().longValue()+"  "+(((FormDataObjIFace)valObj).getId().longValue()));
                            if (((FormDataObjIFace)value).getId().intValue() == (((FormDataObjIFace)valObj).getId().intValue()))
                            {
                                data = pli.getTitle();
                                break;                                
                            }
                        } else if (pli.getValue().equals(value.toString()))
                        {
                            data = pli.getTitle();
                            break;                            
                        }
                    }
                } 
                
                if (data == null)
                {
                    data = StringUtils.isNotEmpty(defaultValue) ? defaultValue : ""; //$NON-NLS-1$
                }
                
                setText(data);
                
            } else if (adapter != null)
            {
                for (PickListItemIFace item : adapter.getList())
                {
                    if (item.getValue().equals(value.toString()))
                    {
                        setText(item.getTitle());
                    }
                }
            }

            repaint();
        } else
        {
            if (nullIndex == null)
            {
                if (adapter != null)
                {
                    int inx = 0;
                    for (PickListItemIFace item : adapter.getList())
                    {
                        if (item.getValue().equals("|null|"))
                        {
                            nullIndex = inx;
                            setText(item.getTitle());
                            break;
                        }
                        inx++;
                    }
                    if (nullIndex == null)
                    {
                        nullIndex = -1;
                    }
                }
            } else if (nullIndex > -1)
            {
                PickListItemIFace item = adapter.getList().get(nullIndex);
                if (item != null)
                {
                    setText(item.getTitle());
                }
                return;
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }
    
    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsChangeListener#preferenceChange(edu.ku.brc.af.prefs.AppPrefsChangeEvent)
     */
    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("valtextcolor")) //$NON-NLS-1$
        {
            //textField.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }

}
