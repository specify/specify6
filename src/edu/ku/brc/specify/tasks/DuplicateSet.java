package edu.ku.brc.specify.tasks;

import edu.ku.brc.af.core.db.DBTableInfo;

import java.util.ArrayList;
import java.util.List;

public class DuplicateSet {
    final DBTableInfo table;
    final List<Integer> ids;
    
    public DuplicateSet(final DBTableInfo table, final List<Object> ids) {
        this.table = table;
        this.ids = new ArrayList<>();
        for (Object id : ids) {
            this.ids.add((Integer)id);
        }
    }

    public DBTableInfo getTable() {
        return table;
    }

    public List<Integer> getIds() {
        return ids;
    }
}
