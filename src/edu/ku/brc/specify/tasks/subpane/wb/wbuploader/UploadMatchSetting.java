/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
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
    
    public static Vector<String> getModeTexts()
    {
        Vector<String> result = new Vector<String>();
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_ASK"));
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_ADD"));
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_FIRST"));
        result.addElement(getResourceString("WB_UPLOAD_MATCH_MODE_SKIP_ROW"));
        return result;
    }
    
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
    
    public void addSelection(final MatchSelection selection)
    {
        selections.add(selection);
        if (remember || mode == UploadMatchSetting.ADD_NEW_MODE)
        {
            lookups.put(selection.getRowValues(), selection);
        }
    }
    
    public String buildRowValues(final Vector<Pair<String, String>> rowVals)
    {
        StringBuilder result = new StringBuilder();
        for (Pair<String, String> val : rowVals)
        {
            result.append(val.getSecond());
            result.append("\t");
        }
        return result.toString();
    }
    
    public Object doLookup(final Vector<Pair<String, String>> values)
    {
        MatchSelection match = lookups.get(buildRowValues(values));
        if (match != null)
        {
            return match.getSelectionKeyId();
        }
        return null;
    }
    
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
        public MatchSelection(final Vector<Pair<String, String>> rowValues, int rowNumber, Object selectionKeyId, int mode)
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
    
    
}
