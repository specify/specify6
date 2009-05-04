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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;

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

    /**
     * @param current
     * @return
     */
    protected DataModelObjBase retrieveDefault(boolean current)
    {
        throw new RuntimeException("DeterminationStatusSetter? Why is this class being used?");
//        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
//        try
//        {
//            String hql = "from DeterminationStatus where type = ";
//            if (current)
//            {
//                hql += String.valueOf(DeterminationStatus.CURRENT);
//            }
//            else
//            {
//                hql += String.valueOf(DeterminationStatus.NOTCURRENT);
//            }
//            hql += " and disciplineID = " + AppContextMgr.getInstance().getClassObject(Discipline.class).getId();
//            QueryIFace q = session.createQuery(hql, false);
//            List<?> result = q.list();
//            if (result.size() == 0)
//            {
//                return null;
//            }
//            //But what if there is more than one current or noncurrent??
//            return (DataModelObjBase)result.get(0);
//        }
//        finally
//        {
//            session.close();
//        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.RelatedClassSetter#refresh(int)
     */
    @Override
    public void refresh(final Object data)
    {
        super.refresh(data);
        this.detCount = (Integer)data;
    }
    
}
