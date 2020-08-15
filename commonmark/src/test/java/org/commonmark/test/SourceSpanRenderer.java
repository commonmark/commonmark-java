package org.commonmark.test;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SourceSpanRenderer {

    public static String render(Node document, String source) {
        SourceSpanMarkersVisitor visitor = new SourceSpanMarkersVisitor();
        document.accept(visitor);
        Map<Integer, Map<Integer, List<String>>> markers = visitor.getMarkers();

        StringBuilder sb = new StringBuilder();

        String[] lines = source.split("\n");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            Map<Integer, List<String>> lineMarkers = markers.get(lineIndex);
            for (int i = 0; i < line.length(); i++) {
                appendMarkers(lineMarkers, i, sb);
                sb.append(line.charAt(i));
            }
            appendMarkers(lineMarkers, line.length(), sb);
            sb.append("\n");
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

        private final Map<Integer, Map<Integer, List<String>>> markers = new HashMap<>();
        private final String opening = "({[<⸢⸤";
        private final String closing = ")}]>⸣⸥";

        private int markerIndex;

        public Map<Integer, Map<Integer, List<String>>> getMarkers() {
            return markers;
        }

        @Override
        protected void visitChildren(Node parent) {
            if (!parent.getSourceSpans().isEmpty()) {
                for (SourceSpan sourceSpan : parent.getSourceSpans()) {
                    String opener = String.valueOf(opening.charAt(markerIndex % opening.length()));
                    String closer = String.valueOf(closing.charAt(markerIndex % closing.length()));

                    int col = sourceSpan.getColumnIndex();
                    getMarkers(sourceSpan.getLineIndex(), col).add(opener);
                    getMarkers(sourceSpan.getLineIndex(), col + sourceSpan.getLength()).add(0, closer);
                }
                markerIndex++;
            }
            super.visitChildren(parent);
        }

        private List<String> getMarkers(int lineIndex, int columnIndex) {
            Map<Integer, List<String>> columnMap = markers.get(lineIndex);
            if (columnMap == null) {
                columnMap = new HashMap<>();
                markers.put(lineIndex, columnMap);
            }

            List<String> markers = columnMap.get(columnIndex);
            if (markers == null) {
                markers = new LinkedList<>();
                columnMap.put(columnIndex, markers);
            }

            return markers;
        }
    }
}
