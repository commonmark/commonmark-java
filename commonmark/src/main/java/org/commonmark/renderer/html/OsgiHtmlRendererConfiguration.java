package org.commonmark.renderer.html;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "HtmlRenderer configuration", description = "HtmlRenderer configuration")
public @interface OsgiHtmlRendererConfiguration {

    boolean escapeHtml() default false;

    boolean percentEncodeUrls() default false;

    String softbreak() default "\n";
}
