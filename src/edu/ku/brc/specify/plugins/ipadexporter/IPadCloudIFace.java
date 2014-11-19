/*
 * Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute, 1345 Jayhawk Boulevard,
 * Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package edu.ku.brc.specify.plugins.ipadexporter;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.util.Pair;

public interface IPadCloudIFace
{
    public enum LoginStatus {eLoggedIn, eError, eCancelled}
    
    /**
     * @return
     */
    public abstract boolean isLoggedIn();

    /**
     * @param usrName
     * @return
     */
    public abstract boolean isUserNameOK(String usrName);

    /**
     * @param usrName
     * @param pwd
     * @return
     */
    public abstract boolean login(String usrName, String pwd);

    /**
     * @return
     */
    public abstract boolean logout();

    /**
     * @param usrName
     * @param collGuid
     * @return
     */
    public abstract boolean addUserAccessToDataSet(String usrName, String collGuid);

    /**
     * @param usrName
     * @param collGuid
     * @return
     */
    public abstract boolean addDataSetToUser(String usrName, String collGuid);

    /**
     * @param usrName
     * @param collGuid
     * @return
     */
    public abstract boolean removeUserAccessFromDataSet(String usrName,
                                                        String collGuid);

    /**
     * @param usrName
     * @param collGuid
     * @return
     */
    public abstract boolean removeDataSetFromUser(String usrName, String collGuid);

    /**
     * @param dsName
     * @param dirName
     * @param website
     * @param div
     * @param disp
     * @param coll
     * @param isGlobal
     * @param iconName
     * @param curator
     * @param collGuid
     * @return
     */
    public abstract boolean addNewDataSet(String dsName,
                                          String dirName,
                                          String guid,
                                          String div,
                                          String disp,
                                          String coll,
                                          Boolean isGlobal,
                                          String iconName,
                                          String curator,
                                          String collGuid);

    /**
     * @param guid
     * @param website
     * @return
     */
    public abstract boolean doesDataSetExist(String dsName, String guid);
    
    /**
     * @param guid
     * @param guid
     * @return
     */
    public abstract boolean removeDataSet(String dsName, String guid);
    
    /**
     * @param usrName
     * @param pwd
     * @param guid 
     * @return
     */
    public abstract boolean addNewUser(String usrName, String pwd, String guid);

    /**
     * @param collGuid
     * @return
     */
    public abstract List<String> getAccessList(String collGuid);
    
    /**
     * @param instGuid
     * @return
     */
    public abstract List<Pair<Integer, String>> getDatasetList(String instGuid);
    
    /**
     * @param instGuid
     * @return
     */
    public abstract int getNumberOfDatasets(String instGuid);
    
    /**
     * @param collGuid
     * @param isGlobal
     * @return
     */
    public abstract boolean makeDataSetGlobal(String collGuid, boolean isGlobal);

    /**
     * @return
     */
    public abstract boolean removeAccount(Integer instId, String instGUID);

    /**
     * @param newPwd
     * @return
     */
    public abstract boolean setPassword(String newPwd);

    /**
     * @return
     */
    public abstract boolean sendPwdReminder();

    /**
     * @param userId
     * @return
     */
    public abstract Vector<DataSetInfo> getOwnerList();
    
    /**
     * @return
     */
    public abstract boolean isNetworkError();
    
    //---------------------------------------------------------
    // Institution Methods
    //---------------------------------------------------------
    
    /**
     * @return a list of the available Institutions
     */
    public abstract List<String> getInstList();
    
    /**
     * @param webSiteURI the unique URI for the institution
     * @return null if doesn't exist
     */
    public abstract Integer getInstId(String guid);
    
    /**
     * @param instId the InstID
     * @return a hash map of the column names to the values.
     */
    public abstract HashMap<String, Object> getInstitutionInfo(int instId);
    
    /**
     * Creates a new Institution
     * @param title title of Institution
     * @param webSiteURI unique URI of Institution
     * @param code the Institution code
     * @param guid the Institution GUID
     * @return the existing or new Inst ID
     */
    public abstract Integer createInstitution(String title, String webSiteURI, String code, String guid);

    /**
     * Updates an Institution
     * @param instId null if new Inst, or must have a valid Inst ID
     * @param title title of Institution
     * @param webSiteURI unique URI of Institution
     * @return the existing or new Inst ID
     */
    public abstract boolean updateInstitution(Integer instId, String title, String webSiteURI);

}