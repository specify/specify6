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
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.tasks.BaseTask;
import edu.ku.brc.specify.tasks.PermissionOptionPersist;

/**
 * This class enumerates Data Objects permissions associated with a principal in a given scope
 * 
 * @code_status Beta
 *
 * Aug 15, 2008
 *
 */
public class TablePermissionEnumerator extends PermissionEnumerator 
{
    protected List<SecurityOptionIFace>   tableOptions = null;
    protected Hashtable<Integer, Boolean> tableIdHash  = null;
    
    /**
     * 
     */
    public TablePermissionEnumerator()
    {
        super("DO", "ADMININFO_DESC");
    }
    
    /**
     * @param tableIds the int array of ids
     */
    public TablePermissionEnumerator(final int[] tableIds)
    {
        this();
        
        setTableIds(tableIds);
    }
    
    /**
     * Sets the usable Table Ids into the enumerator.
     * @param tableIds the int array of ids
     */
    public void setTableIds(final int[] tableIds)
    {
        if (tableIdHash == null)
        {
            tableIdHash = new Hashtable<Integer, Boolean>();
        } else
        {
            tableIdHash.clear();
        }
        
        for (int id : tableIds)
        {
            tableIdHash.put(id, true);
        }
        if (tableOptions != null)
        {
            tableOptions.clear();
        }
        tableOptions = null;
    }
    
    /**
     * @param tblInfo the table info
     * @return true if table is ok
     */
    protected boolean isTableOK(final DBTableInfo tblInfo)
    {
        if (tblInfo.getTableId() < 500 && (tableIdHash == null || tableIdHash.get(tblInfo.getTableId()) != null))
        {
            String shortName = tblInfo.getShortClassName();
            if ((!shortName.startsWith("Workbench") || shortName.equals("Workbench")) &&
                 !shortName.equals("SpecifyUser"))
            {
                if (!(TreeDefItemIface.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      AttributeDef.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectionObjectAttr.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectingEventAttr.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      PreparationAttr.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      AutoNumberingScheme.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectionRelationship.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectionRelType.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      RecordSet.class.isAssignableFrom(tblInfo.getClassObj())))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getSecurityOptions()
     */
    @Override
    protected List<SecurityOptionIFace> getSecurityOptions()
    {
        if (tableOptions == null)
        {
            // Used for creating defaults
            /*if (true)
            {
                XStream xstream = new XStream();
                PermissionOptionPersist.config(xstream);
                try
                {
                    Hashtable<String, Hashtable<String, PermissionOptionPersist>> hash = new Hashtable<String, Hashtable<String, PermissionOptionPersist>>();
                    for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
                    {
                        String name = ti.getName();
                        
                        boolean isOK = true;
                        if (ti.getTableId() == Accession.getClassTableId() ||
                                ti.getTableId() == AccessionAgent.getClassTableId() ||
                                ti.getTableId() == AccessionAuthorization.getClassTableId() ||
                                ti.getTableId() == RepositoryAgreement.getClassTableId() ||
                                ti.getTableId() == Loan.getClassTableId() ||
                                ti.getTableId() == LoanAgent.getClassTableId() ||
                                ti.getTableId() == LoanPreparation.getClassTableId() ||
                                ti.getTableId() == LoanReturnPreparation.getClassTableId() ||
                                ti.getTableId() == ExchangeOut.getClassTableId() ||
                                ti.getTableId() == ExchangeIn.getClassTableId() ||
                                ti.getTableId() == Shipment.getClassTableId() ||
                                ti.getTableId() == Institution.getClassTableId() ||
                                ti.getTableId() == Division.getClassTableId() ||
                                ti.getTableId() == Discipline.getClassTableId() ||
                                ti.getTableId() == Collection.getClassTableId() ||
                                ti.getTableId() == Gift.getClassTableId() ||
                                ti.getTableId() == GiftAgent.getClassTableId() ||
                                ti.getTableId() == GiftPreparation.getClassTableId() ||
                                ti.getTableId() == TaxonTreeDef.getClassTableId() ||
                                ti.getTableId() == StorageTreeDef.getClassTableId() ||
                                ti.getTableId() == GeologicTimePeriodTreeDef.getClassTableId() ||
                                ti.getTableId() == LithoStratTreeDef.getClassTableId() ||
                                ti.getTableId() == GeographyTreeDef.getClassTableId())
                        {
                            isOK = false;
                        }
                        String mgrStr    = SpecifyUserTypes.UserType.Manager.toString();
                        String fulAccStr = SpecifyUserTypes.UserType.FullAccess.toString();
                        String limAccStr = SpecifyUserTypes.UserType.LimitedAccess.toString();
                        String guestStr = SpecifyUserTypes.UserType.Guest.toString();
                        
                        Hashtable<String, PermissionOptionPersist> hashItem = new Hashtable<String, PermissionOptionPersist>();
                        hashItem.put(mgrStr,    new PermissionOptionPersist(name, mgrStr, true, true, true, true));
                        hashItem.put(fulAccStr, new PermissionOptionPersist(name, fulAccStr,             isOK, false, false, false));
                        hashItem.put(limAccStr, new PermissionOptionPersist(name, limAccStr,         isOK, isOK, false, true));
                        hashItem.put(guestStr,  new PermissionOptionPersist(name, guestStr,         isOK, isOK, false, true));
                    
                        hash.put(name, hashItem);
                    }
                    FileUtils.writeStringToFile(new File("dataobjs.xml"), xstream.toXML(hash)); //$NON-NLS-1$
                    //System.out.println(xstream.toXML(config));
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }*/
            tableOptions = new ArrayList<SecurityOptionIFace>();
            
            Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPermsFromXML("dataobjs.xml");
            
            for (DBTableInfo tblInfo : DBTableIdMgr.getInstance().getTables())
            {
                if (isTableOK(tblInfo))
                {
                    SecurityOption securityOption = new SecurityOption(tblInfo.getName(), 
                                                                       tblInfo.getTitle(), 
                                                                       "DO",
                                                                       tblInfo.getName());
                    tableOptions.add(securityOption);
                    
                    if (mainHash != null)
                    {
                        Hashtable<String, PermissionOptionPersist> hash = mainHash.get(tblInfo.getName());
                        if (hash != null)
                        {
                            for (PermissionOptionPersist tp : hash.values())
                            {
                                PermissionIFace defPerm = tp.getDefaultPerms();
                                securityOption.addDefaultPerm(tp.getUserType(), defPerm);
                            }
                        }
                    }

                }
            }
        }
        return tableOptions;
    }
}
