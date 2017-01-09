/*
 *      Tricode News module
 *      Is a News app for Magnolia CMS.
 *      Copyright (C) 2015  Tricode Business Integrators B.V.
 *
 * 	  This program is free software: you can redistribute it and/or modify
 *		  it under the terms of the GNU General Public License as published by
 *		  the Free Software Foundation, either version 3 of the License, or
 *		  (at your option) any later version.
 *
 *		  This program is distributed in the hope that it will be useful,
 *		  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *		  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *		  GNU General Public License for more details.
 *
 *		  You should have received a copy of the GNU General Public License
 *		  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tricode.magnolia.news;

import info.magnolia.jcr.util.NodeTypes;

@SuppressWarnings("unused") //Node representation in java.
public class NewsNodeTypes {

    private NewsNodeTypes() {
        // Prevent instantiating this class.
    }

    /**
     * Represents the nodeType mgnl:news.
     */
    public static class News {
        // Node Type Name
        public static final String NAME = NodeTypes.MGNL_PREFIX + "news";

        // Node Type Folder Name
        public static final String FOLDER = NodeTypes.MGNL_PREFIX + "newsFolder";

        // Property Name
        public static final String PROPERTY_TITLE = "title";
        public static final String PROPERTY_SUMMARY = "summary";
        public static final String PROPERTY_TEXT = "text";
        public static final String PROPERTY_STARTDATE = "startDate";
        public static final String PROPERTY_UNPUBLISHDATE = "unpublishDate";
        public static final String PROPERTY_COMMENTS_ENABLED = "comments";
        public static final String PROPERTY_EXTERNAL_SOURCE = "externalSource";
        public static final String PROPERTY_CATEGORIES = "categories";
        public static final String PROPERTY_TAGS = "tags";

        private News() {
            // Prevent instantiating this class
        }
    }
}