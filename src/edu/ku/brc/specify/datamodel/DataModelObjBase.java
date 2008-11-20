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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.DataObjectGettable;
import edu.ku.brc.af.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.af.ui.forms.DataObjectSettable;
import edu.ku.brc.af.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.weblink.WebLinkDataProviderIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

@MappedSuperclass
public abstract class DataModelObjBase implements FormDataObjIFace,
                                                  WebLinkDataProviderIFace, 
                                                  Cloneable
{
    private static final Logger log = Logger.getLogger(DataModelObjBase.class);
    
    protected PropertyChangeSupport changes;
    
    protected Timestamp timestampCreated;
    protected Timestamp timestampModified;
    
    protected Agent     createdByAgent;
    protected Agent     modifiedByAgent;
    
    protected int       version;
    
    // Transient
    protected static String errMsg = null;
    
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
        createdByAgent    = AppContextMgr.getInstance() == null? null : (AppContextMgr.getInstance().hasContext() ? Agent.getUserAgent() : null);
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
        try
        {
            String str = DataObjFieldFormatMgr.getInstance().format(this, getDataClass());
            if (StringUtils.isEmpty(str))
            {
                return DBTableIdMgr.getInstance().getByClassName(getClass().getName()).getTitle();
            }
            return str;
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return getClass().getName() + hashCode();
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
    @Column(name = "TimestampModified")
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

    @Version
    @Column(name="Version")
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getModifiedByAgent()
     */
    @ManyToOne
    @JoinColumn(name = "ModifiedByAgentID")
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
    @ManyToOne
    @JoinColumn(name = "CreatedByAgentID", updatable = false)
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
            DataObjectGettable getter     = DataObjectGettableFactory.get(dataObject.getClass().getName(), FormHelper.DATA_OBJ_GETTER);
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
        DataObjectGettable getter     = DataObjectGettableFactory.get(dataObject.getClass().getName(), FormHelper.DATA_OBJ_GETTER);
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
    
    /**
     * Retruns whether a given field in a data object is a collection/
     * @param dataObject the data object
     * @param fieldName the field name in the data object
     * @return null the field wasn't found, true it is a Java Collection, False it isn't
     */
    protected Boolean isJavaCollection(final Object dataObject, final String fieldName)
    {
        try
        {
            Field fld = dataObject.getClass().getDeclaredField(fieldName);
            if (fld != null)
            {
                return Collection.class.isAssignableFrom(fld.getType());
                
            }
            log.error("Couldn't find field ["+fieldName+"] in class ["+getClass().getSimpleName()+"]");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void addReference(final FormDataObjIFace ref, final String fieldName)
    {
        addReference(ref, fieldName, true);
    }

    /**
     * @param ref
     * @param fieldName
     * @param doOtherSide
     */
    public void addReference(final FormDataObjIFace ref, final String fieldName, final boolean doOtherSide)
    {
        if (ref == null)
        {
            return;
        }
        
        // Note that this method uses local members for the parent data object the field name.
        // This is because they may need to be adjusted when using '.' notation.
        String           fldName          = fieldName;
        FormDataObjIFace parentDataObject = this;
        
        // Check for DOT notation for setting a child
        // If so then we need to walk the list finding the 'true' parent of the last
        // item in the hierarchy list, and the let it continue with the 'adjusted' parent and 
        // the 'real' field name (the name after the last '.')
        if (StringUtils.contains(fldName, '.'))
        {
            // Get the list of data objects to walk and 
            // create a new list without the last member
            String[] fieldNames = StringUtils.split(fldName, '.');
            String[] parentPath = new String[fieldNames.length-1];
            for (int i=0;i<parentPath.length;i++)
            {
                parentPath[i] = fieldNames[i];
            }
            // Now go get the 'true' parent of the incoming reference by walk the hierarchy
            DataObjectGettable getter = DataObjectGettableFactory.get(getClass().getName(), FormHelper.DATA_OBJ_GETTER);
            Object[] values = UIHelper.getFieldValues(parentPath, this, getter);
            parentDataObject = (FormDataObjIFace)values[parentPath.length-1];
            
            // Set the field name to the last item in the original list
            fldName = fieldNames[fieldNames.length-1];
        }

        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(parentDataObject.getTableId());
        if (tblInfo != null)
        {
            DBRelationshipInfo rel = tblInfo.getRelationshipByName(fldName);
            if (rel != null)
            {
                Boolean isJavaCollection = isJavaCollection(parentDataObject, fldName);
                
                if (isJavaCollection != null)
                {
                    String  otherSide = rel.getOtherSide();
                    if (isJavaCollection)
                    {
                        addToCollection(parentDataObject, fldName, ref);
                        
                        if (StringUtils.isNotEmpty(otherSide) && doOtherSide)
                        {
                            Boolean isOtherSideCollection = isJavaCollection(ref, otherSide);
                            if (isOtherSideCollection != null && isOtherSideCollection)
                            {
                                addToCollection(ref, otherSide, parentDataObject);
                            } else
                            {
                                DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), FormHelper.DATA_OBJ_SETTER);
                                setter.setFieldValue(ref, otherSide, parentDataObject);
                            }
                        }

                    } else if (doOtherSide)
                    {
                        if (otherSide != null)
                        {
                            addToCollection(ref, otherSide, parentDataObject);
                        }
                        DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), FormHelper.DATA_OBJ_SETTER);
                        setter.setFieldValue(parentDataObject, fldName, ref);
                    }
                } else
                {
                    log.error("Could not find field["+fieldName+"] ["+fldName+"] in class["+parentDataObject.getClass().getSimpleName()+"]");
                }
            } else
            {
                log.error("Couldn't find relationship["+fieldName+"] ["+fldName+"] For Table["+ref.getTableId()+"]");
            }
        } else
        {
            log.error("Couldn't find TableInfo ["+ref.getTableId()+"]");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void removeReference(FormDataObjIFace ref, String fieldName)
    {
        removeReference(ref, fieldName, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#removeReference(edu.ku.brc.af.ui.forms.FormDataObjIFace, java.lang.String, boolean)
     */
    public void removeReference(FormDataObjIFace ref, String fieldName, final boolean doOtherSide)
    {
        if (ref == null)
        {
            return;
        }
        
        // Note that this method uses local members for the parent data object the field name.
        // This is because they may need to be adjusted when using '.' notation.
        String           fldName          = fieldName;
        FormDataObjIFace parentDataObject = this;
        
        // Check for DOT notation for setting a child
        // If so then we need to walk the list finding the 'true' parent of the last
        // item in the hierarchy list, and the let it continue with the 'adjusted' parent and 
        // the 'real' field name (the name after the last '.')
        if (StringUtils.contains(fldName, '.'))
        {
            // Get the list of data objects to walk and 
            // create a new list without the last member
            String[] fieldNames = StringUtils.split(fldName, '.');
            String[] parentPath = new String[fieldNames.length-1];
            for (int i=0;i<parentPath.length;i++)
            {
                parentPath[i] = fieldNames[i];
            }
            // Now go get the 'true' parent of the incoming reference by walk the hierarchy
            DataObjectGettable getter = DataObjectGettableFactory.get(getClass().getName(), FormHelper.DATA_OBJ_GETTER);
            Object[] values = UIHelper.getFieldValues(parentPath, this, getter);
            parentDataObject = (FormDataObjIFace)values[parentPath.length-1];
            
            // Set the field name to the last item in the original list
            fldName = fieldNames[fieldNames.length-1];
        }
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(parentDataObject.getTableId());
        if (tblInfo != null)
        {
            DBRelationshipInfo rel = tblInfo.getRelationshipByName(fldName);
            if (rel != null)
            {
                Boolean isJavaCollection = isJavaCollection(parentDataObject, fldName);
                if (isJavaCollection != null)
                {
                    if (isJavaCollection)
                    {
                        String  otherSide = rel.getOtherSide();
                        removeFromCollection(parentDataObject, fldName, ref);
                        if (StringUtils.isNotEmpty(otherSide) && doOtherSide)
                        {
                            Boolean isOtherSideCollection = isJavaCollection(ref, otherSide);
                            if (isOtherSideCollection != null && isOtherSideCollection)
                            {
                                removeFromCollection(ref, otherSide, parentDataObject);
                            }
                            // rods 06/11/08 - this keeps deleting attachments from working
                            // rods 08/21/08 - This is needed when removing CollectionObjects from Projects
                            else
                            {
                              DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), FormHelper.DATA_OBJ_SETTER);
                              setter.setFieldValue(ref, otherSide, null);
                            }
                        }
        
                    } else
                    {
                        DataObjectSettable setter = DataObjectSettableFactory.get(ref.getClass().getName(), FormHelper.DATA_OBJ_SETTER);
                        setter.setFieldValue(parentDataObject, fldName, null);
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
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        
    }
    
    //-------------------------------------------------------------------
    //-- WebLinkProviderIFace
    //-------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.weblink.WebLinkDataProviderIFace#getWebLinkData(java.lang.String)
     */
    public String getWebLinkData(final String dataName)
    {
        if (StringUtils.isNotEmpty(dataName))
        {
            Object data = FormHelper.getValue(this, dataName);
            return data != null ? data.toString() : null;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        DataModelObjBase obj  = (DataModelObjBase)super.clone();
        obj.timestampCreated  = timestampCreated;
        obj.timestampModified = timestampModified;
        obj.modifiedByAgent   = modifiedByAgent;
        return obj;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return super.toString() + " : " + timestampModified;
    }
    
    /**
     * @param cls
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDataObj(final Class<?> cls, final int id)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            DataModelObjBase dmObj = (DataModelObjBase)session.get(cls, id);
            dmObj.forceLoad();
            return (T)dmObj;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return null;
    }
    
    /**
     * Saves the data object.
     * @return true is ok, false if not
     */
    public static boolean save(final Object...dataObjs)
    {
        return save(false, dataObjs);
    }
    
    /**
     * Saves the data object.
     * @param doShowError whether to show the an error dialog
     * @return true is ok, false if not
     */
    public static boolean save(final boolean doShowError, final Object...dataObjs)
    {
        errMsg = null;
        
        // save to database
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            
            boolean doSave = true;
            if (dataObjs.length == 1)
            {
                for (Object obj : dataObjs)
                {
                    if (obj instanceof Collection<?>)
                    {
                        for (Object dObj : (Collection<?>)obj)
                        {
                            session.saveOrUpdate(dObj);
                        }
                        doSave = false;
                    }
                }
            }
            
            if (doSave)
            {
                for (Object obj : dataObjs)
                {
                    session.saveOrUpdate(obj);
                }
            }
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
            errMsg = ex.toString();
            
            if (doShowError)
            {
                UIRegistry.showError(errMsg);
            }
            return false;
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return true;
    }
    
    /**
     * Deletes the data object.
     * @return true is ok, false if not
     */
    public static boolean delete(final Object...dataObjs)
    {
        return delete(false, dataObjs);
    }
    
    /**
     * Deletes the data object.
     * @param doShowError whether to show the an error dialog
     * @return true is ok, false if not
     */
    public static boolean delete(final boolean doShowError, final Object...dataObjs)
    {
        errMsg = null;
        
        // save to database
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (Object obj : dataObjs)
            {
                session.delete(obj);
            }
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
            errMsg = ex.toString();
            
            if (doShowError)
            {
                UIRegistry.showError(errMsg);
            }
            return false;
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return true;
    }
    
    /**
     * Deletes the data object.
     * @return true is ok, false if not
     */
    public static boolean delete(final int id, final int tableId)
    {
        return delete(id, tableId, false);
    }
    
    /**
     * Deletes the data object.
     * @param doShowError whether to show the an error dialog
     * @return true is ok, false if not
     */
    public static boolean delete(final int id, final int tableId, final boolean doShowError)
    {
        errMsg = null;
        
        DBTableInfo ti         = DBTableIdMgr.getInstance().getInfoById(tableId);
        Connection  connection = DBConnection.getInstance().createConnection();
        Statement   stmt       = null;
        try
        {
            stmt = connection.createStatement();
            int numRecs = stmt.executeUpdate("DELETE FROM "+ti.getName()+" WHERE "+ti.getIdColumnName()+" = "+id);
            if (numRecs != 1)
            {
                // TODO need error message
                return false;
            }
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            errMsg = ex.toString();
            
            try
            {
                connection.rollback();
                
            } catch (SQLException ex2)
            {
                ex.printStackTrace();
            }
            return false;
            
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        return true;
    }
    
    /**
     * @return the error message from saving or deleting
     */
    public static String getErrorMsg()
    {
        return errMsg;
    }
    
}
