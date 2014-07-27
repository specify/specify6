/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.util.List;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.plugins.morphbank.CollectionObjectFieldMapper;

/**
 * @author timo
 *
 */
public class DarwinCoreArchiveFieldMapper extends CollectionObjectFieldMapper 
{
	protected final List<Class<?>> extensions;
	public DarwinCoreArchiveFieldMapper(final CollectionObject obj, final Integer dwcMappingId, final List<Class<?>> extensions) throws Exception
	{
		super(obj, dwcMappingId);
		this.extensions = extensions;
	}
	
}
