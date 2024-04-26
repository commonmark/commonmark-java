package org.commonmark.integration;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;

import java.util.List;

public class Extensions {

    static final List<Extension> ALL_EXTENSIONS = List.of(
            AutolinkExtension.create(),
            ImageAttributesExtension.create(),
            InsExtension.create(),
            StrikethroughExtension.create(),
            TablesExtension.create(),
            TaskListItemsExtension.create(),
            YamlFrontMatterExtension.create());
}
