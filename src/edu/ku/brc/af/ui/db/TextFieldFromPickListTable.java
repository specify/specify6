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

        this.adapter = adapter; 
            
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }
    
    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, final String defaultValue)
    {
        dataObj = value;
        
        if (value != null)
        {
            if (adapter == null)
            {
                setText(value.toString());
                return;
            }
            
            if (adapter.isTabledBased())
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
                
            } else 
            {
                boolean fnd = false;
                setText("");
                for (PickListItemIFace item : adapter.getList())
                {
                    if (item.getValue() == null && value == null)
                    {
                        break;
                        
                    } else if (item.getValue() != null && value != null && item.getValue().equals(value.toString()))
                    {
                        setText(item.getTitle());
                        fnd = true;
                        break;
                    }
                }
                
                if (!fnd && !adapter.isReadOnly())
                {
                    setText(value != null ? value.toString() : defaultValue);
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
                        if (item != null && item.getValue() != null && item.getValue().equals("|null|"))
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
            } else if (nullIndex == -1)
            {
                setText("");
            }
        }
    }
    
    /**
     * @return the adapter
     */
    public PickListDBAdapterIFace getAdapter()
    {
        return adapter;
    }

    /**
     * @param adapter the adapter to set
     */
    public void setAdapter(PickListDBAdapterIFace adapter)
    {
        this.adapter = adapter;
    }

    /**
     * @return the adapter
     */
    public PickListDBAdapterIFace getPickListAdapter()
    {
        return adapter;
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
