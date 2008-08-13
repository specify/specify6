/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataModelObjBase;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *
 * Sets Cataloger to createdByAgent (the user performing the upload).
 * Sets CatalogedDate to timestampCreated.
 * 
 * This is necessary because of form view requirements.
 * 
 * It is less than ideal in that if uploaded dataset includes Cataloger or CatalogedDate, then
 * when values in those columns are null, they will be silently replaced by this class. More work would 
 * need to be done in the upload validation process to improve this situation.
 */
public class CollectionObjectRecFinalizer implements UploadedRecFinalizerIFace
{

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadedRecFinalizerIFace#finalizeForWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
     */
    @Override
    public void finalizeForWrite(DataModelObjBase rec, int recNum)
    {
        CollectionObject co = (CollectionObject )rec;
        if (co.getCatalogedDate() == null)
        {
            Timestamp ts = co.getTimestampCreated();
            co.setCatalogedDate(new GregorianCalendar(ts.getYear() + 1900, ts.getMonth(), ts.getDate()));
        }
        if (co.getCataloger() == null)
        {
            co.setCataloger(co.getCreatedByAgent());
        }

    }

}
