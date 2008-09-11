/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.biogeomancer;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * An implementation of {@link TableModel} for use in presenting an array of {@link BioGeomancerResultStruct}s
 * for display in a {@link JTable}.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class BioGeomancerResultsTableModel extends AbstractTableModel
{
    /** The actual data to be displayed. */
    protected BioGeomancerResultStruct[] results;
    
    /**
     * Constructor.
     * 
     * @param results the row data to be displayed
     */
    public BioGeomancerResultsTableModel(BioGeomancerResultStruct[] results)
    {
        this.results = results;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 11;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return results.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        switch(column)
        {
            case 0:
            {
                return getResourceString("BioGeomancerResultsTableModel.ID"); //$NON-NLS-1$
            }
            case 1:
            {
                return getResourceString("BioGeomancerResultsTableModel.COUNTRY"); //$NON-NLS-1$
            }
            case 2:
            {
                return getResourceString("BioGeomancerResultsTableModel.ADM1"); //$NON-NLS-1$
            }
            case 3:
            {
                return getResourceString("BioGeomancerResultsTableModel.ADM2"); //$NON-NLS-1$
            }
            case 4:
            {
                return getResourceString("BioGeomancerResultsTableModel.FEATURE_NAME"); //$NON-NLS-1$
            }
            case 5:
            {
                return getResourceString("BioGeomancerResultsTableModel.FEATURE_TYPE"); //$NON-NLS-1$
            }
            case 6:
            {
                return getResourceString("BioGeomancerResultsTableModel.GASETTEER"); //$NON-NLS-1$
            }
            case 7:
            {
                return getResourceString("BioGeomancerResultsTableModel.COORDINATES"); //$NON-NLS-1$
            }
            case 8:
            {
                return getResourceString("BioGeomancerResultsTableModel.OFFSET"); //$NON-NLS-1$
            }
            case 9:
            {
                return getResourceString("BioGeomancerResultsTableModel.BOUNDINGBOX"); //$NON-NLS-1$
            }
            case 10:
            {
                return getResourceString("BioGeomancerResultsTableModel.LOCALITY"); //$NON-NLS-1$
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        BioGeomancerResultStruct result = results[rowIndex];
        
        switch(columnIndex)
        {
            case 0:
            {
                return Integer.toString(rowIndex+1);
            }
            case 1:
            {
                return result.country;
            }
            case 2:
            {
                return result.adm1;
            }
            case 3:
            {
                return result.adm2;
            }
            case 4:
            {
                return result.featureName;
            }
            case 5:
            {
                return result.featureType;
            }
            case 6:
            {
                return result.gazetteer;
            }
            case 7:
            {
                return result.coordinates;
            }
            case 8:
            {
                return result.offset;
            }
            case 9:
            {
                return result.boundingBox;
            }
            case 10:
            {
                return result.locality;
            }
        }
        
        return null;
    }

}
