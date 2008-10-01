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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.specify.config.init.DataBuilder.createGeographyTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeologicTimePeriodTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

public class DisciplineBusRules extends BaseBusRules implements CommandListener
{   
    //private final Logger         log      = Logger.getLogger(DisciplineBusRules.class);
    private static final String CMD_TYPE = "DisciplineBusRules"; 
    /**
     * 
     */
    public DisciplineBusRules()
    {
        super(Discipline.class);
        CommandDispatcher.register(CMD_TYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        CommandDispatcher.unregister(CMD_TYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (dataObj != null)
        {
            Discipline dis     = AppContextMgr.getInstance().getClassObject(Discipline.class);
            Discipline dataDis = (Discipline)dataObj;
            if (dis.getId() != null && dataDis.getId() != null && dis.getId().equals(dataDis.getId()))
            {
                return false;
            }
            
            reasonList.clear();
            
            if (!okToDelete("collection", "DisciplineID", ((FormDataObjIFace)dataObj).getId()))
            {
                return false;
            }
            
            if (!okToDelete("attributedef", "DisciplineID", ((FormDataObjIFace)dataObj).getId()))
            {
                return false;
            }
            
            if (!okToDelete("spappresourcedir", "DisciplineID", ((FormDataObjIFace)dataObj).getId()))
            {
                return false;
            }
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj,session);
        
        Discipline ct = (Discipline)dataObj;
        if (ct.getTaxonTreeDef() == null)
        {
            TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
            ct.setTaxonTreeDef(taxonTreeDef);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Discipline)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(Discipline.getClassTableId());
            return getLocalizedMessage("DISCIPLINE_DELETED", ti.getTitle());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");

        Discipline discipline = (Discipline)newDataObj;

        taxonTreeDef.setDiscipline(discipline);
        discipline.setTaxonTreeDef(taxonTreeDef);
        
        geoTreeDef.getDisciplines().add(discipline);
        discipline.setGeographyTreeDef(geoTreeDef);

        gtpTreeDef.getDisciplines().add(discipline);
        discipline.setGeologicTimePeriodTreeDef(gtpTreeDef);

        lithoStratTreeDef.getDisciplines().add(discipline);
        discipline.setLithoStratTreeDef(lithoStratTreeDef);
        
        
        // create the geo tree def items
        /*GeographyTreeDefItem root    = createGeographyTreeDefItem(null, geoTreeDef, "GeoRoot", 0);
        root.setIsEnforced(true);
        GeographyTreeDefItem cont    = createGeographyTreeDefItem(root, geoTreeDef, "Continent", 100);
        GeographyTreeDefItem country = createGeographyTreeDefItem(cont, geoTreeDef, "Country", 200);
        GeographyTreeDefItem state   = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
        state.setIsInFullName(true);
        GeographyTreeDefItem county  = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);
        county.setIsInFullName(true);
        county.setTextAfter(" Co.");
        */
        
        
        // Create a GeologicTimePeriod tree definition
        /*
        GeologicTimePeriodTreeDefItem defItemLevel0 = createGeologicTimePeriodTreeDefItem(null, taxonTreeDef, "Level 0", 0);
        GeologicTimePeriodTreeDefItem defItemLevel1 = createGeologicTimePeriodTreeDefItem(defItemLevel0, taxonTreeDef, "Level 1", 100);
        GeologicTimePeriodTreeDefItem defItemLevel2 = createGeologicTimePeriodTreeDefItem(defItemLevel1, taxonTreeDef, "Level 2", 200);
        GeologicTimePeriodTreeDefItem defItemLevel3 = createGeologicTimePeriodTreeDefItem(defItemLevel2, taxonTreeDef, "Level 3", 300);
        */
        
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
    }
    
    public void disciplinehasBeenAdded(Division division, final Discipline discipline)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("DivisionSaved"))
        {
            Division divsion = (Division)cmdAction.getData();
            formViewObj.getMVParent().getMultiViewParent().setData(divsion);
            
        } else if (cmdAction.isAction("DivisionError"))
        {
        }
        
    }
    
}
