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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.util.Vector;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This class is used for formatting numeric CatalogNumbers.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jun 29, 2007
 *
 */
public class CatalogNumberUIFieldFormatter extends BaseUIFieldFormatter implements UIFieldFormatterIFace
{
    /**
     * 
     */
    public CatalogNumberUIFieldFormatter()
    {
    	//this(14);
    	this(9);
    }

    public CatalogNumberUIFieldFormatter(int length)
    {
        super();
        this.name          = "NumericCatalogFormatter"; //$NON-NLS-1$
        this.title         = UIRegistry.getResourceString("CatalogNumberUIFieldFormatter.NumericCatalogFormatter"); //$NON-NLS-1$;
        this.isIncrementer = true;
        this.length        = length;
        this.uiLength      = length;
        this.isNumericCatalogNumber = true;
        this.autoNumber    = null;
        
        pattern = UIFieldFormatterMgr.getFormatterPattern(isIncrementer, UIFieldFormatterField.FieldType.numeric, length);
        
        if (isNumericCatalogNumber)
        {
            field      = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, length, pattern, isIncrementer, false); 
            fields     = new Vector<UIFieldFormatterField>();
            fields.add(field);
            incPos     = new Pair<Integer, Integer>(0, length);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.BaseUIFieldFormatter#setLength(int)
     */
    public void setLength(int length)
    {
    	this.length = length;
        pattern = UIFieldFormatterMgr.getFormatterPattern(isIncrementer, UIFieldFormatterField.FieldType.numeric, length);
        if (isNumericCatalogNumber)
        {
            field      = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, length, pattern, isIncrementer, false); 
            fields     = new Vector<UIFieldFormatterField>();
            fields.add(field);
            incPos     = new Pair<Integer, Integer>(0, length);
        }
    }
    
    /**
     * @return isNumericCatalogNumber
     */
    public boolean isNumericCatalogNumber() {
    	return this.isNumericCatalogNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.BaseUIFieldFormatter#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        return CollectionObject.class;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.ui.BaseUIFieldFormatter#addAttrToXML(java.lang.StringBuilder)
	 */
	@Override
	protected void addAttrToXML(StringBuilder sb) {
		super.addAttrToXML(sb);
        xmlAttr(sb, "length", getLength()); //$NON-NLS-1$

	}

    @Override
    public int getMinLength() {
        return getLength();
    }

}
