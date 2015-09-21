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

import com.romcaste.video.movie.Field;
import com.romcaste.video.movie.Movie;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface MediaManager {

    void addMovies( Movie... movies );

    void addMovies( List<Movie> movies );

    void addMovies( File file ) throws MediaException;

    void addMovies( InputStream input ) throws MediaException;

    List<Movie> getMovies();

    List<Movie> sortMovies( Field field, boolean ascending );

    List<Movie> searchMovies( String query );

    List<Movie> filterMovies( Field field, Operator op, String query );
}
