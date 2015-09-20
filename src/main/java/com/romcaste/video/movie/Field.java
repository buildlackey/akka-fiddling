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

import com.romcaste.video.media.MediaManager;

/**
 * An enumeration for each field within a {@link Movie} that can be used by a {@link MediaManager}
 * to filter or sort.
 *
 * @author  <a href="mailto:clark_malmgren@cable.romcaste.com">Clark Malmgren</a>
 */
public enum Field {

	/** @see Movie#getTitle() */
    TITLE,
    
    /** @see Movie#getDescription()*/
    DESCRIPTION, 
    
    /** @see Movie#getActors() */
    ACTORS, 
    
    /** @see Movie#getYear() */
    YEAR, 
    
    /** @see Movie#getRating() */
    RATING, 
    
    /** @see Movie#getMedia() */
    MEDIA
}
