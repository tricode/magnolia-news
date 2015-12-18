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
package nl.tricode.magnolia.news.templates;

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.templating.functions.TemplatingFunctions;
import nl.tricode.magnolia.news.NewsNodeTypes;

import nl.tricode.magnolia.news.util.JcrUtils;
import nl.tricode.magnolia.news.util.NewsWorkspaceUtil;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import java.util.*;

/**
 * Created by mvdmark on 1-7-2014.
 */
public class NewsRenderableDefinition<RD extends RenderableDefinition> extends RenderingModelImpl {
	private static final Logger log = LoggerFactory.getLogger(NewsRenderableDefinition.class);

	private static final int DEFAULT_LATEST_COUNT = 5;
	private static final String NEWS_NODE_TYPE = "mgnl:news";
	private static final String PARAM_CATEGORY = "category";
	private static final String PARAM_TAG = "tag";
	private static final String PARAM_PAGE = "page";

	private final WebContext webContext = MgnlContext.getWebContext();

	private static final List<String> WHITELISTED_PARAMETERS = Arrays.asList(PARAM_PAGE, PARAM_TAG, PARAM_CATEGORY);

	private final Map<String, String> filter;

	protected final TemplatingFunctions templatingFunctions;

	@Override
	public String execute() {
		webContext.getResponse().setHeader("Cache-Control", "no-cache");
		return super.execute();
	}

