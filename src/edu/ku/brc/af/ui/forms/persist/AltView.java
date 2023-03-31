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
package edu.ku.brc.af.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Vector;

/**
 * Definition of an Alternate View which can be an "Edit", "View", "None"
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public class AltView implements Comparable<AltViewIFace>, Cloneable, AltViewIFace
{
    protected ViewIFace     view;
    protected String        name;
    protected String        title;
    protected AltViewIFace.CreationMode  mode;
    protected boolean       validated;
    protected boolean       isDefault;
    
    protected ViewDefIFace  viewDef = null;
    
    protected String        selectorName  = null;
    protected String        selectorValue = null;
    protected List<AltViewIFace> subViews      = null;

    /**
     * 
     */
    public AltView()
    {
        // do nothing
    }

    public AltView(final ViewIFace view, 
                   final String name, 
                   final String title, 
                   final CreationMode mode, 
                   final boolean validated, 
                   final boolean isDefault, 
                   final ViewDefIFace viewDef)
    {
        this.view = view;
        this.name = name;
        this.title = title;
        this.mode = mode;
        this.validated = validated;
        this.isDefault = isDefault;
        this.viewDef = viewDef;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getMode()
     */
    public CreationMode getMode()
    {
        return mode;
    }

    /**
     * @param validated the validated to set
     */
    public void setValidated(boolean validated)
    {
        this.validated = validated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getViewDefName()
     */
    public String getViewDefName()
    {
        return viewDef != null ? viewDef.getName() : null;
    }

    /**
     * @return the title (or name if title is null).
     */
    public String getTitle()
    {
        return title != null ? title : name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#isValidated()
     */
    public boolean isValidated()
    {
        return validated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getViewDef()
     */
    public ViewDefIFace getViewDef()
    {
        return viewDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setViewDef(edu.ku.brc.ui.forms.persist.ViewDefIFace)
     */
    public void setViewDef(ViewDefIFace viewDef)
    {
        this.viewDef = viewDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#isDefault()
     */
    public boolean isDefault()
    {
        return isDefault;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setDefault(boolean)
     */
    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }
    
    public void setIsDefault(final boolean isDef)
    {
        isDefault = isDef;
    }
    
    public boolean getIsDefault()
    {
        return isDefault;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getView()
     */
    public ViewIFace getView()
    {
        return view;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setMode(edu.ku.brc.ui.forms.persist.AltView.CreationMode)
     */
    public void setMode(CreationMode mode)
    {
        this.mode = mode;
    }

    @Override
    public String toString()
    {
        return name + (isDefault ? " (Default)" : ""); // I18N (Maybe)
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getSelectorName()
     */
    public String getSelectorName()
    {
        return selectorName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setSelectorName(java.lang.String)
     */
    public void setSelectorName(String selectorName)
    {
        this.selectorName = selectorName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getSelectorValue()
     */
    public String getSelectorValue()
    {
        return selectorValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setSelectorValue(java.lang.String)
     */
    public void setSelectorValue(String selectorValue)
    {
        this.selectorValue = selectorValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getSubViews()
     */
    public List<AltViewIFace> getSubViews()
    {
        return subViews;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setSubViews(java.util.List)
     */
    public void setSubViews(List<AltViewIFace> subViews)
    {
        this.subViews = subViews;
    }

    public static AltView.CreationMode parseMode(final String modeStr, final AltViewIFace.CreationMode defaultMode)
    {
        if (isNotEmpty(modeStr))
        {
            return AltViewIFace.CreationMode.valueOf(modeStr.toUpperCase());
        }
        return defaultMode;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        AltView altView = (AltView)super.clone();
        altView.view      = view;
        altView.name      = name;
        altView.title     = title;
        altView.mode      = mode;
        altView.validated = validated;
        altView.isDefault = isDefault;
        altView.viewDef   = viewDef;
        altView.selectorName = selectorName;
        altView.selectorValue = selectorValue;
        altView.subViews = new Vector<AltViewIFace>(subViews); // OK not to clone the references
        return altView;      
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuilder sb)
    {
        sb.append("        <altview ");
        xmlAttr(sb, "name", name);
        xmlAttr(sb, "viewdef", viewDef.getName());
        xmlAttr(sb, "title", title);
        xmlAttr(sb, "validated", validated);
        xmlAttr(sb, "default", isDefault);
        xmlAttr(sb, "selector", selectorName);
        xmlAttr(sb, "selector_value", selectorValue);
        sb.append("/>\n");
    }

    //-------------------------------------
    // Comparable
    //-------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#compareTo(edu.ku.brc.ui.forms.persist.AltView)
     */
    public int compareTo(AltViewIFace obj)
    {
        if (name.equals(obj.getName()))
        {
            return 0;

        }
        // else
        return name.compareTo(obj.getName());
    }

}
