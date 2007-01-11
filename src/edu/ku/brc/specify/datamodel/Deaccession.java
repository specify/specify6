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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
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
     protected Set<DeaccessionAgents> deaccessionAgents;
     protected Set<DeaccessionPreparation> deaccessionPreparations;


    // Constructors

    /** default constructor */
    public Deaccession() {
    }
    
    /** constructor with id */
    public Deaccession(Long deaccessionId) {
        this.deaccessionId = deaccessionId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        deaccessionId = null;
        type = null;
        deaccessionNumber = null;
        deaccessionDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        deaccessionAgents = new HashSet<DeaccessionAgents>();
        deaccessionPreparations = new HashSet<DeaccessionPreparation>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getDeaccessionId() {
        return this.deaccessionId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.deaccessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
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
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Name institution assigns to the deacession
     */
    public String getDeaccessionNumber() {
        return this.deaccessionNumber;
    }
    
    public void setDeaccessionNumber(String deaccessionNumber) {
        this.deaccessionNumber = deaccessionNumber;
    }

    /**
     * 
     */
    public Calendar getDeaccessionDate() {
        return this.deaccessionDate;
    }
    
    public void setDeaccessionDate(Calendar deaccessionDate) {
        this.deaccessionDate = deaccessionDate;
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
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    public Set<DeaccessionAgents> getDeaccessionAgents() {
        return this.deaccessionAgents;
    }
    
    public void setDeaccessionAgents(Set<DeaccessionAgents> deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     * 
     */
    public Set<DeaccessionPreparation> getDeaccessionPreparations() {
        return this.deaccessionPreparations;
    }
    
    public void setDeaccessionPreparations(Set<DeaccessionPreparation> deaccessionPreparations) {
        this.deaccessionPreparations = deaccessionPreparations;
    }





    // Add Methods

    public void addDeaccessionAgents(final DeaccessionAgents deaccessionAgent)
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

    public void removeDeaccessionAgents(final DeaccessionAgents deaccessionAgent)
    {
        this.deaccessionAgents.remove(deaccessionAgent);
        deaccessionAgent.setDeaccession(null);
    }

    public void removeDeaccessionPreparations(final DeaccessionPreparation deaccessionPreparation)
    {
        this.deaccessionPreparations.remove(deaccessionPreparation);
        deaccessionPreparation.setDeaccession(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 34;
    }

}
