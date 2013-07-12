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
package edu.ku.brc.specify.prefs;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 31, 2008
 *
 */
public class TreePrefs extends GenericPrefsPanel
{
    private static final String PREF_NAME = "TreeEditor.Rank.Threshold.";
    
    protected Hashtable<Class<?>, DefModelFiller<?,?,?>> fillers = new Hashtable<Class<?>, DefModelFiller<?,?,?>>();
    
    /**
     * 
     */
    public TreePrefs()
    {
        super();
        createUI();
    }
    
    /**
     * It's a shame I have to do it for each tree, but the Josh implemented 
     * the interfaces 
     */
    protected void createUI()
    {
        createForm("Preferences", "TreeOptions");
        
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            DefModelFiller<Taxon, TaxonTreeDef, TaxonTreeDefItem> txFiller = new DefModelFiller<Taxon, TaxonTreeDef, TaxonTreeDefItem>();
            txFiller.fill("1", session, AppContextMgr.getInstance().getClassObject(TaxonTreeDef.class), Taxon.class);
            fillers.put(Taxon.class, txFiller);
            
            DefModelFiller<Geography, GeographyTreeDef, GeographyTreeDefItem> geoFiller = new DefModelFiller<Geography, GeographyTreeDef, GeographyTreeDefItem>();
            geoFiller.fill("2", session, AppContextMgr.getInstance().getClassObject(GeographyTreeDef.class), Geography.class);
            fillers.put(Geography.class, geoFiller);
            
            LithoStratTreeDef lstd = AppContextMgr.getInstance().getClassObject(LithoStratTreeDef.class);
            if (lstd != null)
            {
                DefModelFiller<LithoStrat, LithoStratTreeDef, LithoStratTreeDefItem> lithoFiller = new DefModelFiller<LithoStrat, LithoStratTreeDef, LithoStratTreeDefItem>();
                lithoFiller.fill("3", session, lstd, LithoStrat.class);
                fillers.put(LithoStrat.class, lithoFiller);
            }
        
            GeologicTimePeriodTreeDef gtptd = AppContextMgr.getInstance().getClassObject(GeologicTimePeriodTreeDef.class);
            if (gtptd != null)
            {
                DefModelFiller<GeologicTimePeriod, GeologicTimePeriodTreeDef, GeologicTimePeriodTreeDefItem> gtpFiller = new DefModelFiller<GeologicTimePeriod, GeologicTimePeriodTreeDef, GeologicTimePeriodTreeDefItem>();
                gtpFiller.fill("4", session, gtptd, GeologicTimePeriod.class);
                fillers.put(GeologicTimePeriod.class, gtpFiller);
            }
            
            DefModelFiller<Storage, StorageTreeDef, StorageTreeDefItem> storageFiller = new DefModelFiller<Storage, StorageTreeDef, StorageTreeDefItem>();
            storageFiller.fill("5", session, AppContextMgr.getInstance().getClassObject(StorageTreeDef.class), Storage.class);
            fillers.put(Storage.class, storageFiller);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreePrefs.class, ex);
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsTree";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        super.savePrefs();
        
        for (DefModelFiller<?,?,?> filler : fillers.values())
        {
            filler.setValueIntoPref();
        }
    }
    
    
    //------------------------------------------------------------------
    //-- Inner Classes
    //------------------------------------------------------------------
    
    class DefModelFiller<T extends Treeable<T,D,I>,
                         D extends TreeDefIface<T,D,I>,
                         I extends TreeDefItemIface<T,D,I>>
    {
        protected String   id;
        protected Class<?> clazz;
        
        public DefModelFiller()
        {
            // TODO Auto-generated constructor stub
        }
        
        /**
         * @param fillerId
         * @param session
         * @param tdi
         * @param fillerClass
         */
        public void fill(final String                   fillerId,
                         final DataProviderSessionIFace session, 
                         final TreeDefIface<T,D,I>      tdi,
                         final Class<?>                 fillerClass)
        {
            this.id    = fillerId;
            this.clazz = fillerClass;
            
            ValComboBox cbx = form.getCompById(fillerId);
            if (tdi == null)
            {
                cbx.setEnabled(false);
                return;
            }
            
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            cbx.getComboBox().setModel(model);
            
            int rankId = AppPreferences.getRemote().getInt(PREF_NAME+fillerClass.getSimpleName(), 0);

            //session.attach(tdi); It seems there is no reason to attach the treedef? Skipping attach fixes bug #6932.
            Vector<TreeDefItemIface<T,D,I>> list = new Vector<TreeDefItemIface<T,D,I>>(tdi.getTreeDefItems());
            
            Comparator<TreeDefItemIface<T,D,I>> itemComparator = new Comparator<TreeDefItemIface<T,D,I>>()
            {
                public int compare(TreeDefItemIface<T,D,I> o1, TreeDefItemIface<T,D,I> o2)
                {
                    return o1.getRankId().compareTo(o2.getRankId());
                }
            };
            Collections.sort(list, itemComparator);
            
            int inx = -1;
            int i   = 0;
            for (TreeDefItemIface<T,D,I> ttdi : list)
            {
                if (ttdi.getRankId() == rankId)
                {
                    inx = i;
                }
                model.addElement(ttdi);
                i++;
            }
            if (inx > -1)
            {
                cbx.getComboBox().setSelectedIndex(inx);
            }
        }
        
        @SuppressWarnings("unchecked")
        public void setValueIntoPref()
        {
            ValComboBox taxonCBX = form.getCompById(id);
            
            TreeDefItemIface<T,D,I> tdi = (TreeDefItemIface<T,D,I>)taxonCBX.getComboBox().getSelectedItem();
            if (tdi != null)
            {
                AppPreferences.getRemote().put(PREF_NAME+clazz.getSimpleName(), Integer.toString(tdi.getRankId()));
            }
        }

    }
}
