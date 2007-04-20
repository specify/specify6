package edu.ku.brc.specify.exporters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

public class DiGIRExporter implements RecordSetExporter
{
    public static final String NAME = "DiGIR";
    
    public void exportRecordSet(RecordSet data, Properties reqParams)
    {
        JFrame topFrame = (JFrame)UIRegistry.get(UIRegistry.TOPFRAME);
        Icon icon = IconManager.getIcon(NAME);
        JOptionPane.showMessageDialog(topFrame, "Not yet implemented", NAME + " data export", JOptionPane.ERROR_MESSAGE, icon);
    }

    public Class<?>[] getHandledClasses()
    {
        return new Class<?>[] {CollectionObject.class};
    }

    public String getIconName()
    {
        return NAME;
    }

    public String getName()
    {
        return NAME;
    }

    public String getDescription()
    {
        return getResourceString(NAME+"_Description");
    }

    public void exportList(List<?> data, Properties reqParams)
    {
        // TODO Auto-generated method stub
        
    }
}
