package org.commonmark.content;

import org.commonmark.node.*;

public class TextContentRenderer {
    private final boolean stripNewlines;

    private Integer orderedListCounter;
    private Character orderedListDelimiter;

    private Character bulletListMarker;

    private TextContentRenderer(Builder builder) {
        this.stripNewlines = builder.stripNewlines;
    }

    /**
     * Create a new builder for configuring an {@link TextContentRenderer}.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public void render(Node node, Appendable output) {
        RendererVisitor rendererVisitor = new RendererVisitor(new TextContentWriter(output));
        node.accept(rendererVisitor);
    }

    /**
     * Render the tree of nodes to text content.
     *
     * @param node the root node
     * @return the rendered text content
     */
    public String render(Node node) {
        StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    /**
     * Builder for configuring an {@link TextContentRenderer}. See methods for default configuration.
     */
    public static class Builder {

        private boolean stripNewlines = false;

        /**
         * @return the configured {@link TextContentRenderer}
         */
        public TextContentRenderer build() {
            return new TextContentRenderer(this);
        }

        /**
         * Set the value of flag for stripping new lines.
         *
         * @param stripNewlines true for stripping new lines and render text as "single line",
         *                      false for keeping all line breaks
         * @return {@code this}
         */
        public Builder stripNewlines(boolean stripNewlines) {
            this.stripNewlines = stripNewlines;
            return this;
        }
    }

    private class RendererVisitor extends AbstractVisitor {
        private final TextContentWriter textContent;

        public RendererVisitor(TextContentWriter textContentWriter) {
            textContent = textContentWriter;
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            textContent.write('«');
            visitChildren(blockQuote);
            textContent.write('»');

            writeEndOfLine(blockQuote, null);
        }

        @Override
        public void visit(BulletList bulletList) {
            bulletListMarker = bulletList.getBulletMarker();
            visitChildren(bulletList);
            writeEndOfLine(bulletList, null);
            bulletListMarker = null;
        }

        @Override
        public void visit(Code code) {
            textContent.write('\"');
            textContent.write(code.getLiteral());
            textContent.write('\"');
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            if (stripNewlines) {
                textContent.writeStripped(fencedCodeBlock.getLiteral());
                writeEndOfLine(fencedCodeBlock, null);
            } else {
                textContent.write(fencedCodeBlock.getLiteral());
            }
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            writeEndOfLine(hardLineBreak, null);
        }

        @Override
        public void visit(Heading heading) {
            visitChildren(heading);
            writeEndOfLine(heading, ':');
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            if (!stripNewlines) {
                textContent.write("***");
            }
            writeEndOfLine(thematicBreak, null);
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            writeText(htmlInline.getLiteral());
        }

        @Override
        public void visit(HtmlBlock htmlBlock) {
            writeText(htmlBlock.getLiteral());
        }

        @Override
        public void visit(Image image) {
            writeLink(image, image.getTitle(), image.getDestination());
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            if (stripNewlines) {
                textContent.writeStripped(indentedCodeBlock.getLiteral());
                writeEndOfLine(indentedCodeBlock, null);
            } else {
                textContent.write(indentedCodeBlock.getLiteral());
            }
        }

        @Override
        public void visit(Link link) {
            writeLink(link, link.getTitle(), link.getDestination());
        }

        @Override
        public void visit(ListItem listItem) {
            if (orderedListCounter != null) {
                textContent.write(String.valueOf(orderedListCounter) + orderedListDelimiter + " ");
                visitChildren(listItem);
                writeEndOfLine(listItem, null);
                orderedListCounter++;
            } else if (bulletListMarker != null) {
                if (!stripNewlines) {
                    textContent.write(bulletListMarker + " ");
                }
                visitChildren(listItem);
                writeEndOfLine(listItem, null);
            }
        }

        @Override
        public void visit(OrderedList orderedList) {
            orderedListCounter = orderedList.getStartNumber();
            orderedListDelimiter = orderedList.getDelimiter();
            visitChildren(orderedList);
            writeEndOfLine(orderedList, null);
            orderedListCounter = null;
            orderedListDelimiter = null;
        }

        @Override
        public void visit(Paragraph paragraph) {
            visitChildren(paragraph);
            // Add "end of line" only if its "root paragraph.
            if (paragraph.getParent() == null || paragraph.getParent() instanceof Document) {
                writeEndOfLine(paragraph, null);
            }
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            writeEndOfLine(softLineBreak, null);
        }

        @Override
        public void visit(Text text) {
            writeText(text.getLiteral());
        }

        private void writeText(String text) {
            if (stripNewlines) {
                textContent.writeStripped(text);
            } else {
                textContent.write(text);
            }
        }

        private void writeLink(Node node, String title, String destination) {
            boolean hasChild = node.getFirstChild() != null;
            boolean hasTitle = title != null;
            boolean hasDestination = destination != null && !destination.equals("");

            if (hasChild) {
                textContent.write('"');
                visitChildren(node);
                textContent.write('"');
                if (hasTitle || hasDestination) {
                    textContent.whitespace();
                    textContent.write('(');
                }
            }

            if (hasTitle) {
                textContent.write(title);
                if (hasDestination) {
                    textContent.colon();
                    textContent.whitespace();
                }
            }

            if (hasDestination) {
                textContent.write(destination);
            }

            if (hasChild && (hasTitle || hasDestination)) {
                textContent.write(')');
            }
        }

        private void writeEndOfLine(Node node, Character c) {
            if (stripNewlines) {
                if (c != null) {
                    textContent.write(c);
                }
                if (node.getNext() != null) {
                    textContent.whitespace();
                }
            } else {
                if (node.getNext() != null) {
                    textContent.line();
                }
            }
        }
    }
}
