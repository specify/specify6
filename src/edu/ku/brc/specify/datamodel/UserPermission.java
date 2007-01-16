package edu.ku.brc.specify.datamodel;





/**

 */
public class UserPermission extends DataModelObjBase implements java.io.Serializable
{
    // Fields    

     protected Long userPermissionId;
     protected Boolean dataAccessPrivilege;
     protected Boolean adminPrivilege;
     protected SpecifyUser specifyUser;
     protected CollectionObjDef collectionObjDef;


    // Constructors

    /** default constructor */
    public UserPermission() {
    }
    
    /** constructor with id */
    public UserPermission(Long userPermissionId) {
        this.userPermissionId = userPermissionId;
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        userPermissionId = null;
        dataAccessPrivilege = null;
        adminPrivilege = null;
        specifyUser = null;
        collectionObjDef = null;
    }  

    // Property accessors

    /**
     * 
     */
    public Long getUserPermissionId() {
        return this.userPermissionId;
    }
    
    public void setUserPermissionId(Long userPermissionId) {
        this.userPermissionId = userPermissionId;
    }

    /**
     *      * User definable
     */
    public Boolean getDataAccessPrivilege() {
        return this.dataAccessPrivilege;
    }
    
    public void setDataAccessPrivilege(Boolean dataAccessPrivilege) {
        this.dataAccessPrivilege = dataAccessPrivilege;
    }

    /**
     *      * User definable
     */
    public Boolean getAdminPrivilege() {
        return this.adminPrivilege;
    }
    
    public void setAdminPrivilege(Boolean adminPrivilege) {
        this.adminPrivilege = adminPrivilege;
    }

    /**
     * 
     */
    public SpecifyUser getSpecifyUser() {
        return this.specifyUser;
    }
    
    public void setSpecifyUser(SpecifyUser owner) {
        this.specifyUser = owner;
    }
    
    /**
     * 
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.userPermissionId;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 88;
    }
    @Override
    public String getIdentityTitle()
    { 
        return super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

}