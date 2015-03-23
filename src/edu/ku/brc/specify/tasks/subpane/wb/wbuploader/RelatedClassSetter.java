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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Stores info about related tables.
 */
public class RelatedClassSetter
{
    protected static final Logger log = Logger.getLogger(RelatedClassSetter.class);
    
    /**
     * the UploadTable that defined this entry
     */
    protected final UploadTable uploadTbl;
    /**
     * Java class for the related table.
     */
    protected Class<?>          relatedClass;
    /**
     * Name of the foreign key.
     */
    protected String            fieldName;
    /**
     * Default key value for the related date.
     */
    protected Vector<Object>      defaultIds = new Vector<Object>();
    /**
     * Default object of the related class.
     */
    protected Vector<Object>            defaultObjs = new Vector<Object>();
    /**
     * tblClass method to set related class values.
     */
    protected Method            setter;
    /**
     * @param relatedClass
     * @param fieldName
     * @param defaultId
     * @param defaultObj
     * @param setter
     */
    /**
     * Name of the hibernate property of the foreign key
     */
    protected String            propertyName;

    protected RelatedClassSetter(final UploadTable uploadTbl, Class<?> relatedClass, String fieldName, Object defaultId,
            Object defaultObj, Method setter)
    {
        this.uploadTbl = uploadTbl;
        this.relatedClass = relatedClass;
        this.fieldName = fieldName;
        
        this.defaultIds.add(defaultId);
        this.defaultObjs.add(defaultObj);
        this.setter = setter;
        if (this.setter.getName().startsWith("set"))
        {
            this.propertyName = UploadTable.deCapitalize(this.setter.getName().substring(3));
        }
        else
        {
            this.propertyName = UploadTable.deCapitalize(this.setter.getName());
        }
    }
    /**
     * @return the defaultValue
     */
    public Object getDefaultId(int idx)
    {
        return defaultIds.get(adjustIdx(idx));
    }
    
    public Object getDefaultId()
    {
        return getDefaultId(0);
    }
    /**
     * @param defaultId the defaultId to set
     */
    public void setDefaultId(Object defaultId, int idx)
    {
        while (defaultIds.size() <= idx)
        {
            defaultIds.add(null);
            defaultObjs.add(null);
        }
        defaultIds.set(idx, defaultId);
        defaultObjs.set(idx, null);
    }

    /**
     * @param defaultId the defaultId to set
     */
    public void setDefaultId(Object defaultId)
    {
        setDefaultId(defaultId, 0);
    }

    /**
     * @return the fieldName
     */
    public final String getFieldName()
    {
        return fieldName;
    }
    /**
     * @return the relatedClass
     */
    public final Class<?> getRelatedClass()
    {
        return relatedClass;
    }
    /**
     * @return the setter
     */
    public final Method getSetter()
    {
        return setter;
    }
    private Method getSessionGetMethod()
    {
        Method[] meths = DataProviderSessionIFace.class.getMethods();
        for (Method result : meths)
        {
            if (result.getName().equals("get") && result.getParameterTypes().length == 2)
            {
                return result;
            }                    
        }
        return null;
    }
    
    /**
     * @return the number of values set for this Setter
     */
    protected int getSettings()
    {
        return defaultIds.size();
    }
    
    /**
     * @param idx
     * @return Object set as default for idx
     */
    protected Object getSetDefault(int idx)
    {
        return defaultObjs.get(adjustIdx(idx));
    }
    
    protected int adjustIdx(int idx)
    {
        //if idx is greater than number of vals set, assume same val (at idx=0) is to be used for all idxs.
        int index = idx;
        if (index >= getSettings())
        {
            index = 0;
            //if idx is greater than number of vals set and number of vals set is > 1, something is wrong.
            if (getSettings() > 1)
            {
                log.error("invalid idx for defaultObj");
            }
        }
        return index;
    }
    /**
     * @return the defaultObj
     */
    public Object getDefaultObj(int idx) throws UploaderException
    {
        if (getDefaultId(idx) != null && getSetDefault(idx) == null)
        {
            DataProviderSessionIFace objSession = DataProviderFactory.getInstance()
                    .createSession();
            Method getMethod = getSessionGetMethod();
            if (getMethod != null)
            {
                Class<?> keyClass = getMethod.getParameterTypes()[1];
                try
                {
                    //defaultObj = objSession.get(relatedClass, keyClass.cast(defaultId));
                    Object[] args = new Object[2];
                    args[0] = relatedClass;
                    args[1] = keyClass.cast(getDefaultId(idx));
                    defaultObjs.set(adjustIdx(idx), getMethod.invoke(objSession, args));
                    if (getDefaultObj(idx) == null) { throw new UploaderException("Object with id "
                            + getDefaultId(idx).toString() + " does not exist.",
                            UploaderException.ABORT_IMPORT); }
                }
                catch (IllegalAccessException iaEx)
                {
                    throw new UploaderException(iaEx, UploaderException.ABORT_IMPORT);
                }
                catch (InvocationTargetException itEx)
                {
                    throw new UploaderException(itEx, UploaderException.ABORT_IMPORT);
                }
                 finally
                {
                    objSession.close();
                }
            }
            else
            {
                throw new UploaderException(
                        "Could not find DataProviderSessionIFace.get method.",
                        UploaderException.ABORT_IMPORT);
            }
        }
        return getSetDefault(idx);
    }
    /**
     * @return the propertyName
     */
    public final String getPropertyName()
    {
        return propertyName;
    }
    
