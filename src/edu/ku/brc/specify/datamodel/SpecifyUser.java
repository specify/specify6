package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class SpecifyUser  implements java.io.Serializable {

    // Fields

     protected Integer specifyUserId;
     protected String name;
     protected String password;
     protected Short privLevel;
     protected Set<CollectionObjDef> collectionObjDef;
     protected Set<RecordSet> recordSets;
     protected UserGroup userGroup;


    // Constructors

    /** default constructor */
    public SpecifyUser() {
    }

    /** constructor with id */
    public SpecifyUser(Integer specifyUserId) {
        this.specifyUserId = specifyUserId;
    }




    // Initializer
    public void initialize()
    {
        specifyUserId = null;
        name = null;
        password = null;
        privLevel = null;
        collectionObjDef = new HashSet<CollectionObjDef>();
        recordSets = new HashSet<RecordSet>();
        userGroup = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Integer getSpecifyUserId() {
        return this.specifyUserId;
    }

    public void setSpecifyUserId(Integer specifyUserId) {
        this.specifyUserId = specifyUserId;
    }

    /**
     *
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     */
    public Short getPrivLevel() {
        return this.privLevel;
    }

    public void setPrivLevel(Short privLevel) {
        this.privLevel = privLevel;
    }

    /**
     *
     */
    public Set<CollectionObjDef> getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(Set<CollectionObjDef> collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     *
     */
    public Set<RecordSet> getRecordSets() {
        return this.recordSets;
    }

    public void setRecordSets(Set<RecordSet> recordSets) {
        this.recordSets = recordSets;
    }

    /**
     *
     */
    public UserGroup getUserGroup() {
        return this.userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }





    // Add Methods

    public void addCollectionObjDef(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef.add(collectionObjDef);
        collectionObjDef.setSpecifyUser(this);
    }

    public void addRecordSet(final RecordSet recordSet)
    {
        this.recordSets.add(recordSet);
        recordSet.setOwner(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjDef(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef.remove(collectionObjDef);
        collectionObjDef.setSpecifyUser(null);
    }

    public void removeRecordSet(final RecordSet recordSet)
    {
        this.recordSets.remove(recordSet);
        recordSet.setOwner(null);
    }

    // Delete Add Methods
}
