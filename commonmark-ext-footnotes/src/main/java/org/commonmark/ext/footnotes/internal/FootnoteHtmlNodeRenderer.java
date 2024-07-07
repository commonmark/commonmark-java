package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.*;
import java.util.function.Consumer;

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
 *
 * <h4>Nested footnotes</h4>
 * Text in footnote definitions can reference other footnotes, even ones that aren't referenced in the main text.
 * This makes them tricky because it's not enough to just go through the main text for references.
 * And before we can render a definition, we need to know all references (because we add links back to references).
 * <p>
 * In other words, footnotes form a directed graph. Footnotes can reference each other so cycles are possible too.
 * <p>
 * One way to implement it, which is what cmark-gfm does, is to go through the whole document (including definitions)
 * and find all references in order. That guarantees that all definitions are found, but it has strange results for
 * ordering or when the reference is in an unreferenced definition, see tests. In graph terms, it renders all
 * definitions that have an incoming edge, no matter whether they are connected to the main text or not.
 * <p>
 * The way we implement it:
 * <ol>
 * <li>Start with the references in the main text; we can render them as we go</li>
 * <li>After the main text is rendered, we have the referenced definitions, but there might be more from definition text</li>
 * <li>To find the remaining definitions, we visit the definitions from before to look at references</li>
 * <li>Repeat (breadth-first search) until we've found all definitions (note that we can't render before that's done because of backrefs)</li>
 * <li>Now render the definitions (and any references inside)</li>
 * </ol>
 * This means we only render definitions whose references are actually rendered, and in a meaningful order (all main
 * text footnotes first, then any nested ones).
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

    /**
     * Information about references that should be rendered as footnotes. This doesn't contain all references, just the
     * ones from inside definitions.
     */
    private final Map<FootnoteReference, ReferenceInfo> references = new HashMap<>();

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
        // Collect all definitions first, so we can look them up when encountering a reference later.
        var visitor = new DefinitionVisitor();
        node.accept(visitor);
        definitionMap = visitor.definitions;
    }

    @Override
    public void render(Node node) {
        if (node instanceof FootnoteReference) {
            // This is called for all references, even ones inside definitions that we render at the end.
            var ref = (FootnoteReference) node;
            // Use containsKey because if value is null, we don't need to try registering again.
            var info = references.containsKey(ref) ? references.get(ref) : registerReference(ref);
            renderReference(ref, info);
        }
    }

    @Override
    public void afterRoot(Node node) {
        // Now render the referenced definitions if there are any.
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

        // Check whether there are any footnotes inside the definitions that we're about to render. For those, we might
        // need to render more definitions. So do a breadth-first search to find all relevant definition.
        var check = new LinkedList<>(referencedDefinitions.keySet());
        while (!check.isEmpty()) {
            var def = check.removeFirst();
            def.accept(new ReferenceVisitor(ref -> {
                var d = definitionMap.get(ref.getLabel());
                if (d != null) {
                    if (!referencedDefinitions.containsKey(d)) {
                        check.addLast(d);
                    }
                    references.put(ref, registerReference(ref));
                }
            }));
        }

        for (var entry : referencedDefinitions.entrySet()) {
            // This will also render any footnote references inside definitions
            renderDefinition(entry.getKey(), entry.getValue());
        }

        html.tag("/ol");
        html.line();
        html.tag("/section");
        html.line();
    }

    private ReferenceInfo registerReference(FootnoteReference ref) {
        var def = definitionMap.get(ref.getLabel());
        if (def == null) {
            return null;
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

        var definitionId = definitionId(def.getLabel());

        return new ReferenceInfo(id, definitionId, definitionNumber);
    }

    private void renderReference(FootnoteReference ref, ReferenceInfo referenceInfo) {
        if (referenceInfo == null) {
            // A reference without a corresponding definition is rendered as plain text
            html.text("[^" + ref.getLabel() + "]");
            return;
        }

        html.tag("sup", context.extendAttributes(ref, "sup", Map.of("class", "footnote-ref")));

        var href = "#" + referenceInfo.definitionId;
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("href", href);
        attrs.put("id", referenceInfo.id);
        attrs.put("data-footnote-ref", null);
        html.tag("a", context.extendAttributes(ref, "a", attrs));
        html.raw(String.valueOf(referenceInfo.definitionNumber));
        html.tag("/a");
        html.tag("/sup");
    }

    private void renderDefinition(FootnoteDefinition def, ReferencedDefinition referencedDefinition) {
        // <ol> etc
        var id = definitionId(def.getLabel());
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("id", id);
        html.tag("li", context.extendAttributes(def, "li", attrs));
        html.line();

        if (def.getLastChild() instanceof Paragraph) {
            // Add backlinks into last paragraph before </p>. This is what GFM does.
            var lastParagraph = (Paragraph) def.getLastChild();
            var node = def.getFirstChild();
            while (node != lastParagraph) {
                if (node instanceof Paragraph) {
                    // Because we're manually rendering the <p> for the last paragraph, do the same for all other
                    // paragraphs for consistency (Paragraph rendering might be overwritten by a custom renderer).
                    html.tag("p", context.extendAttributes(node, "p", Map.of()));
                    renderChildren(node);
                    html.tag("/p");
                    html.line();
                } else {
                    context.render(node);
                }
                node = node.getNext();
            }

            html.tag("p", context.extendAttributes(lastParagraph, "p", Map.of()));
            renderChildren(lastParagraph);
            html.raw(" ");
            renderBackrefs(def, referencedDefinition);
            html.tag("/p");
            html.line();
        } else {
            renderChildren(def);
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

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private static class DefinitionVisitor extends AbstractVisitor {

        private final DefinitionMap<FootnoteDefinition> definitions = new DefinitionMap<>(FootnoteDefinition.class);

        @Override
        public void visit(CustomBlock customBlock) {
            if (customBlock instanceof FootnoteDefinition) {
                var def = (FootnoteDefinition) customBlock;
                definitions.putIfAbsent(def.getLabel(), def);
            } else {
                super.visit(customBlock);
            }
        }
    }

    private static class ReferenceVisitor extends AbstractVisitor {
        private final Consumer<FootnoteReference> consumer;

        private ReferenceVisitor(Consumer<FootnoteReference> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof FootnoteReference) {
                var ref = (FootnoteReference) customNode;
                consumer.accept(ref);
            } else {
                super.visit(customNode);
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

    private static class ReferenceInfo {
        /**
         * The ID of the reference; in the corresponding definition, a link back to this reference will be rendered.
         */
        private final String id;
        /**
         * The ID of the definition, for linking to the definition.
         */
        private final String definitionId;
        /**
         * The definition number, rendered in superscript.
         */
        private final int definitionNumber;

        private ReferenceInfo(String id, String definitionId, int definitionNumber) {
            this.id = id;
            this.definitionId = definitionId;
            this.definitionNumber = definitionNumber;
        }
    }
}
