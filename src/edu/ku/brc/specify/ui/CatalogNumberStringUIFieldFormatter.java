/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.ui;

import java.util.Vector;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.util.Pair;

/**
 * This class is used for formatting String CatalogNumbers with no format (only a length constraint).
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * May 23, 2008
 *
 */
public class CatalogNumberStringUIFieldFormatter extends BaseUIFieldFormatter implements UIFieldFormatterIFace
{
    /**
     * Constructs a string based non-formatter formatter.
     */
    public CatalogNumberStringUIFieldFormatter()
    {
        super();
        
        this.name      = "CatalogNumberString";
        this.title     = UIRegistry.getResourceString("SpecifyUIFieldFormatterMgr.StringCatalogFormatter");
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBFieldInfo fi = ti.getFieldByName("catalogNumber");
        
        this.length                 = fi.getLength();
        this.uiLength               = 10;
        this.isNumericCatalogNumber = false;
        this.isIncrementer          = false;
        this.autoNumber             = null;
        
        pattern    = UIFieldFormatterMgr.getFormatterPattern(isIncrementer, UIFieldFormatterField.FieldType.anychar, length);
        
        field      = new UIFieldFormatterField(UIFieldFormatterField.FieldType.anychar, length, pattern, isIncrementer, false); 
        fields     = new Vector<UIFieldFormatterField>();
        fields.add(field);
        incPos     = new Pair<Integer, Integer>(0, length);
        
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.BaseUIFieldFormatter#isLengthOK(int)
     */
    @Override
    public boolean isLengthOK(int lengthOfData)
    {
        return lengthOfData < length;
    }
    
}
