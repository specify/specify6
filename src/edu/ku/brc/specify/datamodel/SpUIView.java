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
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.ui.forms.persist.View.xmlAttr;
import static edu.ku.brc.ui.forms.persist.View.xmlNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spuiview")
@org.hibernate.annotations.Table(appliesTo="spuiview", indexes =
    {   @Index (name="SpUIViewNameIDX", columnNames={"Name"})
    })
public class SpUIView extends DataModelObjBase implements ViewIFace
{
    private static final Logger  log       = Logger.getLogger(SpUIView.class);
    
    protected Integer spUIViewId;
    protected String  name;
    protected String  javaClassName;
    protected String  businessRulesClassName;
    protected Boolean useResourceLabels;
    protected String  description;
    protected String  resourceLabels;
    protected String  selectorName;
    protected String  objTitle;
    protected String  defaultModeName;
    
    protected SpUIViewSet       spViewSet;
    protected Set<SpUIAltView>  spAltViews;
    
    // Transient
    protected BusinessRulesIFace busRule     = null;
    protected CreationMode       defaultMode = null;
    protected Boolean            isSpecial   = null;
    protected String             viewSetName = null;
    
    /**
     * 
     */
    public SpUIView()
    {
        // no op
    }
    
    public SpUIView(final String viewSetName, 
                    final String name, 
                    final String objTitle, 
                    final String className, 
                    final String businessRulesClassName,
                    final String desc,
                    final boolean useResourceLabels,
                    final String resourceLabels)
    {
        super.init();
        
        this.viewSetName       = viewSetName;
        this.name              = name;
        this.objTitle          = objTitle;
        this.javaClassName     = className;
        this.businessRulesClassName = businessRulesClassName;
        this.description       = desc;
        this.useResourceLabels = useResourceLabels;
        this.resourceLabels   = resourceLabels;
        
        spUIViewId        = null;
        selectorName      = null;
        defaultModeName   = null;
        
        spAltViews = new HashSet<SpUIAltView>();
        spViewSet  = null;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spUIViewId        = null;
        name              = null;
        javaClassName     = null;
        businessRulesClassName = null;
        useResourceLabels = null;
        description       = null;
        resourceLabels    = null;
        selectorName      = null;
        objTitle          = null;
        defaultModeName   = null;
        
        spAltViews = new HashSet<SpUIAltView>();
        spViewSet  = null;
         
    }

