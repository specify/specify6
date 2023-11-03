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
import static edu.ku.brc.helpers.XMLHelper.xmlNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * A view is a virtual object that may contain one or more "alternate" views. Typically, there is a 
 * read-only or display view and an "edit" view.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class View implements ViewIFace
{
    protected String               viewSetName;
    protected String               name;
    protected String               desc;
    protected String               objTitle; // The title of a single object
    protected String               className;
    protected String               businessRulesClassName;
    protected List<AltViewIFace>   altViews           = new Vector<AltViewIFace>();
    
    protected AltViewIFace.CreationMode defaultMode   = AltViewIFace.CreationMode.VIEW;
    protected String               selectorName       = null;
    protected boolean              useDefaultBusRules = false;
    protected boolean              isInternal         = false;
    
    protected String               title              = null;
    
    // transient data members
    protected Boolean              isSpecial    = null;
   
    /**
     * Constructs a View.
     * @param viewSetName the ViewSet Name
     * @param objTitle the name of a single object of this view
     * @param name the name of the view
     * @param className the class name fr the data object
     * @param businessRulesClassName the fully specified class name of the busniess rules object (implementing BusniessRulesIFace)
     * @param desc a description of the view (for express search indexing)
     * @param resourceLabels (not sure)
     */
    public View(final String viewSetName, 
                final String name, 
                final String objTitle, 
                final String className, 
                final String businessRulesClassName,
                final boolean useDefaultBusRules,
                final boolean isInternal,
                final String desc)
    {
        this.viewSetName    = viewSetName;
        this.name           = name;
        this.objTitle       = objTitle;
        this.className      = className;
        this.businessRulesClassName = businessRulesClassName;
        this.desc           = desc;
        this.useDefaultBusRules = useDefaultBusRules;
        this.isInternal     = isInternal;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#addAltView(edu.ku.brc.ui.forms.persist.AltViewIFace)
     */
    public AltViewIFace addAltView(final AltViewIFace altView)
    {
        altViews.add(altView);
        return altView;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultAltView(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode, java.lang.String)
     */
    public AltViewIFace getDefaultAltView(final AltViewIFace.CreationMode creationMode, final String altViewType)
    {
        // If both are not null/empty then we do the complicated lookup
        // if not we simply return the default
        if (creationMode != null && StringUtils.isNotEmpty(altViewType))
        {
            // First determine the type of AltView we are looking for
            // and collect them into a list, note that forms are special
            // Also track which AltView is the default.
            AltViewIFace defAltView = null;
            boolean      isForm     = altViewType.equals("form");
            List<AltViewIFace> list = new ArrayList<AltViewIFace>();
            for (AltViewIFace altView : altViews)
            {
                if (altView.getViewDef() != null)
                {
                    ViewDef.ViewType type = altView.getViewDef().getType();
                    if (type != null)
                    {
                        //System.err.println("View.getDefaultAltView ["+type+"]["+altView.getName()+"] mode["+altView.getMode()+"]["+creationMode+"] isDef "+altView.isDefault());
                        if (isForm && type == ViewDefIFace.ViewType.form ||
                            !isForm && type != ViewDefIFace.ViewType.form)
                        {
                            if (altView.getMode() == creationMode)
                            {
                                list.add(altView);
                            }
                        }
                        
                        if (altView.isDefault()) // keep track of the one and only default
                        {
                            defAltView = altView;
                        }
                    } else
                    {
                        // new RuntimeException
                        FormDevHelper.appendFormDevError(String.format("ViewDef's (%s) Type is null for AltView %s", altView.getViewDef().getName(), altView.getName()));
                    }
                } else
                {
                    // new RuntimeException
                    FormDevHelper.appendFormDevError(String.format("ViewDef is null for AltView %s", altView.getName()));
                }
            }
            
            // Now see if we found in any the 'category' we are looking for
            if (list.size() > 0)
            {
                // return the default if it is part of the category
                if (defAltView != null && list.contains(defAltView))
                {
                    return defAltView;
                }
                // if not then we basically return the only one we found
                return list.get(0);
            }
            
            // As a last result, return the default if there is one (there should be)
            if (defAltView != null)
            {
                return defAltView;
            }
            
        } else
        {
            for (AltViewIFace altView : altViews)
            {
                if (altView.isDefault())
                {
                    return altView;
                }
            }
        }

        FormDevHelper.appendFormDevError("No default Alt View in View["+name+"]");
        return null;
        //throw new RuntimeException("No default Alt View in View["+name+"]");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultAltView()
     */
    public AltViewIFace getDefaultAltView()
    {
        return getDefaultAltView(null, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultAltViewWithMode(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode, java.lang.String)
     */
    public AltViewIFace getDefaultAltViewWithMode(final AltViewIFace.CreationMode creationMode, final String defAltViewType)
    {
        // First get default AltViewIFace and check to see if it's 
        // edit mode matches the desired edit mode
        AltViewIFace defAltView = getDefaultAltView(creationMode, defAltViewType);
        if (defAltView.getMode() == creationMode || altViews.size() == 1)
        {
            return defAltView;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // OK, so we need to use the AltViewIFace that is the opposite of the 
        // of the default AltViewIFace's edit mode.
        for (AltViewIFace av : altViews)
        {
            if (av.getViewDefName() != null)
            {
                if (defAltView.getViewDefName() != null)
                {
                    if (!av.isDefault() && av.getViewDefName().equals(defAltView.getViewDefName()))
                    {
                        return av;
                    }
                } else
                {
                    sb.append(String.format("The defAltView's ViewDefName is NULL for Alt View '%s' for creationMode: '%s'\n", defAltView.getName(), creationMode.toString()));
                }
            } else
            {
                sb.append(String.format("The ViewDefName is NULL for Alt View '%s' for creationMode: '%s'\n", av.getName(), creationMode.toString()));
            }
        }
        
        if (sb.length() > 0)
        {
            UIRegistry.showError(sb.toString());
        }
        
        return defAltView;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getAltView(java.lang.String)
     */
    public AltViewIFace getAltView(final String nameStr)
    {
        if (nameStr == null)
        {
            return getDefaultAltView();
            
        }
        // else
        for (AltViewIFace altView : altViews)
        {
            if (altView.getName().equals(nameStr))
            {
                return altView;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#isSpecialViewAndEdit()
     */
    public boolean isSpecialViewAndEdit()
    {
        // Note: it may still be special even if altView == 3, but then it was agumented with the Grid View
        if (isSpecial == null)
        {
            if (altViews.size() == 2)
            {
                AltViewIFace av0 = altViews.get(0);
                AltViewIFace av1 = altViews.get(1);
                
                isSpecial = av0.getViewDefName().equals(av1.getViewDefName());
                
            } else
            {
                isSpecial = false;
            }
        }
        return isSpecial;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#createBusinessRule()
     */
    public BusinessRulesIFace createBusinessRule()
    {
        BusinessRulesIFace businessRule = null;
        if (StringUtils.isNotEmpty(businessRulesClassName))
        {
            try 
            {
                businessRule =  (BusinessRulesIFace)Class.forName(businessRulesClassName).newInstance();
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(View.class, e);
                
                InternalError error = new InternalError("Can't instantiate BusinessRulesIFace [" + businessRulesClassName + "]");
                error.initCause(e);
                throw error;
            }
            
        } else if (useDefaultBusRules)
        {
            businessRule = DBTableIdMgr.getInstance().getBusinessRule(className);
        }
        
        return businessRule;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#useDefaultBusinessRules()
     */
    public boolean useDefaultBusinessRules()
    {
        return useDefaultBusRules;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getBusinessRulesClassName()
     */
    public String getBusinessRulesClassName()
    {
        return businessRulesClassName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#setBusinessRulesClassName(java.lang.String)
     */
    public void setBusinessRulesClassName(String businessRulesClassName)
    {
        this.businessRulesClassName = businessRulesClassName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#cleanUp()
     */
    public void cleanUp()
    {
        altViews.clear();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ViewIFace obj)
    {
        return name.compareTo(obj.getName());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getAltViews()
     */
    public List<AltViewIFace> getAltViews()
    {
        return altViews;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDesc()
     */
    public String getDesc()
    {
        return desc;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getObjTitle()
     */
    public String getObjTitle()
    {
        return StringUtils.isNotEmpty(objTitle) ? objTitle : name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getClassName()
     */
    public String getClassName()
    {
        return className;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getViewSetName()
     */
    public String getViewSetName()
    {
        return viewSetName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultMode()
     */
    public AltViewIFace.CreationMode getDefaultMode()
    {
        return defaultMode;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getSelectorName()
     */
    public String getSelectorName()
    {
        return selectorName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#setDefaultMode(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode)
     */
    public void setDefaultMode(AltViewIFace.CreationMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#setSelectorName(java.lang.String)
     */
    public void setSelectorName(String selectorName)
    {
        this.selectorName = selectorName;
    }
    
    /**
     * @return the isInternal
     */
    public boolean isInternal()
    {
        return isInternal;
    }

    /**
     * @param isInternal the isInternal to set
     */
    public void setInternal(boolean isInternal)
    {
        this.isInternal = isInternal;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("    <view ");
        xmlAttr(sb, "name", name);
        xmlAttr(sb, "class", className);
        xmlAttr(sb, "busrule", businessRulesClassName);
        xmlAttr(sb, "usedefbusrule", useDefaultBusRules);
        if (!isInternal) xmlAttr(sb, "isinternal", !isInternal);
        sb.append(">\n    ");
        xmlNode(sb, "desc", desc, true);
        sb.append("      <altviews");
        xmlAttr(sb, "defaultmode", defaultMode.toString().toLowerCase());
        xmlAttr(sb, "selector", selectorName);
        sb.append(">\n");
        for (AltViewIFace av : altViews)
        {
            av.toXML(sb);
        }
        sb.append("      </altviews>\n");
        sb.append("    </view>\n");
    }
    
}