    /**
     * @return the uploadTbl
     */
    public final UploadTable getUploadTbl()
    {
        return uploadTbl;
    }

    /**
     * @return
     * @throws UploaderException
     */
    protected Discipline getDiscipline() throws UploaderException
    {
    	Discipline discipline;
		DataProviderSessionIFace session = DataProviderFactory.getInstance()
				.createSession();
		try 
		{
			DataModelObjBase temp = AppContextMgr.getInstance().getClassObject(Discipline.class);
			discipline = (Discipline) session.get(temp.getDataClass(), temp
					.getId());
		} catch (Exception ex) 
		{
			throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
		} finally 
		{
			session.close();
		}
		return discipline;
    }

    /**
     * @return
     * @throws UploaderException
     */
    protected Collection getCollection() throws UploaderException
    {
    	Collection collection;
		DataProviderSessionIFace session = DataProviderFactory.getInstance()
				.createSession();
		try 
		{
			DataModelObjBase temp = AppContextMgr.getInstance().getClassObject(Collection.class);
			collection = (Collection) session.get(temp.getDataClass(), temp
					.getId());
		} catch (Exception ex) 
		{
			throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
		} finally 
		{
			session.close();
		}
		return collection;
    }

    /**
     * @return
     * @throws UploaderException
     */
    protected Institution getInstitution() throws UploaderException
    {
    	Institution institution;
		DataProviderSessionIFace session = DataProviderFactory.getInstance()
				.createSession();
		try 
		{
			DataModelObjBase temp = AppContextMgr.getInstance().getClassObject(Institution.class);
			institution = (Institution) session.get(temp.getDataClass(), temp
					.getId());
		} catch (Exception ex) 
		{
			throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
		} finally 
		{
			session.close();
		}
		return institution;
    }

    /**
	 * @return true if default value can be set for foreign key represented by
	 *         this Setter.
	 */
    public boolean defaultSetting() throws UploaderException
    {
        if (relatedClass.equals(Discipline.class))
        {
            setDefaultId(getDiscipline().getDisciplineId());
            return true;
        }
        
        if (relatedClass.equals(Collection.class))
        {
            setDefaultId(getCollection().getCollectionId());
            return true;
        }

        if (relatedClass.equals(Division.class))
        {
            setDefaultId(getDiscipline().getDivision().getId());
            return true;
        }
        
        if (relatedClass.equals(Institution.class))
        {
        	setDefaultId(getInstitution().getId());
        }
        
        log.debug("unable to meet requirement: " + getUploadTbl().getTblClass().getSimpleName() + "<->"
                + getRelatedClass().getSimpleName());
        return false;
    }
    
    public boolean isDefined()
    {
        return (defaultObjs.size() > 0 && defaultObjs.get(0) != null) || getDefaultId(0) != null;
    }

    /**
     * @param data
     * 
     * Update stuff in case anything has changed since initialization.
     * 
     */
    public void refresh(final Object data)
    {
        //nothing to do here
    }
    public static RelatedClassSetter createRelatedClassSetter(final UploadTable uploadTbl, Class<?> relatedClass, String fieldName, Object defaultId,
            Object defaultObj, Method setter, int count)
    {
//        if (relatedClass.equals(DeterminationStatus.class))
//        {
//            return new DeterminationStatusSetter(uploadTbl, relatedClass, fieldName, defaultObj, defaultId, setter, count);
//        }
        return new RelatedClassSetter(uploadTbl, relatedClass, fieldName, defaultObj, defaultId, setter);
    }
}
