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

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * An interface for performing table-specific modifications or additions
 * that are necessary before writing an uploaded record to storage.
 * 
 * Classes that implement this class must be contained in the
 * "edu.ku.brc.specify.tasks.subpane.wb.wbuploader" package and
 * must be named according to the following format: "[ClassName]RecFinalizer" where
 * ClassName is the name of a class that extends edu.ku.brc.specify.datamodel.DataModelObjBase.
 */
public interface UploadedRecFinalizerIFace
{
    /**
     * @param rec
     * @param recNum
     * 
     * Performs final field assignment in preparation for persisting the rec.
     */
    public void finalizeForWrite(final DataModelObjBase rec, int recNum, final Uploader uploader) throws UploaderException;
}
