Okay, here's a reorganized version of the `commonmark-java` README content, structured with a Table of Contents (TOC) for better readability and navigation.

-----

# CommonMark-Java

**A Java library for parsing and rendering Markdown text according to the CommonMark specification (and some extensions).**

| Stars      | Forks | Watching | License        | Latest Release |
| :--------- | :---- | :------- | :------------- | :------------- |
| **2.5k** | **311** | **102** | BSD-2-Clause   | 0.24.0 (Oct 21, 2024) |

**Repository:** [commonmark/commonmark-java](https://github.com/commonmark/commonmark-java) (Assumed link)

-----

## Table of Contents

1.  [Introduction](https://www.google.com/search?q=%23introduction)
      * [Features](https://www.google.com/search?q=%23features)
      * [Supported Runtimes](https://www.google.com/search?q=%23supported-runtimes)
2.  [Getting Started](https://www.google.com/search?q=%23getting-started)
      * [Installation](https://www.google.com/search?q=%23installation)
      * [API Stability](https://www.google.com/search?q=%23api-stability)
      * [Specification and Dingus](https://www.google.com/search?q=%23specification-and-dingus)
3.  [Core Usage](https://www.google.com/search?q=%23core-usage)
      * [Parse and Render to HTML](https://www.google.com/search?q=%23parse-and-render-to-html)
      * [Render to Markdown](https://www.google.com/search?q=%23render-to-markdown)
      * [Render to Plain Text](https://www.google.com/search?q=%23render-to-plain-text)
      * [Using Visitors to Process Nodes](https://www.google.com/search?q=%23using-visitors-to-process-nodes)
      * [Accessing Source Positions](https://www.google.com/search?q=%23accessing-source-positions)
4.  [Advanced Customization](https://www.google.com/search?q=%23advanced-customization)
      * [Adding or Changing HTML Element Attributes](https://www.google.com/search?q=%23adding-or-changing-html-element-attributes)
      * [Customizing HTML Rendering](https://www.google.com/search?q=%23customizing-html-rendering)
      * [Adding Your Own Node Types](https://www.google.com/search?q=%23adding-your-own-node-types)
      * [Customizing Parsing](https://www.google.com/search?q=%23customizing-parsing)
5.  [Thread-Safety](https://www.google.com/search?q=%23thread-safety)
6.  [API Documentation](https://www.google.com/search?q=%23api-documentation)
7.  [Extensions](https://www.google.com/search?q=%23extensions)
      * [Using Extensions](https://www.google.com/search?q=%23using-extensions)
      * [Available Core Extensions](https://www.google.com/search?q=%23available-core-extensions)
          * [Autolink](https://www.google.com/search?q=%23autolink)
          * [Strikethrough (GFM)](https://www.google.com/search?q=%23strikethrough-gfm)
          * [Tables (GFM)](https://www.google.com/search?q=%23tables-gfm)
          * [Footnotes](https://www.google.com/search?q=%23footnotes)
          * [Heading Anchor](https://www.google.com/search?q=%23heading-anchor)
          * [Ins (Underline)](https://www.google.com/search?q=%23ins-underline)
          * [YAML Front Matter](https://www.google.com/search?q=%23yaml-front-matter)
          * [Image Attributes](https://www.google.com/search?q=%23image-attributes)
          * [Task List Items](https://www.google.com/search?q=%23task-list-items)
      * [Third-Party Extensions](https://www.google.com/search?q=%23third-party-extensions)
8.  [Project Information](https://www.google.com/search?q=%23project-information)
      * [Used By](https://www.google.com/search?q=%23used-by)
      * [See Also](https://www.google.com/search?q=%23see-also)
      * [Contributing](https://www.google.com/search?q=%23contributing)
      * [License](https://www.google.com/search?q=%23license)
      * [Repository Topics](https://www.google.com/search?q=%23repository-topics)

-----

## 1\. Introduction

`commonmark-java` is a Java library for parsing Markdown text to an abstract syntax tree (AST), visiting and manipulating nodes, and rendering to HTML or back to Markdown. It originated as a port of `commonmark.js` but has since evolved into a powerful and extensible library.

### Features

  * **Small:** The core library has no external dependencies. Extensions are provided in separate artifacts.
  * **Fast:** Significantly faster (10-20 times) than older libraries like `pegdown`. Benchmarks are available in the repository.
  * **Flexible:** Allows manipulation of the AST after parsing and customization of HTML rendering.
  * **Extensible:** Supports various extensions like tables, strikethrough, autolinking, and more.

### Supported Runtimes

  * **Java:** Java 11 and later.
  * **Android:** Supported on a best-effort basis (minimum API level 19). Refer to the `commonmark-android-test` directory for details. Issues should be reported.

-----

## 2\. Getting Started

### Installation

Add the core library to your project using Maven (see [Maven Central](https://search.maven.org/artifact/org.commonmark/commonmark) for all artifacts):

```xml
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.24.0</version>
</dependency>
```

For Java 9+ modules, the module names correspond to package names (e.g., `org.commonmark`, `org.commonmark.ext.autolink`).

### API Stability

For `0.x` releases, the API is not considered stable and may change between minor releases. Semantic Versioning will be followed after the `1.0` release. Packages containing `beta` are not subject to stable API guarantees yet but should be safe for normal usage.

### Specification and Dingus

  * Refer to the `spec.txt` file in the repository to see which version of the CommonMark specification is currently implemented.
  * Explore the syntax and edge cases using the [CommonMark dingus](https://spec.commonmark.org/dingus/).
  * If you clone the repository, the `DingusApp` class allows for interactive testing.

-----

## 3\. Core Usage

### Parse and Render to HTML

```java
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

Parser parser = Parser.builder().build();
Node document = parser.parse("This is *Markdown*");
HtmlRenderer renderer = HtmlRenderer.builder().build();
renderer.render(document);  // "<p>This is <em>Markdown</em></p>\n"
```

Both `Parser.Builder` and `HtmlRenderer.Builder` offer configuration methods:

  * `escapeHtml(true)` on `HtmlRenderer`: Escapes raw HTML tags and blocks.
  * `sanitizeUrls(true)` on `HtmlRenderer`: Strips potentially unsafe URLs from `<a>` and `<img>` tags.

**Note:** This library does not sanitize the HTML for allowed tags. If exposing the HTML, consider using a sanitizer.

### Render to Markdown

```java
import org.commonmark.node.*;
import org.commonmark.renderer.markdown.MarkdownRenderer;

MarkdownRenderer renderer = MarkdownRenderer.builder().build();
Node document = new Document(); // Or parse existing Markdown
Heading heading = new Heading();
heading.setLevel(2);
heading.appendChild(new Text("My title"));
document.appendChild(heading);
renderer.render(document);  // "## My title\n"
```

### Render to Plain Text

For rendering plain text with minimal markup, use `TextContentRenderer`.

### Using Visitors to Process Nodes

The AST can be traversed or modified using a visitor pattern.

```java
Node node = parser.parse("Example\n=======\n\nSome more text");
WordCountVisitor visitor = new WordCountVisitor();
node.accept(visitor);
// visitor.wordCount;  // 4 (after execution)

class WordCountVisitor extends AbstractVisitor {
    int wordCount = 0;

    @Override
    public void visit(Text text) {
        // This is called for all Text nodes.
        wordCount += text.getLiteral().split("\\W+").length;
        // Descend into children (Text nodes don't have children, but good practice).
        visitChildren(text);
    }
}
```

### Accessing Source Positions

To get the source position (line, column, index) of parsed nodes:

```java
import org.commonmark.parser.IncludeSourceSpans;

var parser = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();
var source = "foo\n\nbar *baz*";
var doc = parser.parse(source);
var emphasis = doc.getLastChild().getLastChild(); // Assuming 'bar *baz*' is the last paragraph
var s = emphasis.getSourceSpans().get(0);

s.getLineIndex();    // 2 (third line)
s.getColumnIndex();  // 4 (fifth column)
s.getInputIndex();   // 9 (string index 9)
s.getLength();       // 5
source.substring(s.getInputIndex(), s.getInputIndex() + s.getLength());  // "*baz*"
```

Use `IncludeSourceSpans.BLOCKS` if only block-level positions are needed.

-----

## 4\. Advanced Customization

### Adding or Changing HTML Element Attributes

Use an `AttributeProviderFactory` to add or modify HTML attributes. Example: adding `class="border"` to `<img>` tags.

```java
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
// Other imports: Node, Image, Parser, HtmlRenderer, Map

Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder()
        .attributeProviderFactory(new AttributeProviderFactory() {
            public AttributeProvider create(AttributeProviderContext context) {
                return new ImageAttributeProvider();
            }
        })
        .build();
Node document = parser.parse("![text](/url.png)");
renderer.render(document); // "<p><img src=\"/url.png\" alt=\"text\" class=\"border\" /></p>\n"

class ImageAttributeProvider implements AttributeProvider {
    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        if (node instanceof Image) {
            attributes.put("class", "border");
        }
    }
}
```

### Customizing HTML Rendering

For more control over HTML output, use an `HtmlNodeRendererFactory`. Example: changing indented code block rendering.

```java
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlWriter;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
// Other imports: Parser, HtmlRenderer, Set

Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder()
        .nodeRendererFactory(new HtmlNodeRendererFactory() {
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new IndentedCodeBlockNodeRenderer(context);
            }
        })
        .build();
Node document = parser.parse("Example:\n\n    code");
renderer.render(document); // "<p>Example:</p>\n<pre>code\n</pre>\n"

class IndentedCodeBlockNodeRenderer implements NodeRenderer {
    private final HtmlWriter html;

    IndentedCodeBlockNodeRenderer(HtmlNodeRendererContext context) {
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(IndentedCodeBlock.class);
    }

    @Override
    public void render(Node node) {
        IndentedCodeBlock codeBlock = (IndentedCodeBlock) node;
        html.line();
        html.tag("pre");
        html.text(codeBlock.getLiteral());
        html.tag("/pre");
        html.line();
    }
}
```

### Adding Your Own Node Types

Create subclasses of `CustomNode` to store additional data or create custom HTML elements. Define their HTML rendering using a `NodeRenderer` as shown above.

### Customizing Parsing

Extend or override parsing behavior via methods on `Parser.Builder`:

  * `enabledBlockTypes`: Enable/disable parsing of specific block types (e.g., headings, code blocks).
  * `customBlockParserFactory`: Extend/override parsing of blocks.
  * `customInlineContentParserFactory`: Extend/override parsing of inline content.
  * `customDelimiterProcessor`: Extend parsing of delimiters in inline content.
  * `linkProcessor` and `linkMarker`: Customize processing of links.

Refer to "Blocks and inlines" in the CommonMark spec for an overview.

-----

## 5\. Thread-Safety

Both `Parser` and `HtmlRenderer` are designed for thread-safety. Configure them once using their builders, then reuse instances across multiple threads. State for parsing/rendering is separated from configuration. Report any bugs found.

-----

## 6\. API Documentation

Javadocs are available online at [javadoc.io](https://www.javadoc.io/doc/org.commonmark/commonmark). (Assumed current link from typical practice)

-----

## 7\. Extensions

Extensions can modify the parser, HTML renderer, or both. They are optional and reside in separate artifacts.

### Using Extensions

1.  **Add Dependency:** Include the extension's artifact.
2.  **Configure Builders:** Pass the extension to `Parser.Builder` and `HtmlRenderer.Builder`.

Example with GFM Tables:

**Dependency:**

```xml
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark-ext-gfm-tables</artifactId>
    <version>0.24.0</version>
</dependency>
```

**Configuration:**

```java
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.List;
// Other imports: Parser, HtmlRenderer

List<Extension> extensions = List.of(TablesExtension.create());

Parser parser = Parser.builder()
        .extensions(extensions)
        .build();
HtmlRenderer renderer = HtmlRenderer.builder()
        .extensions(extensions)
        .build();
```

### Available Core Extensions

The following extensions are developed alongside `commonmark-java`:

#### 7.2.1. Autolink

Turns plain URLs and email addresses into links (based on `autolink-java`).

  * **Class:** `AutolinkExtension`
  * **Artifact:** `commonmark-ext-autolink`

#### 7.2.2. Strikethrough (GFM)

Enables strikethrough text using `~~text~~`.

  * **Class:** `StrikethroughExtension`
  * **Artifact:** `commonmark-ext-gfm-strikethrough`

#### 7.2.3. Tables (GFM)

Enables GitHub Flavored Markdown-style tables using pipes.

  * **Class:** `TablesExtension`
  * **Artifact:** `commonmark-ext-gfm-tables`

#### 7.2.4. Footnotes

Enables footnotes like `Main text[^1]` and `[^1]: Footnote content.`. Inline footnotes `^[inline footnote]` are supported via `FootnotesExtension.Builder#inlineFootnotes`.

  * **Class:** `FootnotesExtension`
  * **Artifact:** `commonmark-ext-footnotes`

#### 7.2.5. Heading Anchor

Adds auto-generated `id` attributes to heading tags (e.g., `# Heading` becomes `<h1 id="heading">Heading</h1>`).

  * **Class:** `HeadingAnchorExtension`
  * **Artifact:** `commonmark-ext-heading-anchor`
    For custom rendering, use `IdGenerator` with `HtmlNodeRendererFactory`.

#### 7.2.6. Ins (Underline)

Enables underlined text using `++text++`, rendered with `<ins>` tags.

  * **Class:** `InsExtension`
  * **Artifact:** `commonmark-ext-ins`

#### 7.2.7. YAML Front Matter

Adds support for metadata via a YAML front matter block (supports a subset of YAML).

```yaml
---
key: value
list:
  - value 1
  - value 2
literal: |
  this is literal value.

  literal values 2
---
document start here
```

Use `YamlFrontMatterVisitor` to fetch metadata.

  * **Class:** `YamlFrontMatterExtension`
  * **Artifact:** `commonmark-ext-yaml-front-matter`

#### 7.2.8. Image Attributes

Allows specifying attributes (e.g., `width`, `height`) for images: `![text](/url.png){width=640 height=480}`.

  * **Class:** `ImageAttributesExtension`
  * **Artifact:** `commonmark-ext-image-attributes`
    **Note:** Uses `{ }` as delimiters, which may conflict with other delimiter processors using curly braces.

#### 7.2.9. Task List Items

Adds support for GFM-style task lists:

```markdown
- [ ] task #1
- [x] task #2
```

Renders as:

```html
<ul>
<li><input type="checkbox" disabled=""> task #1</li>
<li><input type="checkbox" disabled="" checked=""> task #2</li>
</ul>
```

  * **Class:** `TaskListItemsExtension`
  * **Artifact:** `commonmark-ext-task-list-items`

### Third-Party Extensions

  * **commonmark-ext-notifications:** Creates notification/admonition paragraphs (INFO, SUCCESS, WARNING, ERROR).

-----

## 8\. Project Information

### Used By

  * Atlassian (where the library was initially developed)
  * Java (OpenJDK) ([link](https://www.google.com/search?q=https://github.com/openjdk/skara/blob/c5a897407420e67970abb95321922b87977498a0/bots/notify/src/main/java/org/openjdk/skara/bots/notify/MailingListUpdater.java%23L52))
  * Gerrit code review/Gitiles ([link](https://www.google.com/search?q=https://gerrit.googlesource.com/gitiles/%2B/refs/heads/master/java/com/google/gitiles/doc/MarkdownToHtml.java%2360))
  * Clerk (moldable live programming for Clojure)
  * Znai
  * Open Note (Android markdown editor)
  * Quarkus Roq (Static Site Generator)

(Feel free to submit a PR to add your project.)

### See Also

  * **Markwon:** Android library for rendering markdown as system-native Spannables.
  * **flexmark-java:** A fork that added support for more syntax and flexibility.

### Contributing

Please see the `CONTRIBUTING.md` file in the repository.

### License

Copyright (c) 2015, Robin Stocker.
BSD (2-clause) licensed. See the `LICENSE.txt` file in the repository.

### Repository Topics

`java` `markdown` `parser` `library` `commonmark` `renderer`

-----
