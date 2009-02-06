/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.ui;

import java.util.List;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 6, 2009
 *
 */
public interface SearchTermParserIFace
{

    /**
     * @return the fields
     */
    public abstract List<SearchTermField> getFields();

    /**
     * @param searchTermArg
     * @param parseAsSingleTerm
     * @return true if all the tokens are valid
     */
    public abstract boolean parse(final String searchTermArg, final boolean parseAsSingleTerm);

    /**
     * @param term
     * @param abbrevArg
     * @param fieldName
     * @param termStr
     * @return
     */
    public abstract String createWhereClause(final SearchTermField term,
                                             final String abbrevArg,
                                             final String fieldName);

}