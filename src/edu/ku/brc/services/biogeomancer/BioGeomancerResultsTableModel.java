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
                return getResourceString("Id");
            }
            case 1:
            {
                return getResourceString("Country");
            }
            case 2:
            {
                return getResourceString("Adm1");
            }
            case 3:
            {
                return getResourceString("Adm2");
            }
            case 4:
            {
                return getResourceString("FeatureName");
            }
            case 5:
            {
                return getResourceString("FeatureType");
            }
            case 6:
            {
                return getResourceString("Gazetteer");
            }
            case 7:
            {
                return getResourceString("Coordinates");
            }
            case 8:
            {
                return getResourceString("Offset");
            }
            case 9:
            {
                return getResourceString("BoundingBox");
            }
            case 10:
            {
                return getResourceString("Locality");
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
