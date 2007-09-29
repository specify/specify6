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
package edu.ku.brc.specify.tools.fielddesc;

import java.util.Locale;

public interface LocalizedStrIFace
{
    /**
     * @return the text
     */
    public abstract String getText();

    /**
     * @return the country
     */
    public abstract String getCountry();

    /**
     * @return the lang
     */
    public abstract String getLanguage();

    /**
     * @return the variant
     */
    public abstract String getVariant();

    /**
     * @param text the text to set
     */
    public abstract void setText(String text);

    /**
     * @param country the country to set
     */
    public abstract void setCountry(String country);

    /**
     * @param lang the lang to set
     */
    public abstract void setLanguage(String lang);

    /**
     * @param variant the variant to set
     */
    public abstract void setVariant(String variant);

    public abstract boolean isLocale(final Locale locale);

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;
}
