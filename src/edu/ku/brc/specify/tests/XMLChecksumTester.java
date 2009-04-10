/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tests;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.util.XMLChecksumUtil;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Mar 1, 2007
 *
 */
public class XMLChecksumTester extends TestCase
{
    /**
     * Test method for {@link edu.ku.brc.util.XMLChecksumUtil#checkSignature(java.io.File, java.lang.String)}.
     */
    @Test
    public void testChecksum() throws IOException
    {
        XMLChecksumUtil.createChecksum();
        
        File[] files = FileUtils.convertFileCollectionToFileArray(FileUtils.listFiles(XMLHelper.getConfigDir(null), new String[] {"xml"}, true));
        for (File file : files)
        {
            assertTrue(XMLChecksumUtil.checkSignature(file));
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception 
    {
        //checksumFile.delete();
    }
}
