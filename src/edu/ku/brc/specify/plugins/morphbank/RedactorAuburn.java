/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.util.Vector;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Locality;

/**
 * @author timo
 *
 */
public class RedactorAuburn implements RedactorIFace {

	protected boolean isRedactableConcept(MappingInfo m)
	{
		if (m.getMappedTblId() != Locality.getClassTableId()) 				
		{
			return false;
		}
				
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.plugins.morphbank.RedactorIFace#isRedacted(edu.ku.brc.specify.plugins.morphbank.DarwinCoreSpecimen, edu.ku.brc.specify.plugins.morphbank.MappingInfo)
	 */
	@Override
	public boolean isRedacted(DarwinCoreSpecimen spec, MappingInfo mi) throws Exception
	{
		if (mi.getName().equals("CollectionCode")) //XXX HACK!!!!!!!!!!!!!!!!!!!)
		{
			return true;
		}

		if (!isRedactableConcept(mi))
		{
			return false;
		}


		Integer coID = spec.getCollectionObjectId();
		if (coID == null && spec.getCollectionObject() != null)
		{
			coID = spec.getCollectionObject().getCollectionObjectId();
		}
		
		if (coID == null)
		{
			throw new Exception("Specimen has no key.");
		}

		//very very stupid, but for Auburn, redaction is based on the image file name, not a property of the specimen
		Vector<Object> fileNames = BasicSQLUtils.querySingleCol(DwcMapper.connection, "select OrigFilename from collectionobjectattachment coa "
				+ "inner join attachment att on att.AttachmentID = coa.AttachmentID "
				+ "where coa.CollectionObjectID = " + coID);
		for (Object f : fileNames)
		{
			if (f.toString().toLowerCase().endsWith("a.jpg")) //ALL auburn images are jpg
			{
				return true;
			}
		}
		
		return false;
	}

}
