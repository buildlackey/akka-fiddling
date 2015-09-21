/* 
 * ===========================================================================
 * This file is the intellectual property of romcaste Corp.  It
 * may not be copied in whole or in part without the express written
 * permission of romcaste or its designees.
 * ===========================================================================
 * Copyright (c) 2010 romcaste Corp. All rights reserved.
 * ===========================================================================
 */

package com.romcaste.video.media;

/**
 * Enumeration of all of the different valid operators for filtering a list of movie assets.
 *
 */
public enum Operator {

    /**
     * Case insensitive string search. When executing a contains within a number, that number should
     * first be converted to a decimal string representation before executing the search.
     */
    CONTAINS,

    /** Equality operator. In the case of strings, this is case sensitive. */
    EQUALS,

    /** Less than operator. String comparisons are lexicographical maintaining case */
    LESS_THAN,

    /** Greater than operator. String comparisons are lexicographical maintaining case */
    GREATER_THAN
}
