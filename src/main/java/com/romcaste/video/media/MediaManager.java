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

/**
 * A <i>MediaManager</i> is responsible for maintaining and handing a collection of movies. The
 * MediaManager needs to be able to handle some of the largest personal collections of movies with
 * ease because why write a program to manage only a few simple assets.
 *
 * <p>When implementing a MediaManager pay special attention to not only write bug-free code, but
 * also strive to maintain a good balance between maintainability, robustness and performance.</p>
 *
 * @author  <a href="mailto:clark_malmgren@cable.romcaste.com">Clark Malmgren</a>
 */
public interface MediaManager {

    /**
     * Add a movie (or multiple movies) to the internal database.
     *
     * @param  movies  the movie(s) to add
     */
    void addMovies( Movie... movies );

    /**
     * Add a list of movies to the internal database.
     *
     * @param  movies  the movies to add
     */
    void addMovies( List<Movie> movies );

    /**
     * Parse the <code>file</code> adding each movie to the internal database. The format is
     * described in {@link MediaManager#addMovies(InputStream)}
     *
     * @param   file  the file to read from
     *
     * @throws  MediaException  if there was any problem reading or parsing the given file
     */
    void addMovies( File file ) throws MediaException;

    /**
     * Parse the <code>file</code> adding each movie to the internal database. The requirements for
     * the format are as follows:
     *
     * <pre>
     *   1. The list of assets SHALL only contain base ASCII characters
     *   2. Each asset SHALL be separated by a single newline
     *   3. Each field within an asset shall be separated by a single pipe "|" character
     *   4. The fields of a movie will appear in the following order:
     *      1) Title
     *      2) Description
     *      3) Actors (comma delimited list)
     *      4) Year
     *      5) Rating
     *      6) Media
     * </pre>
     *
     * <p>In addition each of the special characters may be escaped and used as part of the field
     * instead of a delimiter if it is immediately preceded with a forward slash (<code>\</code>).
     * For example, explicit newlines within a movie's description will appear as the string <code>
     * "\n"</code> and the movie entitled <code>"Escape | A Made Up Movie"</code> would appear as
     * <code>"Escape \| A Made Up Movie"</code>.</p>
     *
     * <p>An example input file is below (remember the pre tags and the astrisks are just HTML and
     * javadoc markup).</p>
     *
     * <pre>
     * Gone with the Wind | American classic | Clark Gable, Vivien Leigh, Thomas Mitchell | 1939 | G | VHS
     * Antitrust | It's about Microsoft | Ryan Phillippe, Tim Robbins, Rachael Leigh Cook | 2001 | PG-13 | DVD
     * </pre>
     *
     * @param   input  the input stream to read from
     *
     * @throws  MediaException  if there was any problem reading or parsing the given stream
     */
    void addMovies( InputStream input ) throws MediaException;

    /**
     * Get a list of movies in the internal database.
     *
     * @return  a list of movies in the database
     */
    List<Movie> getMovies();

    /**
     * Get a sorted list of movies from the internal database sorting on the given field.
     *
     * @param   field      the {@link Field} to sort on
     * @param   ascending  if <code>true</code>, sort in ascending order, otherwise in descending
     *                     order
     *
     * @return  the sorted list of movies
     */
    List<Movie> sortMovies( Field field, boolean ascending );

    /**
     * Get an unsorted list of movies from the internal database that contains the entire query
     * string given. They query string search should be case insensitive and can occur in any field
     * of the {@link Movie}.
     *
     * @param   query  the query string to use to conduct the search
     *
     * @return  a unsorted list of movies that were found in the search
     */
    List<Movie> searchMovies( String query );

    /**
     * Returns an unsorted filtered list of movies according to the given parameters. The individual
     * filter operators are defined in the {@link Operator} Enum.
     *
     * @param   field  the field to filter on
     * @param   op     the operator to use while filtering
     * @param   query  the query string to use when executing the appropriate operation
     *
     * @return  the filtered list of movies
     */
    List<Movie> filterMovies( Field field, Operator op, String query );
}
