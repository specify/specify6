/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import java.util.List;

/**
 * @author timo
 * 
 * Gets records that image files should be attached to by parsing the image file names.
 *
 */
public interface FileNameParserIFace
{
	/**
	 * @return the table id
	 */
	public Integer getTableId();
	/**
	 * @param fileName
	 * @return a list of keys for records identified by the fileName
	 */
	public List<Integer> getRecordIds(String fileName);
}
