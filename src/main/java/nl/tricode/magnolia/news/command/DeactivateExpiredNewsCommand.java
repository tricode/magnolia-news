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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;

import info.magnolia.objectfactory.Components;
import nl.tricode.magnolia.news.templates.NewsRenderableDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Created by mvdmark on 2-12-2014.
 */
public class DeactivateExpiredNewsCommand extends BaseRepositoryCommand {
    private static final Logger log = LoggerFactory.getLogger(DeactivateExpiredNewsCommand.class);

    private static final String NEWS = "mgnl:news";
    private static final String DEACTIVATE_PROPERTY = "unpublishDate";
	private static final String WORKSPACE = "collaboration";

    @Override
    public boolean execute(Context context) {
        try {
            // Get a list of all news nodes with expiryDate.
            List<Node> expiredNodes = NewsRenderableDefinition
                    .getWrappedNodesFromQuery(NewsRenderableDefinition.buildQuery(NEWS, DEACTIVATE_PROPERTY), NEWS, WORKSPACE);
            log.debug("newsNodes size [" + expiredNodes.size() + "].");

            //Unpublish expired nodes.
            unpublishExpiredNodes(context, expiredNodes);
        } catch (Exception e) {
            log.error("Exception: ", e);
            return false;
        }
        return true;
    }

	/**
	 * This method unpublishes the news nodes that are expired.
	 * @param context
	 * @param expiredNodes
	 * @throws javax.jcr.RepositoryException
	 * @throws info.magnolia.cms.exchange.ExchangeException
	 */
	private void unpublishExpiredNodes(Context context, List<Node> expiredNodes) {
		Syndicator syndicator = Components.getComponentProvider().newInstance(Syndicator.class);
		syndicator.init(context.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this.getRepository()), new Rule());

		try {
			//Looping the nodes to unpublish
			for (Node expiredNode : expiredNodes) {
				//Saving the removal of the propery on the session because on the node is deprecated.
				//TODO using new Node object and syndicator by inversion of control (Constructor injection)

				syndicator.deactivate(ContentUtil.asContent(expiredNode));
				log.debug("Node [" + expiredNode.getName() + "  " + expiredNode.getPath() + "] unpublished.");
			}
		} catch (RepositoryException e) {
			log.error("RepositoryException", e);
		} catch (ExchangeException e) {
			log.error("ExchangeException", e);
		}
	}
}