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
    
    //protected Vector tableAttrs = new Vector(LAST_TABLE_TYPE);
    protected Vector<Vector<Vector<SubAttrType>>> disciplineList = new Vector<Vector<Vector<SubAttrType>>>(LAST_TABLE_TYPE);
    
    
    public AttrsMgr()
    {
    }
    
    /**
     * @param discipline
     */
    public void loadAttrs(short discipline)
    {
        try
        {
            Vector<Vector<SubAttrType>> tables = new Vector<Vector<SubAttrType>>();
            
            Session session = HibernateUtil.getCurrentSession();
            
            Criteria prepTypesCriteria = session.createCriteria(PrepTypes.class);
            List     prepTypesList     = prepTypesCriteria.list();
            for (Iterator iter=prepTypesList.iterator();iter.hasNext();)
            {
                PrepTypes pt = (PrepTypes)iter.next();
                System.out.println(pt.getPrepTypeID() + " " + pt.getName());
            }
            
            for (short i=START_TABLE_TYPE;i<LAST_TABLE_TYPE;i++)
            {
                Vector<SubAttrType> typeList = new Vector<SubAttrType>();
                
                if (i == PREP_TABLE_TYPE)
                {
                    short       subType = -1;
                    SubAttrType sat    = null;
                    
                    System.out.println("\n**** Loading Table "+i);
                    
                    Query query = session.createQuery("from AttrsDef as ad where ad.disciplineType = "+discipline+" and ad.tableType = "+i+" ORDER BY ad.disciplineType ASC, ad.tableType ASC");
                    for (Iterator iter=query.iterate();iter.hasNext();)
                    {
                        
                        AttrsDef ad = (AttrsDef)iter.next();
                        if (ad.getSubType().shortValue() != subType) 
                        {
                            sat = new SubAttrType();
                            typeList.addElement(sat);
                        }
                        sat.add(ad);
                        System.out.println(ad.getFieldName() + " " + ad.getDataType());
                    }
                } else
                {
                    
                    
                    System.out.println("\n**** Loading Table "+i);
                    Query query = session.createQuery("from AttrsDef as ad where ad.disciplineType = "+FISH_DISCIPLINE+" and ad.tableType = "+i);
                    
                    SubAttrType sat = new SubAttrType(query.iterate());
                    typeList.addElement(sat);
                    
                    for (Enumeration e=sat.getAttrDefs().elements();e.hasMoreElements();)
                    {
                        AttrsDef ad = (AttrsDef)e.nextElement();
                        System.out.println(ad.getFieldName() + " " + ad.getDataType());
                    }
                    
                }
                tables.addElement(typeList);
                
            }
            disciplineList.addElement(tables);
            
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param discipline
     * @param subType
     * @param tableType
     * @return
     */
    public List getAttrDefs(short discipline, short subType, short tableType)
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();

            Query query = session.createQuery("from AttrsDef as ad where ad.disciplineType = "+discipline+
                                                " and ad.subType = "+subType+
                                                " and ad.tableType = "+tableType+
                                                " ORDER BY ad.disciplineType ASC, ad.subType ASC, ad.tableType ASC");
                                        Vector list = new Vector();
            for (Iterator iter=query.iterate();iter.hasNext();)
            {
                list.add(iter.next());
            }
            
        } catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
    
    
    class SubAttrType 
    {
        protected Vector _atttTypes = new Vector();
        
        public SubAttrType()
        {
        }
        
        public SubAttrType(Iterator aIter)
        {
            for (Iterator iter=aIter;iter.hasNext();)
            {
                _atttTypes.addElement(iter.next());
            }
        }
        
        public void add(AttrsDef aAD)
        {
            _atttTypes.addElement(aAD);
        }
        
        public Vector getAttrDefs()
        {
            return _atttTypes;
        }
   
    }
}
