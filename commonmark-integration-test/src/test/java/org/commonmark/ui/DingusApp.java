package org.commonmark.ui;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Simple UI to quickly test out different rendering of CommonMark inputs.
 * Similar to <a href="https://spec.commonmark.org/dingus/">commonmark.js dingus</a>.
 **/
public class DingusApp {

    private final Parser parser = Parser.builder().build();
    private final TextContentRenderer textRenderer = TextContentRenderer.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    private final JTabbedPane tabbedPane;
    private final JEditorPane htmlVisualRendererOutput;
    private final JTextArea htmlSourceRendererOutput;
    private final JTextArea textRendererOutput;

    public static void main(String[] args) {
        new DingusApp().run();
    }

    private DingusApp() {
        tabbedPane = new JTabbedPane();

        htmlVisualRendererOutput = new JEditorPane();
        htmlVisualRendererOutput.setEnabled(false);
        htmlVisualRendererOutput.setContentType("text/html");

        htmlSourceRendererOutput = new JTextArea();
        htmlSourceRendererOutput.setEnabled(false);
        htmlSourceRendererOutput.setLineWrap(true);
        htmlSourceRendererOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        textRendererOutput = new JTextArea();
        textRendererOutput.setEnabled(false);
        textRendererOutput.setLineWrap(true);
        textRendererOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private void run() {
        JFrame frame = new JFrame("commonmark-java dingus");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.setSize(new Dimension(1200, 675));

        final JTextArea input = new JTextArea();
        input.setBorder(BorderFactory.createTitledBorder("Input"));
        input.setLineWrap(true);
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOutput(input.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOutput(input.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        tabbedPane.addTab("HTML rendered", htmlVisualRendererOutput);
        tabbedPane.addTab("HTML source", htmlSourceRendererOutput);
        tabbedPane.addTab("Plain text", textRendererOutput);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateOutput(input.getText());
            }
        });

        input.setText("# Example\n" +
                "Enter text *here* and see how it renders on the right.\n\n" +
                "* Try\n* this\n\n" +
                "```\nor this\n```");
        updateOutput(input.getText());

        frame.setLayout(new GridLayout());
        frame.add(input);
        frame.add(tabbedPane);

        frame.setVisible(true);
    }

    private void updateOutput(String inputText) {
        if (tabbedPane.getSelectedComponent() == htmlVisualRendererOutput) {
            String rendered = htmlRenderer.render(parser.parse(inputText));
            htmlVisualRendererOutput.setText(rendered);
        } else if (tabbedPane.getSelectedComponent() == htmlSourceRendererOutput) {
            String rendered = htmlRenderer.render(parser.parse(inputText));
            htmlSourceRendererOutput.setText(rendered);
        } else if (tabbedPane.getSelectedComponent() == textRendererOutput) {
            String rendered = textRenderer.render(parser.parse(inputText));
            textRendererOutput.setText(rendered);
        }
    }
}
