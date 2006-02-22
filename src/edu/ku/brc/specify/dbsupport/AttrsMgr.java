/*
 * Created on Aug 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ku.brc.specify.dbsupport;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.AttrsDef;
import edu.ku.brc.specify.datamodel.PrepTypes;

/**
 * Work in Progress (this will probably not be needed)
 * 
 * @author Rod Spears
 *
 * 
 */
public class AttrsMgr
{
    public static final short INT_TYPE  = 0;
    public static final short SHRT_TYPE = 1;
    public static final short FLT_TYPE  = 2;
    public static final short DBL_TYPE  = 3;
    public static final short DATE_TYPE = 4;
    public static final short VARC_TYPE = 5;
    public static final short MEMO_TYPE = 6;
    public static final short TAXON_TYPE = 7;
    
    public static final short PREP_TABLE_TYPE    = 0;
    public static final short BIO_TABLE_TYPE     = 1;
    public static final short HABITAT_TABLE_TYPE = 2;
    
    protected static final short START_TABLE_TYPE   = 0;
    protected static final short LAST_TABLE_TYPE    = 3;
   
    public static final short FISH_DISCIPLINE    = 0;
    public static final short BIRD_DISCIPLINE    = 1;
    public static final short PALEO_DISCIPLINE   = 2;

}
