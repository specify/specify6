package edu.ku.brc.specify.exporters;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;

public class DiGIRExporter implements RecordSetExporter
{
    public static final String NAME = "DiGIR";
    
    public void exportRecordSet(RecordSet data)
    {
        JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
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

}
