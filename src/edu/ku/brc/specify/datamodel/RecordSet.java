/* Copyright (C) 2021, Specify Collections Consortium
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
package edu.ku.brc.specify.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.swing.ImageIcon;

import org.hibernate.annotations.Index;

import edu.ku.brc.af.auth.specify.policy.DatabaseService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;


/**
 * RecordSet generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "recordset")
@org.hibernate.annotations.Table(appliesTo="recordset", indexes =
    {   @Index (name="RecordSetNameIDX", columnNames={"name"})
    })
public class RecordSet extends CollectionMember implements java.io.Serializable, RecordSetIFace, Cloneable
{
    public final static Byte GLOBAL    = 0;
    public final static Byte WB_UPLOAD = 1;
    public final static Byte HIDDEN    = 2;
       
    // Fields
     protected Integer                 recordSetId;
     protected Byte                    type;
     protected String                  name;
     protected Integer                 dbTableId;
     protected String                  remarks;
     protected Set<RecordSetItem>      recordSetItems;
     protected SpecifyUser             specifyUser;
     protected Integer                 ownerPermissionLevel;
     protected Integer                 groupPermissionLevel;
     protected Integer                 allPermissionLevel;
     protected SpPrincipal             group;
     
     protected InfoRequest             infoRequest;
     
     // Transient
     protected ArrayList<RecordSetItemIFace> items = null;
     protected boolean areItemsInOrder             = false;

     // Non-Database Members
     protected ImageIcon dataSpecificIcon = null;
     
    // Constructors

    /** default constructor */
    public RecordSet() 
    {
    }

    // Initializer
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        recordSetId          = null;
        type                 = GLOBAL;
        name                 = null;
        dbTableId            = null;
        remarks              = null;
        ownerPermissionLevel = null;
        groupPermissionLevel = null;
        allPermissionLevel   = null;
        recordSetItems       = new HashSet<RecordSetItem>();
        specifyUser          = null;
        group                = null;
        infoRequest          = null;
    }
    // End Initializer
    
    /**
     * @param nameArg
     * @param dbTableIdArg
     * @param typeArg
     */
    public void set(final String nameArg, final int dbTableIdArg, final Byte typeArg)
    {
        this.name      = nameArg;
        this.dbTableId = dbTableIdArg;
        this.type      = typeArg;
    }

    // Property accessors

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#getRecordSetId()
     */
    @Id
    @GeneratedValue
    @Column(name = "RecordSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getRecordSetId()
    {
        return this.recordSetId;
    }

    /**
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Byte type)
    {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#setRecordSetId(java.lang.Integer)
     */
    public void setRecordSetId(Integer recordSetId)
    {
        this.recordSetId = recordSetId;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return recordSetId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return RecordSet.class;
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
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#getName()
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return this.name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#getDbTableId()
     */
    @Column(name = "TableID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDbTableId()
    {
        return this.dbTableId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#setDbTableId(java.lang.Integer)
     */
    public void setDbTableId(Integer tableId)
    {
        this.dbTableId = tableId;
    }

    /**
     * @return
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "recordSet")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<RecordSetItem> getRecordSetItems() 
    {
        return this.recordSetItems;
    }

    /**
     * @param recordSetItems
     */
    public void setRecordSetItems(Set<RecordSetItem> recordSetItems) 
    {
        this.recordSetItems = recordSetItems;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "InfoRequestID", unique = false, nullable = true, insertable = true, updatable = true)
    public InfoRequest getInfoRequest() {
        return this.infoRequest;
    }
    
    /**
     * @param infoRequest
     */
    public void setInfoRequest(InfoRequest infoRequest)
    {
        this.infoRequest = infoRequest;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() 
    {
        return this.specifyUser;
    }
    
    public void setSpecifyUser(SpecifyUser owner) 
    {
        this.specifyUser = owner;
    }
    
    @Transient
    public SpecifyUser getOwner()
    {
        return getSpecifyUser();
    }
    
    /**
     * @param owner
     */
    public void setOwner(SpecifyUser owner)
    {
        setSpecifyUser(owner);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#getRemarks()
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpPrincipalID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpPrincipal getGroup() {
        return this.group;
    }
    
    public void setGroup(SpPrincipal group) {
        this.group = group;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#setRemarks(java.lang.String)
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }
    /**
     * 
     */
    @Column(name = "OwnerPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getOwnerPermissionLevel() 
    {
        return this.ownerPermissionLevel;
    }
    
    public void setOwnerPermissionLevel(Integer ownerPermissionLevel) 
    {
        this.ownerPermissionLevel = ownerPermissionLevel;
    }

    /**
     * 
     */
    @Column(name = "GroupPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getGroupPermissionLevel() {
        return this.groupPermissionLevel;
    }
    
    public void setGroupPermissionLevel(Integer groupPermissionLevel) 
    {
        this.groupPermissionLevel = groupPermissionLevel;
    }

    /**
     * 
     */
    @Column(name = "AllPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getAllPermissionLevel() 
    {
        return this.allPermissionLevel;
    }
    
    public void setAllPermissionLevel(Integer allPermissionLevel) 
    {
        this.allPermissionLevel = allPermissionLevel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getNumItems()
     */
    @Transient
    public int getNumItems()
    {
        return recordSetItems.size();
    }
    
    /**
     * Ensures the Vector is created and empty.
     */
    private void ensureItemsList(boolean doClear)
    { 
        if (this.items == null)
        {
            this.items = new ArrayList<RecordSetItemIFace>();
            this.items.addAll(this.recordSetItems);
            
        } else if (doClear)
        {
            this.items.clear();
        }   
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#addAll(java.util.Collection)
     */
    public void addAll(Collection<RecordSetItemIFace> list)
    {
        ensureItemsList(true);
        
        for (RecordSetItemIFace rsi : list)
        {
            items.add(rsi);
            recordSetItems.add((RecordSetItem)rsi);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#clearItems()
     */
    public void clearItems()
    {
        if (items != null)
        {
            items.clear();
        }
        
        recordSetItems.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getItems()
     */
    @Transient
    public Set<RecordSetItemIFace> getItems() 
    {
        if (items == null)
        {
            items = new ArrayList<RecordSetItemIFace>();
            for (RecordSetItem rsi : recordSetItems)
            {
                this.items.add(rsi);
            }
        }
        return new HashSet<RecordSetItemIFace>(this.items);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getOrderedItems()
     */
    @Transient
    public List<RecordSetItemIFace> getOrderedItems() 
    {
        if (items == null || !areItemsInOrder)
        {
        	ArrayList<RecordSetItem> oItems = new ArrayList<RecordSetItem>();
            
            for (RecordSetItem rsi : recordSetItems)
            {
            	oItems.add(rsi);
            }
            Collections.sort(oItems, new Comparator<RecordSetItem>() {

    			/* (non-Javadoc)
    			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    			 */
    			@Override
    			public int compare(RecordSetItem o1, RecordSetItem o2) {
    				Integer id1 = o1.getOrder();
    				Integer id2 = o2.getOrder();
    				if (id1 == null && id2 == null) {
    					return o1.compareTo(o2); //use record ids.
    				}
    				if (id1 != null) {
    					if (id2 != null) {
    						return id1.compareTo(id2);
    					} else {
    						return 1;
    					}
    				}
    				return -1;
    			}
            	
            });
            items = new ArrayList<RecordSetItemIFace>();
            items.addAll(oItems);
            areItemsInOrder = true;
        }
        return items;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#removeItem(edu.ku.brc.dbsupport.RecordSetItemIFace)
     */
    public void removeItem(final RecordSetItemIFace rsi)
    {
        if (items != null)
        {
            items.remove(rsi);
        }
        
        recordSetItems.remove(rsi);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#setItems(java.util.Set)
     */
    public void setItems(final Set<RecordSetItemIFace> itemsSet) 
    {
        ensureItemsList(true);
        
        recordSetItems.clear();
        for (RecordSetItemIFace rsi : itemsSet)
        {
            items.add(rsi);
            recordSetItems.add((RecordSetItem)rsi);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#addItem(java.lang.Integer)
     */
    public RecordSetItemIFace addItem(final Integer recordId)
    {
        ensureItemsList(false);

        RecordSetItem rsi = new RecordSetItem(recordId, this.items.size());
        this.items.add(rsi);
        this.recordSetItems.add(rsi);
        rsi.setRecordSet(this);
        return rsi;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#addItem(java.lang.String)
     */
    public RecordSetItemIFace addItem(final String recordId)
    {
        ensureItemsList(false);

        RecordSetItem rsi = new RecordSetItem(recordId, this.items.size());
        this.items.add(rsi);
        this.recordSetItems.add(rsi);
        rsi.setRecordSet(this);
        return rsi;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#addItems(edu.ku.brc.specify.datamodel.RecordSetItemIFace)
     */
    public RecordSetItemIFace addItem(final RecordSetItemIFace item)
    {
        ensureItemsList(false);

        ((RecordSetItem)item).setOrder(this.items.size());
        this.items.add(item);
        this.recordSetItems.add((RecordSetItem)item);
        ((RecordSetItem)item).setRecordSet(this);
        return item;
    }


    /**
     *
     * @param rsID
     * @return
     */
    public static Long getUniqueSize(Integer rsID) {
        return getSize(rsID, true);
    }

    /**
     *
     * @param rsID
     * @return
     */
    public static Long getSize(Integer rsID) {
        return getSize(rsID, false);
    }

    /**
     *
     * @param rsID
     * @param distinctIDs
     * @return
     */
    public static Long getSize(Integer rsID, boolean distinctIDs) {
        String sql = "select count(" + (distinctIDs ? "distinct " : "") + "recordid) from recordsetitem where recordsetid = " + rsID;
        Connection conn = null;
        Statement stmt = null;
        Long result = null;
        try {
            conn = DatabaseService.getInstance().getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getLong(1);
            }
            rs.close();
        } catch (SQLException e) {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordSet.class, e);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordSet.class, e);
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     *
     * @param rsId
     * @return
     */
    public static List<Integer> getUniqueIdList(Integer rsId) {
        return getUniqueIdList(rsId, -1, -1L);
    }

    /**
     *
     * @param rsId
     * @param limit
     * @param offset
     * @return
     */
    public static List<Integer> getUniqueIdList(Integer rsId, int limit, long offset) {
        return getIdList(rsId, null, true, limit, offset);
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getIdList()
     */
    public static List<Integer> getIdList(final Integer rsId, final Set<RecordSetItem>rsiSet) {
        return getIdList(rsId, rsiSet, false, -1, -1L);
    }

    public static List<Integer> getIdList(final Integer rsId, final Set<RecordSetItem> rsiSet, boolean unique, int limit, long offset) {

        if (rsId != null) {
            String sql = "FROM recordsetitem WHERE RecordSetID = " + rsId;
            if (limit != -1) {
                sql += " limit " + limit;
                if (offset != -1L) {
                    sql += " offset " + offset;
                }
            }
            int count = limit != -1 ? limit : BasicSQLUtils.getCount("SELECT count(" + (unique ? "distinct RecordId" : "*") + ") " + sql);
            if (count > 0) {
                ArrayList<Integer> ids = new ArrayList<Integer>(count);

                Connection conn = null;
                Statement stmt = null;
                try {
                    conn = DatabaseService.getInstance().getConnection();
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String selectSql = "select " + (unique ? "distinct " : "") + "recordid ";
                    ResultSet rs = stmt.executeQuery(selectSql + sql);

                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                    rs.close();

                } catch (SQLException e) {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordSet.class, e);
                    e.printStackTrace();

                } finally {
                    try {
                        if (conn != null) conn.close();
                        if (stmt != null) stmt.close();

                    } catch (SQLException e) {
                        edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordSet.class, e);
                        e.printStackTrace();
                    }
                }
                return ids;
            }
            return new ArrayList<Integer>();
        }

        if (rsiSet != null && rsiSet.size() > 0) {
            ArrayList<Integer> ids = new ArrayList<Integer>(rsiSet.size());
            for (RecordSetItem item : rsiSet) {
                ids.add(item.getRecordSetItemId());
            }
            return ids;
        }

        return new ArrayList<Integer>();
    }
    
    //--------------------------------------------------------------
    //-- Non-Database Methods
    //--------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getDataClassFormItems()
     */
    @Transient
    public Class<?> getDataClassFormItems()
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(dbTableId);
        if (ti != null)
        {
            try
            {
                return Class.forName(ti.getClassName());
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordSet.class, ex);
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * Returns the only item in the RecordSet and return null if there is more than one.
     * @return returns the only item
     */
    @Transient
    public RecordSetItemIFace getOnlyItem()
    {
        if (items != null && items.size() == 1)
        {
            return items.iterator().next();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#getDataSpecificIcon()
     */
    @Transient
    public ImageIcon getDataSpecificIcon()
    {
        return dataSpecificIcon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetIFace#setDataSpecificIcon(javax.swing.ImageIcon)
     */
    public void setDataSpecificIcon(ImageIcon dataSpecificIcon)
    {
        this.dataSpecificIcon = dataSpecificIcon;
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
        return 68;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        RecordSet obj = (RecordSet)super.clone();
        obj.initialize();
        
        obj.recordSetId          = null;
        obj.allPermissionLevel   = allPermissionLevel;
        obj.dbTableId            = dbTableId;
        obj.dataSpecificIcon     = dataSpecificIcon;
        obj.group                = group;
        obj.groupPermissionLevel = groupPermissionLevel;
        obj.name                 = name;
        obj.ownerPermissionLevel = ownerPermissionLevel;
        obj.remarks              = remarks;
        obj.specifyUser          = specifyUser;
        obj.type                 = type;
        
        obj.timestampCreated     = new Timestamp(System.currentTimeMillis());
        obj.timestampModified    = timestampCreated;
        
        obj.items                = null;
        
        return obj;
    }

}