	@Inject
	public NewsRenderableDefinition(Node content, RD definition, RenderingModel<?> parent, TemplatingFunctions templatingFunctions) {
		super(content, definition, parent);
		this.templatingFunctions = templatingFunctions;

		filter = new HashMap<String, String>();
		final Iterator<Map.Entry<String, String>> it = MgnlContext.getWebContext().getParameters().entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, String> pairs = it.next();
			if (WHITELISTED_PARAMETERS.contains(pairs.getKey()) && StringUtils.isNotEmpty(pairs.getValue())) {
				filter.put(pairs.getKey(), pairs.getValue());
				log.debug("Added to filter: " + pairs.getKey());
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	public TemplatingFunctions getTemplatingFunctions() {
		return templatingFunctions;
	}

	/**
	 * Get all available nodes of type mgnl:news.
	 *
	 * @param path          Start node path in hierarchy
	 * @param maxResultSize Number of items to return. When empty <code>Integer.MAX_VALUE</code> will be used.
	 * @return List of news nodes sorted by date created in descending order
	 * @throws RepositoryException
	 */
	public List<ContentMap> getNews(String path, String maxResultSize) throws RepositoryException {
		int resultSize = Integer.MAX_VALUE;
		if (StringUtils.isNumeric(maxResultSize)) {
			resultSize = Integer.parseInt(maxResultSize);
		}
		String customFilters = getTagPredicate(filter) + getCategoryPredicate(filter);
		final String sqlNewsItems = JcrUtils.buildQuery(path, NEWS_NODE_TYPE, true, customFilters);
		return templatingFunctions.asContentMapList(JcrUtils.getWrappedNodesFromQuery(sqlNewsItems, resultSize, getPageNumber(), NewsNodeTypes.News.NAME));
	}

	/**
	 * Get latest nodes of type mgnl:news.
	 *
	 * @param path          Start node path in hierarchy
	 * @param maxResultSize Number of items to return. When empty <code>5</code> will be used.
	 * @return List of news nodes sorted by date created in descending order
	 * @throws RepositoryException
	 */
	public List<ContentMap> getLatestNews(String path, String maxResultSize) throws RepositoryException {
		return getLatest(path, maxResultSize, NEWS_NODE_TYPE, getPageNumber(), NewsNodeTypes.News.NAME);
	}

	public List<ContentMap> getLatest(String path, String maxResultSize, String nodeType, int pageNumber, String nodeTypeName) throws RepositoryException {
		int resultSize = DEFAULT_LATEST_COUNT;
		if (StringUtils.isNumeric(maxResultSize)) {
			resultSize = Integer.parseInt(maxResultSize);
		}
		final String sqlBlogItems = JcrUtils.buildQuery(path, nodeType);
		return templatingFunctions.asContentMapList(JcrUtils.getWrappedNodesFromQuery(sqlBlogItems, resultSize, pageNumber, nodeTypeName));
	}

	/**
	 * Get total number of news for current state.
	 * (Performs additional JCR-SQL2 query to obtain count!)
	 *
	 * @param path Start node path in hierarchy
	 * @return long Number of news
	 */
	public int getNewsCount(String path) throws RepositoryException {
		final String sqlNewsItems = JcrUtils.buildQuery(path, NEWS_NODE_TYPE);
		return IteratorUtils.toList(QueryUtil.search(NewsWorkspaceUtil.COLLABORATION, sqlNewsItems, Query.JCR_SQL2, NewsNodeTypes.News.NAME)).size();
	}

	/**
	 * Determine if older news posts exists
	 *
	 * @param path
	 * @param maxResultSize
	 * @return Boolean true when older news exists
	 */
	public boolean hasOlderNews(String path, int maxResultSize) throws RepositoryException {
		final long totalNews = getNewsCount(path);
		final int pageNumber = getPageNumber();

		return hasOlderPosts(path, maxResultSize, totalNews, pageNumber);
	}

	/**
	 * Determine if older blog posts exists
	 *
	 * @param path
	 * @param maxResultSize
	 * @return Boolean true when older blog posts exists
	 */
	public boolean hasOlderPosts(String path, int maxResultSize, long totalBlogs, int pageNumber) throws RepositoryException {
		final int maxPage = (int) Math.ceil((double) totalBlogs / (double) maxResultSize);
		return maxPage >= pageNumber + 1;
	}

	/**
	 * Determine the next following page number containing older news
	 *
	 * @param path
	 * @param maxResultSize
	 * @return page number with older news
	 */
	public int pageOlderPosts(String path, int maxResultSize) throws RepositoryException {
		return (hasOlderNews(path, maxResultSize)) ? getPageNumber() + 1 : getPageNumber();
	}

	/**
	 * Determine if newer news exists
	 *
	 * @return Boolean true when newer news exists
	 */
	public Boolean hasNewerPosts() {
		return getPageNumber() > 1;
	}

	/**
	 * Determine the previous following page number containing newer news
	 *
	 * @return page number with newer news
	 */
	public int pageNewerPosts() {
		return (hasNewerPosts()) ? getPageNumber() - 1 : getPageNumber();
	}

	private int getPageNumber() {
		int pageNumber = 1;
		if (filter.containsKey(PARAM_PAGE)) {
			pageNumber = Integer.parseInt(filter.get(PARAM_PAGE));
		}
		return pageNumber;
	}

	/**
	 * Get categories for given news node
	 *
	 * @param news
	 * @return List of category nodes
	 */
	@Deprecated
	public List<ContentMap> getNewsCategories(ContentMap news) {
		return getItems(news.getJCRNode(), NewsNodeTypes.News.PROPERTY_CATEGORIES, NewsWorkspaceUtil.CATEGORIES);
	}

	/**
	 * Get tags for given news node
	 *
	 * @param news
	 * @return List of tag nodes
	 */
	public List<ContentMap> getNewsTags(ContentMap news) {
		return getItems(news.getJCRNode(), NewsNodeTypes.News.PROPERTY_TAGS, NewsWorkspaceUtil.COLLABORATION);
	}

	protected String getTagPredicate(Map<String, String> filter) {
		if (filter.containsKey(PARAM_TAG)) {
			final ContentMap contentMap = templatingFunctions.contentByPath(filter.get(PARAM_TAG), NewsWorkspaceUtil.COLLABORATION);
			if (contentMap != null) {
				final String tagId = (String) contentMap.get("@id");
				if (StringUtils.isNotEmpty(tagId)) {
					return "AND p.tags like '%" + tagId + "%' ";
				}
			} else {
				log.debug("Tag [{}] not found", filter.get(PARAM_TAG));
			}
		}
		return StringUtils.EMPTY;
	}

	@Deprecated
	protected String getCategoryPredicate(Map<String, String> filter) {
		if (filter.containsKey(PARAM_CATEGORY)) {
			final ContentMap contentMap = templatingFunctions.contentByPath(filter.get(PARAM_CATEGORY), NewsWorkspaceUtil.CATEGORIES);
			if (contentMap != null) {
				final String categoryId = (String) contentMap.get("@id");
				if (StringUtils.isNotEmpty(categoryId)) {
					return "AND p.categories like '%" + categoryId + "%' ";
				}
			} else {
				log.debug("Category [{}] not found", filter.get(PARAM_CATEGORY));
			}
		}
		return StringUtils.EMPTY;
	}

	public List<ContentMap> getItems(Node item, String nodeType, String workspace) {
		final List<ContentMap> items = new ArrayList<ContentMap>(0);

		try {
			final Value[] values = item.getProperty(nodeType).getValues();
			if (values != null) {
				for (Value value : values) {
					items.add(templatingFunctions.contentById(value.getString(), workspace));
				}
			}
		} catch (RepositoryException e) {
			log.error("Exception while getting items", e.getMessage());
		}
		return items;
	}
}