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
package nl.tricode.magnolia.news.templates;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.templating.functions.TemplatingFunctions;
import nl.tricode.magnolia.news.NewsNodeTypes;
import nl.tricode.magnolia.news.util.JcrUtils;
import nl.tricode.magnolia.news.util.NewsRepositoryConstants;
import nl.tricode.magnolia.news.util.NewsWorkspaceUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused") //Used in freemarker components.
public class NewsSearchRenderableDefinition<RD extends RenderableDefinition> extends RenderingModelImpl<RD> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NewsSearchRenderableDefinition.class);

	private static final String SEARCH_TERM = "s";
	private static final String PAGENUMBER = "p";
	private static final String SEARCH_PROXIMITY = "~0.6";
	private static final String NEWS_NODETYPE = "mgnl:news";

	private Multimap<String, String> filter;

	private String nodetype;
	private String workspace;
	private int count;
	private int numPages;

	private final TemplatingFunctions templatingFunctions;
	private final WebContext webContext = MgnlContext.getWebContext();

	private List<ContentMap> searchResults = new ArrayList<>();

	@Inject
	public NewsSearchRenderableDefinition(Node content, RD definition, RenderingModel<?> parent, TemplatingFunctions templatingFunctions) {
		super(content, definition, parent);
		this.templatingFunctions = templatingFunctions;
		setWorkspace(NewsRepositoryConstants.COLLABORATION);
		setNodetype(NewsNodeTypes.News.NAME);

		filter = LinkedListMultimap.create();
		Set<String> parameters = webContext.getParameters().keySet();
		for (String parameterKey : parameters) {
			if (allowedParameters().contains(parameterKey)) {
				String[] parameterValues = webContext.getParameterValues(parameterKey);
				for (String parameterValue : parameterValues) {
					if (StringUtils.isNotEmpty(parameterValue)) {
						filter.get(parameterKey).add(parameterValue);
					}
				}
			}
			webContext.remove(parameterKey);
		}
		LOGGER.debug("Running constructor NewsSearchRenderableDefinition");
	}

	@Override
	public String execute() {
		String filters = getPredicate();
		String queryString = JcrUtils.buildQuery(getSearchPath(), NEWS_NODETYPE, true, filters, true);
		LOGGER.debug("NewsSearchRenderableDefinition Query executed: {}", queryString);

		// Do not cache this response!
		// More info: http://documentation.magnolia-cms.com/display/DOCS/Cache+module#Cachemodule-Cacheheadernegotiation
		webContext.getResponse().setHeader("Cache-Control", "no-cache");

		if (StringUtils.isBlank(queryString)) {
			return null;
		}

		try {
			executePagedNodesQuery(queryString, getMaxResultsPerPage(), getPageNumber(), getWorkspace(), getNodetype());
		} catch (Exception e) {
			LOGGER.error("{} caught while parsing query for search term [{}] : {}", e.getClass().getName(), queryString, e.getMessage());
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Fetching paged node items.
	 *
	 * @param statement     SQL2 statement
	 * @param maxResultSize Max results returned
	 * @param pageNumber    paging number
     * @param workspace     Repository workspace
     * @param nodeType      Node type
	 * @throws javax.jcr.RepositoryException Repostory exception.
	 */
	protected void executePagedNodesQuery(String statement, int maxResultSize, int pageNumber, String workspace, String nodeType) throws RepositoryException {
		List<Node> nodeList = new ArrayList<>(0);
		List<Node> nodeListPaged = new ArrayList<>(0);
		NodeIterator items = QueryUtil.search(workspace, statement, Query.JCR_SQL2, nodeType);
		while (items.hasNext()) {
			nodeList.add(new I18nNodeWrapper(items.nextNode()));
		}
		int total = nodeList.size();

		// Paging result set
		int startRow = (maxResultSize * (pageNumber - 1));
		int newLimit = maxResultSize;
		if (total > startRow) {
			if (total < startRow + maxResultSize) {
				newLimit = total - startRow;
			}
			nodeListPaged = nodeList.subList(startRow, startRow + newLimit);
		}

		int calcNumPages = total / maxResultSize;
		if ((total % maxResultSize) > 0) {
			calcNumPages++;
		}
		// Set template model properties
		setCount(total);
		setNumPages(calcNumPages);
		setSearchResults(templatingFunctions.asContentMapList(nodeListPaged));
	}

	protected Set<String> allowedParameters() {
		return Sets.newHashSet(SEARCH_TERM, "r", PAGENUMBER);
	}

	public String getPredicate() {
		String searchTermPredicate = StringUtils.EMPTY;
		if (filter.containsKey(SEARCH_TERM)) {
			String searchString = filter.get(SEARCH_TERM).iterator().next().replaceAll("'", "''");
			searchString = searchString + SEARCH_PROXIMITY;
			searchTermPredicate = MessageFormat.format("AND contains(p.*, ''{0}'') ", searchString);
		}
		return searchTermPredicate;
	}

	/**
	 * Get searchPath content property. If not set then "/" (root) is used.
	 *
	 * @return search path
	 */
	protected String getSearchPath() {
		String searchPath = "/";
		try {
			if (content.hasProperty("searchPath")) {
				searchPath = templatingFunctions.nodeById(content.getProperty("searchPath").getString()).getPath();
			}
		} catch (Exception e) {
			LOGGER.info("no searchPath property set on content", e);
		}
		return searchPath;
	}

	/**
	 * Get request parameter for current page.
	 *
	 * @return pagenumber
	 */
	public int getPageNumber() {
		int pageNumber = 1;
		if (filter.containsKey(PAGENUMBER)) {
			pageNumber = Integer.parseInt(filter.get(PAGENUMBER).iterator().next());
		}
		return pageNumber;
	}

	/**
	 * Get Max. results per page content property. If not set then Integer.MAX_VALUE is used.
	 *
	 * @return maximum results per page number
	 */
	protected int getMaxResultsPerPage() {
		int maxResultsPerPage = Integer.MAX_VALUE;
		try {
			if (content.hasProperty("maxResultsPerPage")) {
				maxResultsPerPage = Integer.parseInt(content.getProperty("maxResultsPerPage").getString());
			}
		} catch (Exception e) {
			LOGGER.info("no maxResultsPerPage property set on content", e);
		}
		return maxResultsPerPage;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getNodetype() {
		return nodetype;
	}

	public void setNodetype(String nodetype) {
		this.nodetype = nodetype;
	}

	public List<ContentMap> getSearchResults() {
		LOGGER.debug("get Search Results  size[{}]", searchResults.size());
		return searchResults;
	}

	public void setSearchResults(List<ContentMap> searchResults) {
		this.searchResults = searchResults;
	}

	@SuppressWarnings("unused") //Used in freemarker components.
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@SuppressWarnings("unused") //Used in freemarker components.
	public int getNumPages() {
		return numPages;
	}

	public void setNumPages(int numPages) {
		this.numPages = numPages;
	}
}