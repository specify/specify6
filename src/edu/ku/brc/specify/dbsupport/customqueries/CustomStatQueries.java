/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport.customqueries;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;

import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Loan;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 4, 2007
 *
 */
public class CustomStatQueries implements CustomQuery
{
    public enum Type {CatalogedLast7Days, CatalogedLast30Days, CatalogedLastYear, OverdueLoans};

    protected Type                      type;
    protected List<QueryResultsDataObj> qrdoResults = null;
    protected boolean                   inError     = false;
    protected List<?>                   resultsList = null;
    
    /**
     * Constrcutor.
     * @param type type of query to execute.
     */
    public CustomStatQueries(final Type type)
    {
        this.type = type;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute()
     */
    public boolean execute()
    {
        boolean retVal = executeInternal();
        if (retVal)
        {
            if (qrdoResults == null)
            {
                qrdoResults = new Vector<QueryResultsDataObj>();
            }
            for (Object obj : resultsList)
            {
                qrdoResults.add(new QueryResultsDataObj(obj));
            }
        }
        
        return retVal;
    }
    
    /**
     * @return
     */
    protected boolean executeInternal()
    {

        switch (type)
        {
            case CatalogedLast7Days :
                return catalogedLastXDays(7);
                
            case CatalogedLast30Days :
                return  catalogedLastXDays(30);
                
            case CatalogedLastYear :
                return catalogedLastXDays(365);
            case OverdueLoans:
                return overdueLoans();
        }
        
        return false;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    public void execute(final CustomQueryListener cql)
    {
        final CustomStatQueries thisItem = this;

        final SwingWorker worker = new SwingWorker()
        {
             public Object construct()
            {
                inError = executeInternal();
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                if (inError)
                {
                    cql.executionError(thisItem);
                    
                } else
                {
                    cql.exectionDone(thisItem);
                }
            }
        };
        worker.start();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getDataObjects()
     */
    public List<?> getDataObjects()
    {
        return resultsList;
    }
    
    /**
     * Returns how many items ere cataloged over a perio of time.
     * @param numDays how many days back
     * @return true if executed correctly
     */
    protected boolean catalogedLastXDays(final int numDays)
    {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate   = Calendar.getInstance();

        startDate.clear(Calendar.HOUR_OF_DAY);
        startDate.clear(Calendar.MINUTE);
        startDate.clear(Calendar.SECOND);
        
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DAY_OF_MONTH, -numDays);
        startDate.clear();
        startDate.set(Calendar.YEAR,         today.get(Calendar.YEAR));
        startDate.set(Calendar.MONTH,        today.get(Calendar.MONTH));
        startDate.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

        /*
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        System.out.print(sdf.format(startDate.getTime()));
        System.out.println(" * "+sdf.format(endDate.getTime()));//new Date(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH))));
        */
        
        Session  session  = HibernateUtil.getNewSession();
        
        Criteria criteria = session.createCriteria(CollectionObject.class);
        criteria.add(Expression.ge("timestampCreated", startDate.getTime()));
        criteria.add(Expression.le("timestampCreated", endDate.getTime()));

        criteria.setProjection(Projections.rowCount());
        resultsList = criteria.list();
        
        //for (Object data : resultsList)
        //{
          //System.out.println(((CollectionObject)data).getIdentityTitle());
        //    System.out.println(data);
        //}
        session.close();

        return true;
    }
    
    protected boolean overdueLoans()
    {
        //String sql = "select loanId from Loan where (not (currentDueDate is null)) and loan.IsGift = false and (IsClosed = false or IsClosed is null) and datediff(CURDATE(), currentduedate) > 0;
        //select count(loanid) as OpenLoanCount from loan where loanid in (select loanid from loan where (not (currentduedate is null)) and loan.IsGift = false and (IsClosed = false or IsClosed is null) and datediff(CURDATE(), currentduedate) > 0)
            
        Session  session  = HibernateUtil.getNewSession();
        
        Calendar endDate = Calendar.getInstance();
        Calendar today   = Calendar.getInstance();
        endDate.clear();
        endDate.set(Calendar.YEAR,         today.get(Calendar.YEAR));
        endDate.set(Calendar.MONTH,        today.get(Calendar.MONTH));
        endDate.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        
        Criteria criteria = session.createCriteria(Loan.class);
        criteria.add(Expression.isNotNull("currentDueDate"));
        criteria.add(Expression.ge("currentDueDate", endDate));

        criteria.setProjection(Projections.rowCount());
        resultsList = criteria.list();
        
        //for (Object data : resultsList)
        //{
        //    System.out.println("overdueLoans "+data);
        //}
        session.close();
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getQueryDefinition()
     */
    public List<QueryResultsContainerIFace> getQueryDefinition()
    {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getResults()
     */
    public List<QueryResultsDataObj> getResults()
    {
        return qrdoResults;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#isExecutable()
     */
    public boolean isExecutable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getName()
     */
    public String getName()
    {
        return type.toString();
    }
    
}
