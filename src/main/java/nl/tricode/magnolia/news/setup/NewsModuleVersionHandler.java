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

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;

import javax.jcr.ImportUUIDBehavior;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to handle installation and updates of your module.
 */
public class NewsModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final String MODULE_NAME = "magnolia-news-module";

    /**
     * Constructor.
     * <p/>
     * Here you can register deltas for tasks that need to be run when UPDATING an EXISTING module.
     */
    public NewsModuleVersionHandler() {
        register(DeltaBuilder.update("1.1.1", "Add a userrole news-editor")
                .addTask(new BootstrapSingleResource("Userrole config", "Installing a userrole for the news module",
                        "/mgnl-bootstrap/magnolia-news-module/userroles/userroles.news-editor.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING)));

        register(DeltaBuilder.update("1.1.4", "Upgrading news module to Magnolia 5.5")
                .addTask(new RemoveNodeTask("Remove old nodes", "/modules/" + MODULE_NAME + "/apps"))
                .addTask(new RemoveNodeTask("Remove old nodes", "/modules/" + MODULE_NAME + "/dialogs"))
        );
    }

    /**
     * Override this method when defining tasks that need to be executed when INITIALLY INSTALLING the module.
     *
     * @param installContext Context of the install, can be used to display messages
     * @return A list of tasks to execute on initial install
     */
    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        final List<Task> tasks = new ArrayList<>();
        tasks.addAll(super.getExtraInstallTasks(installContext));

        return tasks;
    }

    @Override
    protected List<Task> getStartupTasks(InstallContext ctx) {
        final List<Task> startupTasks = new ArrayList<>(0);
        startupTasks.addAll(super.getStartupTasks(ctx));

        if ("SNAPSHOT".equals(ctx.getCurrentModuleDefinition().getVersion().getClassifier())) {
            // force updates for snapshots
            startupTasks.add(new RemoveNodeTask("Remove snapshot information", "", "config", "/modules/" + MODULE_NAME + "/commands"));
            startupTasks.add(new ModuleBootstrapTask());
        }

        return startupTasks;
    }

}