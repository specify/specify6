/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadMatchSetting
{
    public static int ASK_MODE = 0;
    public static int ADD_NEW_MODE = 1;
    public static int PICK_FIRST_MODE = 2;
    
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
    protected Vector<MatchSelection> lookups;
    
    /**
     * Default value for MatchSelection.lookup
     */
    protected boolean remember;
    
    public UploadMatchSetting()
    {
        mode = UploadMatchSetting.ASK_MODE;        
        colsToMatch = new Vector<Integer>();
        selections = new Vector<MatchSelection>();
        lookups = new Vector<MatchSelection>();
        remember = true;
    }
    
    public void addSelection(final MatchSelection selection)
    {
        selections.add(selection);
        if (selection.isLookup())
        {
            lookups.add(selection);
        }
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
         * one of the 'MODE' statics above 
         */
        protected int selectionMode;
        
        
        /**
         * The next time matching rowValues occur, use this selection w/o asking user.
         */
        protected boolean lookup;
        /**
         * @param rowValues
         * @param rowNumber
         * @param object
         */
        public MatchSelection(final Vector<String> rowValues, int rowNumber, Object selectionKeyId, int mode, boolean lookup)
        {
            super();
            this.rowValues = buildRowValues(rowValues);
            this.rowNumber = rowNumber;
            this.selectionKeyId = selectionKeyId;
            this.selectionMode = mode;
            this.lookup = lookup;
        }
        
        protected String buildRowValues(final Vector<String> rowVals)
        {
            StringBuilder result = new StringBuilder();
            for (String val : rowVals)
            {
                result.append(val);
                result.append("\t");
            }
            return result.toString();
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
         * @return the mode
         */
        public int getSelectionMode()
        {
            return selectionMode;
        }

        /**
         * @return the lookup
         */
        public boolean isLookup()
        {
            return lookup;
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
    
    
}
