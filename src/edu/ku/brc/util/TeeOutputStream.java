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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

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

    public TeeOutputStream(final PrintStream out1, final PrintStream out2)
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
     * @see java.io.PrintStream#append(char)
     */
    @Override
    public PrintStream append(char c)
    {
        out.append(c);
        return super.append(c);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#append(java.lang.CharSequence, int, int)
     */
    @Override
    public PrintStream append(CharSequence csq, int start, int end)
    {
        out.append(csq, start, end);
        return super.append(csq, start, end);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#append(java.lang.CharSequence)
     */
    @Override
    public PrintStream append(CharSequence csq)
    {
        out.append(csq);
        return super.append(csq);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#format(java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream format(String format, Object... args)
    {
        out.format(format, args);
        return super.format(format, args);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(boolean)
     */
    @Override
    public void print(boolean b)
    {
        out.print(b);
        super.print(b);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(char)
     */
    @Override
    public void print(char c)
    {
        out.print(c);
        super.print(c);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(char[])
     */
    @Override
    public void print(char[] s)
    {
        out.print(s);
        super.print(s);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(double)
     */
    @Override
    public void print(double d)
    {
        out.print(d);
        super.print(d);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(float)
     */
    @Override
    public void print(float f)
    {
        out.print(f);
        super.print(f);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(int)
     */
    @Override
    public void print(int i)
    {
        out.print(i);
        super.print(i);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(long)
     */
    @Override
    public void print(long l)
    {
        out.print(l);
        super.print(l);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(java.lang.Object)
     */
    @Override
    public void print(Object obj)
    {
        out.print(obj);
        super.print(obj);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#print(java.lang.String)
     */
    @Override
    public void print(String s)
    {
        out.print(s);
        super.print(s);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#printf(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream printf(Locale l, String format, Object... args)
    {
        out.printf(l, format, args);
        return super.printf(l, format, args);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#printf(java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream printf(String format, Object... args)
    {
        out.printf(format, args);
        return super.printf(format, args);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println()
     */
    @Override
    public void println()
    {
        out.println();
        super.println();
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(boolean)
     */
    @Override
    public void println(boolean x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(char)
     */
    @Override
    public void println(char x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(char[])
     */
    @Override
    public void println(char[] x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(double)
     */
    @Override
    public void println(double x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(float)
     */
    @Override
    public void println(float x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(int)
     */
    @Override
    public void println(int x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(long)
     */
    @Override
    public void println(long x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(java.lang.Object)
     */
    @Override
    public void println(Object x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#println(java.lang.String)
     */
    @Override
    public void println(String x)
    {
        out.println(x);
        super.println(x);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#write(int)
     */
    @Override
    public void write(int b)
    {
        out.println(b);
        super.write(b);
    }

    /* (non-Javadoc)
     * @see java.io.FilterOutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        out.println(b);
        super.write(b);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close()
    {
        out.close();
        super.close();
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#format(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream format(Locale l, String format, Object... args)
    {
        out.format(l, format, args);
        return super.format(l, format, args);
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