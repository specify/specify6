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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.tasks.BaseTask.ASK_TYPE;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.ui.db.AskForNumbersDlg;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 27, 2010
 *
 */
public class ColObjSourceHelper
{
    public enum TypeOfRS {eColObjWithPreps, eColObjs}
    
    protected ASK_TYPE askTypeRV = ASK_TYPE.Cancel;
    protected TypeOfRS typeOfRSs;
    
    /**
     * @param interactionsTask
     */
    public ColObjSourceHelper(final TypeOfRS typeOfRSs)
    {
        super();
        this.typeOfRSs = typeOfRSs;
    }
    
    /**
     * @return the chosen RecordSet
     */
    public RecordSetIFace getRecordSet(final Vector<RecordSetIFace> rsList)
    {
        RecordSetIFace recordSet = null;
        if (recordSet == null)
        {        
            // Get a List of InfoRequest RecordSets
            RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
            List<RecordSetIFace>   colObjRSList = rsTask == null ? null : rsTask.getRecordSets(CollectionObject.getClassTableId());
         
            boolean isRSEmpty       = rsList == null || rsList.size() == 0;
            boolean isColObjRSEmpty = colObjRSList == null || colObjRSList.size() == 0;
            
            // If the List is empty then
            if (isRSEmpty && isColObjRSEmpty)
            {
                recordSet = askForCatNumbersRecordSet();

            } else 
            {
                askTypeRV = askSourceOfPreps(!isRSEmpty, !isColObjRSEmpty);
                if (askTypeRV == ASK_TYPE.ChooseRS)
                {
                    recordSet = RecordSetTask.askForRecordSet(CollectionObject.getClassTableId(), rsList);
                    
                } else if (askTypeRV == ASK_TYPE.EnterDataObjs)
                {
                    recordSet = askForCatNumbersRecordSet();
                    
                } else if (askTypeRV == ASK_TYPE.Cancel)
                {
                    return null;
                }
            }
        }
        
        askTypeRV = recordSet != null ? ASK_TYPE.ChooseRS : ASK_TYPE.Cancel;
        return recordSet;
    }
    
    /**
     * (This needs to be moved to the BaseTask in specify package)
     * @return a RecordSet of newly entered Catalog Numbers
     */
    public static RecordSetIFace askForCatNumbersRecordSet()
    {
        String titleKey = "BT_TITLE_"+CollectionObject.class.getSimpleName();
        String labelKey = "BT_LABEL_"+CollectionObject.class.getSimpleName();

        AskForNumbersDlg dlg = new AskForNumbersDlg(titleKey, labelKey, CollectionObject.class, "catalogNumber");
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return dlg.getRecordSet();
        }
        return null;
    }

    /**
     * Asks where the source of the Loan Preps should come from.
     * @param hasInfoReqs
     * @param hasColObjRS
     * @return the source enum
     */
    protected ASK_TYPE askSourceOfPreps(final boolean hasInfoReqs, 
                                        final boolean hasColObjRS)
    {
        String label;
        if (hasInfoReqs && hasColObjRS)
        {
            label = getResourceString("NEW_INTER_USE_RS_IR");
            
        } else if (hasInfoReqs)
        {
            label = getResourceString("NEW_INTER_USE_IR");
        } else
        {
            label = getResourceString("NEW_INTER_USE_RS");
        }
        
        Object[] options = { 
                label, 
                getResourceString("NEW_INTER_ENTER_CATNUM") 
              };
        String msgKey = "NEW_INTER_CHOOSE_RSOPT" + (typeOfRSs == TypeOfRS.eColObjs ? "" : "_PREPS");
        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                     getResourceString(msgKey), 
                                                     getResourceString("NEW_INTER_CHOOSE_RSOPT_TITLE"), 
                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (userChoice == JOptionPane.NO_OPTION)
        {
            return ASK_TYPE.EnterDataObjs;
            
        } else if (userChoice == JOptionPane.YES_OPTION)
        {
            return ASK_TYPE.ChooseRS;
        }
        return ASK_TYPE.Cancel;
    }

    /**
     * @return the askTypeRV
     */
    public ASK_TYPE getAskTypeRV()
    {
        return askTypeRV;
    }

}
