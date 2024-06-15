package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.*;

/**
 * HTML rendering for footnotes.
 * <p>
 * Aims to match the rendering of cmark-gfm (which is slightly different from GitHub's when it comes to class
 * attributes, not sure why).
 * <p>
 * Some notes on how rendering works:
 * <ul>
 * <li>Footnotes are numbered according to the order of references, starting at 1</li>
 * <li>Definitions are rendered at the end of the document, regardless of where the definition was in the source</li>
 * <li>Definitions are ordered by number</li>
 * <li>Definitions have links back to their references (one or more)</li>
 * </ul>
 */
public class FootnoteHtmlNodeRenderer implements NodeRenderer {

    private final HtmlWriter html;
    private final HtmlNodeRendererContext context;

    /**
     * All definitions (even potentially unused ones), for looking up references
     */
    private DefinitionMap<FootnoteDefinition> definitionMap;

    /**
     * Definitions that were referenced, in order in which they should be rendered.
     */
    private final Map<FootnoteDefinition, ReferencedDefinition> referencedDefinitions = new LinkedHashMap<>();

    public FootnoteHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.html = context.getWriter();
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(FootnoteReference.class, FootnoteDefinition.class);
    }

    @Override
    public void beforeRoot(Node node) {
        // Collect definitions so we can look them up when encountering a reference later
        var visitor = new FootnotesVisitor();
        node.accept(visitor);
        definitionMap = visitor.definitions;
    }

    @Override
    public void render(Node node) {
        if (node instanceof FootnoteReference) {
            renderReference((FootnoteReference) node);
        }
    }

    private void renderReference(FootnoteReference ref) {
        var def = definitionMap.get(ref.getLabel());
        if (def == null) {
            // A reference without a corresponding definition is rendered as plain text
            html.text("[^" + ref.getLabel() + "]");
            return;
        }

        // The first referenced definition gets number 1, second one 2, etc.
        var referencedDef = referencedDefinitions.computeIfAbsent(def, k -> new ReferencedDefinition(referencedDefinitions.size() + 1));
        var definitionNumber = referencedDef.definitionNumber;
        // The reference number for that particular definition. E.g. if there's two references for the same definition,
        // the first one is 1, the second one 2, etc. This is needed to give each reference a unique ID so that each
        // reference can get its own backlink from the definition.
        var refNumber = referencedDef.references.size() + 1;
        var id = referenceId(def.getLabel(), refNumber);
        referencedDef.references.add(id);

        html.tag("sup", context.extendAttributes(ref, "sup", Map.of("class", "footnote-ref")));

        var href = "#" + definitionId(def.getLabel());
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("href", href);
        attrs.put("id", id);
        attrs.put("data-footnote-ref", null);
        html.tag("a", context.extendAttributes(ref, "a", attrs));
        html.raw(String.valueOf(definitionNumber));
        html.tag("/a");
        html.tag("/sup");
    }

    @Override
    public void afterRoot(Node node) {
        // Now render the referenced definitions if there are any
        if (referencedDefinitions.isEmpty()) {
            return;
        }

        var firstDef = referencedDefinitions.keySet().iterator().next();
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("class", "footnotes");
        attrs.put("data-footnotes", null);
        html.tag("section", context.extendAttributes(firstDef, "section", attrs));
        html.line();
        html.tag("ol");
        html.line();
        for (var entry : referencedDefinitions.entrySet()) {
            renderDefinition(entry.getKey(), entry.getValue());
        }
        html.tag("/ol");
        html.line();
        html.tag("/section");
        html.line();
    }

    private void renderDefinition(FootnoteDefinition def, ReferencedDefinition referencedDefinition) {
        // <ol> etc
        var id = definitionId(def.getLabel());
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("id", id);
        html.tag("li", context.extendAttributes(def, "li", attrs));
        html.line();

        if (def.getLastChild() instanceof Paragraph) {
            // Add backlinks into last paragraph before "p". This is what GFM does.
            var lastParagraph = (Paragraph) def.getLastChild();
            renderChildren(def, lastParagraph);

            html.line();
            // This is a tiny bit strange, we're rendering the <p> ourselves here instead of delegating the rendering.
            // What if the rendering was overwritten to not use <p>, or do something else entirely?
            // TODO: I think it would be better if we rendered *all* paragraphs ourselves in this case, for consistency.
            html.tag("p", context.extendAttributes(lastParagraph, "p", Map.of()));
            renderChildren(lastParagraph, null);
            html.raw(" ");
            renderBackrefs(def, referencedDefinition);
            html.tag("/p");
            html.line();
        } else {
            renderChildren(def, null);
            html.line();
            renderBackrefs(def, referencedDefinition);
        }

        html.tag("/li");
        html.line();
    }

    private void renderBackrefs(FootnoteDefinition def, ReferencedDefinition referencedDefinition) {
        var refs = referencedDefinition.references;
        for (int i = 0; i < refs.size(); i++) {
            var ref = refs.get(i);
            var refNumber = i + 1;
            var idx = referencedDefinition.definitionNumber + (refNumber > 1 ? ("-" + refNumber) : "");

            var attrs = new LinkedHashMap<String, String>();
            attrs.put("href", "#" + ref);
            attrs.put("class", "footnote-backref");
            attrs.put("data-footnote-backref", null);
            attrs.put("data-footnote-backref-idx", idx);
            attrs.put("aria-label", "Back to reference " + idx);
            html.tag("a", context.extendAttributes(def, "a", attrs));
            if (refNumber > 1) {
                html.tag("sup", context.extendAttributes(def, "sup", Map.of("class", "footnote-ref")));
                html.raw(String.valueOf(refNumber));
                html.tag("/sup");
            }
            // U+21A9 LEFTWARDS ARROW WITH HOOK
            html.raw("↩");
            html.tag("/a");
            if (i + 1 < refs.size()) {
                html.raw(" ");
            }
        }
    }

    private String referenceId(String label, int number) {
        return "fnref-" + label + (number == 1 ? "" : ("-" + number));
    }

    private String definitionId(String label) {
        return "fn-" + label;
    }

    private void renderChildren(Node parent, Node until) {
        Node node = parent.getFirstChild();
        while (node != until) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private static class FootnotesVisitor extends AbstractVisitor {

        private final DefinitionMap<FootnoteDefinition> definitions = new DefinitionMap<>(FootnoteDefinition.class);

        @Override
        public void visit(CustomBlock customBlock) {
            if (customBlock instanceof FootnoteDefinition) {
                var def = (FootnoteDefinition) customBlock;
                definitions.putIfAbsent(def.getLabel(), def);
            }
        }
    }

    private static class ReferencedDefinition {
        /**
         * The definition number, starting from 1, and in order in which they're referenced.
         */
        final int definitionNumber;
        /**
         * The IDs of references for this definition, for backrefs.
         */
        final List<String> references = new ArrayList<>();

        ReferencedDefinition(int definitionNumber) {
            this.definitionNumber = definitionNumber;
        }
    }
}
