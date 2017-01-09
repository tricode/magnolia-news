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
package nl.tricode.magnolia.news.command;

import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import nl.tricode.magnolia.news.NewsNodeTypes;
import nl.tricode.magnolia.news.util.JcrUtils;
import nl.tricode.magnolia.news.util.NewsRepositoryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DeactivateExpiredNewsCommand extends BaseRepositoryCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeactivateExpiredNewsCommand.class);

    private static final String DEACTIVATE_PROPERTY = "unpublishDate";

    private final Syndicator syndicator;

    @Inject
    public DeactivateExpiredNewsCommand(Syndicator syndicator) {
        this.syndicator = syndicator;
    }

    @Override
    public boolean execute(final Context context) {
        try {
            // Get a list of all news nodes with expiryDate
            final List<Node> expiredNodes = JcrUtils.getWrappedNodesFromQuery(buildQuery(NewsNodeTypes.News.NAME, DEACTIVATE_PROPERTY), NewsNodeTypes.News.NAME, NewsRepositoryConstants.COLLABORATION);
            LOGGER.debug("expiredNodes size [{}].", expiredNodes.size());

            // Unpublish expired nodes
            unpublishExpiredNodes(context, expiredNodes);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private void unpublishExpiredNodes(final Context context, final List<Node> expiredNodes)
            throws RepositoryException, ExchangeException {
        // Syndicator init method still needed because there is no other way to set user and workspace
        // Magnolia does the same in their activation module
        syndicator.init(context.getUser(), this.getRepository(), NewsRepositoryConstants.COLLABORATION, new Rule());

        // Looping the nodes to unpublish
        for (Node expiredNode : expiredNodes) {
            // Saving the removal of the property on the session because on the node is deprecated
            // Still using ContentUtil until there is a replacement
            this.syndicator.deactivate(ContentUtil.asContent(expiredNode));

            LOGGER.debug("Node [{}, {}] unpublished.", expiredNode.getName(), expiredNode.getPath());
        }
    }

    /**
     * Build query to fetch expired and activated nodes.
     *
     * @param nodeType           The nodetype to query.
     * @param deactivateProperty Name of the deactivation property.
     * @return Returns the query.
     */
    private static String buildQuery(final String nodeType, final String deactivateProperty) {
        Calendar calendar = Calendar.getInstance();

        // Current date minus one day
        calendar.add(Calendar.DATE, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String currentDate = sdf.format(calendar.getTime());

        String query = "SELECT * FROM [" + nodeType + "] " +
                "WHERE [" + nodeType + "]." + deactivateProperty + " IS NOT NULL " +
                "AND [" + nodeType + "].[mgnl:activationStatus]=true " +
                "AND [" + nodeType + "]." + deactivateProperty + " < CAST('" + currentDate + "' AS DATE)";
        LOGGER.debug(query);
        return query;
    }

}