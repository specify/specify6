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
package edu.ku.brc.specify.datamodel;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 26, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spuiviewset")
@org.hibernate.annotations.Table(appliesTo="spuiviewset", indexes =
    {   @Index (name="SpUIViewSetNameIDX", columnNames={"Name"})
    })
public class SpUIViewSet extends DataModelObjBase implements ViewSetIFace
{
    public static final Byte USER_TYPE   = 0;
    public static final Byte SYSTEM_TYPE = 1;
    
    protected Integer          spUIViewSetId;
    protected Byte             viewType;
    protected String           name;
    protected String           title;
    protected String           fileName;
    protected String           i18NResourceName;
    protected File             dirPath;

    protected SpViewSetObj     spViewSetObj;
    protected Set<SpUIView>    spViews;
    protected Set<SpUIViewDef> spViewDefs;
    
    // Transient
    protected Hashtable<String, ViewDefIFace> viewDefHash = null;
    protected Hashtable<String, ViewIFace>    viewHash    = null;


    /**
     * 
     */
    public SpUIViewSet()
    {
        // No Op
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spUIViewSetId    = null;
        viewType         = null;
        name             = null;
        title            = null;
        fileName         = null;
        dirPath          = null;
        i18NResourceName = null;

        spViewSetObj     = null;
        spViews          = new HashSet<SpUIView>();
        spViewDefs       = new HashSet<SpUIViewDef>();

    }
    
    /**
     * @return the spUIViewSetId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUIViewSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUIViewSetId()
    {
        return spUIViewSetId;
    }


    /**
     * @param spUIViewSetId the spUIViewSetId to set
     */
    public void setSpUIViewSetId(Integer spUIViewSetId)
    {
        this.spUIViewSetId = spUIViewSetId;
    }


    /**
     * @return the dirPath
     */
    @Transient
    public File getDirPath()
    {
        return dirPath;
    }


    /**
     * @param dirPath the dirPath to set
     */
    public void setDirPath(File dirPath)
    {
        this.dirPath = dirPath;
    }


    /**
     * @return the fileName
     */
    @Column(name = "File", unique = false, nullable = false, insertable = true, updatable = true, length = 255)
    public String getFileName()
    {
        return fileName;
    }


    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
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
     * @return the title
     */
    @Column(name = "Title", unique = false, nullable = false, insertable = true, updatable = true, length = 128)
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
     * @return the i18NResourceName
     */
    @Column(name = "I18NResourceName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getI18NResourceName()
    {
        return i18NResourceName;
    }


    /**
     * @param resourceName the i18NResourceName to set
     */
    public void setI18NResourceName(String resourceName)
    {
        i18NResourceName = resourceName;
    }


    /**
     * @return the type
     */
    @Column(name = "ViewType", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getViewType()
    {
        return viewType;
    }


    /**
     * @param type the type to set
     */
    public void setViewType(Byte type)
    {
        this.viewType = type;
    }


    /**
     * @return the viewDefs
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewSet")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUIViewDef> getSpViewDefs()
    {
        return spViewDefs;
    }


    /**
     * @param viewDefs the viewDefs to set
     */
    public void setSpViewDefs(Set<SpUIViewDef> viewDefs)
    {
        this.spViewDefs = viewDefs;
    }


    /**
     * @return the views
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewSet")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUIView> getSpViews()
    {
        return spViews;
    }


    /**
     * @param views the views to set
     */
    public void setSpViews(Set<SpUIView> views)
    {
        this.spViews = views;
    }


    /**
     * @return the viewSetObj
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpViewSetObjID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpViewSetObj getSpViewSetObj()
    {
        return spViewSetObj;
    }

    /**
     * @param viewSetObj the viewSetObj to set
     */
    public void setSpViewSetObj(SpViewSetObj spViewSetObj)
    {
        this.spViewSetObj = spViewSetObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spUIViewSetId;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpUIViewSet.class;
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
        return 511;
    }
    
    //------------------------------------------------
    // ViewSetIFace
    //------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#cleanUp()
     */
    public void cleanUp()
    {
        // no op
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#compareTo(edu.ku.brc.ui.forms.persist.ViewSetIFace)
     */
    public int compareTo(ViewSetIFace obj)
    {
        return name.compareTo(obj.getName());
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getType()
     */
    @Transient
    public Type getType()
    {
        return viewType == USER_TYPE ? ViewSetIFace.Type.User : ViewSetIFace.Type.System;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getView(java.lang.String)
     */
    @Transient
    public ViewIFace getView(String nameStr)
    {
        for (SpUIView view : spViews)
        {
            if (view.getName().equals(nameStr))
            {
                return view;
            }
        }
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getViewDefs()
     */
    @Transient
    public Hashtable<String, ViewDefIFace> getViewDefs()
    {
        if (viewDefHash == null)
        {
            viewDefHash = new Hashtable<String, ViewDefIFace>();
            for (SpUIViewDef vd : spViewDefs)
            {
                viewDefHash.put(vd.getName(), vd);
            }
        }
        return viewDefHash;
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
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getViews()
     */
    @Transient
    public Hashtable<String, ViewIFace> getViews()
    {
        if (viewHash == null)
        {
            viewHash = new Hashtable<String, ViewIFace>();
            for (SpUIView v : spViews)
            {
                viewHash.put(v.getName(), v);
            }
        }
        return viewHash;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#isSystem()
     */
    @Transient
    public boolean isSystem()
    {
        return viewType == SYSTEM_TYPE;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("<viewset name=\""+name+"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        
        sb.append("    <views>\n");
        
        Vector<SpUIView> sortedViews = new Vector<SpUIView>();
        sortedViews.addAll(spViews);
        Collections.sort(sortedViews);
        
        for (SpUIView view : sortedViews)
        {
            view.toXML(sb);
        }
        sb.append("    </views>\n");
        
        sb.append("    <viewdefs>\n");
        
        Vector<SpUIViewDef> sortedViewDefs = new Vector<SpUIViewDef>();
        sortedViewDefs.addAll(spViewDefs);
        Collections.sort(sortedViewDefs);

        for (SpUIViewDef viewDef : sortedViewDefs)
        {
            viewDef.toXML(sb);
        }
        sb.append("    </viewdefs>\n");
        sb.append("</viewset>\n");
    }
    
}
