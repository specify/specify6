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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "deaccession")
public class Deaccession extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long deaccessionId;
     protected String type;
     protected String deaccessionNumber;
     protected Calendar deaccessionDate;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<DeaccessionAgent> deaccessionAgents;
     protected Set<DeaccessionPreparation> deaccessionPreparations;


    // Constructors

    /** default constructor */
    public Deaccession() {
        //
    }
    
    /** constructor with id */
    public Deaccession(Long deaccessionId) {
        this.deaccessionId = deaccessionId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        deaccessionId = null;
        type = null;
        deaccessionNumber = null;
        deaccessionDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        deaccessionAgents = new HashSet<DeaccessionAgent>();
        deaccessionPreparations = new HashSet<DeaccessionPreparation>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "DeaccessionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getDeaccessionId() {
        return this.deaccessionId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.deaccessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Deaccession.class;
    }
    
    public void setDeaccessionId(Long deaccessionId) {
        this.deaccessionId = deaccessionId;
    }

    /**
     *      * Description of the Type of deaccession; i.e. Gift, disposal, lost
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Name institution assigns to the deacession
     */
    @Column(name = "DeaccessionNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getDeaccessionNumber() {
        return this.deaccessionNumber;
    }
    
    public void setDeaccessionNumber(String deaccessionNumber) {
        this.deaccessionNumber = deaccessionNumber;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DeaccessionDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDeaccessionDate() {
        return this.deaccessionDate;
    }
    
    public void setDeaccessionDate(Calendar deaccessionDate) {
        this.deaccessionDate = deaccessionDate;
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
     *      * User definable
     */
    @Column(name = "Text1", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "deaccession")
    public Set<DeaccessionAgent> getDeaccessionAgents() {
        return this.deaccessionAgents;
    }
    
    public void setDeaccessionAgents(Set<DeaccessionAgent> deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     * 
     */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "deaccession")
    public Set<DeaccessionPreparation> getDeaccessionPreparations() {
        return this.deaccessionPreparations;
    }
    
    public void setDeaccessionPreparations(Set<DeaccessionPreparation> deaccessionPreparations) {
        this.deaccessionPreparations = deaccessionPreparations;
    }





    // Add Methods

    public void addDeaccessionAgent(final DeaccessionAgent deaccessionAgent)
    {
        this.deaccessionAgents.add(deaccessionAgent);
        deaccessionAgent.setDeaccession(this);
    }

    public void addDeaccessionPreparations(final DeaccessionPreparation deaccessionPreparation)
    {
        this.deaccessionPreparations.add(deaccessionPreparation);
        deaccessionPreparation.setDeaccession(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeDeaccessionAgent(final DeaccessionAgent deaccessionAgent)
    {
        this.deaccessionAgents.remove(deaccessionAgent);
        deaccessionAgent.setDeaccession(null);
    }

    public void removeDeaccessionPreparations(final DeaccessionPreparation deaccessionPreparation)
    {
        this.deaccessionPreparations.remove(deaccessionPreparation);
        deaccessionPreparation.setDeaccession(null);
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
        return 34;
    }

}
