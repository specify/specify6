/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config;

import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;

/**
 * Class used to create RecordSetIFace objects.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 14, 2008
 *
 */
public class SpecifyRecordSetFactory extends RecordSetFactory
{
    /**
     * 
     */
    public SpecifyRecordSetFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.RecordSetFactory#createRecordSet()
     */
    @Override
    public RecordSetIFace createRecordSet()
    {
        RecordSet rs =  new RecordSet();
        rs.initialize();
        return rs;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.RecordSetFactory#createRecordSet(java.lang.String, int, java.lang.Byte)
     */
    @Override
    public RecordSetIFace createRecordSet(final String name, final int dbTableId, final Byte type)
    {
        RecordSet rs =  new RecordSet();
        rs.initialize();
        rs.set(name, dbTableId, type);
        return rs;
    }

}
