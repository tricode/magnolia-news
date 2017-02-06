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

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class JcrUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JcrUtils.class);

    private JcrUtils() {
        // Util class, prevent instantiating
    }

    /**
     * Query news items using JCR SQL2 syntax.
     *
     * @param query         Query string
     * @param maxResultSize Max results returned
     * @param pageNumber    paging number
     * @param nodeTypeName  Node type
     * @return List List of news item nodes
     * @throws javax.jcr.RepositoryException Repository Exceotopn
     */
    public static List<Node> getWrappedNodesFromQuery(String query, int maxResultSize, int pageNumber, String nodeTypeName) throws RepositoryException {
        final List<Node> itemsListPaged = new ArrayList<>(0);
        final NodeIterator items = QueryUtil.search(NewsRepositoryConstants.COLLABORATION, query, Query.JCR_SQL2, nodeTypeName);

        // Paging result set
        final int startRow = (maxResultSize * (pageNumber - 1));
        if (startRow > 0) {
            try {
                items.skip(startRow);
            } catch (NoSuchElementException e) {
                LOGGER.error("No more news items found beyond this item number: {}", startRow);
            }
        }

        int count = 1;
        while (items.hasNext() && count <= maxResultSize) {
            itemsListPaged.add(new I18nNodeWrapper(items.nextNode()));
            count++;
        }
        return itemsListPaged;
    }

    public static List<Node> getWrappedNodesFromQuery(String query, String nodeTypeName, String workspace) throws RepositoryException {
        final List<Node> itemsListPaged = new ArrayList<>(0);
        final NodeIterator items = QueryUtil.search(workspace, query, Query.JCR_SQL2, nodeTypeName);

        while (items.hasNext()) {
            itemsListPaged.add(new I18nNodeWrapper(items.nextNode()));
        }
        return itemsListPaged;
    }

    public static String buildQuery(String path, String contentType) {
        return buildQuery(path, contentType, false, null);
    }

    public static String buildQuery(String path, String contentType, boolean useFilters, String customFilters) {
        return buildQuery(path, contentType, useFilters, customFilters, false);
    }

    public static String buildQuery(String path, String contentType, boolean useFilters, String customFilters, boolean orderBySearch) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT p.* FROM [").append(contentType).append("] AS p ");
        query.append("WHERE ISDESCENDANTNODE(p, '").append(org.apache.commons.lang.StringUtils.defaultIfEmpty(path, "/")).append("') ");

        if (useFilters) {
            query.append(customFilters);
        }

        if (orderBySearch) {
            query.append("score() desc ");
        } else {
            query.append("ORDER BY p.[mgnl:created] desc");
        }

        LOGGER.debug("BuildQuery [{}].", query.toString());
        return query.toString();
    }
}