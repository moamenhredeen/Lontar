module app.orgx.desktop {
    requires javafx.controls;
    requires jfx.incubator.richtext;
    requires java.desktop;
    requires org.apache.logging.log4j;
    requires org.commonmark;
    requires org.apache.lucene.core;
    requires org.apache.lucene.queryparser;
    requires org.apache.lucene.highlighter;

    opens app.orgx.desktop to javafx.graphics;

    exports app.orgx.desktop;
    exports app.orgx.desktop.core;
    exports app.orgx.desktop.core.events;
    exports app.orgx.desktop.model;
    exports app.orgx.desktop.vault;
    exports app.orgx.desktop.markdown;
    exports app.orgx.desktop.editor;
    exports app.orgx.desktop.search;
    exports app.orgx.desktop.config;
    exports app.orgx.desktop.ui;
    exports app.orgx.desktop.ui.palette;
}
