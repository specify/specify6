/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;

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
@Table(name = "spuialtview")
@org.hibernate.annotations.Table(appliesTo="spuialtview", indexes =
    {   @Index (name="SpUIAltViewNameIDX", columnNames={"Name"})
    })
public class SpUIAltView extends DataModelObjBase implements AltViewIFace
{
    private static final Logger  log       = Logger.getLogger(SpUIAltView.class);
            
    protected Integer     spUIAltViewId;
    protected String      name;
    protected String      title;
    protected String      modeName;
    protected Boolean     isValidated;
    protected Boolean     isDefaultAltView;
    protected String      selectorValue;
    protected String      selectorName;
    protected SpUIViewDef spViewDef;
    protected SpUIView    spView;
    
    // Transient
    protected List<AltViewIFace> subViews = null;
    
    /**
     * 
     */
    public SpUIAltView()
    {
        // No op
    }
    
    
    public SpUIAltView(final ViewIFace view, 
                       final String name, 
                       final String title, 
                       final CreationMode mode, 
                       final boolean validated, 
                       final boolean isDefault, 
                       final ViewDefIFace viewDef)
    {
        super.init();
        
        this.spView = (SpUIView)view;
        this.name = name;
        this.title = title;
        this.modeName = SpUIView.getCreationModeStrFrom(mode);
        this.isValidated = validated;
        this.isDefaultAltView = isDefault;
        this.spViewDef = (SpUIViewDef)viewDef;
        
        this.selectorValue = null;
        this.selectorName  = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spUIAltViewId = null;
        name          = null;
        title         = null;
        modeName      = null;
        isValidated   = null;
        isDefaultAltView = null;
        spViewDef     = null;
        spView        = null;
        selectorValue = null;
        selectorName  = null;
    }
    
    /**
     * @return the spUIAltViewId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUIAltViewID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUIAltViewId()
    {
        return spUIAltViewId;
    }

    /**
     * @return the defaultAltView
     */
    @Column(name = "IsDefaultAltView", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsDefaultAltView()
    {
        return isDefaultAltView;
    }

    /**
     * @param defaultAltView the defaultAltView to set
     */
    public void setIsDefaultAltView(Boolean defaultAltView)
    {
        this.isDefaultAltView = defaultAltView;
    }

    /**
     * @return the title
     */
    @Column(name = "Title", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
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

    /**
     * @return the mode
     */
    @Column(name = "ModeName", unique = false, nullable = false, insertable = true, updatable = true, length = 16)
    public String getModeName()
    {
        return modeName;
    }

    /**
     * @param mode the mode to set
     */
    public void setModeName(String modeName)
    {
        this.modeName = modeName;
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
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param spUIAltViewId the spUIAltViewId to set
     */
    public void setSpUIAltViewId(Integer spUIAltViewId)
    {
        this.spUIAltViewId = spUIAltViewId;
    }

    /**
     * @return the validated
     */
    @Column(name = "IsValidated", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsValidated()
    {
        return isValidated;
    }

    /**
     * @param validated the validated to set
     */
    public void setIsValidated(Boolean isValidated)
    {
        this.isValidated = isValidated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getSelectorValue()
     */
    @Column(name = "SelectorValue", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSelectorValue()
    {
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getSelectorName()
     */
    @Column(name = "SelectorName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
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
    
    /**
     * @return the viewDef
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIAltViewForViewDefID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpUIViewDef getSpViewDef()
    {
        return spViewDef;
    }

    /**
     * @param viewDef the viewDef to set
     */
    public void setSpViewDef(SpUIViewDef viewDef)
    {
        this.spViewDef = viewDef;
    }

    /**
     * @return the view
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIAltViewForViewID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpUIView getSpView()
    {
        return spView;
    }


    /**
     * @param view the view to set
     */
    public void setSpView(SpUIView spView)
    {
        this.spView = spView;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpUIAltView.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spUIAltViewId;
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
        return 507;
    }

    //------------------------------------------------
    // AltViewIFace
    //------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#compareTo(edu.ku.brc.ui.forms.persist.AltViewIFace)
     */
    public int compareTo(AltViewIFace obj)
    {
        return name != null && obj != null && obj.getName() != null ? name.compareTo(obj.getName()) : 0;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getMode()
     */
    @Transient
    public CreationMode getMode()
    {
        return SpUIView.getCreationModeFromStr(modeName);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getSubViews()
     */
    @Transient
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


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getViewDefName()
     */
    @Transient
    public String getViewDefName()
    {
        return spViewDef.getName();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#isDefault()
     */
    @Transient
    public boolean isDefault()
    {
        return isDefaultAltView == null ? false : isDefaultAltView;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#isValidated()
     */
    @Transient
    public boolean isValidated()
    {
        return isValidated == null ? false : isValidated;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setDefault(boolean)
     */
    public void setDefault(boolean isDefault)
    {
        this.isDefaultAltView = isDefault;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setMode(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode)
     */
    public void setMode(CreationMode mode)
    {
        modeName = SpUIView.getCreationModeStrFrom(mode);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setSelectorValue(java.lang.String)
     */
    public void setSelectorValue(String selectorValue)
    {
        this.selectorValue = selectorValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#setViewDef(edu.ku.brc.ui.forms.persist.ViewDefIFace)
     */
    public void setViewDef(ViewDefIFace viewDef)
    {
        if (viewDef instanceof SpUIViewDef)
        {
            spViewDef = (SpUIViewDef)viewDef;
        } else
        {
            log.error("Attempting to add a ViewDefIFace that is not of class SpUIViewDef");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getViewDef()
     */
    @Transient
    public ViewDefIFace getViewDef()
    {
        return spViewDef;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.AltViewIFace#getView()
     */
    @Transient
    public ViewIFace getView()
    {
        return spView;
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

    /**
     * Copies a AltViewIFace into this persistable AltView
     * @param altView the source
     */
    public void copyInto(final AltViewIFace altView)
    {
        // Need to set the View and the ViewDef Externally
        
        name             = altView.getName();
        title            = altView.getTitle();
        modeName         = altView.getMode().toString().toLowerCase();
        isValidated      = altView.isValidated();
        isDefaultAltView = altView.isDefault();
        
        selectorName     = altView.getSelectorName();
        selectorValue    = altView.getSelectorValue();
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuilder sb)
    {
        //<altview name="AgentNameSearch" viewdef="AgentNameSearch" label="Edit" mode="edit" validated="true" default="true"/>
        sb.append("                <altview");
        xmlAttr(sb, "name", name);
        xmlAttr(sb, "viewdef", spViewDef.getName());
        xmlAttr(sb, "title", title);
        xmlAttr(sb, "mode", modeName);
        xmlAttr(sb, "default", isDefaultAltView);
        xmlAttr(sb, "validated", isValidated);
        xmlAttr(sb, "selector", selectorName);
        xmlAttr(sb, "selector_value", selectorValue);
        sb.append("/>\n");
    }

}
