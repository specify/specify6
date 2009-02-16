/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify;

/**
 * Implements an enum for describing user types.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 16, 2009
 *
 */
public class SpecifyUserTypes
{

    public enum UserType {Manager, FullAccess, LimitedAccess, Guest}
    
    /*public enum UserType 
    {
        Manager(0,       "Manager"),
        FullAccess(1,    "FullAccess"),
        LimitedAccess(2, "LimitedAccess"),
        Guest(3,         "Guest");

        UserType(final int id, final String name)
        {
            this.id   = id;
            this.name = name;
        }

        private int    id;
        private String name;

        public Integer getId()          { return id; }
        public String  getName()        { return name; }
        public String  getDirName()     { return name.toLowerCase(); }
        
        @Override
        public String  toString()       { return name; }
    }*/

}
