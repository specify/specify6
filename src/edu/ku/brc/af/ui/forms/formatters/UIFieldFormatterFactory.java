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
package edu.ku.brc.af.ui.forms.formatters;

import edu.ku.brc.af.core.db.DBFieldInfo;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 *
 */
public abstract class UIFieldFormatterFactory
{
	protected DBFieldInfo fieldInfo;
	
	/**
	 * Constructor
	 * 
	 * @param fieldInfo
	 */
	public UIFieldFormatterFactory(final DBFieldInfo fieldInfo)
	{
		this.fieldInfo = fieldInfo;
	}
	
	/**
	 * Creates a UIFieldFormatter that corresponds to the format string and checks whether it violates 
	 * any existing data on database. It calls abstract method createFormat(String)
	 * 
	 * @param formattingString String defining the format
	 * @param sampler Field value sampler used to check whether formatter invalidates existing data
	 * @return New formatter corresponding to the provided format string
	 * @throws UIFieldFormatterParsingException
	 */
	public UIFieldFormatter createFormat(final String formattingString, 
										 final UIFieldFormatterSampler sampler) 
		throws UIFieldFormatterParsingException, UIFieldFormatterInvalidatesExistingValueException
	{
		UIFieldFormatter formatter = createFormat(formattingString);
		sampler.isValid(formatter); // exception must be thrown if not valid
		return formatter;
	}
	
	/**
	 * Creates a UIFieldFormatter that corresponds to the format string or throws an exception if the
	 * format string is invalid. Implementors are responsible for a given field type (number, text, etc)
	 * 
	 * @param formattingString String defining the format
	 * @return New formatter corresponding to the provided format string
	 * @throws UIFieldFormatterParsingException
	 */
	protected abstract UIFieldFormatter createFormat(final String formattingString) throws UIFieldFormatterParsingException;

	/**
	 * Returns the help message (in HTML format) to be displayed in the formatter dialog.
	 * @return the help message (in HTML format) to be displayed in the formatter dialog.
	 */
	public abstract String getHelpHtml();
}
