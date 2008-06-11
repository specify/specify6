/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.ui;

import java.util.Vector;

import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
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
        super();
        this.name          = "NumericCatalogFormatter"; //$NON-NLS-1$
        this.title         = UIRegistry.getResourceString("CatalogNumberUIFieldFormatter.NumericCatalogFormatter"); //$NON-NLS-1$;
        this.isIncrementer = true;
        this.length        = 9;
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
}
