/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Element;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.XMLHelper;

/**
 * @author timo
 *
 * @code_status Alpha
 */
public class ConceptMapUtils
{
	//TODO: more then just the concept name is really needed for the key in the map.
	// concept namespace and other stuff will probably turn out to be necessary.
	protected static Map<String, Vector<MappedFieldInfo>> autoMaps = null;
	
	/**
	 * @return default mappings for darwin core concepts.
	 * 
	 * 
	 */
	public static Map<String, Vector<MappedFieldInfo>> getDefaultDarwinCoreMappings()
	{
		if (autoMaps == null)
		{
	        autoMaps = new HashMap<String, Vector<MappedFieldInfo>>();
			try
	        {
	            Element root       = XMLHelper.readDOMFromConfigDir("dwcdefaultmap.xml");
	            List<?> mapNodes = root.selectNodes("/default_mappings/default_mapping");
	            String prevName = "";
	            Vector<MappedFieldInfo> ams = new Vector<MappedFieldInfo>();
	            for (Object obj : mapNodes)
	            {
	            	String name = XMLHelper.getAttr((Element)obj, "name", null);
	        		if (!"".equals(prevName) && !prevName.equals(name) && ams.size() > 0)
	        		{
	        			autoMaps.put(prevName, ams);
	        			ams = new Vector<MappedFieldInfo>();
	        		}
	        		MappedFieldInfo am = new MappedFieldInfo((Element )obj);
	        		if (am.isActive)
	        		{
	        				ams.add(am);
	        		}	
	                prevName = name;
	            }
	            if (ams.size() > 0)
	            {
	            	autoMaps.put(prevName, ams);
	            }
	        }
	        catch (Exception ex)
	        {
	        	UsageTracker.incrHandledUsageCount();
	            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConceptMapUtils.class, ex);
	            ex.printStackTrace();
	        }
		}
		return autoMaps;
	}

}
