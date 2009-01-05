/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.auth;

import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 24, 2008
 *
 */
public class UserMasterPasswordDBMSMgr extends UserAndMasterPasswordMgr
{
    private String username = null;
    private String password = null;
    
    /**
     * 
     */
    public UserMasterPasswordDBMSMgr()
    {
        super();
    }

    /**
     * @param usersPassword
     */
    public void setUsersPassword(String usersPassword)
    {
        password = usersPassword;
    }

    /**
     * @param usersUserName
     */
    public void setUsersUserName(String usersUserName)
    {
        username = usersUserName;
    }
    
    /**
     * @return
     */
    public Pair<String, String> getUserNamePassword()
    {
        return new Pair<String, String>(username, password);
    }
}
