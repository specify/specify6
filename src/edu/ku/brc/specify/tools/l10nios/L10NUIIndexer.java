/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools.l10nios;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Dec 5, 2012
 *
 */
public class L10NUIIndexer
{
    private static final Logger  log = Logger.getLogger(L10NUIIndexer.class);
    
    protected HashSet<String> hashSet = new HashSet<String>();
    protected File            rootDir;
    protected String          contents;
    
    /**
     * @param rootDir
     */
    public L10NUIIndexer(final File rootDir)
    {
        super();
        this.rootDir = rootDir;
    }

    /**
     * @param file
     */
    public void load(final File file)
    {
        try
        {
            contents = FileUtils.readFileToString(file);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param idStr
     * @return
     */
    public boolean hasId(final String idStr)
    {
        if (contents != null)
        {
            String key = String.format("id=\"%s\"", idStr);
            return contents.contains(key);
        }
        return false;
    }
}
