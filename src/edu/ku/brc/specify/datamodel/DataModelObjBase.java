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
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

@MappedSuperclass
public abstract class DataModelObjBase implements FormDataObjIFace, Cloneable
{
    private static final Logger log = Logger.getLogger(DataModelObjBase.class);
    
    protected PropertyChangeSupport changes;
    
    protected Date   timestampCreated;
    protected Date   timestampModified;
    protected String lastEditedBy;
    
    
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
        Date now = new Date();
        timestampCreated  = now;
        timestampModified = now;
        lastEditedBy      = null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getId()
     */
    @Transient
    public abstract Long getId();
    
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
    @Column(name = "TimestampCreated", unique = false, nullable = false, insertable = true, updatable = false, length = 23)
    public Date getTimestampCreated()
    {
        return this.timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampCreated(java.util.Date)
     */
    public void setTimestampCreated(Date timestampCreated)
    {
        this.timestampCreated = timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTimestampModified()
     */
    @Column(name = "TimestampModified", unique = false, nullable = false, insertable = true, updatable = true)
    public Date getTimestampModified()
    {
        return this.timestampModified != null ? this.timestampModified : timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampModified(java.util.Date)
     */
    public void setTimestampModified(Date timestampModified)
    {
        this.timestampModified = timestampModified;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getLastEditedBy()
     */
    @Column(name = "LastEditedBy", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLastEditedBy()
    {
        return this.lastEditedBy;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setLastEditedBy(java.lang.String)
     */
    public void setLastEditedBy(String lastEditedBy)
    {
        this.lastEditedBy = lastEditedBy;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#isRestrictable()
     */
    @Transient
    public boolean isRestrictable()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void addReference(FormDataObjIFace ref, String refType)
    {
        DataObjectGettable getter     = DataObjectGettableFactory.get(getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
        Object             dataMember = getter.getFieldValue(this, refType);
        if (dataMember instanceof Set)
        {
            try
            {
                Method method = dataMember.getClass().getMethod("add", Object.class);
                if (method != null)
                {
                    method.invoke(dataMember, ref);
                } else
                {
                    log.error("Missing method add(Object) for this type of set ["+dataMember.getClass()+"]");
                }
    
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            try
            {
                String methodName = "set" + getClass().getSimpleName();
                Method method = ref.getDataClass().getMethod(methodName, getClass());
                if (method != null)
                {
                    method.invoke(ref, this);    
                } else
                {
                    log.error("Missing method "+methodName+" for this type of set ["+dataMember.getClass()+"]");
                }
    
            } catch (Exception ex)
            {
                ex.printStackTrace();
    
            }
        } else
        {
            log.error("Tring to add a reference to a non-set field["+refType+"] ");
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        DataObjectGettable getter     = DataObjectGettableFactory.get(getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
        Object             dataMember = getter.getFieldValue(this, refType);
        if (dataMember instanceof Set)
        {
            try
            {
                Method method = dataMember.getClass().getMethod("remove", Object.class);
                if (method != null)
                {
                    method.invoke(dataMember, ref);
                } else
                {
                    log.error("Missing method remove(Object) for this type of set ["+dataMember.getClass()+"]");
                }
    
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

        } else
        {
            log.error("Tring to add a reference to a non-set field["+refType+"] ");
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
    public boolean isIndexable()
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

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        // TODO Auto-generated method stub
        DataModelObjBase obj = (DataModelObjBase)super.clone();
        obj.timestampCreated  = timestampCreated;
        obj.timestampModified = timestampModified;
        obj.lastEditedBy      = lastEditedBy;
        return obj;
    }
    
    
}
