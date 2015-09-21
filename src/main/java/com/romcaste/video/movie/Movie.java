/* 
 * ===========================================================================
 * This file is the intellectual property of romcaste Corp.  It
 * may not be copied in whole or in part without the express written
 * permission of romcaste or its designees.
 * ===========================================================================
 * Copyright (c) 2010 romcaste Corp. All rights reserved.
 * ===========================================================================
 */

package com.romcaste.video.movie;

public interface Movie {

    String getTitle();

    String getDescription();

    String[] getActors();

    short getYear();

    Rating getRating();

    MediaType getMedia();
    
    Object getField(Field field);
}
