commonmark-java
===============

Java library for parsing and rendering [Markdown] text according to the
[CommonMark] specification (and some extensions).

[![Maven Central status](https://img.shields.io/maven-central/v/org.commonmark/commonmark.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.commonmark%22)
[![javadoc](https://www.javadoc.io/badge/org.commonmark/commonmark.svg?color=blue)](https://www.javadoc.io/doc/org.commonmark/commonmark)
[![ci](https://github.com/commonmark/commonmark-java/workflows/ci/badge.svg)](https://github.com/commonmark/commonmark-java/actions?query=workflow%3Aci)
[![codecov](https://codecov.io/gh/commonmark/commonmark-java/branch/main/graph/badge.svg)](https://codecov.io/gh/commonmark/commonmark-java)
[![SourceSpy Dashboard](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/commonmarkcommonmarkjava/)

Introduction
------------

Provides classes for parsing input to an abstract syntax tree of nodes
(AST), visiting and manipulating nodes, and rendering to HTML. It
started out as a port of [commonmark.js], but has since evolved into a
full library with a nice API and the following features:

* Small (core has no dependencies, extensions in separate artifacts)
* Fast (10-20 times faster than pegdown, see benchmarks in repo)
* Flexible (manipulate the AST after parsing, customize HTML rendering)
* Extensible (tables, strikethrough, autolinking and more, see below)

The library is supported on Java 8 or later. It should work on Java 7
and Android too, but that is on a best-effort basis, please report
problems. For Android the minimum API level is 19, see the
[commonmark-android-test](commonmark-android-test) directory.

Coordinates for core library (see all on [Maven Central]):

```xml
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.20.0</version>
</dependency>
```

The module names to use in Java 9 are `org.commonmark`,
`org.commonmark.ext.autolink`, etc, corresponding to package names.

Note that for 0.x releases of this library, the API is not considered stable
yet and may break between minor releases. After 1.0, [Semantic Versioning] will
be followed.

See the [spec.txt](commonmark-test-util/src/main/resources/spec.txt)
file if you're wondering which version of the spec is currently
implemented. Also check out the [CommonMark dingus] for getting familiar
with the syntax or trying out edge cases. If you clone the repository,
you can also use the `DingusApp` class to try out things interactively.


Usage
-----

#### Parse and render to HTML

```java
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

Parser parser = Parser.builder().build();
Node document = parser.parse("This is *Sparta*");
HtmlRenderer renderer = HtmlRenderer.builder().build();
renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
```

This uses the parser and renderer with default options. Both builders have
methods for configuring their behavior:

* `escapeHtml(true)` on `HtmlRenderer` will escape raw HTML tags and blocks.
* `sanitizeUrls(true)` on `HtmlRenderer` will strip potentially unsafe URLs
  from `<a>` and `<img>` tags
* For all available options, see methods on the builders.

Note that this library doesn't try to sanitize the resulting HTML with regards
to which tags are allowed, etc. That is the responsibility of the caller, and
if you expose the resulting HTML, you probably want to run a sanitizer on it
after this.

For rendering to plain text, there's also a `TextContentRenderer` with
a very similar API.

#### Use a visitor to process parsed nodes

After the source text has been parsed, the result is a tree of nodes.
That tree can be modified before rendering, or just inspected without
rendering:

```java
Node node = parser.parse("Example\n=======\n\nSome more text");
WordCountVisitor visitor = new WordCountVisitor();
node.accept(visitor);
visitor.wordCount;  // 4

class WordCountVisitor extends AbstractVisitor {
    int wordCount = 0;

    @Override
    public void visit(Text text) {
        // This is called for all Text nodes. Override other visit methods for other node types.

        // Count words (this is just an example, don't actually do it this way for various reasons).
        wordCount += text.getLiteral().split("\\W+").length;

        // Descend into children (could be omitted in this case because Text nodes don't have children).
        visitChildren(text);
    }
}
```

#### Add or change attributes of HTML elements

Sometimes you might want to customize how HTML is rendered. If all you
want to do is add or change attributes on some elements, there's a
simple way to do that.

In this example, we register a factory for an `AttributeProvider` on the
renderer to set a `class="border"` attribute on `img` elements.

```java
Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder()
        .attributeProviderFactory(new AttributeProviderFactory() {
            public AttributeProvider create(AttributeProviderContext context) {
                return new ImageAttributeProvider();
            }
        })
        .build();

Node document = parser.parse("![text](/url.png)");
renderer.render(document);
// "<p><img src=\"/url.png\" alt=\"text\" class=\"border\" /></p>\n"

class ImageAttributeProvider implements AttributeProvider {
    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        if (node instanceof Image) {
            attributes.put("class", "border");
        }
    }
}
```

#### Customize HTML rendering

If you want to do more than just change attributes, there's also a way
to take complete control over how HTML is rendered.

In this example, we're changing the rendering of indented code blocks to
only wrap them in `pre` instead of `pre` and `code`:

```java
Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder()
        .nodeRendererFactory(new HtmlNodeRendererFactory() {
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new IndentedCodeBlockNodeRenderer(context);
            }
        })
        .build();

Node document = parser.parse("Example:\n\n    code");
renderer.render(document);
// "<p>Example:</p>\n<pre>code\n</pre>\n"

class IndentedCodeBlockNodeRenderer implements NodeRenderer {

    private final HtmlWriter html;

    IndentedCodeBlockNodeRenderer(HtmlNodeRendererContext context) {
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        // Return the node types we want to use this renderer for.
        return Collections.<Class<? extends Node>>singleton(IndentedCodeBlock.class);
    }

    @Override
    public void render(Node node) {
        // We only handle one type as per getNodeTypes, so we can just cast it here.
        IndentedCodeBlock codeBlock = (IndentedCodeBlock) node;
        html.line();
        html.tag("pre");
        html.text(codeBlock.getLiteral());
        html.tag("/pre");
        html.line();
    }
}
```

#### Add your own node types

In case you want to store additional data in the document or have custom
elements in the resulting HTML, you can create your own subclass of
`CustomNode` and add instances as child nodes to existing nodes.

To define the HTML rendering for them, you can use a `NodeRenderer` as
explained above.

#### Thread-safety

Both the `Parser` and `HtmlRenderer` are designed so that you can
configure them once using the builders and then use them multiple
times/from multiple threads. This is done by separating the state for
parsing/rendering from the configuration.

Having said that, there might be bugs of course. If you find one, please
report an issue.

### API documentation

Javadocs are available online on
[javadoc.io](https://www.javadoc.io/doc/org.commonmark/commonmark).


Extensions
----------

Extensions need to extend the parser, or the HTML renderer, or both. To
use an extension, the builder objects can be configured with a list of
extensions. Because extensions are optional, they live in separate
artifacts, so additional dependencies need to be added as well.

Let's look at how to enable tables from GitHub Flavored Markdown.
First, add an additional dependency (see [Maven Central] for others):

```xml
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark-ext-gfm-tables</artifactId>
    <version>0.20.0</version>
</dependency>
```

Then, configure the extension on the builders:

```java
import org.commonmark.ext.gfm.tables.TablesExtension;

List<Extension> extensions = Arrays.asList(TablesExtension.create());
Parser parser = Parser.builder()
        .extensions(extensions)
        .build();
HtmlRenderer renderer = HtmlRenderer.builder()
        .extensions(extensions)
        .build();
```

To configure another extension in the above example, just add it to the list.

The following extensions are developed with this library, each in their
own artifact.

### Autolink

Turns plain links such as URLs and email addresses into links (based on [autolink-java]).

Use class `AutolinkExtension` from artifact `commonmark-ext-autolink`.

### Strikethrough

Enables strikethrough of text by enclosing it in `~~`. For example, in
`hey ~~you~~`, `you` will be rendered as strikethrough text.

Use class `StrikethroughExtension` in artifact `commonmark-ext-gfm-strikethrough`.

### Tables

Enables tables using pipes as in [GitHub Flavored Markdown][gfm-tables].

Use class `TablesExtension` in artifact `commonmark-ext-gfm-tables`.

### Heading anchor

Enables adding auto generated "id" attributes to heading tags. The "id"
is based on the text of the heading.

`# Heading` will be rendered as:

```
<h1 id="heading">Heading</h1>
```

Use class `HeadingAnchorExtension` in artifact `commonmark-ext-heading-anchor`.

In case you want custom rendering of the heading instead, you can use
the `IdGenerator` class directly together with a
`HtmlNodeRendererFactory` (see example above).

### Ins

Enables underlining of text by enclosing it in `++`. For example, in
`hey ++you++`, `you` will be rendered as underline text. Uses the &lt;ins&gt; tag.

Use class `InsExtension` in artifact `commonmark-ext-ins`.

### YAML front matter

Adds support for metadata through a YAML front matter block. This extension only supports a subset of YAML syntax. Here's an example of what's supported:

```
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

Use class `YamlFrontMatterExtension` in artifact `commonmark-ext-yaml-front-matter`. To fetch metadata, use `YamlFrontMatterVisitor`.

### Image Attributes

Adds support for specifying attributes (specifically height and width) for images.

The attribute elements are given as `key=value` pairs inside curly braces `{ }` after the image node to which they apply,
for example:
```
![text](/url.png){width=640 height=480}
```
will be rendered as:
```
<img src="/url.png" alt="text" width="640" height="480" />
```

Use class `ImageAttributesExtension` in artifact `commonmark-ext-image-attributes`.

Note: since this extension uses curly braces `{` `}` as its delimiters (in `StylesDelimiterProcessor`), this means that
other delimiter processors *cannot* use curly braces for delimiting.

### Task List Items

Adds support for tasks as list items.

A task can be represented as a list item where the first non-whitespace character is a left bracket `[`, then a single
whitespace character or the letter `x` in lowercase or uppercase, then a right bracket `]` followed by at least one
whitespace before any other content.

For example:
```
- [ ] task #1
- [x] task #2
```
will be rendered as:
```
<ul>
<li><input type="checkbox" disabled=""> task #1</li>
<li><input type="checkbox" disabled="" checked=""> task #2</li>
</ul>
```

Use class `TaskListItemsExtension` in artifact `commonmark-ext-task-list-items`.

### Third-party extensions

You can also find other extensions in the wild:

* [commonmark-ext-notifications](https://github.com/McFoggy/commonmark-ext-notifications): this extension allows to easily create notifications/admonitions paragraphs like `INFO`, `SUCCESS`, `WARNING` or `ERROR`

See also
--------

* [Markwon](https://github.com/noties/Markwon): Android library for rendering markdown as system-native Spannables
* [flexmark-java](https://github.com/vsch/flexmark-java): Fork that added support for a lot more syntax and flexibility

Contributing
------------

See [CONTRIBUTING.md](CONTRIBUTING.md) file.

License
-------

Copyright (c) 2015-2019 Atlassian and others.

BSD (2-clause) licensed, see LICENSE.txt file.

[CommonMark]: https://commonmark.org/
[Markdown]: https://daringfireball.net/projects/markdown/
[commonmark.js]: https://github.com/commonmark/commonmark.js
[CommonMark Dingus]: https://spec.commonmark.org/dingus/
[Maven Central]: https://search.maven.org/#search|ga|1|g%3A%22org.commonmark%22
[Semantic Versioning]: https://semver.org/
[autolink-java]: https://github.com/robinst/autolink-java
[gfm-tables]: https://help.github.com/articles/organizing-information-with-tables/
