commonmark-java
===============

Java implementation of [CommonMark], a specification of the [Markdown] format for turning plain text into formatted text.
Parses input to an AST (tree of nodes) and then renders to HTML.

This started out as a port of [commonmark.js] and has evolved into a full
library with a nice Java API and some optional extensions. Features:

* Small with minimal dependencies
* Extensible (see below)
* Fast (10-20 times faster than pegdown, see benchmarks in repo)

Requirements:

* Java 7 or above
* The core has no dependencies; for extensions, see below

Coordinates for core library (see all on [Maven Central]):

```xml
<dependency>
    <groupId>com.atlassian.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.1.0</version>
</dependency>
```

Note that for 0.x releases of this library, the API is not considered stable
yet and may break between minor releases. After 1.0, [Semantic Versioning] will
be followed.

[![Build status](https://travis-ci.org/atlassian/commonmark-java.svg?branch=master)](https://travis-ci.org/atlassian/commonmark-java)


Usage
-----

#### Parse and render to HTML

```java
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;

Parser parser = Parser.builder().build();
Node document = parser.parse("This is *Sparta*");
HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
```

This uses the parser and renderer with default options, except for escaping raw
HTML tags and blocks. For all the available options, see other methods on the
builder objects.

Note that this library doesn't try to sanitize HTML; that is the responsibility
of the caller.

#### Use a visitor to process parsed nodes

```java
Node node = parser.parse("...");
MyVisitor visitor = new MyVisitor();
node.accept(visitor);

class MyVisitor extends AbstractVisitor {
    @Override
    public void visit(Paragraph paragraph) {
        // Do something with paragraph (override other methods for other nodes):
        System.out.println(paragraph);
        // Descend into children:
        visitChildren(paragraph);
    }
}
```

### API documentation

Javadocs are available online on
[javadoc.io](http://www.javadoc.io/doc/com.atlassian.commonmark/commonmark).


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
    <groupId>com.atlassian.commonmark</groupId>
    <artifactId>commonmark-ext-gfm-tables</artifactId>
    <version>0.1.0</version>
</dependency>
```

Then, configure the extension on the builders:

```java
import org.commonmark.ext.gfm.tables.TablesExtension;

List<Extension> extensions = Arrays.asList(TablesExtension.create());
Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
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


Contributing
------------

Pull requests, issues and comments welcome ☺. For pull requests:

* Add tests for new features and bug fixes
* Follow the existing style (always use braces, 4 space indent)
* Separate unrelated changes into multiple pull requests

See the existing "help wanted" issues for things to start contributing.

For bigger changes, make sure you start a discussion first by creating
an issue and explaining the intended change.


License
-------

Copyright (c) 2015 Atlassian and others.

BSD (2-clause) licensed, see LICENSE.txt file.

[CommonMark]: http://commonmark.org/
[Markdown]: https://daringfireball.net/projects/markdown/
[commonmark.js]: https://github.com/jgm/commonmark.js
[Maven Central]: https://search.maven.org/#search|ga|1|g%3A%22com.atlassian.commonmark%22
[Semantic Versioning]: http://semver.org/
[autolink-java]: https://github.com/robinst/autolink-java
[gfm-tables]: https://help.github.com/articles/github-flavored-markdown/#tables
