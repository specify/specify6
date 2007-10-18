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
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.datamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

@MappedSuperclass
public abstract class DataModelObjBase implements FormDataObjIFace, Cloneable
{
    private static final Logger log = Logger.getLogger(DataModelObjBase.class);
    
    protected PropertyChangeSupport changes;
    
    protected Timestamp timestampCreated;
    protected Timestamp timestampModified;
    
    protected Agent     createdByAgent;
    protected Agent     modifiedByAgent;
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#initialize()
     */
    public abstract void initialize();

    /**
     * This can be called in order to initialize the fields common to all DataModelObjBase
     * implementations.
     */
    protected void init()
    {
        Timestamp now     = new Timestamp(System.currentTimeMillis());
        timestampCreated  = now;
        timestampModified = null;
        createdByAgent    = SpecifyUser.getCurrentUser() != null ? SpecifyUser.getCurrentUser().getAgent() : null;
        modifiedByAgent   = null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getId()
     */
    @Transient
    public abstract Integer getId();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getIdentityTitle()
     */
    @Transient
    public String getIdentityTitle()
    {
        String str = DataObjFieldFormatMgr.format(this, getDataClass());
        if (StringUtils.isEmpty(str))
        {
            return getClass().getSimpleName() + ": " + getId();
        }
        return str;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTimestampCreated()
     */
    @Column(name = "TimestampCreated", nullable = false, updatable = false)
    public Timestamp getTimestampCreated()
    {
        return this.timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampCreated(java.sql.Timestamp)
     */
    public void setTimestampCreated(Timestamp timestampCreated)
    {
        this.timestampCreated = timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTimestampModified()
     */
    @Version
    @Column(name = "TimestampModified", nullable = false)
    public Timestamp getTimestampModified()
    {
        return this.timestampModified;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampModified(java.util.Date)
     */
    public void setTimestampModified(Timestamp timestampModified)
    {
        this.timestampModified = timestampModified;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getModifiedByAgent()
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ModifiedByAgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getModifiedByAgent()
    {
        return this.modifiedByAgent;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setModifiedByAgent(edu.ku.brc.specify.datamodel.Agent)
     */
    public void setModifiedByAgent(Agent lastEditedBy)
    {
        this.modifiedByAgent = lastEditedBy;
    }
    
    /**
     * @return the createdByAgent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByAgentID", unique = false, nullable = true, insertable = true, updatable = false)
    public Agent getCreatedByAgent()
    {
        return createdByAgent;
    }

    /**
     * @param createdByAgent the createdByAgent to set
     */
    public void setCreatedByAgent(Agent createdByAgent)
    {
        this.createdByAgent = createdByAgent;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#isRestrictable()
     */
    @Transient
    public boolean isRestrictable()
    {
        return false;
    }
    
    /**
     * Sets a value into the current object.
     * @param clazz the class of the data being set
     * @param ref the value
     * @return true on success
     */
    public static boolean setDataMember(final FormDataObjIFace dataObject, final String fieldName, final FormDataObjIFace ref)
    {
        String methodName = "";
        try
        {
            methodName = "set" + StringUtils.capitalize(fieldName);
            //System.out.println(dataObject.getClass().getSimpleName()+"."+methodName+"("+clazz.getSimpleName()+")");
            Method method     = dataObject.getClass().getMethod(methodName, ref.getDataClass());
            if (method != null)
            {
                method.invoke(dataObject, ref);
                return true;
            }
            
        } catch (java.lang.NoSuchMethodException ex)
        {
            log.error("Couldn't find method ["+methodName+"] on Class ["+dataObject.getDataClass().getSimpleName()+"] for arg["+ref.getDataClass()+"]");
            log.error("For Class "+dataObject.getClass().getSimpleName());
            for (Method m : dataObject.getClass().getMethods())
            {
                String mName = m.getName();
                if (mName.startsWith("set"))
                {
                    log.error(mName + " " + mName.equals(methodName));
                }
            }
            ex.printStackTrace();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();

        }
        return false;
    }
    
    /**
     * Adds a data object 'ref' to a collection by the name of 'refType' from the data object 'dataObject'.
     * @param dataObject the data object that owns the collection
     * @param ref the data being removed from the collection
     * @param fieldName the name of the data member
     * @return true on success
     */
    public static boolean addToCollection(final FormDataObjIFace dataObject, String fieldName, FormDataObjIFace ref)
    {
        try
        {
            DataObjectGettable getter     = DataObjectGettableFactory.get(dataObject.getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
            Object             dataMember = getter.getFieldValue(dataObject, fieldName);
            
            if (dataMember != null)
            {
                try
                {
                    Method method = dataMember.getClass().getMethod("add", Object.class);
                    if (method != null)
                    {
                        method.invoke(dataMember, ref);
                        return true;
                        
                    }
                    log.error("Missing method add(Object) for this type of set ["+dataMember.getClass()+"]");
        
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            } else
            {
                log.error("DataMember ["+fieldName+"] was null in object of class["+dataObject.getDataClass().getSimpleName()+"]");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Removes a data object 'ref' from a collection by the name of 'refType' from the data object 'dataObject'.
     * @param dataObject the data object that owns the collection
     * @param ref the data being removed from the collection
     * @param fieldName the name of the data member
     * @return true on success
     */
    public static boolean removeFromCollection(final Object dataObject, final String fieldName, final FormDataObjIFace ref)
    {
        DataObjectGettable getter     = DataObjectGettableFactory.get(dataObject.getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
        Object             dataMember = getter.getFieldValue(dataObject, fieldName);
        try
        {
            Method method = dataMember.getClass().getMethod("remove", Object.class);
            if (method != null)
            {
                method.invoke(dataMember, ref);
                return true;
                
            }
            log.error("Missing method remove(Object) for this type of set ["+dataMember.getClass()+"]");

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void addReference(FormDataObjIFace ref, String fieldName)
    {
        if (ref != null)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(getTableId());
            if (tblInfo != null)
            {
                DBRelationshipInfo rel = tblInfo.getRelationshipByName(fieldName);
                if (rel != null)
                {
                    //String fieldName = rel.getName();
                    String otherSide = rel.getOtherSide();
                    
                    Field   fld           = null;
                    boolean isACollection = false;
                    try
                    {
                        fld = getClass().getDeclaredField(fieldName);
                        if (fld != null)
                        {
                            isACollection = Collection.class.isAssignableFrom(fld.getType());
                        } else
                        {
                            log.error("Couldn't find field ["+fieldName+"] in class ["+getClass().getSimpleName()+"]");
                        }
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    if (fld != null)
                    {
                        if (isACollection)
                        {
                            addToCollection(this, fieldName, ref);
                            DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                            setter.setFieldValue(ref, otherSide, this);
    
                        } else
                        {
                            if (otherSide != null)
                            {
                                addToCollection(ref, otherSide, this);
                            }
                            DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                            setter.setFieldValue(this, fieldName, ref);
                        }
                    } else
                    {
                        log.error("Could not find field["+fieldName+"] in class["+getClass().getSimpleName()+"]");
                    }
                } else
                {
                    log.error("Couldn't find relationship["+fieldName+"] For Table["+ref.getTableId()+"]");
                }
            } else
            {
                log.error("Couldn't find TableInfo ["+ref.getTableId()+"]");
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void removeReference(FormDataObjIFace ref, String fieldName)
    {
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(getTableId());
        if (tblInfo != null)
        {
            DBRelationshipInfo rel = tblInfo.getRelationshipByName(fieldName);
            if (rel != null)
            {
                //String fieldName = rel.getName();
                //String otherSide = rel.getOtherSide();
                
                Field   fld           = null;
                boolean isACollection = false;
                try
                {
                    fld = getClass().getDeclaredField(fieldName);
                    if (fld != null)
                    {
                        isACollection = Collection.class.isAssignableFrom(fld.getType());
                    } else
                    {
                        log.error("Couldn't find field ["+fieldName+"] in class ["+getClass().getSimpleName()+"]");
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                if (fld != null)
                {
                    if (isACollection)
                    {
                        removeFromCollection(this, fieldName, ref);
//                        DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
//                        setter.setFieldValue(ref, otherSide, null);
        
                    } else
                    {
                        DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                        setter.setFieldValue(this, fieldName, null);
                        // in this case, most likely, the Collection isn't even loaded since the parent object probably isn't visible in a form
                        // calling removeFromCollection() would trigger a LazyInstantiationException
//                        removeFromCollection(ref, otherSide, this);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getReferenceValue(java.lang.String)
     */
    public Object getReferenceValue(String refName)
    {
        throw new RuntimeException(this.getClass() + "MUST override getReferenceValue()");
    }

    public void onDelete()
    {
        // do nothing
    }


    public void onSave()
    {
        // do nothing
    }


    public void onUpdate()
    {
        // do nothing
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    public abstract int getTableId();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    public abstract Class<?> getDataClass();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#isIndexable()
     */
    @Transient
    public boolean isChangeNotifier()
    {
        return true;
    }

    //---------------------------------------------------------------------------
    // Property Change Support
    //---------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Notifies all attached {@link PropertyChangeListener}s of a {@link PropertyChangeEvent}.
     * See {@link PropertyChangeSupport}.firePropertyChange(PropertyChangeEvent).
     * 
     * @param evt
     */
    public void firePropertyChange(PropertyChangeEvent evt)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.firePropertyChange(evt);
    }

    /**
     * Notifies all attached {@link PropertyChangeListener}s of a {@link PropertyChangeEvent}.
     * See {@link PropertyChangeSupport}.firePropertyChange(String,boolean,boolean).
     * 
     * @param propertyName the name of the bound property that changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notifies all attached {@link PropertyChangeListener}s of a {@link PropertyChangeEvent}.
     * See {@link PropertyChangeSupport}.firePropertyChange(String,int,int).
     * 
     * @param propertyName the name of the bound property that changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    public void firePropertyChange(String propertyName, int oldValue, int newValue)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notifies all attached {@link PropertyChangeListener}s of a {@link PropertyChangeEvent}.
     * See {@link PropertyChangeSupport}.firePropertyChange(String,Object,Object).
     * 
     * @param propertyName the name of the bound property that changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
       changes.firePropertyChange(propertyName, oldValue, newValue);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        if (changes == null)
        {
            changes = new PropertyChangeSupport(this);
        }
        changes.removePropertyChangeListener(propertyName, listener);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        DataModelObjBase obj = (DataModelObjBase)super.clone();
        obj.timestampCreated  = timestampCreated;
        obj.timestampModified = timestampModified;
        obj.modifiedByAgent      = modifiedByAgent;
        return obj;
    }
    
    @Override
    public String toString()
    {
        return super.toString() + " : " + timestampModified;
    }
    
}
