package org.commonmark.test;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;

import java.util.*;

public class SourceSpanRenderer {

    /**
     * Render source spans in the document using source position's line and column index.
     */
    public static String renderWithLineColumn(Node document, String source) {
        SourceSpanMarkersVisitor visitor = new SourceSpanMarkersVisitor();
        document.accept(visitor);
        var lineColumnMarkers = visitor.getLineColumnMarkers();

        StringBuilder sb = new StringBuilder();

        String[] lines = source.split("\n");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            Map<Integer, List<String>> lineMarkers = lineColumnMarkers.get(lineIndex);
            for (int i = 0; i < line.length(); i++) {
                appendMarkers(lineMarkers, i, sb);
                sb.append(line.charAt(i));
            }
            appendMarkers(lineMarkers, line.length(), sb);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Render source spans in the document using source position's input index.
     */
    public static String renderWithInputIndex(Node document, String source) {
        SourceSpanMarkersVisitor visitor = new SourceSpanMarkersVisitor();
        document.accept(visitor);
        var markers = visitor.getInputIndexMarkers();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            markers.getOrDefault(i, List.of()).forEach(marker -> sb.append(marker));
            sb.append(source.charAt(i));
        }
        return sb.toString();
    }

    private static void appendMarkers(Map<Integer, List<String>> lineMarkers, int columnIndex, StringBuilder sb) {
        if (lineMarkers != null) {
            List<String> columnMarkers = lineMarkers.get(columnIndex);
            if (columnMarkers != null) {
                for (String marker : columnMarkers) {
                    sb.append(marker);
                }
            }
        }
    }

    private static class SourceSpanMarkersVisitor extends AbstractVisitor {

        private static final String OPENING = "({[<⸢⸤";
        private static final String CLOSING = ")}]>⸣⸥";

        private final Map<Integer, Map<Integer, List<String>>> lineColumnMarkers = new HashMap<>();
        private final Map<Integer, List<String>> inputIndexMarkers = new HashMap<>();

        private int markerIndex;

        public Map<Integer, Map<Integer, List<String>>> getLineColumnMarkers() {
            return lineColumnMarkers;
        }

        public Map<Integer, List<String>> getInputIndexMarkers() {
            return inputIndexMarkers;
        }

        @Override
        protected void visitChildren(Node parent) {
            if (!parent.getSourceSpans().isEmpty()) {
                for (var span : parent.getSourceSpans()) {
                    String opener = String.valueOf(OPENING.charAt(markerIndex % OPENING.length()));
                    String closer = String.valueOf(CLOSING.charAt(markerIndex % CLOSING.length()));

                    int line = span.getLineIndex();
                    int col = span.getColumnIndex();
                    var input = span.getInputIndex();
                    int length = span.getLength();
                    getMarkers(line, col).add(opener);
                    getMarkers(line, col + length).add(0, closer);

                    inputIndexMarkers.computeIfAbsent(input, k -> new LinkedList<>()).add(opener);
                    inputIndexMarkers.computeIfAbsent(input + length, k -> new LinkedList<>()).add(0, closer);
                }
                markerIndex++;
            }
            super.visitChildren(parent);
        }

        private List<String> getMarkers(int lineIndex, int columnIndex) {
            var columnMap = lineColumnMarkers.computeIfAbsent(lineIndex, k -> new HashMap<>());
            return columnMap.computeIfAbsent(columnIndex, k -> new LinkedList<>());
        }
    }
}
