package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class SpecifyUser  implements java.io.Serializable {

    protected static SpecifyUser currentUser = null;;
    
    // Fields

     protected Integer specifyUserId;
     protected String name;
     protected String email;
     protected String userType;
     protected Short privLevel;
     protected Set<CollectionObjDef> collectionObjDef;
     protected Set<RecordSet> recordSets;
     protected UserGroup userGroup;
     protected Set<AppResourceDefault> appResourceDefaults;
     
    // Constructors

    /** default constructor */
    public SpecifyUser() {
    }

    /** constructor with id */
    public SpecifyUser(Integer specifyUserId) {
        this.specifyUserId = specifyUserId;
    }

    /**
     * Return the current Specify User.
     * @return the current Specify User
     */
    public static SpecifyUser getCurrentUser()
    {
        return currentUser;
    }
    
    /**
     * Sets the Current Specify User.
     * @param currentUser the current specify user
     */
    public static void setCurrentUser(final SpecifyUser currentUser)
    {
        SpecifyUser.currentUser = currentUser;
    }


    // Initializer
    public void initialize()
    {
        specifyUserId = null;
        name = null;
        email = null;
        privLevel = null;
        collectionObjDef = new HashSet<CollectionObjDef>();
        recordSets = new HashSet<RecordSet>();
        userGroup = null;
        appResourceDefaults = new HashSet<AppResourceDefault>();
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
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
    *
    */
    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
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



    public Set<AppResourceDefault> getAppResourceDefaults()
    {
        return appResourceDefaults;
    }

    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults)
    {
        this.appResourceDefaults = appResourceDefaults;
    }


    // Add Methods


    public void addCollectionObjDefs(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef.add(collectionObjDef);
        collectionObjDef.setSpecifyUser(this);
    }

    public void addRecordSets(final RecordSet recordSet)
    {
        this.recordSets.add(recordSet);
        recordSet.setOwner(this);
    }

    public void addAppResourceDefaults(final AppResourceDefault appResourceDefault)
    {
        this.appResourceDefaults.add(appResourceDefault);
        appResourceDefault.setSpecifyUser(this);
    }


    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjDefs(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef.remove(collectionObjDef);
        collectionObjDef.setSpecifyUser(null);
    }

    public void removeRecordSets(final RecordSet recordSet)
    {
        this.recordSets.remove(recordSet);
        recordSet.setOwner(null);
    }
    
    public void removeAppResourceDefaults(final AppResourceDefault appResourceDefault)
    {
        this.appResourceDefaults.remove(appResourceDefault);
        appResourceDefault.setSpecifyUser(null);
    }


    // Delete Add Methods
}
