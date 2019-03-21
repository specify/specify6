package edu.ku.brc.specify.tasks.subpane.wb;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.UIRegistry;

public class WBUnMappedItemException extends Exception {
    private WorkbenchTemplateMappingItem item;
    public WBUnMappedItemException(WorkbenchTemplateMappingItem item) {
        super();
        this.item = item;
    }

    @Override
    public String getMessage() {
        if (item != null) {
           return UIRegistry.getResourceString("WBUnMappedItemMsgDefault") + "\n"
                   + String.format(UIRegistry.getResourceString("WBUnMappedItemMsg"), item.getTableName() + "." + item.getFieldName(),
                    item.getCaption(), item.getImportedColName());
        } else {
            return UIRegistry.getResourceString("WBUnMappedItemMsgDefault");
        }
    }
}