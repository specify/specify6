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
