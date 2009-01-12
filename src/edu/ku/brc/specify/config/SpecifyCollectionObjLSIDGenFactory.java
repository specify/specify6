/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.CollectionObjLSIDGenFactory;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Institution;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 9, 2009
 *
 */
public class SpecifyCollectionObjLSIDGenFactory extends CollectionObjLSIDGenFactory
{
    protected StringBuilder errMsg   = new StringBuilder();
    protected Boolean       isReady  = null;
    
    protected String        uriStr   = null;
    protected String        instCode = null;
    protected String        colCode  = null;

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#isReady()
     */
    @Override
    public boolean isReady()
    {
        if (isReady == null)
        {
            errMsg.setLength(0);
            Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
            if (inst != null)
            {
                uriStr = inst.getUri();
                if (StringUtils.isEmpty(uriStr))
                {
                    errMsg.append("Institution URI is empty.\n");  // I18N
                }
                instCode = inst.getCode();
                if (StringUtils.isEmpty(uriStr))
                {
                    errMsg.append("Institution Code is empty.\n");  
                }
            } else
            {
                errMsg.append("Institution cannot be null to generate the LSID.\n");  
                return isReady = false;
            }
            
            Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            if (collection != null)
            {
                colCode = collection.getCode();
                if (StringUtils.isEmpty(colCode))
                {
                    errMsg.append("Collection Code is empty.\n");  
                }
            } else
            {
                errMsg.append("Collection cannot be null to generate the LSID.\n");  
                return isReady = false;
            }
            isReady = errMsg.length() == 0;
        }
        return isReady;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#getErrorMsg()
     */
    @Override
    public String getErrorMsg()
    {
        return super.getErrorMsg();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#reset()
     */
    @Override
    public void reset()
    {
        isReady = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#getLSID(java.lang.String)
     */
    @Override
    public String getLSID(final String catalogNumer)
    {
        if (isReady)
        {
            return String.format("urn:lsid:%s:%s:%s:%s", uriStr, instCode, colCode, catalogNumer);
        }
        return null;
    }

}
