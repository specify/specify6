/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.helpers.XMLHelper.addAttr;
import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 25, 2008
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spreport")
@org.hibernate.annotations.Table(appliesTo="spreport", indexes =
    {   @Index (name="SpReportNameIDX", columnNames={"Name"})
    })
public class SpReport extends DataModelObjBase
{
    private static final Logger log = Logger.getLogger(SpReport.class);

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    protected Integer			spReportId;
	protected String			name;
	protected String			remarks;

	protected SpQuery			query;
	protected WorkbenchTemplate	workbenchTemplate;

	protected SpAppResource		appResource;
	protected SpecifyUser		specifyUser;

	protected Integer			repeatCount;
	protected String			repeatField;
    
 
    /**
     * 
     */
    public SpReport()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
	{
		super.init();

		spReportId = null;
		name = null;
		remarks = null;
		query = null;
		workbenchTemplate = null;
		appResource = null;
		specifyUser = null;
		repeatCount = null;
		repeatField = null;
	}
    
    @Id
    @GeneratedValue
    @Column(name = "SpReportId", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpReportId()
    {
        return spReportId;
    }

    public void setSpReportId(Integer spReportId)
    {
        this.spReportId = spReportId;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param owner
     */
    public void setSpecifyUser(SpecifyUser owner)
    {
        this.specifyUser = owner;
    }

    /**
     * @param query
     */
    protected void setQuery(SpQuery query)
    {
        if (workbenchTemplate != null)
        {
        	workbenchTemplate = null; //call it a business rule
        	log.warn("Setting query for a report with a previously assigned workbenchTemplate. This is not supposed to happen." );
        }
    	this.query = query;
    }

    /**
     * @param repo
     */
    public void setReportObject(DataModelObjBase repo)
    {
        if (repo instanceof SpQuery)
        {
        	setQuery((SpQuery )repo);
        }
        else if (repo instanceof Workbench)
        {
        	Workbench wb = (Workbench )repo;
        	try
        	{
        		setWorkbenchTemplate((WorkbenchTemplate )wb.getWorkbenchTemplate().clone());
        	} catch (CloneNotSupportedException cnsex)
        	{
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpReport.class, cnsex);
                cnsex.printStackTrace();
                throw new RuntimeException(cnsex);
        	}
        }
        else
        {
        	log.error("unable to set report object of class " + repo.getClass().getName());
        }
    }

    
    /**
     * @param appResource
     */
    public void setAppResource(SpAppResource appResource)
    {
        this.appResource = appResource;
    }

    /**
     * @param remarks
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }
    
    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @return the remarks
     */
    @Lob
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    /**
     * @return the query
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpQueryID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpQuery getQuery()
    {
        return query;
    }
    
    /**
     * @return the workbenchTemplate
     */
//    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
//    @JoinColumn(name = "WorkbenchTemplateID", unique = false, nullable = true, insertable = true, updatable = true)
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OneToOne
    @Cascade( {CascadeType.DELETE} )
    @JoinColumn(name="WorkbenchTemplateID")
    public WorkbenchTemplate getWorkbenchTemplate()
    {
        return workbenchTemplate;
    }

    /**
     * @param workbenchTemplate the workbenchTemplate to set
     */
    protected void setWorkbenchTemplate(WorkbenchTemplate workbenchTemplate)
    {
    	this.workbenchTemplate = workbenchTemplate;
    }
    
    /**
     * @return the object the report is based on
     */
    @Transient
    public DataModelObjBase getReportObject()
    {
    	if (query != null)
    	{
    		return query;
    	}
    	return workbenchTemplate;
    }
    
    /**
     * @return the appResource
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AppResourceID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpAppResource getAppResource()
    {
        return appResource;
    }

    /**
     * return the specifyUser
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() 
    {
        return this.specifyUser;
    }

    /**
     * @return the repeatCount;
     */
    @Column(name = "RepeatCount", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getRepeatCount()
    {
        return repeatCount;
    }
    
    /**
     * @param repeatCount the repeatCount to set.
     */
    public void setRepeatCount(Integer repeatCount)
    {
        this.repeatCount = repeatCount;
    }
    
    /**
     * @return the repeatField.
     */
    @Column(name = "RepeatField", unique = false, nullable = true, insertable = true, updatable = true)
    public String getRepeatField()
    {
        return repeatField;
    }
    
    /**
     * @return the repeatCount or the name of the field which determines repeatCount 
     */
    @Transient
    public Object getRepeats()
    {
        return repeatCount != null ? repeatCount : repeatField;
    }
    
    /**
     * @param repeats the count or field to set.
     */
    public void setRepeats(Object repeats)
    {
        if (repeats == null)
        {
            setRepeatCount(null);
            setRepeatField(null);
        }
        else if (repeats instanceof Integer)
        {
            setRepeatCount((Integer )repeats);
            setRepeatField(null);
        }
        else if (repeats instanceof String)
        {
            setRepeatCount(null);
            setRepeatField((String )repeats);
        }
        else
        {
            log.error("invalid repeats parameter: " + repeats);
            setRepeatCount(null);
            setRepeatField(null);            
        }
    }
    
    /**
     * @param repeatField the repeatField to set.
     */
    public void setRepeatField(String repeatField)
    {
        this.repeatField = repeatField;
    }

    
    /**
     * Assuming object is attached to an open session, loads lazily-loaded members.
     */
    @Override
    public void forceLoad()
    {
        //getQuery().forceLoad(false);
    	DataModelObjBase repObj = getReportObject();
    	if (repObj != null)
    	{
    		repObj.forceLoad();
    	}
        getAppResource();
        getSpecifyUser();
    }
    
    //----------------------------------------------------------------------
    //-- DataModelObjBase
    //----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Override
    @Transient
    public boolean isChangeNotifier()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpReport.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spReportId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 519;
    }
       
    /**
     * @param sb
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("<report ");
        addAttr(sb, "name", name);
        addAttr(sb, "remarks", remarks);
        addAttr(sb, "repeatCount", repeatCount);
        addAttr(sb, "repeatField", repeatField);
        sb.append(">\r\n");
        if (query != null)
        {
        	query.forceLoad();
        	query.toXML(sb);
        } else if (workbenchTemplate != null)
        {
        	workbenchTemplate.forceLoad();
        	workbenchTemplate.toXML(sb);
        }
        sb.append("</report>\r\n");
    }

    /**
     * @param element
     */
    public void fromXML(final Element element, final boolean alwaysMakeNew, final AppContextMgr contextMgr)
    {
        String repeatCountStr = getAttr(element, "repeatCount", null);
        name             = getAttr(element, "name", null);
        remarks     	 =  getAttr(element, "remarks", null);
        repeatCount      = StringUtils.isBlank(repeatCountStr) ? null : Integer.valueOf(repeatCountStr);
        repeatField      = getAttr(element, "repeatField", null);
        if (StringUtils.isBlank(repeatField))
        {
        	repeatField = null;
        }
        query = null;
        workbenchTemplate = null;
        Element qryNode = element.element("query");
        if (qryNode != null)
        {
        	SpQuery newQ = new SpQuery();
        	newQ.initialize();
        	newQ.fromXML(qryNode);
        	newQ.setSpecifyUser(contextMgr.getClassObject(SpecifyUser.class));
        	query = alwaysMakeNew ? newQ : determineQueryForImport(newQ);
        	if (query.getId() == null)
        	{
        		query.setName(getUniqueNameForImportQuery(query.getName()));
        	}
        }
        Element wbtNode = element.element("workbenchtemplate");
        if (wbtNode != null)
        {
        	WorkbenchTemplate newWbt = new WorkbenchTemplate();
        	newWbt.initialize();
        	newWbt.fromXML(wbtNode);
        	newWbt.setSpecifyUser(contextMgr.getClassObject(SpecifyUser.class));
        	workbenchTemplate = newWbt;
//        	workbenchTemplate = determineWbTemplateForImport(newWbt);
//        	if (workbenchTemplate.getId() == null)
//        	{
//        		query.setName(getUniqueNameForImportWbTemplate(workbenchTemplate.getName()));
//        	}
        }
    }
    
    /**
     * @param importedQ
     * @return a query equivalent to importedQ if one exists, or importedQ.
     */
    protected SpQuery determineQueryForImport(final SpQuery importedQ)
    {
    	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
    	try
    	{
    		List<SpQuery> matches = session.getDataList(SpQuery.class);
    		for (SpQuery qObj : matches)
    		{
    			qObj.forceLoad();
    			if (qObj.isEquivalent(importedQ))
    			{
    				return qObj;
    			}
    		}
			return importedQ;
    	}
    	finally
    	{
    		session.close();
    	}
    }

    /**
     * @param qName
     * @return qName with extension.
     */
    protected String getUniqueNameForImportQuery(final String qName)
    {
    	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
    	try
    	{
    		boolean done = false;
    		String result = qName;
    		Integer tries = 1;
    		while (!done)
    		{
    			List<?> matches = session.getDataList("from SpQuery where name = '" + result.replace("'", "''")
                        + "' and specifyUserId = " +  AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getId());
    			if (matches.size() == 0)
    			{
    				done = true;
    			}
    			else
    			{
    				result = qName + tries++; //el cheapo
    			}
    		}
    		return result;
    	}
    	finally
    	{
    		session.close();
    	}
    }

}
