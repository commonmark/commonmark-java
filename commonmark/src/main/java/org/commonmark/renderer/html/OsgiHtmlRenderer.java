package org.commonmark.renderer.html;

import org.commonmark.node.Node;
import org.commonmark.renderer.Renderer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = Renderer.class, property = {"format=html"}, immediate = false)
@Designate(ocd = OsgiHtmlRendererConfiguration.class)
public class OsgiHtmlRenderer implements Renderer {

    private HtmlRenderer renderer;

    @Activate
    public void activate(OsgiHtmlRendererConfiguration config) {
        renderer = HtmlRenderer.builder()
                .escapeHtml(config.escapeHtml())
                .percentEncodeUrls(config.percentEncodeUrls())
                .softbreak(config.softbreak())
                .build();
    }

    @Override
    public void render(Node node, Appendable output) {
        renderer.render(node, output);
    }

    @Override
    public String render(Node node) {
        return renderer.render(node);
    }

}
