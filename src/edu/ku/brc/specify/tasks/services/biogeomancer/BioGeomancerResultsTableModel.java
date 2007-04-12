package edu.ku.brc.specify.tasks.services.biogeomancer;

import javax.swing.table.AbstractTableModel;


public class BioGeomancerResultsTableModel extends AbstractTableModel
{
    protected BioGeomancerResult[] results;
    
    public BioGeomancerResultsTableModel(BioGeomancerResult[] results)
    {
        this.results = results;
    }
    
    public int getColumnCount()
    {
        return 11;
    }

    public int getRowCount()
    {
        return results.length;
    }

    @Override
    public String getColumnName(int column)
    {
        switch(column)
        {
            case 0:
            {
                return "Id";
            }
            case 1:
            {
                return "Country";
            }
            case 2:
            {
                return "Adm 1";
            }
            case 3:
            {
                return "Adm 2";
            }
            case 4:
            {
                return "Feat. Name";
            }
            case 5:
            {
                return "Feat. Type";
            }
            case 6:
            {
                return "Gazetteer";
            }
            case 7:
            {
                return "Coordinates";
            }
            case 8:
            {
                return "Offset";
            }
            case 9:
            {
                return "Bounding Box";
            }
            case 10:
            {
                return "Locality";
            }
        }
        
        return null;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        BioGeomancerResult result = results[rowIndex];
        
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
