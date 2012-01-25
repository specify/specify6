/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

/**
 * @author timo
 *
 */
public class NonRedactor implements RedactorIFace {

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.plugins.morphbank.RedactorIFace#isRedacted(edu.ku.brc.specify.plugins.morphbank.DarwinCoreSpecimen, edu.ku.brc.specify.plugins.morphbank.MappingInfo)
	 */
	@Override
	public boolean isRedacted(DarwinCoreSpecimen spec, MappingInfo mi) throws Exception
	{
		return false;
	}

}
