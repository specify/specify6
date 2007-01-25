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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**

 */
@Entity
@Table(name = "deaccessionagents", uniqueConstraints = { @UniqueConstraint(columnNames = { "Role", "AgentID", "DeaccessionID" }) })
public class DeaccessionAgents extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long deaccessionAgentsId;
     protected String role;
     protected String remarks;
     protected Agent agent;
     protected Deaccession deaccession;


    // Constructors

    /** default constructor */
    public DeaccessionAgents() {
        //
    }
    
    /** constructor with id */
    public DeaccessionAgents(Long deaccessionAgentsId) {
        this.deaccessionAgentsId = deaccessionAgentsId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        deaccessionAgentsId = null;
        role = null;
        remarks = null;
        agent = null;
        deaccession = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "DeaccessionAgentsID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getDeaccessionAgentsId() {
        return this.deaccessionAgentsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.deaccessionAgentsId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return DeaccessionAgents.class;
    }
    
    public void setDeaccessionAgentsId(Long deaccessionAgentsId) {
        this.deaccessionAgentsId = deaccessionAgentsId;
    }

    /**
     *      * Role agent played in deaccession
     */
    @Column(name = "Role", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * AgentID for agent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Deaccession agent played role in
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DeaccessionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Deaccession getDeaccession() {
        return this.deaccession;
    }
    
    public void setDeaccession(Deaccession deaccession) {
        this.deaccession = deaccession;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 35;
    }

}
