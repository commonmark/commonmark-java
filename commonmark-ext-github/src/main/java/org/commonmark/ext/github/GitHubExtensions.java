package org.commonmark.ext.github;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.gfm.alerts.AlertsExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;

/** A preset for tables, strikethrough, autolinks, task lists, alerts and footnotes. */
public final class GitHubExtensions {
    private GitHubExtensions() {}

    /**
     * Creates the extensions with their default settings. Use the same list when building the
     * parser and renderer.
     *
     * @return an unmodifiable list of extensions
     */
    public static List<Extension> create() {
        return Collections.unmodifiableList(
                Arrays.asList(
                        AutolinkExtension.create(),
                        FootnotesExtension.create(),
                        AlertsExtension.create(),
                        StrikethroughExtension.create(),
                        TablesExtension.create(),
                        TaskListItemsExtension.create()));
    }
}
