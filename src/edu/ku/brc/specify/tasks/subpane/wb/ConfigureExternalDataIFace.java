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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public interface ConfigureExternalDataIFace
{
    public enum Status {None, Valid, Error, Cancel}
    
    /**
     * Returns whether the configuration was valid.
     * @return the status.
     */
    public Status getStatus();
    
    /**
     * configures import/export settings for file.
     * 
     * @param file
     */
    public void readConfig(final File file);

     /**
     * does the first row of data contain column names?
     * 
     * @return
     */
    public boolean getFirstRowHasHeaders();
    
    /**
     * @param value
     */
    public void setFirstRowHasHeaders(boolean value);

    // 
    /**
     * the columns in the file.
     * 
     * @return
     */
    public Vector<ImportColumnInfo> getColInfo();

    /**
     * the file containing the data to be imported.
     * 
     * @return
     */
    public File getFile();
    
    /**
     * @return 
     */
    public String getFileName();

    /** 
     * if interactive then column headers, separators, etc are obtained from user. else
     * prefs/defaults are used.
     * 
     * @param arg to be or not to be interactive
     */
    public void setInteractive(boolean arg);

    /**
     * @return the properties for the configuration
     */
    public Properties getProperties();
    
    /**
     * @param headers captions for the columns headings. 
     */
    public void setHeaders(String[] headers);
}
