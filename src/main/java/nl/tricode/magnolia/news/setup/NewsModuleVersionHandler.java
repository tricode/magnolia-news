/**
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

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;

import nl.tricode.magnolia.news.setup.task.ModuleDependencyBootstrapTask;
import nl.tricode.magnolia.news.setup.task.UpdateModuleBootstrapTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ImportUUIDBehavior;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to handle installation and updates of your module.
 */
public class NewsModuleVersionHandler extends DefaultModuleVersionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(NewsModuleVersionHandler.class);
	private static final String MODULE_NAME = "magnolia-news-module";

	public NewsModuleVersionHandler() {
		final Delta for_1_1_2 = DeltaBuilder.update("1.1.2", "Reload Content App.")
				  .addTask(new BootstrapSingleResource("Content app", "Removing expiry Date",
							 "/mgnl-bootstrap/magnolia-news-module/apps/config.modules.magnolia-news-module.apps.tricode-news.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));

		register(for_1_1_2);

		final Delta for_1_1_1 = DeltaBuilder.update("1.1.1", "Add a userrole news-editor")
				  .addTask(new BootstrapSingleResource("Userrole config", "Installing a userrole config in the content app",
							 "/mgnl-bootstrap/magnolia-news-module/apps/config.modules.magnolia-news-module.apps.tricode-news.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING))
				  .addTask(new BootstrapSingleResource("Userrole config", "Installing a userrole for the news module",
							 "/mgnl-bootstrap/magnolia-news-module/userroles/userroles.news-editor.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));
		register(for_1_1_1);
	}

	@Override
	protected List<Task> getStartupTasks(InstallContext ctx) {
		ModuleDefinition module = ctx.getCurrentModuleDefinition();

		List<Task> startupTasks = new ArrayList<>(0);
		startupTasks.addAll(super.getStartupTasks(ctx));

		if ("SNAPSHOT".equals(module.getVersion().getClassifier())) {
			// force updates for snapshots
			startupTasks.add(new RemoveNodeTask("Remove snapshot information", "", "config", "/modules/" + MODULE_NAME + "/apps"));
			startupTasks.add(new RemoveNodeTask("Remove snapshot information", "", "config", "/modules/" + MODULE_NAME + "/commands"));
			startupTasks.add(new RemoveNodeTask("Remove snapshot information", "", "config", "/modules/" + MODULE_NAME + "/dialogs"));
			startupTasks.add(new ModuleBootstrapTask());
		}
		startupTasks.addAll(getOptionalTasks(ctx));

		return startupTasks;
	}

	/**
	 * Method of installing optional Tasks
	 *
	 * @param ctx Install Context
	 * @return a list of optional tasks.
	 */
	private List<Task> getOptionalTasks(InstallContext ctx) {
		List<Task> tasks = new ArrayList<>(0);


		if (ctx.getHierarchyManager("config").isExist("/modules/tricode-tags")) {
			LOG.info("Bootstrapping optional Tricode Tags for Tricode News");
			tasks.add(new BootstrapSingleResource("Tricode news optional Tags", "Bootstrap the optional tab for Tags", "/mgnl-bootstrap/optional/tricode-tags/config.modules.magnolia-news-module.apps.tricode-news.subApps.detail.editor.form.tabs.tagstab.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING));
		}

		/** Remove in future if we are changing to Magnolia category module*/
		if (ctx.getHierarchyManager("config").isExist("/modules/tricode-categories")) {
			LOG.info("Bootstrapping optional Tricode Categories for Tricode News");
			tasks.add(new BootstrapSingleResource("Tricode news optional Categories", "Bootstrap the optional tab for Categories", "/mgnl-bootstrap/optional/tricode-categories/config.modules.magnolia-news-module.apps.tricode-news.subApps.detail.editor.form.tabs.categoriestab.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING));
		}
		return tasks;
	}

	@Override
	protected List<Task> getDefaultUpdateTasks(Version forVersion) {
		final List<Task> tasks = new ArrayList<>();
		tasks.addAll(super.getDefaultUpdateTasks(forVersion));

		// Always update templates, resources no matter what version is updated!
		tasks.add(new UpdateModuleBootstrapTask(MODULE_NAME, "dialogs"));

		return tasks;
	}

	@Override
	protected List<Task> getExtraInstallTasks(InstallContext ctx) {
		final List<Task> tasks = new ArrayList<>();
		tasks.addAll(super.getExtraInstallTasks(ctx));

		//Get Optional samples for bootstrapping.
		tasks.add(new ModuleDependencyBootstrapTask("/mgnl-bootstrap-samples/optional", "tricode-tags"));
		tasks.add(new ModuleDependencyBootstrapTask("/mgnl-bootstrap-samples/optional", "tricode-categories"));

		return tasks;
	}
}