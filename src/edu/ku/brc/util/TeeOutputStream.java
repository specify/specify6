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
package edu.ku.brc.util;

import java.io.PrintStream;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 24, 2011
 *
 */
public class TeeOutputStream extends PrintStream 
{
    PrintStream out;

    public TeeOutputStream(PrintStream out1, PrintStream out2)
    {
        super(out1);
        this.out = out2;
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#write(byte[], int, int)
     */
    @Override
    public void write(byte buf[], int off, int len)
    {
        try
        {
            super.write(buf, off, len);
            out.write(buf, off, len);
            
        } catch (Exception e)
        {
        }
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#flush()
     */
    @Override
    public void flush()
    {
        super.flush();
        out.flush();
    }
}