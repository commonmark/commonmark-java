module org.commonmark.ext.github {
    exports org.commonmark.ext.github;

    requires transitive org.commonmark;
    requires org.commonmark.ext.autolink;
    requires org.commonmark.ext.footnotes;
    requires org.commonmark.ext.gfm.alerts;
    requires org.commonmark.ext.gfm.strikethrough;
    requires org.commonmark.ext.gfm.tables;
    requires org.commonmark.ext.task.list.items;
}
