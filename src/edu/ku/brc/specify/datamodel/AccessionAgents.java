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
package edu.ku.brc.specify.datamodel;

import java.util.Date;

import edu.ku.brc.ui.forms.FormDataObjIFace;




/**

 */
public class AccessionAgents extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long accessionAgentsId;
     protected String role;
     protected String remarks;
     protected Agent agent;
     protected Accession accession;
     protected RepositoryAgreement repositoryAgreement;


    // Constructors

    /** default constructor */
    public AccessionAgents()
    {
        // do nothing
    }
    
    /** constructor with id */
    public AccessionAgents(Long accessionAgentsId)
    {
        this.accessionAgentsId = accessionAgentsId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        accessionAgentsId = null;
        role = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        agent = null;
        accession = null;
        repositoryAgreement = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getAccessionAgentsId() {
        return this.accessionAgentsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    public Long getId()
    {
        return this.accessionAgentsId;
    }
    
    public void setAccessionAgentsId(Long accessionAgentsId) {
        this.accessionAgentsId = accessionAgentsId;
    }

    /**
     *      * Role the agent played in the accession process
     */
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * AgentAdress of agent playing role in Accession
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Accession in which the Agent played a role
     */
    public Accession getAccession() {
        return this.accession;
    }
    
    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    /**
     * 
     */
    public RepositoryAgreement getRepositoryAgreement() {
        return this.repositoryAgreement;
    }
    
    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement) 
    {
        this.repositoryAgreement = repositoryAgreement;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        super.addReference(ref, refType);
        
        /*
        if (StringUtils.isNotEmpty(refType))
        {
            if (refType.equals("agent"))
            {
                if (ref instanceof Agent)
                {
                    agent = (Agent)ref;
                    ((Agent)ref).getAccessionAgents().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Agent");
                }
                
            } else if (refType.equals("accession"))
            {
                if (ref instanceof Accession)
                {
                    accession = (Accession)ref;
                    ((Accession)ref).getAccessionAgents().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Accession");
                }
                
            } else if (refType.equals("repositoryAgreement"))
            {
                if (ref instanceof RepositoryAgreement)
                {
                    repositoryAgreement = (RepositoryAgreement)ref;
                    ((RepositoryAgreement)ref).getRepositoryAgreementAgents().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of RepositoryAgreement");
                }
                
            }
        } else
        {
            throw new RuntimeException("Adding Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
        */
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        super.removeReference(ref, refType);
        /*
        if (StringUtils.isNotEmpty(refType))
        {
            if (refType.equals("agent"))
            {
                if (ref instanceof Agent)
                {
                    agent = null;
                    ((Agent)ref).getAccessionAgents().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Agent");
                }
                
            } else if (refType.equals("accession"))
            {
                if (ref instanceof Accession)
                {
                    accession = null;
                    ((Accession)ref).getAccessionAgents().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Accession");
                }
                
            } else if (refType.equals("repositoryAgreement"))
            {
                if (ref instanceof RepositoryAgreement)
                {
                    repositoryAgreement = null;
                    ((RepositoryAgreement)ref).getRepositoryAgreementAgents().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of RepositoryAgreement");
                }
                
            }
        } else
        {
            throw new RuntimeException("Removing Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
        */
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 12;
    }

    @Override
    public String getIdentityTitle()
    {
        return role + ": " + agent.getIdentityTitle();
    }
}
