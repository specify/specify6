/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.toycode;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.System;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class BirthdayQ
{

    protected static final int InstitutionCode = 1;
    //protected static final int CatalogNumber = 2;
    protected static final int ScientificName = 2;
    //protected static final int Family = 4;
    protected static final int ContinentOrOcean = 3;
    protected static final int Country = 4;
    protected static final int CollectorName = 5;
    protected static final int Year = 6;
    protected static final int Month = 7;
    protected static final int Day = 8;
    protected static final int State = 9;
    
    
    protected static String quotequote(final String arg)
    {
        return arg.replaceAll("'", "''");
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Statement stmt = null;
        ResultSet rs = null;
        Statement inserter = null;
        try
        {
            Class.forName("com.mysql.jdbc.Driver");

            String gbifConnStr = "jdbc:mysql://sdl.nhm.ku.edu:3399/gbifCache?"
                + "user=tnoble&password=specify4us";
            Connection spConn = DriverManager.getConnection("jdbc:mysql://localhost/testfishDYWB?"
                    + "user=rods&password=rods");
            Connection gbifConn = DriverManager.getConnection(gbifConnStr);
            
            
            int minKey;
            
            minKey = 94705801;
//            stmt = gbifConn.createStatement();
//            rs = stmt.executeQuery("select min(id) from gbifCache.raw_occurrence_record");
//            rs.next();
//            minKey = rs.getInt(1);
//            rs.close();
//            stmt.close();
            
            stmt = gbifConn.createStatement();
            rs = stmt.executeQuery("select max(id) from gbifCache.raw_occurrence_record");
            rs.next();
            int maxKey = rs.getInt(1);
            rs.close();
            stmt.close();

            String gbifSQL = "SELECT institution_code, scientific_name, continent_ocean, country, collector_name, year, month, day, state_province from "
                + "gbifCache.raw_occurrence_record where scientific_name is not null and year is not null and month is not null and day is not null "
                + "and (country is not null or continent_ocean is not null) and collector_name is not null and id =";
            long recCount = 0;
            long skipCount = 0;
            long errCount = 0;
            StringBuilder sb = null;
            for (int id = minKey; id <= maxKey; id++)
            {
                stmt = gbifConn.createStatement();
                rs = stmt.executeQuery(gbifSQL + id);
                if (rs.isBeforeFirst())
                {
                    rs.next();
                    int year;
                    try
                    {
                    	year = rs.getInt(Year);
                    }
                    catch (SQLException ex)
                    {
                    	year = 0;
                    }
                    if (year > 1915)
                    {
                        inserter = spConn.createStatement();
                        try
                        {

                            String dateStr = rs.getInt(Year) + "/" + rs.getInt(Month) + "/"
                                    + rs.getInt(Day);
                            // insert into collectionobject (TimestampCreated, Version,
                            // CollectionMemberID, Availability, CatalogNumber, CatalogedDate,
                            // FieldNumber, Modifier, Name, Text1, Text2) Values((select
                            // TimestampCreated from collectingevent where collectingeventid = 1),
                            // 1, 1, 'none', '5555', '2008-02-13', 'Bubba 88', 'NHM', 'Bubbus
                            // bubbus', 'Dwayne Dwyer', 'plop', 1)
                            sb = new StringBuilder();
                            sb.append("insert into collectionobject (TimestampCreated, Version, CollectionMemberID, collectionid, CatalogNumber, Availability, CatalogedDate, Remarks, Name, Text1, Text2)");
                            sb.append("values((select TimestampCreated from collectingevent where collectingeventid = 1), 1, 1, 1,"); // TimestampCreated,
                            // Version,
                            // CollectionMemberId,
                            // collectionId
                            sb.append("'" + String.valueOf(id) + "',"); // CatalogNumber
                            String inst = rs.getString(InstitutionCode);
                            if (inst != null)
                            {
                                inst = inst.trim();
                                if (inst.length() > 32)
                                {
                                    inst = inst.substring(0,30);
                                }
                            }
                            sb.append(inst == null ? "null," : "'"
                                    + quotequote(inst) + "', "); // Availability
                            //sb.append("'" + quotequote(rs.getString(CatalogNumber).trim()) + "', "); // CatalogedDateVerbatim
                            sb.append("'" + dateStr + "', "); // CatalogedDate
//                            sb.append((rs.getString(ContinentOrOcean) == null ? "null," : "'"
//                                    + quotequote(rs.getString(ContinentOrOcean).trim()) + "', ")); // Remarks
//                            sb.append("'" + quotequote(rs.getString(Country).trim()) + "', "); // Modifier
                            String LocStr = (rs.getString(ContinentOrOcean) != null ? rs.getString(ContinentOrOcean).trim() : "");
                            if (rs.getString(Country) != null)
                            {
                                if (!LocStr.equals(""))
                                {
                                    LocStr += ": ";
                                }
                                LocStr = LocStr + rs.getString(Country).trim();
                            }
                            sb.append("'" + LocStr + "', "); //Remarks    
                            sb.append("'" + quotequote(rs.getString(CollectorName).trim()) + "', "); // Name
                            sb.append("'" + quotequote(rs.getString(ScientificName).trim())
                                            + "',"); // Text1
                            sb.append((rs.getString(State) == null ? "null)" : "'"
                                    + quotequote(rs.getString(State).trim()) + "')")); //Text2
                            inserter.execute(sb.toString());
                            recCount++;
                            System.out
                                    .println(recCount + " records added." + id + " maximum gbif id.");
                        }
                        catch (SQLException ex)
                        {
                            System.out.println(ex);
                            System.out.println(sb);
                            System.out.println(++errCount);
                        }
                        finally
                        {
                            inserter.close();
                        }
                    }
                }
                else
                {
                    System.out.println("skipped: " + ++skipCount);
                }
                //                if (recCount > 500000)
                //                {
                //                    break;
                //                }
                rs.close();
                stmt.close();
            }
            System.out.println("done." + " records added: " + recCount + ", skipped: " + skipCount
                    + ", errors: " + errCount);
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println(ex);
        }
        catch (SQLException ex)
        {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

}
