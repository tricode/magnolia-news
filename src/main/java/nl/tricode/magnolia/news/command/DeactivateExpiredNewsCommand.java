/**
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

import nl.tricode.magnolia.news.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DeactivateExpiredNewsCommand extends BaseRepositoryCommand {
    private static final Logger log = LoggerFactory.getLogger(DeactivateExpiredNewsCommand.class);

    private static final String NEWS = "mgnl:news";
    private static final String DEACTIVATE_PROPERTY = "unpublishDate";
	 private static final String WORKSPACE = "collaboration";

	 private Syndicator syndicator;

	 @Inject
	 public DeactivateExpiredNewsCommand(Syndicator syndicator) {
		 this.syndicator = syndicator;
	 }

    @Override
    public boolean execute(Context context) {
        try {
            /** Get a list of all news nodes with expiryDate. */
            List<Node> expiredNodes = JcrUtils.getWrappedNodesFromQuery(buildQuery(NEWS, DEACTIVATE_PROPERTY), NEWS, WORKSPACE);
            log.debug("newsNodes size [" + expiredNodes.size() + "].");

            /** Unpublish expired nodes. */
            unpublishExpiredNodes(context, expiredNodes);
        } catch (Exception e) {
            log.error("Exception: ", e);
            return false;
        }
        return true;
    }

	/**
	 * This method unpublishes the news nodes that are expired.

	 * @param expiredNodes
	 * @throws javax.jcr.RepositoryException
	 * @throws info.magnolia.cms.exchange.ExchangeException
	 */
	private void unpublishExpiredNodes(Context context, List<Node> expiredNodes) {
		/** Syndicator init method still needed because there is no other way to set user and workspace.
		  * Magnolia does the same in there activation module. */
		syndicator.init(context.getUser(), this.getRepository(), WORKSPACE, new Rule());
		try {
			/** Looping the nodes to unpublish. */
			for (Node expiredNode : expiredNodes) {
				/** Saving the removal of the propery on the session because on the node is deprecated. */
				this.syndicator.deactivate(ContentUtil.asContent(expiredNode));

				log.debug("Node [" + expiredNode.getName() + "  " + expiredNode.getPath() + "] unpublished.");
			}
		} catch (RepositoryException e) {
			log.error("RepositoryException", e);
		} catch (ExchangeException e) {
			log.error("ExchangeException", e);
		}
	}

	/**
	 * Build query to fetch expired and activated nodes.
	 * @param nodeType
	 * @param deactivateProperty
	 * @return
	 */
	private String buildQuery(String nodeType, String deactivateProperty) {
		Calendar calendar = Calendar.getInstance();

		/** Current date minus one day. */
		calendar.add(Calendar.DATE, -1);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String currentDate = sdf.format(calendar.getTime());

		String query =  "SELECT * FROM [" + nodeType + "] " +
				  "WHERE [" + nodeType + "]." + deactivateProperty + " IS NOT NULL " +
				  "AND [" + nodeType + "].[mgnl:activationStatus]=true " +
				  "AND [" + nodeType + "]." + deactivateProperty + " < CAST('" + currentDate + "' AS DATE)";
		log.debug(query);
		return query;
	}
}