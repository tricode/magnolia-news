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
package nl.tricode.magnolia.news.util;

import info.magnolia.cms.core.Path;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class NewsWorkspaceUtil {

    private NewsWorkspaceUtil() {
        // Util class, prevent instantiating
    }

    /**
     * @param node Node
     * @param propertyName Name of property.
     * @throws javax.jcr.RepositoryException Repository Exception.
     * @return nodeName
     * Create a new Node Unique NodeName.
     */
    public static String generateUniqueNodeName(final Node node, String propertyName) throws RepositoryException {
        String newNodeName = NewsWorkspaceUtil.defineNodeName(node, propertyName);
        return Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), newNodeName);
    }

    public static boolean hasNameChanged(Node node, String nameProperty) throws RepositoryException {
        return !node.getName().equals(NewsWorkspaceUtil.defineNodeName(node, nameProperty));
    }

    /**
     * Filters characters like ?, !, etc and replaces spaces with -
     *
     * @param input Non filtered string.
     * @return output Filtered string on non word characters.
     */
    private static String filterNonWordCharacters(String input) {
        String output = input.trim();
        return (output.replaceAll("[^\\w\\s\\-]", StringUtils.EMPTY).replaceAll("\\s+", NewsStringUtils.HYPHEN));
    }

    /**
     * Define the Node Name. Node Name will be title in lower case and spaces replaced by '-'
     * Characters that will be removed are % ^ { } etc.
     */
    private static String defineNodeName(final Node node, String propertyName) throws RepositoryException {
        String title = node.getProperty(propertyName).getString();
        return NewsWorkspaceUtil.filterNonWordCharacters(title).toLowerCase();
    }
}