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
package edu.ku.brc.af.ui.forms.formatters;

/**
 * Checked exception meaning that a formatter invalidates existing data.
 * 
 * @author ricardo
 *
 * @code_status Alpha
 *
 * Created Date: Jun 26, 2008
 *
 */

public class UIFieldFormatterInvalidatesExistingValueException extends Exception
{
	private String faultyFormat;
	private Object invalidatedValue;
	
	/*
	 * Constructor
	 */
	public UIFieldFormatterInvalidatesExistingValueException(String message, String faultyFormat, Object invalidatedValue) 
	{
		super(message);
		this.faultyFormat = faultyFormat;
		this.invalidatedValue = invalidatedValue;
	}

	public String getFaultyFormat() 
	{
	    return faultyFormat;
	}

	/**
	 * @return
	 */
	public Object getInvalidatedValue()
	{
		return invalidatedValue;
	}

}
