/*
 * Tricode News module
 * Is a News app for Magnolia CMS.
 * Copyright (C) 2015  Tricode Business Integrators B.V.
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tricode.magnolia.news.setup;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import nl.tricode.magnolia.news.NewsNodeTypes;
import nl.tricode.magnolia.news.util.NewsRepositoryConstants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;

/**
 * Due to a configuration error in this component, it was possible (until now) to create nested news items within
 * news items. This is not the designed behavior and therefore we chose to correct it. The component configuration
 * has been fixed and this task will seek out any nested news item nodes and move them to the parent level.
 */
public class FindAndMoveNestedNewsTask implements Task {

    @Override
    public String getName() {
        return "Find and move nested news items";
    }

    @Override
    public String getDescription() {
        return "News items were nested by accident, this task will fix that";
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        try {
            final Session session = MgnlContext.getJCRSession(NewsRepositoryConstants.COLLABORATION);

            final Node rootNode = session.getRootNode();
            final int numberOfMovedNodes = processNewsItems(installContext, rootNode, 0);

            if (numberOfMovedNodes > 0) {
                installContext.info("Nested news items were found and have been un-nested.");
                installContext.info("Note: If present, duplicate news items now have a name that ends with '-duplicate'.");
                installContext.info("Please check all news items and re-publish if needed.");
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Error while un-nesting event nodes", e);
        }
    }

    private static int processNewsItems(final InstallContext installContext, final Node rootNode, int numberOfMovedNodes)
            throws RepositoryException, TaskExecutionException {
        final List<Node> allNewsNodes = NodeUtil.asList(NodeUtil.getNodes(rootNode, NewsNodeTypes.News.NAME));

        for (Node newsNode : allNewsNodes) {
            if (newsNode.hasNodes()) {
                final NodeIterator subNodesIterator = newsNode.getNodes();

                while (subNodesIterator.hasNext()) {
                    final Node subNode = subNodesIterator.nextNode();

                    if ("image".equalsIgnoreCase(subNode.getName())) {
                        continue;
                    }

                    // Found a nested news item node, let's move it to the rootNode
                    String targetPath = determineTargetPath(rootNode, subNode);

                    if (rootNode.getSession().nodeExists(targetPath)) {
                        // Target node exists! Do not overwrite it, but rename the old node before moving it
                        targetPath += "-duplicate";
                    }

                    // Moving node
                    new MoveNodeTask(
                            "Moving news item node",
                            "Moving node '" + subNode.getName() + "' to: " + rootNode.getPath(),
                            NewsRepositoryConstants.COLLABORATION,
                            subNode.getPath(),
                            targetPath,
                            false
                    ).execute(installContext);

                    numberOfMovedNodes++;
                }
            }
        }

        // ALso check sub folders (if there are any)
        final List<Node> allFolderNodes = NodeUtil.asList(NodeUtil.getNodes(rootNode, NewsNodeTypes.Folder.NAME));

        for (Node folderNode : allFolderNodes) {
            numberOfMovedNodes += processNewsItems(installContext, folderNode, numberOfMovedNodes);
        }

        return numberOfMovedNodes;
    }

    private static String determineTargetPath(final Node rootNode, final Node subNode) throws RepositoryException {
        final int lastSlashIndex = subNode.getPath().lastIndexOf("/");

        if ("/".equals(rootNode.getPath())) {
            // Prevent double '/'
            return subNode.getPath().substring(lastSlashIndex);
        } else {
            return rootNode.getPath() + subNode.getPath().substring(lastSlashIndex);
        }
    }

}