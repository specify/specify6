/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.services;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace;
import edu.ku.brc.specify.tools.schemalocale.PickListEditorDlg;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Re-factored code from the PickListEditorDlg.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Jun 24, 2011
 *
 */
public class PickListUtils
{

    
    public static  List<PickList> getPickLists(final LocalizableIOIFace localizableIO,
                                               final boolean doAll, 
                                               final boolean isSystemPL)
    {
        List<PickList> items = null;

        if (localizableIO != null)
        {
            items = localizableIO.getPickLists(null);

        } else
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();

                Vector<PickList> plItems = new Vector<PickList>();

                String sysPLSQL = "";
                if (!doAll)
                {
                    sysPLSQL = " AND isSystem = " + (isSystemPL ? 1 : 0);
                }

                String sqlStr = "FROM PickList WHERE collectionId = COLLID" + sysPLSQL;
                String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sqlStr);
                List<?> pickLists = session.getDataList(sql);

                for (Object obj : pickLists)
                {
                    PickList pl = (PickList) obj;
                    pl.getPickListItems().size();
                    // System.out.println(pl.getName()+" - "+pl.getPickListItems().size());
                    plItems.add(pl);
                }
                items = plItems;
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListEditorDlg.class, ex);

            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }

        java.util.Collections.sort(items);

        return items;
    }
  
    /**
     * @return name created from discipline and collection names
     */
    protected static String getPickListXMLName()
    {
        Discipline dsp = AppContextMgr.getInstance().getClassObject(Discipline.class);   
        Collection col = AppContextMgr.getInstance().getClassObject(Collection.class);
        
        return String.format("%s_%s.xml", dsp.getName(), col.getCollectionName());
    }
    
    /**
     * @param bpl
     * @param currCollection
     * @return
     */
    private static PickList createPickList(final BldrPickList bpl, final Collection currCollection)
    {
        PickList pickList = DataBuilder.createPickList(bpl.getName(), bpl.getType(), bpl.getTableName(),
                                                       bpl.getFieldName(), bpl.getFormatter(), bpl.getReadOnly(),
                                                       bpl.getSizeLimit(), bpl.getIsSystem(), bpl.getSortType(), 
                                                       currCollection);
        pickList.setIsSystem(bpl.getIsSystem());
        pickList.setTimestampCreated(bpl.getTimestampCreated());
        pickList.setTimestampModified(bpl.getTimestampModified());
        pickList.setVersion(bpl.getVersion());
        pickList.setCollection(currCollection);

        if (bpl.getItems() != null)
        {
            for (BldrPickListItem item : bpl.getItems())
            {
                PickListItem pli = (PickListItem) pickList.addItem(item.getTitle(), item.getValue(), item.getOrdinal());
                pli.setTimestampCreated(item.getTimestampCreated());
                pli.setTimestampModified(item.getTimestampModified());
                pli.setVersion(item.getVersion());
            }
        }
        return pickList;
    }

    
    /**
     * @param localizableIO
     */
    public static boolean importPickLists(final LocalizableIOIFace localizableIO,
                                       final Collection collection)
    {
        // Apply is Import All PickLists
        
        FileDialog dlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), getResourceString(getI18n("PL_IMPORT")), FileDialog.LOAD);
        dlg.setDirectory(UIRegistry.getUserHomeDir());
        dlg.setFile(getPickListXMLName());
        UIHelper.centerAndShow(dlg);
        
        String dirStr   = dlg.getDirectory();
        String fileName = dlg.getFile();
        if (StringUtils.isEmpty(dirStr) || StringUtils.isEmpty(fileName))
        {
            return false;
        }
        
        final String path = dirStr + fileName;
        
        File file = new File(path);
        if (!file.exists())
        {
            UIRegistry.showLocalizedError(getI18n("PL_FILE_NOT_EXIST"), file.getAbsoluteFile());
            return false;
        }
        List<BldrPickList> bldrPickLists = DataBuilder.getBldrPickLists(null, file);
        
        Integer cnt    = null;
        boolean wasErr = false;
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();

            HashMap<String, PickList> plHash = new HashMap<String, PickList>();
            List<PickList> items = getPickLists(localizableIO, true, false);
            
            for (PickList pl : items)
            {
                plHash.put(pl.getName(), pl);
                //System.out.println("["+pl.getName()+"]");
            }
            
            for (BldrPickList bpl : bldrPickLists)
            {
                PickList pickList = plHash.get(bpl.getName());
                //System.out.println("["+bpl.getName()+"]["+(pickList != null ? pickList.getName() : "null") + "]");
                if (pickList == null)
                {
                    // External PickList is new
                    pickList = createPickList(bpl, collection);
                    session.saveOrUpdate(pickList);
                    if (cnt == null) cnt = 0;
                    cnt++;
                    
                } else if (!pickListsEqual(pickList, bpl))
                {
                    session.delete(pickList);
                    collection.getPickLists().remove(pickList);
                    pickList = createPickList(bpl, collection);
                    session.saveOrUpdate(pickList);
                    collection.getPickLists().add(pickList);
                    if (cnt == null) cnt = 0;
                    cnt++;
                }
            }
            session.commit();
            
        } catch (Exception ex)
        {
            wasErr = true;
            if (session != null) session.rollback();
            
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListEditorDlg.class, ex);
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        String key = wasErr ? "PL_ERR_IMP" : cnt != null ? "PL_WASIMPORT" : "PL_MATCHIMP";
        
        UIRegistry.displayInfoMsgDlgLocalized(getI18n(key), cnt);
        
        return true;
    }
    
    public static boolean equals(Boolean v1, Boolean v2) {
        return (v1 == null ? v2 == null : v1.equals(v2));
    }

    public static boolean equals(Byte v1, Byte v2) {
        return (v1 == null ? v2 == null : v1.equals(v2));
    }

    public static boolean equals(Integer v1, Integer v2) {
        return (v1 == null ? v2 == null : v1.equals(v2));
    }

    private static Comparator<PickListItem> pliComparatorOrd = new Comparator<PickListItem>()
    {
        @Override
        public int compare(PickListItem o1, PickListItem o2)
        {
            return o1.getOrdinal().compareTo(o2.getOrdinal());
        }
    };
    
    private static Comparator<PickListItem> pliComparatorTitle = new Comparator<PickListItem>()
    {
        @Override
        public int compare(PickListItem o1, PickListItem o2)
        {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };
    
    private static Comparator<BldrPickListItem> bldrPliComparatorTitle = new Comparator<BldrPickListItem>()
    {
        @Override
        public int compare(BldrPickListItem o1, BldrPickListItem o2)
        {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };
    
    /**
     * @param pl
     * @param bpl
     * @return
     */
    private static boolean pickListsEqual(final PickList pl, final BldrPickList bpl)
    {
        if (!StringUtils.equals(pl.getName(), bpl.getName())) return false;
        if (!StringUtils.equals(pl.getTableName(), bpl.getTableName())) return false;
        if (!StringUtils.equals(pl.getFieldName(), bpl.getFieldName())) return false;
        if (!StringUtils.equals(pl.getFormatter(), bpl.getFormatter())) return false;
        
        if (!equals(pl.getType(), bpl.getType())) return false;
        if (!equals(pl.getReadOnly(), bpl.getReadOnly())) return false;
        if (!equals(pl.getSizeLimit(), bpl.getSizeLimit())) return false;
        if (!equals(pl.getIsSystem(), bpl.getIsSystem())) return false;
        if (!equals(pl.getSortType(), bpl.getSortType())) return false;
        
        Vector<PickListItem>     plis  = new Vector<PickListItem>(pl.getPickListItems());
        Vector<BldrPickListItem> bplis = bpl.getItems();
        
        if ((plis.size() == 0) && (bplis == null || bplis.size() == 0)) return true;
        
        if (plis.size() != bplis.size()) return false;
        
        if (pl.getSortType() == PickListIFace.PL_ORDINAL_SORT)
        {
            Collections.sort(plis, pliComparatorOrd);
            Collections.sort(bplis);
        } else
        {
            Collections.sort(plis, pliComparatorTitle);
            Collections.sort(bplis, bldrPliComparatorTitle);
        }
        
        for (int i=0;i<plis.size();i++)
        {
            PickListItem     pli  = plis.get(i);
            BldrPickListItem bpli = bplis.get(i);
            //System.out.println("["+pli.getOrdinal()+"]["+bpli.getOrdinal()+"]["+pli.getTitle()+"]["+bpli.getTitle()+"]["+pli.getValue()+"]["+bpli.getValue()+"]");
            if (!StringUtils.equals(pli.getTitle(), bpli.getTitle())) return false;
            if (!StringUtils.equals(pli.getValue(), bpli.getValue())) return false;
        }
        
        return true;
    }
    
    /**
     * @param localizableIO
     * @param hash HashSet of names of picklists to be pre-selected (can be null)
     */
    public static void exportPickList(final LocalizableIOIFace localizableIO, 
                                      final HashSet<String> hash)
    {
        // Cancel is Export All PickLists
        
        List<PickList> items         = getPickLists(localizableIO, true, false);
        List<PickList> selectedItems = new ArrayList<PickList>();
        if (hash != null)
        {
            for (PickList pl : items)
            {
                if (hash.contains(pl.getName()))
                {
                    selectedItems.add(pl);
                }
            }
        }
        
        Window  window   = UIRegistry.getTopWindow();
        boolean isDialog = window instanceof Dialog;
        
        ToggleButtonChooserDlg<PickList> pickDlg;
        if (isDialog)
        {
            pickDlg = new ToggleButtonChooserDlg<PickList>((Dialog)UIRegistry.getMostRecentWindow(), getI18n("PL_EXPORT"), items);   
        } else
        {
            pickDlg = new ToggleButtonChooserDlg<PickList>((Frame)UIRegistry.getMostRecentWindow(), getI18n("PL_EXPORT"), items);   
        }
         
        pickDlg.setUseScrollPane(true);
        pickDlg.setAddSelectAll(true);
        pickDlg.createUI();
        pickDlg.setSelectedObjects(selectedItems);
        UIHelper.centerAndShow(pickDlg);
        
        Integer cnt = null;
        if (!pickDlg.isCancelled())
        {
            items = pickDlg.getSelectedObjects();
            
            String dlgTitle = getResourceString(getI18n("RIE_ExportResource"));
            
            FileDialog dlg;
            if (isDialog)
            {
                dlg = new FileDialog((Dialog)window, dlgTitle, FileDialog.SAVE); 
            } else
            {
                dlg = new FileDialog((Frame)window, dlgTitle, FileDialog.SAVE); 
            }
            dlg.setDirectory(UIRegistry.getUserHomeDir());
            dlg.setFile(getPickListXMLName());
            
            UIHelper.centerAndShow(dlg);
            
            String dirStr   = dlg.getDirectory();
            String fileName = dlg.getFile();
            
            if (StringUtils.isNotEmpty(dirStr) && StringUtils.isNotEmpty(fileName))
            {
                String ext = FilenameUtils.getExtension(fileName);
                if (StringUtils.isEmpty(ext) || !ext.equalsIgnoreCase("xml"))
                {
                    fileName += ".xml";
                }

                try
                {
                    File xmlFile  = new File(dirStr + File.separator + fileName);
                    
                    ArrayList<BldrPickList> bldrPickLists = new ArrayList<BldrPickList>();
                    for (PickList pl : items)
                    {
                        bldrPickLists.add(new BldrPickList(pl));
                    }
                    DataBuilder.writePickListsAsXML(xmlFile, bldrPickLists);
                    cnt = bldrPickLists.size();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                UIRegistry.displayInfoMsgDlgLocalized(getI18n(cnt != null ? "PL_WASEXPORT" : "PL_ERR_IMP"), cnt);
            }
        }
    }
    
    /**
     * @param s
     * @return
     */
    public static String getI18n(final String s)
    {
        return "SystemSetupTask." + s;
    }
}
