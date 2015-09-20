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
 * Generic exception used to wrap and further describe any error that occurs within the movie
 * collection components.
 *
 * @author  <a href="mailto:clark_malmgren@cable.romcaste.com">Clark Malmgren</a>
 */
public class MediaException extends Exception {

    /** Generated Serial Version UID. */
    private static final long serialVersionUID = 4216962453231975255L;

    /**
     * Constructs a new media exception with the specified detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to initCause.
     *
     * @param  message  the detail message. The detail message is saved for later retrieval by the
     *                  getMessage() method.
     */
    public MediaException( String message ) {
        super( message );
    }

    /**
     * Constructs a new media exception with the specified cause and a detail message of <tt>
     * (cause==null ? null : cause.toString())</tt> (which typically contains the class and detail
     * message of <tt>cause</tt>).
     *
     * @param  cause  the cause (which is saved for later retrieval by the {@link #getCause()}
     *                method). (A <tt>null</tt> value is permitted, and indicates that the cause is
     *                nonexistent or unknown.)
     */
    public MediaException( Throwable cause ) {
        super( cause );
    }

    /**
     * Constructs a new media exception with the specified detail message and cause.
     *
     * <p>Note that the detail message associated with <code>cause</code> is <i>not</i>
     * automatically incorporated in this exception's detail message.</p>
     *
     * @param  message  the detail message (which is saved for later retrieval by the {@link
     *                  #getMessage()} method).
     * @param  cause    the cause (which is saved for later retrieval by the {@link #getCause()}
     *                  method). (A <tt>null</tt> value is permitted, and indicates that the cause
     *                  is nonexistent or unknown.)
     */
    public MediaException( String message, Throwable cause ) {
        super( message, cause );
    }
}
