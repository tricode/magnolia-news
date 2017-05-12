# magnolia-news
News app for [Magnolia cms](http://www.magnolia-cms.com).

In this module we provide a content app to create/edit/delete news items. This app enables you to publish your hottest news items right away.

Create your own website components with nl.tricode.magnolia.news.templates.NewsRenderableDefinition
to add to your Magnolia website.

## Prerequisites
* [git](http://git-scm.com/)
* [java 7](http://java.com)
* [Maven 3](http://maven.apache.org)

##License
Copyright (c) 2015 Tricode and contributors. Released under a [GNUv3 license](https://github.com/tricode/magnolia-news/blob/master/license.txt).


##Release notes 1.1.1.
* Added userrole news-editor.
* Added js config file for ckeditor.
* Bugfix in configuration for add News action.
* Bugfix optional tabs still in default bootstrap.
* Changed versioning to three digit.

##Release notes 1.1.2.
* Fixed opening dialog for rename folder.
* Removed duplicate field 'expiry date'.
* Fixed typo in app configuration.
* Added correct query for nodes that needs to deactivated.
* Add IoC for syndicator in scheduled job 'Deactivate published news'
* Update to Java 7.
* Update to Magnolia 5.4.3.
* Update Magnolia scheduler module 2.2.2.
* Separation of Jcr queries.

##Release notes 1.1.3
* Upgrade to Magnolia 5.4.4

##Release notes 1.1.4
* Upgrade to Magnolia 5.5
* Update to Java 8

##Release notes 1.1.5
* Fixed incorrect JavaDoc that caused the release to fail

##Release notes 1.1.6
* Prevent nesting of news items within news items (+ un-nest any incorrect encountered)

##Release notes 1.1.7
* Update to Magnolia 5.5.3 + Nexus URL update
