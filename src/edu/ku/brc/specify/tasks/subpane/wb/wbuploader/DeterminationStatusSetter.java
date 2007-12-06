/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;
import java.util.List;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
class DeterminationStatusSetter extends RelatedClassSetter
{
    protected int detCount;
    
    protected DeterminationStatusSetter(final UploadTable uploadTbl, Class<?> relatedClass, String fieldName, Object defaultId,
            Object defaultObj, Method setter, int detCount)
    {
        super(uploadTbl, relatedClass, fieldName, defaultId, defaultObj, setter);
        this.detCount = detCount;
    }

    protected Object getCurrentId()
    {
        return defaultIds.get(0);
    }
    
    protected Object getNonCurrentId()
    {
        if (detCount == 1)
        {
            //should never happen but...
            return null;
        }
        return defaultIds.get(1);
    }
    
    protected Object getCurrentObj()
    {
        return defaultObjs.get(0);
    }
    
    protected Object getNonCurrentObj()
    {
        if (detCount == 1)
        {
            //should never happen but...
            return null;
        }
        return defaultObjs.get(1);
    }
    
    /**
     * @return the defaultId
     */
    @Override
    public Object getDefaultId(int idx)
    {
        if (idx == 0)
        {
            return getCurrentId();
        }
        return getNonCurrentId();
    }
    
    @Override
    public Object getDefaultObj(int idx)
    {
        if (idx == 0)
        {
            return getCurrentObj();
        }
        return getNonCurrentObj();
    }
    
    /**
     * @return true if default value can be set for foreign key represented by this Setter.
     */
    @Override
    public boolean defaultSetting() 
    {
        DataModelObjBase current = retrieveDefault(true);
        DataModelObjBase nonCurrent = null;
        if (detCount > 1)
        {
            nonCurrent = retrieveDefault(false);
        }
        if (current != null && (detCount == 1 || nonCurrent != null))
        {
            setDefaultId(current.getId(), 0);
            defaultObjs.set(0, current);
            setDefaultId(nonCurrent != null ? nonCurrent.getId() : null, 1);
            defaultObjs.set(1, nonCurrent);
            return true;
        }
        log.debug("unable to meet requirement: " + getUploadTbl().getTblClass().getSimpleName() + "<->"
                + getRelatedClass().getSimpleName());
        return false;
    }

    protected DataModelObjBase retrieveDefault(boolean current)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            //this is very very iffy. Maybe there needs to be a user settable default non and current determinationStatus??
            String hql = "from DeterminationStatus where name = ";
            if (current)
            {
                hql += "'Current'";
            }
            else
            {
                hql += "'Not current'";
            }
            QueryIFace q = session.createQuery(hql);
            List<?> result = q.list();
            if (result.size() == 0)
            {
                return null;
            }
            return (DataModelObjBase)result.get(0);
        }
        finally
        {
            session.close();
        }
    }
}
