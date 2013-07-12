/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.util;

import java.io.File;

import edu.ku.brc.helpers.XMLHelper;

/**
 * Class that knows the path to the POJO source and to the DBtableId source file.
 * 
 * @code_status Beta
 * 
 * @author megkumin
 * 
 */
public class DatamodelHelper 
{
    private final static String SPDATAMODELLISTINGFILE    = "specify_tableid_listing.xml";
    private final static String SPWB_DATAMODELLISTINGFILE = "specify_workbench_upload_def.xml";
    
    private static String outputFileName      = "specify_datamodel.xml";
    private static String outputCacheFileName = "specify_datamodel_cache.xml";
    
    
    /**
     * 
     */
    private DatamodelHelper() 
    {
        super();
    }

    /**
     * @return the outputFileName (the standard name of the file and this file is never written over!)
     */
    public static String getOutputFileName()
    {
        return outputFileName;
    }
    
    /**
     * @param outputFileName the outputFileName to set
     */
    public static void setOutputFileName(String outputFileName)
    {
        DatamodelHelper.outputFileName = outputFileName;
    }

    /**
     * @return the name of the file if it was updated.
     */
    public static String getCachedFileName()
    {
        return outputCacheFileName;
    }

    /**
     * Returns full path to POJO source directory.
     * @return the path to the file
     */
    public static String getDataModelSrcDirPath() 
    {
        return "src" + File.separator +
               "edu" + File.separator + "ku" + File.separator + "brc" +
                File.separator + "specify" + File.separator + "datamodel";
    }

    /**
     * Returns file that points to the data model File.
     * 
     * @return the path to the file
     */
    public static File getDatamodelFilePath() 
    {
        File file = new File(XMLHelper.getConfigDirPath(outputCacheFileName));
        if (file.exists())
        {
            return file;
        }
        return new File(XMLHelper.getConfigDirPath(outputFileName));
    }

    /**
     * Returns full path to file in tableId listing file
     * 
     * @return the path to the file
     */
    public static String getTableIdFilePath() 
    {
        return XMLHelper.getConfigDirPath(SPDATAMODELLISTINGFILE);
    }
    
    /**
     * Returns full path to file workbench upload def file
     * 
     * @return the path to the file
     */
    public static String getWorkbenchUploadDefFilePath()
    {
        return XMLHelper.getConfigDirPath(SPWB_DATAMODELLISTINGFILE);
    }
}
