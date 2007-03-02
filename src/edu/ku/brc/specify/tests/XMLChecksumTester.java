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
/**
 * 
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
