/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tools.schemalocale;

import java.util.Locale;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2007
 *
 */
public interface LocalizableStrIFace
{
    /**
     * @return
     */
    public abstract String getText();

    /**
     * @param text the text to set
     */
    public abstract void setText(String text);

    /**
     * @return the country
     */
    public abstract String getCountry();

    /**
     * @param country the country to set
     */
    public abstract void setCountry(String country);

    /**
     * @return the language
     */
    public abstract String getLanguage();

    /**
     * @param language the language to set
     */
    public abstract void setLanguage(String language);

    /**
     * @return the variant
     */
    public abstract String getVariant();

    /**
     * @param variant the variant to set
     */
    public abstract void setVariant(String variant);
    
    /**
     * @param locale
     * @return
     */
    public abstract boolean isLocale(Locale locale);
    
}
