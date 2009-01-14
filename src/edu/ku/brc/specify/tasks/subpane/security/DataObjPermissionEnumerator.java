/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.AccessionAuthorization;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.ExchangeIn;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.GiftAgent;
import edu.ku.brc.specify.datamodel.GiftPreparation;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgent;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
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
public class DataObjPermissionEnumerator extends PermissionEnumerator 
{
    protected List<SecurityOptionIFace> tableOptions = null;
    
	/**
     * 
     */
    public DataObjPermissionEnumerator()
    {
        super("DO", "ADMININFO_DESC");
    }
    
    /**
     * @param tblInfo
     * @return
     */
    protected boolean isTableOK(final DBTableInfo tblInfo)
    {
        if (tblInfo.getTableId() < 500)
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
                      CollectionRelType.class.isAssignableFrom(tblInfo.getClassObj())))
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
                        Hashtable<String, PermissionOptionPersist> hashItem = new Hashtable<String, PermissionOptionPersist>();
                        hashItem.put("CollectionManager", new PermissionOptionPersist(name, "CollectionManager", true, true, true, true));
                        hashItem.put("Guest",             new PermissionOptionPersist(name, "Guest",             isOK, false, false, false));
                        hashItem.put("DataEntry",         new PermissionOptionPersist(name, "DataEntry",         isOK, isOK, false, true));
                    
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
            
            Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPrefsFromXML("dataobjs.xml");
            
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
