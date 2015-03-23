/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
public class UploaderException extends Exception
{
	protected int status = ABORT_ROW;
	public static final int ABORT_ROW = 10;
	public static final int ABORT_IMPORT = 20;
    public static final int INVALID_DATA = 30;
	
	public UploaderException(final String msg, int abortStatus)
	{
		super(msg);
		this.status = abortStatus;
	}
	
	public UploaderException(final Exception ex)
	{
		super(ex);
	}
    
    public UploaderException(final Exception ex, int abortStatus)
    {
        super(ex);
        this.status = abortStatus;
    }
    
	/**
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}
}
