package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.af.core.db.DBTableInfo;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ERTICaptionInfoCalc extends ERTICaptionInfoQB {
    private static final Logger log = Logger.getLogger(ERTICaptionInfoCalc.class);
    protected final DBTableInfo tblInfo;
    protected final Method m;
    public ERTICaptionInfoCalc(String colName, String colLabel, String colStringId, DBTableInfo tblInfo) {
        super(colName, colLabel, true, null, 0, colStringId, null,null);
        this.tblInfo = tblInfo;
        Method meth = null;
        try {
            meth = this.tblInfo.getClassObj().getMethod("getQueryableTransientFieldValue", String.class, Object[].class);
        } catch (NoSuchMethodException x) {
            log.error(x);
        }
        this.m = meth;
    }

    @Override
    public Object processValue(Object value) {
        Object result = null;
        if (m != null) {
            Object[] val = (Object[])value;
            Object[] arg = new Object[Math.max(1, val.length - 1)];
            if (val.length == 1) {
                arg[0] = val[0];
            } else for (int i=0; i < val.length - 1; i++) {
                arg[i] = val[i];
            }
            try {
                result = m.invoke(null, colName, arg);
                if (result != null) {
                    return result.toString();
                }
            } catch (IllegalAccessException | InvocationTargetException x) {
                log.error(x);
            }
        }
        return result;
    }
}
