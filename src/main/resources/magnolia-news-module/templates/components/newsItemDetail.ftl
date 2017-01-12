[#if (ctx.parameters.name?has_content)]
    [#assign newsItem = cmsfn.contentByPath("/"+ctx.parameters.name, "collaboration") /]

    <article class="news-item">
        <h2>${newsItem.title!"No title found"}</h2>

        <section>
            ${newsItem.text!""}
        </section>

        <div class="postdetails">
            <ul class="list-unstyled inline-list news-info">
                <li><i class="fa fa-calendar">${cmsfn.metaData(newsItem,"mgnl:created")?date("yyyy-MM-dd")}</i></li>
            </ul>
        </div>
    </article>
[#else]
    [#if (cmsfn.isEditMode() && !cmsfn.isPreviewMode() && !ctx.parameters.name?has_content)]
        <div class="alert-base alert edit-mode">
            This is a placeholder for the news detail rendering component which will be shown only in edit/preview mode!
            <a href="" class="close">&times;</a>
        </div>
    [/#if]
[/#if]