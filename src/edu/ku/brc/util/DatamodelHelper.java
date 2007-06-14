/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.util;

import java.io.File;

import edu.ku.brc.helpers.XMLHelper;

/**
 * Class that finds the path to hbm files
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 * 
 */
public class DatamodelHelper 
{
	// Static Data Members
	//private static final Logger log = Logger.getLogger(DatamodelHelper.class);

	public DatamodelHelper() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns full path to file in hbm directory
	 * 
	 * @param fileName
	 *            the name of the file to be read
	 * @return the path to the file
	 */
	public static String getDataModelSrcDirPath() 
	{
        //return ClassLoader.getSystemResource("Accession.hbm.xml").getFile();
        /*String fileName = "Accession.hbm.xml";
		String path = new File(".").getAbsolutePath();
		if (path.endsWith(".")) {
			path = path.substring(0, path.length() - 2);
		}*/
		return"src" + File.separator
				+ "edu" + File.separator + "ku" + File.separator + "brc"
				+ File.separator + "specify" + File.separator + "datamodel";
                
        //return "OldHBMs";
	}

	/**
	 * Gets the path to UI based hbm files
	 * 
	 * @return - path to ui based hbm files String -
	 */
	public static String getUiHbmDirPath() 
	{
	    // return ClassLoader.getSystemResource("PickList.hbm.xml").getFile();
		String fileName = "PickList.hbm.xml";
		String path = new File(".").getAbsolutePath();
		if (path.endsWith(".")) {
			path = path.substring(0, path.length() - 2);
		}
		return path + File.separator + File.separator + "src" + File.separator
				+ "edu" + File.separator + "ku" + File.separator + "brc"
				+ File.separator + "ui" + File.separator + "db"
				+ File.separator + fileName;
	}

	/**
	 * Returns full path to file in datamodel File
	 * 
	 * @return the path to the file
	 */
	public static String getDatamodelFilePath() 
	{
		return XMLHelper.getConfigDirPath("specify_datamodel.xml");
	}

	/**
	 * Returns full path to file in tableId listing file
	 * 
	 * @return the path to the file
	 */
	public static String getTableIdFilePath() 
	{
		return XMLHelper.getConfigDirPath("specify_tableid_listing.xml");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}
}
