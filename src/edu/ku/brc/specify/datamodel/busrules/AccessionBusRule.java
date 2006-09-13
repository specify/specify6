/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgents;
import edu.ku.brc.specify.datamodel.AccessionAuthorizations;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.ui.forms.BusinessRulesDataItem;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.forms.DataObjFieldFormatMgr;

/**
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class AccessionBusRule implements BusinessRulesIFace
{
    private static final Logger  log      = Logger.getLogger(AccessionBusRule.class);
    
    /**
     * Constructor.
     */
    public AccessionBusRule()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getWarningsAndErrors()
     */
    public List<String> getWarningsAndErrors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusiessRules(Object dataObj)
    {
        if (!(dataObj instanceof Accession))
        {
            return STATUS.Error;
        }
        //Accession accession = (Accession)dataObj;
       
        return STATUS.OK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getStandAloneDataItems(java.lang.Object)
     */
    public List<BusinessRulesDataItem> getStandAloneDataItems(Object dataObj)
    {
        
        List<BusinessRulesDataItem> list = new ArrayList<BusinessRulesDataItem>();
        Accession accession = (Accession)dataObj;
        
        for (AccessionAgents accAgent : accession.getAccessionAgents())
        {
            Agent agent = accAgent.getAgent();
            if (agent != null && agent.getAgentId() == null)
            {
                list.add(new AccessionBRS(agent));
            }
        }
        
        for (AccessionAuthorizations auth : accession.getAccessionAuthorizations())
        {
            Permit permit = auth.getPermit();
            if (permit != null && permit.getPermitId() == null)
            {
                list.add(new AccessionBRS(permit));
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#saveStandAloneData(java.lang.Object, java.util.List)
     */
    public void saveStandAloneData(final Object dataObj, final List<BusinessRulesDataItem> list)
    {
        if (!(dataObj instanceof Accession))
        {
            return;
        }
        
        try
        {
            HibernateUtil.beginTransaction();
            Session session = HibernateUtil.getCurrentSession();
            
            for (BusinessRulesDataItem item : list)
            {
                if (item.isChecked())
                {
                    session.save(item.getData());
                }
            }
            /*
            Accession accession = (Accession)dataObj;
            
            for (AccessionAgents accAgent : accession.getAccessionAgents())
            {
                Agent agent = accAgent.getAgent();
                if (HibernateUtil.getCurrentSession().contains(agent))
                {
                    session.save(agent);
                }
            }
            
            for (AccessionAuthorizations auth : accession.getAccessionAuthorizations())
            {
                Permit permit = auth.getPermit();
                if (HibernateUtil.getCurrentSession().contains(permit))
                {
                    session.save(permit);
                }
            }*/
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            log.error(ex);
        }

    }
    
    //-----------------------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------------------
    class AccessionBRS extends BusinessRulesDataItem
    {
        public AccessionBRS(final Object data)
        {
            super(data);
        }
        
        public String toString()
        {
            if (data instanceof Agent)
            {
                return DataObjFieldFormatMgr.format(data, "Agent"); // NOTE: This assumes we definitely have an "Agent" format

            } else if (data instanceof Permit)
            {
                return ((Permit)data).getPermitNumber();
            }
            return null;
        }
    }

}
