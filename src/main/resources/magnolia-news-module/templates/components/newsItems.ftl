[#if (!ctx.parameters.name?has_content)]
    [#if (content.newsGroup?has_content) ]
        [#assign newsGroupPath = cmsfn.contentById(content.newsGroup, "collaboration").@path /]
    [/#if]

    [#assign newsItems = model.getNews(newsGroupPath!"/", content.maxResultsPerPage!"5") /]

    [#if (newsItems)?size > 0 ]
        [#assign pageLink = cmsfn.link(cmsfn.page(content)) /]

        [#if (content.detailsPage?has_content) ]
            [#assign newsItemDetailPageLink = cmsfn.link(cmsfn.contentByPath(content.detailsPage)) /]
        [/#if]

        <div class="news-summaries">
            [#list newsItems as newsItem]
                <article class="news-item">
                    [#if (newsItemDetailPageLink?has_content)]
                        <h2><a href="${newsItemDetailPageLink}?name=${newsItem.@name}">${newsItem.title!"No title found"}</a></h2>
                    [#else]
                        <h2>${newsItem.title!"No title found"}</h2>
                    [/#if]

                    [#assign categories = model.getNewsCategories(newsItem) /]
                    [#if (categories)?size > 0 ]
                        <div class="postdetails">
                            <ul class="list-unstyled inline-list news-categories">
                                [#list categories as category]
                                    <li>
                                        <a href="${pageLink}?category=${category.@path}">
                                            <i class="fa fa-star">${category.@name}</i>
                                        </a>
                                    </li>
                                [/#list]
                            </ul>
                        </div>
                    [/#if]

                    <section>
                    ${newsItem.summary!}
                        [#if (newsItemDetailPageLink?has_content)]
                            <a class="read-more button tiny" href="${newsItemDetailPageLink}?name=${newsItem.@name}">
                                Read more...
                            </a>
                        [/#if]
                    </section>

                    <div class="postdetails">
                        <ul class="list-unstyled inline-list news-info">
                            <li><i class="fa fa-calendar">${cmsfn.metaData(newsItem,"mgnl:created")?date("yyyy-MM-dd")}</i></li>
                        </ul>
                    </div>
                </article>
            [/#list]

            [@renderPagination /]
        </div>
    [#else]
        <div class="news-summaries">
            <p>No news entries available</p>
        </div>
    [/#if]
[/#if]

[#macro renderPagination]
    [#assign olderPages = model.pageOlderPosts(newsGroupPath!"/",(content.maxResultsPerPage!"5")?number) /]

    [#if (olderPages > 1)]
    <ul class="pager">
        [#assign hasOlderPages = model.hasOlderPosts(newsGroupPath!"/",(content.maxResultsPerPage!"5")?number) /]
        [#if (hasOlderPages)]
            <a class="button left small" href="${pageLink}?page=${olderPages}">
                <i class="fa fa-chevron-left">Older</i>
            </a>
        [/#if]

        [#assign hasNewerPages =  model.hasNewerPosts() /]
        [#if (hasNewerPages)]
            [#assign newerPages =  model.pageNewerPosts() /]

            <a class="button right small" href="${pageLink}?page=${newerPages}">
                <i class="fa fa-chevron-right">Newer</i>
            </a>
        [/#if]
    </ul>
    [/#if]
[/#macro]