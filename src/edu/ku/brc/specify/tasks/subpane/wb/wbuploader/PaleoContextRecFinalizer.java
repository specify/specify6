/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.PaleoContext;

/**
 * @author timo
 *
 */
public class PaleoContextRecFinalizer implements UploadedRecFinalizerIFace
{
    protected static final Logger                   log                      = Logger.getLogger(PaleoContextRecFinalizer.class);

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadedRecFinalizerIFace#finalizeForWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader)
	 */
	@Override
	public void finalizeForWrite(DataModelObjBase rec, int recNum,
			Uploader uploader) throws UploaderException
	{
		//Biostrat is not supported now. Because of assumption in wb mapper code,
		//it is always being set to the same value as ChronoStrat.
		//Clearing here it is easy fix.
		log.warn("finalizing PaleoContext: clearing Biostrat.");
		PaleoContext gtp = (PaleoContext )rec;
		gtp.setBioStrat(null);
	}

}
