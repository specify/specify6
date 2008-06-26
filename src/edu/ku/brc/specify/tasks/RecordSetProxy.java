/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.RecordSet;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 20, 2008
 *
 */
public class RecordSetProxy implements RecordSetIFace
{

    private RecordSet                 recordSet = null;
    
    protected Integer                 recordSetId;
    protected Byte                    type;
    protected String                  name;
    protected Integer                 dbTableId;
    protected Class<?>                dataClass;
    protected String                  remarks = null;
    
    // Non-Database Members
    protected ImageIcon dataSpecificIcon = null;
    
    /**
     * @param recordSetId
     * @param type
     * @param name
     * @param dbTableId
     * @param dataClass
     */
    public RecordSetProxy(final Integer recordSetId, 
                          final Byte type, 
                          final String name, 
                          final Integer dbTableId,
                          final String remarks,
                          final Class<?> dataClass)
    {
        super();
        this.recordSetId = recordSetId;
        this.type        = type;
        this.name        = name;
        this.dbTableId   = dbTableId;
        this.remarks     = remarks;
        this.dataClass   = dataClass;
        this.dataSpecificIcon = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#addAll(java.util.Collection)
     */
    public void addAll(Collection<RecordSetItemIFace> list)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#addItem(java.lang.Integer)
     */
    public RecordSetItemIFace addItem(Integer recordId)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#addItem(java.lang.String)
     */
    public RecordSetItemIFace addItem(String recordId)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#addItem(edu.ku.brc.dbsupport.RecordSetItemIFace)
     */
    public RecordSetItemIFace addItem(RecordSetItemIFace item)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#clearItems()
     */
    public void clearItems()
    {
        //throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getDataClassFormItems()
     */
    public Class<?> getDataClassFormItems()
    {
        return dataClass;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getDataSpecificIcon()
     */
    public ImageIcon getDataSpecificIcon()
    {
        return dataSpecificIcon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getDbTableId()
     */
    public Integer getDbTableId()
    {
        return dbTableId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getItems()
     */
    public Set<RecordSetItemIFace> getItems()
    {
        try
        {
            if (loadRecordSet())
            {
                return recordSet.getItems();
            }
        } finally
        {
            recordSet = null;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getNumItems()
     */
    public int getNumItems()
    {
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("SELECT COUNT(*) FROM recordsetitem WHERE RecordSetID = "+recordSetId);
            
            if (rs.next())
            {
                return rs.getInt(1);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getOnlyItem()
     */
    public RecordSetItemIFace getOnlyItem()
    {
        try
        {
            if (loadRecordSet())
            {
                if (recordSet.getItems().size() == 1)
                {
                    return recordSet.getItems().iterator().next();
                }
            }
        } finally
        {
            recordSet = null;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getOrderedItems()
     */
    public List<RecordSetItemIFace> getOrderedItems()
    {
        try
        {
            if (loadRecordSet())
            {
                return recordSet.getOrderedItems();
            }
        } finally
        {
            recordSet = null;
        }
        return null;
    }
    
    /**
     * @return
     */
    private boolean loadRecordSet()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            recordSet = session.getData(RecordSet.class, "recordSetId", recordSetId, DataProviderSessionIFace.CompareType.Equals);
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            recordSet = null;
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return false;
    }

    /**
     * @return the recordSet
     */
    public RecordSet getRecordSet()
    {
        try
        {
            return loadRecordSet() ? recordSet : null;
        } finally
        {
            recordSet = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getRecordSetId()
     */
    public Integer getRecordSetId()
    {
        return recordSetId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getRemarks()
     */
    public String getRemarks()
    {
        return remarks;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#getTableId()
     */
    public int getTableId()
    {
        return RecordSet.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#removeItem(edu.ku.brc.dbsupport.RecordSetItemIFace)
     */
    public void removeItem(RecordSetItemIFace rsi)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#setDataSpecificIcon(javax.swing.ImageIcon)
     */
    public void setDataSpecificIcon(ImageIcon dataSpecificIcon)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#setDbTableId(java.lang.Integer)
     */
    public void setDbTableId(Integer tableId)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#setItems(java.util.Set)
     */
    public void setItems(Set<RecordSetItemIFace> items)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#setRecordSetId(java.lang.Integer)
     */
    public void setRecordSetId(Integer recordSetId)
    {
        throw new RuntimeException("Can't call on proxy");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetIFace#setRemarks(java.lang.String)
     */
    public void setRemarks(String remarks)
    {
        throw new RuntimeException("Can't call on proxy");
    }

}
