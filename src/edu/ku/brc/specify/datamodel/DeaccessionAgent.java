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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "deaccessionagent", uniqueConstraints = { @UniqueConstraint(columnNames = { "Role", "AgentID", "DeaccessionID" }) })
public class DeaccessionAgent extends DataModelObjBase implements java.io.Serializable {

    // Fields

    protected Integer     deaccessionAgentId;
    protected String      role;
    protected String      remarks;
    protected Agent       agent;
    protected Deaccession deaccession;


    // Constructors

    /** default constructor */
    public DeaccessionAgent() {
        //
    }

    /** constructor with id */
    public DeaccessionAgent(Integer deaccessionAgentId) {
        this.deaccessionAgentId = deaccessionAgentId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        deaccessionAgentId = null;
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
    @Column(name = "DeaccessionAgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDeaccessionAgentId() {
        return this.deaccessionAgentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.deaccessionAgentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return DeaccessionAgent.class;
    }

    public void setDeaccessionAgentId(Integer deaccessionAgentId) {
        this.deaccessionAgentId = deaccessionAgentId;
    }

    /**
     *      * Role agent played in deaccession
     */
    @Column(name = "Role", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Deaccession.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return deaccession != null ? deaccession.getId() : null;
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
        return 164;
    }

}
