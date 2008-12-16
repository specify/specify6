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

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.WorkbenchRow;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class LocalityRecFinalizer implements UploadedRecFinalizerIFace
{

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadedRecFinalizerIFace#finalizeForWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader)
     */
    @Override
    public void finalizeForWrite(DataModelObjBase rec, int recNum, Uploader uploader)
    {
        //This assumes that rec is a newly uploaded/created record. 
        //Will need to be re-worked when record updates are implemented
        Locality loc = (Locality )rec;
        WorkbenchRow wbRow = uploader.getWbSS().getWorkbench().getRow(uploader.getRow());
        loc.setLat1text(wbRow.getLat1Text());
        loc.setLat2text(wbRow.getLat2Text());
        loc.setLong1text(wbRow.getLong1Text());
        loc.setLong2text(wbRow.getLong2Text());
    }

}
