/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberGeneric;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 8, 2007
 *
 */
public class AccessionAutoNumberAlphaNum extends AutoNumberGeneric
{
    /**
     * Default Constructor. 
     */
    public AccessionAutoNumberAlphaNum()
    {
        super();
        
        classObj  = Accession.class;
        fieldName = "accessionNumber";
    }

    /**
     * Constructor with args.
     * @param properties the args
     */
    public AccessionAutoNumberAlphaNum(final Properties properties)
    {
        super(properties);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberGeneric#getHighestObject(org.hibernate.Session, java.lang.String, edu.ku.brc.util.Pair, edu.ku.brc.util.Pair)
     */
    @Override
    protected Object getHighestObject(final Session session, 
                                      final String  value,
                                      final Pair<Integer, Integer> yearPos, 
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        Division currDivision = AppContextMgr.getInstance().getClassObject(Division.class);
        
        Integer yearVal = null;
        if (yearPos != null && StringUtils.isNotEmpty(value) && value.length() >= yearPos.second)
        {
            yearVal = extractIntegerValue(yearPos, value);
        }

        StringBuilder sb = new StringBuilder(" From Accession c Join c.division dv Join dv.numberingSchemes ans WHERE ans.id = ");
        sb.append(currDivision.getNumberingSchemesByType(Accession.getClassTableId()).getAutoNumberingSchemeId());
        sb.append(" AND dv.id = ");
        sb.append(currDivision.getId());
        
        if (yearVal != null)
        {
            sb.append(" AND ");
            sb.append(yearVal);
            sb.append(" = substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+")");
        }
        
        sb.append(" ORDER BY");
        
        try
        {
            if (yearPos != null)
            {
                sb.append(" substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+") desc");
            }
            
            if (pos != null)
            {
                if (yearPos != null)
                {
                    sb.append(", ");
                }
                sb.append(" substring("+fieldName+","+(pos.first+1)+","+pos.second+") desc");
            }
            
            System.out.println(sb.toString());
            List<?> list = session.createQuery(sb.toString()).setMaxResults(1).list();
            if (list.size() == 1)
            {
                Object[] objArray = (Object[]) list.get(0);
                //System.err.println(((Accession)objArray[0]).getAccessionNumber());
                return objArray[0];
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionAutoNumberAlphaNum.class, ex);
            ex.printStackTrace();
        }
        return null;
    }
}
