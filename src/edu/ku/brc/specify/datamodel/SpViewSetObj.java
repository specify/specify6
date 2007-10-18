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
package edu.ku.brc.specify.datamodel;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;

/**
 * @author rod
 *
 * @code_status Beta
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spviewsetobj")
@org.hibernate.annotations.Table(appliesTo="spviewsetobj", indexes =
    {   @Index (name="SpViewObjNameIDX", columnNames={"Name"})
    })
public class SpViewSetObj extends DataModelObjBase implements java.io.Serializable, AppResourceIFace 
{
     //private static final Logger  log       = Logger.getLogger(SpViewSetObj.class);
            
     // Fields    

     protected Integer                   spViewSetObjId;
     protected Short                     level;
     protected String                    name;
     protected String                    description;
     protected String                    metaData;
     protected Set<SpAppResourceData>    spAppResourceDatas;
     protected Set<SpAppResourceDir>     spAppResourceDirs;
     
     //protected Set<SpUIViewSet>          spViewSets;

     // Non Persisted Fields
     protected String                    fileName     = null;
     protected Hashtable<String, String> metaDataHash = null;

    // Constructors

    /** default constructor */
    public SpViewSetObj() 
    {
        // no op
    }
    
    /** constructor with id */
    public SpViewSetObj(final Integer spViewSetObjId) 
    {
        this.spViewSetObjId = spViewSetObjId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        spViewSetObjId        = null;
        level               = null;
        name                = null;
        description         = null;
        metaData            = null;
        spAppResourceDirs = new HashSet<SpAppResourceDir>();
        spAppResourceDatas    = new HashSet<SpAppResourceData>();
        //spViewSets            = new HashSet<SpUIViewSet>();
        
        fileName = null;
    }
    // End Initializer

    

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "SpViewSetObjID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpViewSetObjId() 
    {
        return this.spViewSetObjId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spViewSetObjId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpViewSetObj.class;
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
        
    public void setSpViewSetObjId(Integer spViewSetObjId) 
    {
        this.spViewSetObjId = spViewSetObjId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getLevel()
     */
    @Column(name = "Level", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getLevel() 
    {
        return this.level;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setLevel(java.lang.Short)
     */
    public void setLevel(Short level) 
    {
        this.level = level;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getName()
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getDescription()
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescription() 
    {
        return this.description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setDescription(java.lang.String)
     */
    public void setDescription(String description) 
    {
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getMimeType()
     */
    @Transient
    public String getMimeType()
    {
        return "text/xml";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setMimeType(java.lang.String)
     */
    public void setMimeType(String mimeType)
    {
        throw new RuntimeException("Can't set MimeType");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getMetaData()
     */
    @Column(name = "MetaData", unique = false, nullable = true, insertable = true, updatable = true)
    public String getMetaData()
    {
        return metaData;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getMetaData(java.lang.String)
     */
    public String getMetaData(final String attr)
    {
        initMetaData();
        
        return metaDataHash != null ? metaDataHash.get(attr) : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setMetaData(java.lang.String)
     */
    public void setMetaData(String metaData)
    {
        if (StringUtils.isNotEmpty(this.metaData) && metaDataHash != null)
        {
            metaDataHash.clear();
        }
        this.metaData = metaData;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getMetaDataMap()
     */
    @Transient
    public Map<String, String> getMetaDataMap()
    {
        initMetaData();
        
        return metaDataHash;
    }

    /**
     * Builds meta data hash. 
     */
    protected void initMetaData()
    {
        if (metaDataHash == null)
        {
            metaDataHash = new Hashtable<String, String>(); 
        }
        
        if (StringUtils.isNotEmpty(metaData))
        {
            for (String pair : metaData.split(";"))
            {
                if (StringUtils.isNotEmpty(pair))
                {
                    String[] tokens = pair.split("=");
                    if (tokens != null && tokens.length == 2)
                    {
                        metaDataHash.put(tokens[0], tokens[1]);
                    }
                }
            }
        }
    }
    
//    /**
//     * @return the viewSets
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewSetObj")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<SpUIViewSet> getSpViewSets()
//    {
//        return spViewSets;
//    }
//
//    /**
//     * @param viewSets the viewSets to set
//     */
//    public void setSpViewSets(Set<SpUIViewSet> viewSets)
//    {
//        this.spViewSets = viewSets;
//    }

    /**
     * @return
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "spappresdef_viewsetobj", joinColumns = { @JoinColumn(name = "SpViewSetObjID", unique = false, nullable = false, insertable = true, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "SpAppResourceDirID", unique = false, nullable = false, insertable = true, updatable = false) })
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<SpAppResourceDir> getSpAppResourceDirs() 
    {
        return this.spAppResourceDirs;
    }
    
    /**
     * @param spAppResourceDirs
     */
    public void setSpAppResourceDirs(Set<SpAppResourceDir> spAppResourceDirs)
    {
        this.spAppResourceDirs = spAppResourceDirs;
    }

    /**
     * @return
     */
    @Transient
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * @return
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewSetObj")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpAppResourceData> getSpAppResourceDatas() 
    {
        return spAppResourceDatas;
    }
    
    public void setSpAppResourceDatas(Set<SpAppResourceData> spAppResourceDatas) 
    {
        this.spAppResourceDatas = spAppResourceDatas;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setDataAsString(java.lang.String)
     */
    public void setDataAsString(final String dataStr)
    {
        if (fileName != null)
        {
            throw new RuntimeException("Not implemented!");
        }
        
        if (StringUtils.isNotEmpty(dataStr))
        {
            SpAppResourceData ard;
            if (spAppResourceDatas.size() == 0)
            {
                ard = new SpAppResourceData();
                ard.initialize();
                ard.setSpViewSetObj(this);
                spAppResourceDatas.add(ard);
                
            } else
            {
                ard = spAppResourceDatas.iterator().next();
            }

            ard.setData(dataStr.getBytes());


        } else if (spAppResourceDatas.size() > 0)
        {
            spAppResourceDatas.iterator().next().setData(null);
        }
        
        setSpAppResourceDatas(spAppResourceDatas); // Must call this to make sure it knows we changed it
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getDataAsString()
     */
    @Transient
    public String getDataAsString()
    {
        //log.debug("********* "+getFileName()+" size:"+spAppResourceDatas.size());
        
        getSpAppResourceDatas(); // Must call this before accessing it as a local data member
        
        SpAppResourceData ard = null;
        if (spAppResourceDatas.size() > 0)
        {
            ard = spAppResourceDatas.iterator().next();
            if (ard != null)
            {
                return new String(ard.getData());
            }
        }
        
        String str = null;
        if (StringUtils.isNotEmpty(fileName))
        {
            File file = new File(fileName);
            str = XMLHelper.getContents(file);
            timestampCreated  = new Timestamp(file.lastModified());
            //timestampModified = timestampCreated;
        }

        if (str != null && str.length() > 0)
        {
           return StringEscapeUtils.unescapeXml(str);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
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
        return 86;
    }
    
    
//    /**
//     * Converts a ViewIFace to a SpUIView object.
//     * @param srcView the source
//     * @return the new SpUIView object
//     */
//    protected SpUIView convertFromXML(final ViewIFace srcView,
//                                      final Hashtable<AltViewIFace, SpUIAltView> oldToNewAltView)
//    {
//        SpUIView spView = new SpUIView();
//        spView.initialize();
//        spView.copyInto(srcView);
//        
//        for (AltViewIFace av : srcView.getAltViews())
//        {
//            SpUIAltView spAltView = new SpUIAltView();
//            spAltView.initialize();
//            
//            spAltView.copyInto(av);
//            
//            spAltView.setSpView(spView);
//            spView.getSpAltViews().add(spAltView);
//            
//            oldToNewAltView.put(av, spAltView);
//        }
//        
//        return spView;
//    }
//    
//    /**
//     * Converts a ViewDefIFace to a SpUIView object.
//     * @param srcViewDef the source
//     * @return the new SpUIViewDef object
//     */
//    protected SpUIViewDef convertFromXML(final ViewDefIFace srcViewDef)
//    {
//        SpUIViewDef spViewDef = new SpUIViewDef();
//        spViewDef.initialize();
//        
//        spViewDef.copyInto(srcViewDef);
//        
//        if (srcViewDef instanceof FormViewDefIFace)
//        {
//            FormViewDefIFace formVD = (FormViewDefIFace)srcViewDef;
//            short rowNum = 0;
//            for (FormRowIFace row : formVD.getRows())
//            {
//                SpUIRow spRow = new SpUIRow();
//                spRow.initialize();
//                spRow.setRowNum(rowNum++);
//                spRow.setSpViewDef(spViewDef);
//                spViewDef.getSpRows().add(spRow);
//                
//                for (FormCellIFace cell : row.getCells())
//                {
//                    SpUICell spCell = new SpUICell();
//                    spCell.initialize();
//                    
//                    spCell.copyInto(cell);
//                    spRow.getSpCells().add(spCell);
//                    spCell.setSpRow(spRow);
//                }
//            }
//        }
//        
//        if (srcViewDef instanceof TableViewDefIFace)
//        {
//            TableViewDefIFace tableVD = (TableViewDefIFace)srcViewDef;
//            int order = 0;
//            for (FormColumnIFace col : tableVD.getColumns())
//            {
//                SpUIColumn spCol = new SpUIColumn();
//                spCol.initialize();
//                spCol.copyInto(col);
//                spCol.setColOrder(order++);
//                spCol.setSpViewDef(spViewDef);
//                spViewDef.getSpCols().add(spCol);
//            }
//        }        
//        
//        return spViewDef;
//    }
//    
//    /**
//     * Copies an entire ViewSet (the Views and ViewDefs) into persistable classes.
//     * @param viewSet the source
//     */
//    public void copyViewSet(final ViewSetIFace viewSet)
//    {
//        Hashtable<ViewDefIFace, SpUIViewDef> oldToNewViewDef = new Hashtable<ViewDefIFace, SpUIViewDef>();
//        
//        Hashtable<ViewIFace, Hashtable<AltViewIFace, SpUIAltView>> oldToNewViewAltViewMap = new Hashtable<ViewIFace, Hashtable<AltViewIFace,SpUIAltView>>();
//        
//        Vector<SpUIView>    newViews    = new Vector<SpUIView>();
//        Vector<SpUIViewDef> newViewDefs = new Vector<SpUIViewDef>();
//        
//        SpUIViewSet spViewSet = new SpUIViewSet();
//        spViewSet.initialize();
//
//        for (ViewIFace vs : viewSet.getViews().values())
//        {
//            Hashtable<AltViewIFace, SpUIAltView> oldToNewAltView = new Hashtable<AltViewIFace, SpUIAltView>();
//            oldToNewViewAltViewMap.put(vs, oldToNewAltView);
//            SpUIView newView = convertFromXML(vs, oldToNewAltView);
//            newViews.add(newView);
//            
//            newView.setSpViewSet(spViewSet);
//            spViewSet.getSpViews().add(newView);
//        }
//        
//        for (ViewDefIFace vd : viewSet.getViewDefs().values())
//        {
//            SpUIViewDef newViewDef = convertFromXML(vd);
//            oldToNewViewDef.put(vd, newViewDef);
//            newViewDefs.add(newViewDef);
//            
//            newViewDef.setSpViewSet(spViewSet);
//            spViewSet.getSpViewDefs().add(newViewDef);
//        }
//        
//        // Hook up all the AltViews to their ViewDef
//        for (ViewIFace oldView : viewSet.getViews().values())
//        {
//            Hashtable<AltViewIFace, SpUIAltView> oldToNewAltView = oldToNewViewAltViewMap.get(oldView);
//            for (AltViewIFace av : oldView.getAltViews())
//            {
//                ViewDefIFace oldViewDef = av.getViewDef();
//                SpUIViewDef  spViewDef  = oldToNewViewDef.get(oldViewDef);
//                if (spViewDef == null)
//                {
//                    log.error("spViewDef can't be null!");
//                }
//                
//                SpUIAltView  spAltView  = oldToNewAltView.get(av);
//                if (spAltView == null)
//                {
//                    log.error("spAltView can't be null!");
//                }
//                
//                spViewDef.getSpAltViews().add(spAltView);
//                spAltView.setSpViewDef(spViewDef);
//            }
//        }
//        
//        spViewSet.setFileName(viewSet.getFileName() != null ? viewSet.getFileName() : " ");
//        spViewSet.setName(viewSet.getName());
//        spViewSet.setTitle(viewSet.getTitle() != null ? viewSet.getTitle() : " ");
//        spViewSet.setViewType(viewSet.getType() == ViewSetIFace.Type.System ? SpUIViewSet.SYSTEM_TYPE : SpUIViewSet.USER_TYPE);
//        spViewSet.setSpViewDefs(new HashSet<SpUIViewDef>(newViewDefs));
//        spViewSet.setSpViews(new HashSet<SpUIView>(newViews));
//        
//        spViewSet.setSpViewSetObj(this);
//        spViewSets.add(spViewSet);
//        
//        // Testing 
//        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
//        try
//        {
//            session.beginTransaction();
//            
//            session.saveOrUpdate(this);
//
//            session.commit();
//            
//        } catch (Exception ex)
//        {
//            
//            ex.printStackTrace();
//            
//        } finally
//        {
//            session.close();
//        }
//    }
}
