/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class HQLSpecs
{
    protected final String                     hql;
    protected final List<Pair<String, Object>> args;
    protected final List<SortElement>          sortElements;

    public HQLSpecs(final String hql, final List<Pair<String, Object>> args,
            final List<SortElement> sortElements)
    {
        this.hql = hql;
        this.args = args;
        this.sortElements = sortElements;
    }

    public String getHql()
    {
        return hql;
    }

    public List<Pair<String, Object>> getArgs()
    {
        return args;
    }

    public List<SortElement> getSortElements()
    {
        return sortElements;
    }
}
