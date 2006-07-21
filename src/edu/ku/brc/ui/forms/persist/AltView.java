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
 */package edu.ku.brc.ui.forms.persist;

 import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Definition of an Alternate View which can be an "Edit", "View", "None"
 * @author rods
 *
 */
public class AltView
{
    public enum CreationMode {None, Edit, View, Search}
    
    protected View         view;
    protected String       name;
    protected String       label;
    protected CreationMode mode;
    protected boolean      validated;
    protected boolean      isDefault;
    
    protected ViewDef      viewDef = null;

    public AltView()
    {

    }

    public AltView(View view, String name, String label, CreationMode mode, boolean validated, boolean isDefault, final ViewDef viewDef)
    {
        this.view = view;
        this.name = name;
        this.label = label;
        this.mode = mode;
        this.validated = validated;
        this.isDefault = isDefault;
        this.viewDef = viewDef;
    }


    public CreationMode getMode()
    {
        return mode;
    }

    public String getViewDefName()
    {
        return viewDef.getName();
    }

    public String getLabel()
    {
        return label;
    }

    public String getName()
    {
        return name;
    }

    public boolean isValidated()
    {
        return validated;
    }

    public ViewDef getViewDef()
    {
        return viewDef;
    }

    public void setViewDef(ViewDef viewDef)
    {
        this.viewDef = viewDef;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }
    
    public View getView()
    {
        return view;
    }

    public String toString()
    {
        return label;
    }
    
    public static AltView.CreationMode parseMode(final String modeStr, final AltView.CreationMode defaultMode)
    {
        if (isNotEmpty(modeStr))
        {
            if (modeStr.equalsIgnoreCase("edit"))
            {
                return AltView.CreationMode.Edit;
               
            } else if (modeStr.equalsIgnoreCase("view"))
            {
                return AltView.CreationMode.View;
                
            } if (modeStr.equalsIgnoreCase("search"))
            {
                return AltView.CreationMode.Search;
            }
        }
        return defaultMode;
    }
}
