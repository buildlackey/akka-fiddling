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

/**
 * Interface describing the components that make up a movie. This represents a persons own
 * individual copy of a movie.
 *
 * @author  <a href="mailto:clark_malmgren@cable.romcaste.com">Clark Malmgren</a>
 */
public interface Movie {

    /**
     * Get the title of the movie. No distinction or clarification is required on how to handle
     * subtitles.
     *
     * @return  the title of the movie
     */
    String getTitle();

    /**
     * Get a description of the movie.
     *
     * @return  a description of the movie
     */
    String getDescription();

    /**
     * Get a list of actors in the movie. This may only include an abbreviated list of the cast.
     *
     * @return  the list of actors
     */
    String[] getActors();

    /**
     * Get the year that the movie was released.
     *
     * @return  the year the movie was released
     */
    short getYear();

    /**
     * Get the MPAA rating assigned to this movie.
     *
     * @return  the rating
     */
    Rating getRating();

    /**
     * Get the media type for this movie. As this movie represents someone's personally owned asset,
     * this pertains to the copy they own, not the format(s) in which the movie was released.
     *
     * @return  the media type
     */
    MediaType getMedia();
    
    Object getField(Field field);
}