    /**
     * @return the spUIViewId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUIViewID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUIViewId()
    {
        return spUIViewId;
    }

    /**
     * @return the businessRulesClassName
     */
    @Column(name = "BusRulesClassName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getBusinessRulesClassName()
    {
        return businessRulesClassName;
    }

    /**
     * @param spUIViewId the spUIViewId to set
     */
    public void setSpUIViewId(Integer spUIViewId)
    {
        this.spUIViewId = spUIViewId;
    }

    /**
     * @param businessRulesClassName the businessRulesClassName to set
     */
    public void setBusinessRulesClassName(String businessRulesClassName)
    {
        this.businessRulesClassName = businessRulesClassName;
    }

    /**
     * @return the description
     */
    @Lob
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 4098)
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the hasResourceLabels
     */
    @Column(name = "HasResourceLabels", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getHasResourceLabels()
    {
        return useResourceLabels;
    }

    /**
     * @param hasResourceLabels the hasResourceLabels to set
     */
    public void setHasResourceLabels(Boolean hasResourceLabels)
    {
        this.useResourceLabels = hasResourceLabels;
    }

    /**
     * @return the javaClassName
     */
    @Column(name = "JavaClassName", unique = false, nullable = false, insertable = true, updatable = true, length = 128)
    public String getJavaClassName()
    {
        return javaClassName;
    }

    /**
     * @param javaClassName the javaClassName to set
     */
    public void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @return the objTitle
     */
    @Column(name = "ObjTitle", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getObjTitle()
    {
        return objTitle;
    }

    /**
     * @param objTitle the objTitle to set
     */
    public void setObjTitle(String objTitle)
    {
        this.objTitle = objTitle;
    }

    /**
     * @return the resourceLabels
     */
    @Column(name = "ResourceLabels", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getResourceLabels()
    {
        return resourceLabels;
    }

    /**
     * @param resourceLabels the resourceLabels to set
     */
    public void setResourceLabels(String resourceLabels)
    {
        this.resourceLabels = resourceLabels;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getSelectorName()
     */
    @Column(name = "SelectorName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getSelectorName()
    {
        return selectorName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#setSelectorName(java.lang.String)
     */
    public void setSelectorName(String selectorName)
    {
        this.selectorName = selectorName;
    }
    
    /**
     * @return the defaultModeName
     */
    @Column(name = "DefaultModeName", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getDefaultModeName()
    {
        return defaultModeName;
    }

    /**
     * @param defaultModeName the defaultModeName to set
     */
    public void setDefaultModeName(String defaultModeName)
    {
        this.defaultModeName = defaultModeName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the altViews
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spView")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUIAltView> getSpAltViews()
    {
        return spAltViews;
    }

    /**
     * @param altViews the altViews to set
     */
    public void setSpAltViews(Set<SpUIAltView> altViews)
    {
        this.spAltViews = altViews;
    }

    /**
     * @return the spUIViewSet
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIViewSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpUIViewSet getSpViewSet()
    {
        return spViewSet;
    }

    /**
     * @param spUIViewSet the spUIViewSet to set
     */
    public void setSpViewSet(SpUIViewSet spViewSet)
    {
        this.spViewSet = spViewSet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpUIView.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spUIViewId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 506;
    }
    
    //------------------------------------------------
    // ViewSetIFace
    //------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#addAltView(edu.ku.brc.ui.forms.persist.AltViewIFace)
     */
    public AltViewIFace addAltView(AltViewIFace altView)
    {
        if (altView instanceof SpUIAltView)
        {
            SpUIAltView spav = (SpUIAltView)altView;
            spAltViews.add(spav);
            spav.setSpView(this);
            
        } else
        {
            log.error("Attempting to add an AltViewIFace that isn't a SpUIAltView");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#cleanUp()
     */
    public void cleanUp()
    {
        // No Op
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getAltView(java.lang.String)
     */
    @Transient
    public AltViewIFace getAltView(String nameStr)
    {
        if (nameStr == null)
        {
            return getDefaultAltView();
            
        }
        // else
        for (SpUIAltView av : spAltViews)
        {
            if (av.getName().equals(nameStr))
            {
                return av;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getAltViews()
     */
    @Transient
    public List<AltViewIFace> getAltViews()
    {
        return new Vector<AltViewIFace>(spAltViews);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getBusinessRule()
     */
    @Transient
    public BusinessRulesIFace getBusinessRule()
    {
        if (busRule == null && StringUtils.isNotEmpty(businessRulesClassName))
        {
            try
            {
                Class<?> cls = Class.forName(businessRulesClassName);
                return busRule = (BusinessRulesIFace)cls.newInstance();
                
            } catch (ClassNotFoundException ex)
            {
                log.error(ex);
            } catch (InstantiationException ex)
            {
                log.error(ex);
            } catch (IllegalAccessException ex)
            {
                log.error(ex);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getClassName()
     */
    @Transient
    public String getClassName()
    {
        return javaClassName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultAltView(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode, java.lang.String)
     */
    @Transient
    public AltViewIFace getDefaultAltView(final AltViewIFace.CreationMode creationMode, final String altViewType)
    {
        
        if (creationMode != null && StringUtils.isNotEmpty(altViewType))
        {
            AltViewIFace defAltView = null;
            boolean      isForm     = altViewType.equals("form");
            for (AltViewIFace altView : spAltViews)
            {
                ViewDef.ViewType type = altView.getViewDef().getType();
                //System.out.println("View.getDefaultAltView ["+type+"]["+altView.getName()+"] mode["+altView.getMode()+"]["+creationMode+"]");
                if (isForm && type == ViewDefIFace.ViewType.form ||
                    !isForm && type != ViewDefIFace.ViewType.form)
                {
                    if (altView.getMode() == creationMode)
                    {
                        return altView;
                    }
                }
                
                if (altView.isDefault())
                {
                    defAltView = altView;
                }
            }
            
            if (defAltView != null)
            {
                return defAltView;
            }
            
        } else
        {
            for (AltViewIFace altView : spAltViews)
            {
                if (altView.isDefault())
                {
                    return altView;
                }
            }
        }

        throw new RuntimeException("No default Alt View in View["+name+"]");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultAltView()
     */
    @Transient
    public AltViewIFace getDefaultAltView()
    {
        return getDefaultAltView(null, null);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultAltViewWithMode(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode, java.lang.String)
     */
    @Transient
    public AltViewIFace getDefaultAltViewWithMode(CreationMode creationMode, String defAltViewType)
    {
        // First get default AltViewIFace and check to see if it's 
        // edit mode matches the desired edit mode
        AltViewIFace defAltView = getDefaultAltView(creationMode, defAltViewType);
        if (defAltView.getMode() == creationMode || spAltViews.size() == 1)
        {
            return defAltView;
        }
        
        // OK, so we need to use the AltViewIFace that is the opposite of the 
        // of the default AltViewIFace's edit mode.
        for (AltViewIFace av : spAltViews)
        {
            if (!av.isDefault() && av.getViewDefName().equals(defAltView.getViewDefName()))
            {
                return av;
            }
        }
        return defAltView;
        //throw new RuntimeException("No default AltViewIFace in View["+name+"] with the right mode.");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDefaultMode()
     */
    @Transient
    public CreationMode getDefaultMode()
    {
        if (defaultMode == null)
        {
            defaultMode = getCreationModeFromStr(defaultModeName);
        }
        return defaultMode;
    }
    
    /**
     * @param creationModeStr
     * @return
     */
    public static CreationMode getCreationModeFromStr(final String creationModeStr)
    {
        CreationMode mode;
        
        if (StringUtils.isNotEmpty(creationModeStr))
        {
            if (creationModeStr.equals("view"))
            {
                mode = AltViewIFace.CreationMode.VIEW;
                
            } else if (creationModeStr.equals("edit"))
            {
                mode = AltViewIFace.CreationMode.EDIT;
                
            } else if (creationModeStr.equals("search"))
            {
                mode = AltViewIFace.CreationMode.SEARCH;
            } else 
            {
                mode = AltViewIFace.CreationMode.NONE;
            }
        } else
        {
            mode = AltViewIFace.CreationMode.VIEW;
        }
        return mode;
    }
    
    public static String getCreationModeStrFrom(final CreationMode creationMode)
    {
        switch (creationMode)
        {
            case NONE: return "none";
            case EDIT: return "edit";
            case VIEW: return "view";
            case SEARCH: return "search";
        }
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getDesc()
     */
    @Transient
    public String getDesc()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#getViewSetName()
     */
    @Transient
    public String getViewSetName()
    {
        return spViewSet.getName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#isSpecialViewAndEdit()
     */
    @Transient
    public boolean isSpecialViewAndEdit()
    {
        // Note: it may still be special even if altView == 3, but then it was agumented with the Grid View
        if (isSpecial == null)
        {
            if (spAltViews.size() == 2)
            {
                ArrayList<AltViewIFace> list = new ArrayList<AltViewIFace>(spAltViews);
                AltViewIFace av0 = list.get(0);
                AltViewIFace av1 = list.get(1);
                
                isSpecial = av0.getViewDefName().equals(av1.getViewDefName());
                
            } else
            {
                isSpecial = false;
            }
        }
        return isSpecial;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#isUseResourceLabels()
     */
    @Transient
    public boolean isUseResourceLabels()
    {
        return useResourceLabels;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#setDefaultMode(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode)
     */
    public void setDefaultMode(CreationMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewIFace#compareTo(edu.ku.brc.ui.forms.persist.ViewIFace)
     */
    public int compareTo(ViewIFace obj)
    {
        return name.compareTo(obj.getName());
    }

    
    /**
     * Copies an existing View into this persistable View.
     * @param view the source view
     */
    public void copyInto(final ViewIFace view)
    {
        name              = view.getName();
        javaClassName     = view.getClassName();
        businessRulesClassName = view.getBusinessRulesClassName();
        useResourceLabels = view.isUseResourceLabels();
        description       = view.getDesc();
        resourceLabels    = view.getResourceLabels();
        selectorName      = view.getSelectorName();
        objTitle          = view.getObjTitle();
        defaultModeName   = view.getDefaultMode().toString().toLowerCase();
        
        busRule           = view.getBusinessRule();
        defaultMode       = view.getDefaultMode();
        isSpecial         = view.isSpecialViewAndEdit();
        viewSetName       = view.getViewSetName();
    }
    
    public void toXML(final StringBuffer sb)
    {
        sb.append("<view ");
        xmlAttr(sb, "name", name);
        xmlAttr(sb, "class", javaClassName);
        xmlAttr(sb, "busrule", businessRulesClassName);
        xmlAttr(sb, "resourcelabels", useResourceLabels);
        sb.append(">\n");
        xmlNode(sb, "desc", description, true);
        sb.append("  <altviews");
        xmlAttr(sb, "defaultmode", defaultMode.toString().toLowerCase());
        xmlAttr(sb, "selector", selectorName);
        sb.append(">\n");
        for (AltViewIFace av : spAltViews)
        {
            av.toXML(sb);
        }
        sb.append("  </altviews>\n");
        sb.append("</view>\n");
    }
    
}
