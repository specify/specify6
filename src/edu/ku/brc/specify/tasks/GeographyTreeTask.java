/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import javax.persistence.Transient;

import edu.ku.brc.specify.datamodel.CollectionType;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.forms.FormViewObj;

/**
 * Task that handles the UI for viewing geography data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class GeographyTreeTask extends BaseTreeTask<Geography,GeographyTreeDef,GeographyTreeDefItem>
{
	public static final String GEOGRAPHY = "GeographyTree";
	
	/**
	 * Constructor.
	 */
	public GeographyTreeTask()
	{
        super(GEOGRAPHY, getResourceString(GEOGRAPHY));
        treeDefClass = GeographyTreeDef.class;
        icon = IconManager.getIcon(GEOGRAPHY,IconManager.IconSize.Std24);
        
        menuItemText     = getResourceString("GeographyMenu");
        menuItemMnemonic = getResourceString("GeographyMnemonic");
        starterPaneText  = getResourceString("GeographyStarterPaneText");
        commandTypeString = GEOGRAPHY;
        
        initialize();
	}
    
    @Transient
    @Override
    protected GeographyTreeDef getCurrentTreeDef()
    {
        return CollectionType.getCurrentCollectionType().getGeographyTreeDef();
    }

//    protected void adjustTreeDefForm(FormViewObj form)
//    {
//    }
//    
//    protected void adjustTreeDefItemForm(FormViewObj form)
//    {
//    }
    
    @Override
    public void adjustForm(FormViewObj form)
    {
        log.debug(this.getClass().getSimpleName() + ".adjustForm(" + form.getName() + ") : form data obj = " + form.getDataObj());
        if (form.getDataObj() instanceof Geography || form.getViewDef().getClassName().equals(Geography.class.getName()))
        {
            log.debug("Adjusting Geography data entry form");
            adjustNodeForm(form);
        }
        
        
//        else if (form.getDataObj() instanceof GeographyTreeDef)
//        {
//            adjustTreeDefForm(form);
//        }
//        else if (form.getDataObj() instanceof GeographyTreeDefItem)
//        {
//            adjustTreeDefItemForm(form);
//        }
    }
}
