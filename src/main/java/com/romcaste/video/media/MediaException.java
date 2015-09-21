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

public class MediaException extends Exception {

    private static final long serialVersionUID = 4216962453231975255L;

    public MediaException( String message ) {
        super( message );
    }

    public MediaException( Throwable cause ) {
        super( cause );
    }

    public MediaException( String message, Throwable cause ) {
        super( message, cause );
    }
}
