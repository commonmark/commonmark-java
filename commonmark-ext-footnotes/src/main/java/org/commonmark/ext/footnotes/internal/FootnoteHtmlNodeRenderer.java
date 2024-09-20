package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.ext.footnotes.InlineFootnote;
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
    private final Map<Node, ReferencedDefinition> referencedDefinitions = new LinkedHashMap<>();

    /**
     * Information about references that should be rendered as footnotes. This doesn't contain all references, just the
     * ones from inside definitions.
     */
    private final Map<Node, ReferenceInfo> references = new HashMap<>();

    public FootnoteHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.html = context.getWriter();
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(FootnoteReference.class, InlineFootnote.class, FootnoteDefinition.class);
    }

    @Override
    public void beforeRoot(Node rootNode) {
        // Collect all definitions first, so we can look them up when encountering a reference later.
        var visitor = new DefinitionVisitor();
        rootNode.accept(visitor);
        definitionMap = visitor.definitions;
    }

    @Override
    public void render(Node node) {
        if (node instanceof FootnoteReference) {
            // This is called for all references, even ones inside definitions that we render at the end.
            // Inside definitions, we have registered the reference already.
            var ref = (FootnoteReference) node;
            // Use containsKey because if value is null, we don't need to try registering again.
            var info = references.containsKey(ref) ? references.get(ref) : tryRegisterReference(ref);
            if (info != null) {
                renderReference(ref, info);
            } else {
                // A reference without a corresponding definition is rendered as plain text
                html.text("[^" + ref.getLabel() + "]");
            }
        } else if (node instanceof InlineFootnote) {
            var info = references.get(node);
            if (info == null) {
                info = registerReference(node, null);
            }
            renderReference(node, info);
        }
    }

    @Override
    public void afterRoot(Node rootNode) {
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
        // need to render more definitions. So do a breadth-first search to find all relevant definitions.
        var check = new LinkedList<>(referencedDefinitions.keySet());
        while (!check.isEmpty()) {
            var def = check.removeFirst();
            def.accept(new ShallowReferenceVisitor(def, node -> {
                if (node instanceof FootnoteReference) {
                    var ref = (FootnoteReference) node;
                    var d = definitionMap.get(ref.getLabel());
                    if (d != null) {
                        if (!referencedDefinitions.containsKey(d)) {
                            check.addLast(d);
                        }
                        references.put(ref, registerReference(d, d.getLabel()));
                    }
                } else if (node instanceof InlineFootnote) {
                    check.addLast(node);
                    references.put(node, registerReference(node, null));
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

    private ReferenceInfo tryRegisterReference(FootnoteReference ref) {
        var def = definitionMap.get(ref.getLabel());
        if (def == null) {
            return null;
        }
        return registerReference(def, def.getLabel());
    }

    private ReferenceInfo registerReference(Node node, String label) {
        // The first referenced definition gets number 1, second one 2, etc.
        var referencedDef = referencedDefinitions.computeIfAbsent(node, k -> {
            var num = referencedDefinitions.size() + 1;
            var key = definitionKey(label, num);
            return new ReferencedDefinition(num, key);
        });
        var definitionNumber = referencedDef.definitionNumber;
        // The reference number for that particular definition. E.g. if there's two references for the same definition,
        // the first one is 1, the second one 2, etc. This is needed to give each reference a unique ID so that each
        // reference can get its own backlink from the definition.
        var refNumber = referencedDef.references.size() + 1;
        var definitionKey = referencedDef.definitionKey;
        var id = referenceId(definitionKey, refNumber);
        referencedDef.references.add(id);

        return new ReferenceInfo(id, definitionId(definitionKey), definitionNumber);
    }

    private void renderReference(Node node, ReferenceInfo referenceInfo) {
        html.tag("sup", context.extendAttributes(node, "sup", Map.of("class", "footnote-ref")));

        var href = "#" + referenceInfo.definitionId;
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("href", href);
        attrs.put("id", referenceInfo.id);
        attrs.put("data-footnote-ref", null);
        html.tag("a", context.extendAttributes(node, "a", attrs));
        html.raw(String.valueOf(referenceInfo.definitionNumber));
        html.tag("/a");
        html.tag("/sup");
    }

    private void renderDefinition(Node def, ReferencedDefinition referencedDefinition) {
        var attrs = new LinkedHashMap<String, String>();
        attrs.put("id", definitionId(referencedDefinition.definitionKey));
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
        } else if (def instanceof InlineFootnote) {
            html.tag("p", context.extendAttributes(def, "p", Map.of()));
            renderChildren(def);
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

    private void renderBackrefs(Node def, ReferencedDefinition referencedDefinition) {
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
            html.raw("\u21A9");
            html.tag("/a");
            if (i + 1 < refs.size()) {
                html.raw(" ");
            }
        }
    }

    private String referenceId(String definitionKey, int number) {
        return "fnref" + definitionKey + (number == 1 ? "" : ("-" + number));
    }

    private String definitionKey(String label, int number) {
        // Named definitions use the pattern "fn-{name}" and inline definitions use "fn{number}" so as not to conflict.
        // "fn{number}" is also what pandoc uses (for all types), starting with number 1.
        if (label != null) {
            return "-" + label;
        } else {
            return "" + number;
        }
    }

    private String definitionId(String definitionKey) {
        return "fn" + definitionKey;
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

    /**
     * Visit footnote references/inline footnotes inside the parent (but not the parent itself). We want a shallow visit
     * because the caller wants to control when to descend.
     */
    private static class ShallowReferenceVisitor extends AbstractVisitor {
        private final Node parent;
        private final Consumer<Node> consumer;

        private ShallowReferenceVisitor(Node parent, Consumer<Node> consumer) {
            this.parent = parent;
            this.consumer = consumer;
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof FootnoteReference) {
                consumer.accept(customNode);
            } else if (customNode instanceof InlineFootnote) {
                if (customNode == parent) {
                    // Descend into the parent (inline footnotes can contain inline footnotes)
                    super.visit(customNode);
                } else {
                    // Don't descend here because we want to be shallow.
                    consumer.accept(customNode);
                }
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
         * The unique key of the definition. Together with a static prefix it forms the ID used in the HTML.
         */
        final String definitionKey;
        /**
         * The IDs of references for this definition, for backrefs.
         */
        final List<String> references = new ArrayList<>();

        ReferencedDefinition(int definitionNumber, String definitionKey) {
            this.definitionNumber = definitionNumber;
            this.definitionKey = definitionKey;
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
