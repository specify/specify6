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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Stores match settings and options for an UploadTable.
 *
 */
public class UploadMatchSetting
{
    protected static final Logger log = Logger.getLogger(UploadMatchSetting.class);

    public final static int ASK_MODE = 0;
    public final static int ADD_NEW_MODE = 1;
    public final static int PICK_FIRST_MODE = 2;
    public final static int SKIP_ROW_MODE = 3;
    
    /**
     * one of the 'MODE' statics above 
     */
    protected int mode;
    
    /**
     * The indexes of the UploadFields to be checked when matching.
     * When empty all UploadFields are matched.
     */
    protected Vector<Integer> colsToMatch;
    
    /**
     * all the selections made.
     */
    protected Vector<MatchSelection> selections; 
    
    /**
     * selections that should be re-used.
     */
    protected TreeMap<String, MatchSelection> lookups;
    
    /**
     * Default value for MatchSelection.lookup
     */
    protected boolean remember;
    
    /**
     * If true then all fields in the uploaded dataset are used to find matches,
     * if false then fields whose cells are empty are not matched on. 
     */
    protected boolean matchEmptyValues;
    
    /**
     * @return a Vector of String names for the match 'modes'.
     */
    public static Vector<String> getModeTexts()
    {
        Vector<String> result = new Vector<String>();
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_ASK"));
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_ADD"));
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_FIRST"));
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_SKIP_ROW"));
        return result;
    }
    
    /**
     * @param mode
     * @return the name of the mode.
     */
    public static String getModeText(int mode)
    {
        if (mode == ASK_MODE)
        {
            return getResourceString("WB_UPLOAD_MATCH_MODE_ASK");
        }
        if (mode == ADD_NEW_MODE)
        {
            return getResourceString("WB_UPLOAD_MATCH_MODE_ADD");
        }
        if (mode == PICK_FIRST_MODE)
        {
            return getResourceString("WB_UPLOAD_MATCH_MODE_FIRST");
        }
        if (mode == SKIP_ROW_MODE)
        {
            return getResourceString("WB_UPLOAD_MATCH_MODE_SKIP_ROW");
        }
        throw new RuntimeException("Unknown match mode.");
    }
    
    /**
     * @param modeName
     * @return the mode named modeName.
     */
    public static int getMode(final String modeName)
    {
        if (modeName.equals(getResourceString("WB_UPLOAD_MATCH_MODE_ASK")))
        {
            return ASK_MODE;
        }
        if (modeName.equals(getResourceString("WB_UPLOAD_MATCH_MODE_ADD")))
        {
            return ADD_NEW_MODE;
        }
        if (modeName.equals(getResourceString("WB_UPLOAD_MATCH_MODE_FIRST")))
        {
            return PICK_FIRST_MODE;
        }
        if (modeName.equals(getResourceString("WB_UPLOAD_MATCH_MODE_SKIP_ROW")))
        {
            return SKIP_ROW_MODE;
        }
        return -1;
    }
    
    /**
     * Default constructor.
     */
    public UploadMatchSetting()
    {
        //mode = UploadMatchSetting.ASK_MODE;        
        mode = UploadMatchSetting.ASK_MODE;        
        colsToMatch = new Vector<Integer>();
        selections = new Vector<MatchSelection>();
        lookups = new TreeMap<String, MatchSelection>();
        remember = true;
        matchEmptyValues = true; //for now, default probably should be false
    }
    
    /**
     * @param selection
     */
    public void addSelection(final MatchSelection selection)
    {
        selections.add(selection);
        if (remember || mode == UploadMatchSetting.ADD_NEW_MODE)
        {
            lookups.put(selection.getRowValues(), selection);
        }
    }
    
    /**
     * @param rowVals
     * @return a string descriptions of the values used to find a match.
     */
    public String buildRowValues(final Vector<UploadTable.MatchRestriction> rowVals)
    {
        StringBuilder result = new StringBuilder();
        for (UploadTable.MatchRestriction val : rowVals)
        {
            result.append(val.getFieldName());
            result.append("\t");
            result.append(val.getRestriction());
        }
        return result.toString();
    }
    
    /**
     * @param values
     * @return a previously selected object that matches values.
     */
    public Object doLookup(final Vector<UploadTable.MatchRestriction> values)
    {
        MatchSelection match = lookups.get(buildRowValues(values));
        if (match != null)
        {
            return match.getSelectionKeyId();
        }
        return null;
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * Stores info about selected matches.
     */
    public class MatchSelection
    {
        /**
         * A tab-delimited list of cell values.
         */
        protected String rowValues;
        /**
         * The rownumber containing the values.
         */
        protected int rowNumber;
        /**
         * The id of the selected object
         */
        protected Object selectionKeyId;
        
        /**
         * the value of the parent MatchSetting's mode when this selection was created.
         */
        protected int modeOfSelection;
        
        /**
         * @param rowValues
         * @param rowNumber
         * @param object
         */
        public MatchSelection(final Vector<UploadTable.MatchRestriction> rowValues, int rowNumber, Object selectionKeyId, int mode)
        {
            super();
            this.rowValues = buildRowValues(rowValues);
            this.rowNumber = rowNumber;
            this.selectionKeyId = selectionKeyId;
            this.modeOfSelection = mode;
        }
        
        /**
         * @return the rowNumber
         */
        public int getRowNumber()
        {
            return rowNumber;
        }
        /**
         * @return the rowValues
         */
        public String getRowValues()
        {
            return rowValues;
        }
        /**
         * @return the selectionKeyId
         */
        public Object getSelectionKeyId()
        {
            return selectionKeyId;
        }
        
        /**
         * @return modeOfSelection
         */
        public int getModeOfSelection()
        {
            return modeOfSelection;
        }
     }

    /**
     * @return the mode
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(int mode)
    {
        this.mode = mode;
    }

    /**
     * @return the remember
     */
    public boolean isRemember()
    {
        return remember;
    }

    /**
     * @param remember the remember to set
     */
    public void setRemember(boolean remember)
    {
        this.remember = remember;
    }

    /**
     * @return the matchEmptyValues
     */
    public boolean isMatchEmptyValues()
    {
        return matchEmptyValues;
    }

    /**
     * @param matchEmptyValues the matchEmptyValues to set
     */
    public void setMatchEmptyValues(boolean matchEmptyValues)
    {
        this.matchEmptyValues = matchEmptyValues;
    }
    
    /**
     * Clears selections and lookup table.
     */
    public void clear()
    {
        lookups.clear();
        selections.clear();
    }
}
