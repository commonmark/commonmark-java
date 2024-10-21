# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).
This project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html),
with the exception that 0.x versions can break between minor versions.

## [0.24.0] - 2024-10-21
### Added
- `SourceSpan` on nodes now have a `getInputIndex` to get the index within the
  original input string (in addition to the existing line/column indexes).
  This is useful when looking up the input source: It can now be done using
  `substring` instead of having to split the input into lines first (#348)
- Configurable line break rendering for `TextContentRenderer` via `lineBreakRendering`
  on the builder; e.g. `LineBreakRendering.SEPARATE_BLOCKS` will render an empty
  line between blocks (#344)
### Changed
- Adopted small changes from OpenJDK vendoring to make updates easier for them (#343)
### Fixed
- Enable overriding of built-in node rendering for `TextContentRenderer` (#346)

## [0.23.0] - 2024-09-16
### Added
- New extension for footnotes!
  - Syntax:
    ```
    Main text[^1]
    
    [^1]: Additional text in a footnote
    ```
  - Inline footnotes like `^[inline footnote]` are also supported when enabled
    via an option in `FootnotesExtension.Builder`
  - Use class `FootnotesExtension` in artifact `commonmark-ext-footnotes` (#332)
- New option `omitSingleParagraphP` in `HtmlRenderer.Builder` for not using `<p>` 
  tags for when a document only has one paragraph (#150)
- Support for custom link processing during inline parsing (e.g. `[foo]`),
  see `Parser.Builder#linkProcessor`
- Support for extending inline parsing with custom inline content parsers. See
  `Parser.Builder#customInlineContentParserFactory`. This allows users/extensions
  to hook into inline parsing on a deeper level than before (e.g. with delimiter
  processors). It can be used to add support for math/latex formulas or other inline
  syntax. (#321)
### Changed
- The default `DefaultUrlSanitizer` now also allows `data` as a protocol. Use the
  constructor with a list to customize this. (#329)
- `LinkReferenceDefinition` now extends `Block` (it was extending `Node`
  directly before)
- `MarkdownRenderer`: Don't escape `=` text if it's the first node in a block (#335)
### Fixed
- Fix parsing of link reference definitions with incorrect title syntax (followed
  by characters other than space/tab). In that case, the title was set to the
  partially-parsed title and the source spans were wrong. (#315)
- Fix source spans of blocks with lazy continuation lines (#337)
- `MarkdownRenderer`: Preserve thematic break literals (#331)

## [0.22.0] - 2024-03-15
### Added
- New `MarkdownRenderer` for rendering nodes to Markdown (CommonMark) (#306)!
  Note that while care is taken to produce equivalent Markdown, some differences
  in the original Markdown (if parsed) are not preserved, such as:
  - The type of heading used
  - The type of link used (reference links will be rendered as inline links)
  - Whether special characters are escaped or not
  - Leading and trailing whitespace
- Modular JAR (JPMS): All artifacts now include module descriptors (module-info)
  so jlink can be used; the old `Automatic-Module-Name` manifest entries were removed
- New package `org.commonmark.parser.beta` containing classes that are not part of
  the stable API but are exported from the module because they might be useful for
  extension parsers
- New package `org.commonmark.text` for text related utilities that are useful for
  both parsing and rendering
- `TableCell` now has `getWidth` returning the number of dash and colon characters
  in the delimiter row, useful for rendering proportional width tables (#296)
- `ThematicBreak` now has `getLiteral` containing the string that was used in the
  source when parsing (#309)
- `ListItem` now has `getMarkerIndent` and `getContentIndent` for retrieving the
  space between the start of the line and the marker/content
- Deprecated a some properties of `BulletList`, `OrderedList`, `FencedCodeBlock`
  and replaced with nullable ones because they might not be set when constructing
  these nodes manually instead of via parsing
### Changed
- Java 11 or later is now required (dropping support for Java 8)
- Update to CommonMark spec 0.31.2
### Fixed
- Fix `LinkReferenceDefinition` having null `SourceSpan` when title is present
  and parsing with source spans option enabled (#310)

## [0.21.0] - 2022-11-17
### Added
- GitHub strikethrough: With the previous version we adjusted the
  extension to also accept the single tilde syntax. But if you use
  another extension that uses the single tilde syntax, you will get a
  conflict. To avoid that, `StrikethroughExtension` can now be
  configured to require two tildes like before, see Javadoc.

## [0.20.0] - 2022-10-20
### Fixed
- GitHub tables: A single pipe (optional whitespace) now ends a table
  instead of crashing or being treated as an empty row, for consistency
  with GitHub (#255).
- GitHub strikethrough: A single tilde now also works, and more than two
  tildes are not accepted anymore. This brings us in line with what
  GitHub actually does, which is a bit underspecified (#267)
- The autolink extension now handles source spans correctly (#209)

## [0.19.0] - 2022-06-02
### Added
- YAML front matter extension: Limited support for single and double
  quoted string values (#260)
### Changed
- Check argument of `enabledBlockTypes` when building parser instead of NPEing later

## [0.18.2] - 2022-02-24
### Changed
- Test against Java 17
- Bundle LICENSE.txt with artifacts (in addition to Maven metadata)

## [0.18.1] - 2021-11-29
### Fixed
- Fix tables with leading/trailing header pipes and trailing spaces (#244).
  This was a regression in 0.16.1 which is now fixed.

## [0.18.0] - 2021-06-30
### Changed
- Update to CommonMark spec 0.30:
  - Add `textarea` to list of literal HTML block tags.
    Like `script`, `style`, and `pre`, `textarea` blocks can contain
    blank lines without the contents being interpreted as commonmark.
  - Fix case folding for link reference labels in some cases
    (e.g. `ẞ` and `SS` should match)
  - Allow lowercase ASCII in HTML declaration
  - Don't let type 7 HTML blocks interrupt lazy paragraphs either
- Preserve the original case for the label of `LinkReferenceDefinition`.
  Before, we used to store the normalized version (lowercase, collapsed whitespace).

## [0.17.2] - 2021-05-14
### Changed
- Pass original instead of normalized label to `InlineParserContext` for lookup (#204).
  This allows custom contexts to change the lookup logic and have access to the original
  label content.
  In case you have a custom implementation of `InlineParserContext`, you might need to adjust
  it to do normalization.

## [0.17.1] - 2021-02-03
### Fixed
- Fix emphasis surrounded by non-BMP punctuation/whitespace characters
  (characters that are longer than one UTF-16 "char"). Note that this is
  an edge case with rarely used Unicode characters, which a lot of other
  implementations don't handle correctly.
- Fix tables where the row starts with spaces and then the first `|` -
  rows that didn't have spaces before were not affected (#199). This bug
  is present in 0.16.1 and 0.17.0.

## [0.17.0] - 2021-01-15
### Changed
- **ACTION REQUIRED**: Maven groupId has changed from `com.atlassian.commonmark` to `org.commonmark`
  - To continue getting new versions of commonmark-java, change the Maven coordinates in your dependencies:
  - Old: `<groupId>com.atlassian.commonmark</groupId>`
  - New: `<groupId>org.commonmark</groupId>`

## [0.16.1] - 2020-12-11
### Added
- Support for including source spans on block and inline nodes (#1):
  - Answer for "Where in the source input (line/column position and length) does this node come from?"
  - Useful for things like editors that want to keep the input and rendered output scrolled to the same lines,
    or start editing on the node that was selected.
  - Use `includeSourceSpans` on `Parser.Builder` to enable,
    either with `IncludeSourceSpans.BLOCKS` or `IncludeSourceSpans.BLOCKS_AND_INLINES`
  - Read data with `Node.getSourceSpans`
  - Note that enabling this has a small performance impact on parsing (about 10%)
### Changed
- In order to support source spans (see above), a few of the extension
  APIs changed. It should only affect users implementing their own
  extensions. See the Javadoc to see what changed.
- YAML front matter extension: Support dots in key names

## [0.15.2] - 2020-07-20
### Fixed
- image-attributes extension: Fix unexpected altering of text in case
  parsing of attributes fails, e.g. `{NN} text` -> `{NN text}`, thanks @jk1

## [0.15.1] - 2020-05-29
### Added
- Add text content rendering support for `InsExtension`

## [0.15.0] - 2020-05-21
### Added
- Extension for width/height attributes for images, thanks @dohertyfjatl
  - Syntax: `![text](/url.png){width=640 height=480}`
  - Use class `ImageAttributesExtension` in artifact `commonmark-ext-image-attributes`
- Extension for task lists (GitHub-style), thanks @dohertyfjatl
  - Syntax:
    ```
    - [x] task #1
    - [ ] task #2
    ```
  - Use class `TaskListItemsExtension` in artifact `commonmark-ext-task-list-items`

## [0.14.0] - 2020-01-22
### Added
- Add `sanitizeUrls` to `HtmlRenderer.Builder` to enable sanitizing URLs
  of `<a>` and `<img>` tags. Sanitizing logic can be customized via
  `urlSanitizer`. Thanks @VandorpeDavid

## [0.13.1] - 2019-11-25
### Fixed
- Fix potential `StackOverflowError` for regular expressions used in the
  inline parser (e.g. when parsing long HTML), thanks @lehvolk

## [0.13.0] - 2019-07-15
### Added
- `LinkReferenceDefinition` nodes are now part of the document (not
  rendered by default).
- `InlineParserContext.getLinkReferenceDefinition` was added to allow
  custom inline parsers to look up definitions for reference links.
### Changed
- Performance improvements compared to previous version:
  - Parsing 7-10% faster
  - HTML rendering 105% faster - or in other words, twice as fast!
- Update to CommonMark spec 0.29 (#156):
  - Change how newlines/spaces are handled in inline code
  - Info strings for tilde code blocks can contain backticks and tildes
  - Allow spaces inside link destinations in pointy brackets
  - Disallow link destination beginning with `<` unless it is inside `<..>`
  - Disallow unescaped '(' in link title in parens
  - Disallow indenting list item marker by more than 3 spaces
  - No longer treat `<meta>` as a block tag
  - Link reference definitions can now be in setext headings too
- Tables extension: Changes to match GitHub implementation:
  - Escaping now only considers pipe characters when parsing tables:
    `\|` results in a literal `|` instead of a column, everything else
    is passed through to inline parsing.
  - Table body can now contain lazy continuation lines (without `|`).
    An empty line or another block is needed to interrupt the table.
  - For tables without a body, `<tbody>` is no longer rendered in HTML
  - See https://github.github.com/gfm/#tables-extension- for details
- Check non-null arguments early and provide a nicer message
### Fixed
- Fix incorrectly preserving HTML entities when rendering attributes
- Fix pathological case with input `[\\\\...` (a lot of backslashes)
- Fix pathological case with input `[]([]([](...`

## [0.12.1] - 2018-11-13
### Changed
- Speed up parsing significantly: Compared to the previous version, the
  benchmarks show up to 55% faster parsing for both small and large
  documents! (#137, #140)
- Parse backslash followed by unescapable character the same way as
  the reference implementations.
- Build and test on Java 11 as well.
- autolink: Stop URLs at " and \` as well
### Fixed
- Fix tab handling in ATX and Setext headings.

## [0.11.0] - 2018-01-17
### Added
- The extension for tables now also renders to plain text
  (when using a `TextContentRenderer`), thanks @ahjaworski
### Changed
- Add `Automatic-Module-Name` manifest entries so that library can be used
  nicely in Java 9 modules. The module names correspond to the root
  package name: `org.commonmark`, `org.commonmark.ext.autolink`, etc.
- Java 7 is now only supported on a best-effort basis (but it has been
  EOL for quite some time, so yeah)

## [0.10.0] - 2017-09-14
### Added
- Support multiple `DelimiterProcessor` with the same delimiter char as long
  as they have different length, thanks @szeiger
- Add tests for thread-safety and a section to the readme (#83)
### Changed
- Update to CommonMark spec 0.28 (#94):
  - Adapt to changed emphasis parsing rule
  - Allow nested parentheses in inline link destinations
### Fixed
- Fixes for text content rendering, thanks @JinneeJ:
  - Support for mixed lists
  - Fixed that whitespaces between text elements are removed in "stripped" mode.
    For example `**text** and text` had rendered as `textand text`
  - Improved rendering for auto links
- Fix `[\]` being parsed as link label
- Fix `[foo](<\>)` resulting in `\` in href
- Fix multiple of 3 rule for emphasis parsing (see commonmark/cmark#177)
- Fix text node merging when opening/closing delimiters are adjacent (#96)
- autolink: Fix linking of URLs without host, e.g. `http://.` (#99)

## [0.9.0] - 2017-03-03
### Added
- Support restricting which block types are parsed, see `enabledBlockTypes`
  method on `Parser.Builder` (#43), thanks @marksliva, @pivotal-graham-bell and
  @lalunamel. This allows you to disable parsing of e.g. headings, they will
  just be parsed as paragraphs instead.
- Allow customizing the inline parser, see `inlineParserFactory` method on
  `Parser.Builder` (#68), thanks @vreynolds and @lalunamel. Note that this is
  experimental and currently requires using internal classes.
### Changed
- Wrap escaped HTML blocks in a `<p>` tag (#78)
- Add missing `ext-heading-anchor` to `dependencyManagement` in parent pom,
  thanks @drobert 

## [0.8.0] - 2016-12-09
### Changed
- Update to CommonMark spec 0.27 (#73):
  - Treat h2..h6 as HTML blocks well
  - Allow shortcut reference link before open parenthesis (if parenthesis is not
    part of a valid inline link)
- `AttributeProvider.setAttributes` now has an additional `tagName` argument and
  is called for all HTML tags of a block. This allows users to add attributes
  for the `pre` tag of a code block in addition to `code`. Also added attribute
  provider support for additional HTML tags, namely `em`, `strong`, `code` and
  `br`. (#74)
### Fixed
- ext-heading-anchor: Fix IllegalArgumentException on Android (#71)

## [0.7.1] - 2016-10-05
### Added
- Allow to configure prefix/suffix for ID on `HeadingAnchorExtension` (#66),
  thanks @paulthom12345

## [0.7.0] - 2016-09-23
### Added
- Plain text content renderer (#58), thanks to @JinneeJ!
  - Renders a plain text representation of a document instead of HTML, see
    `TextContentRenderer` in core.
  - Extensible in the same way as HTML rendering.
- Heading anchor extension (#26), thanks to @paulthom12345!
  - Adds "id" attribute to heading tags (e.g. `<h1 id="heading">Heading</h1>`),
    useful for linking to sections of a document.
  - ID generation logic can also be used by itself via the `IdGenerator` class.
  - Use class `HeadingAnchorExtension` in artifact `commonmark-ext-heading-anchor`
- Ins (underline) extension (#54), thanks to @pabranch!
  - Enables underlining of text by enclosing it in `++`. It's rendered as an
    `ins` tag in HTML.
  - Use class `InsExtension` in artifact `commonmark-ext-ins`.
### Changed
- `HtmlRenderer` and related classes moved from `org.commonmark.html` to
  `org.commonmark.renderer.html`
- `HtmlRenderer.Builder` no longer takes an `AttributeProvider`, but uses a
  `AttributeProviderFactory` to instantiate a new provider for each rendering.
  Code needs to be changed to create a factory and then return the existing
  provider from its `create` method, similar to node renderers.
- `NodeRendererFactory` was renamed to `HtmlNodeRendererFactory`, same for
  related classes (there's a corresponsing interface for text content rendering)

## [0.6.0] - 2016-07-25
### Added
- Add coverage data to build. Currently at 97 %.
### Changed
- Update to CommonMark spec 0.26 (#55)
  - empty list items can no longer interrupt a paragraph; this resolves an
    ambiguity with setext headers
  - ordered lists can interrupt a paragraph only when beginning with 1
  - the two-blank-lines-breaks-out-of-lists rule has been removed
  - the spec for emphasis and strong emphasis has been refined to give more
    intuitive results in some cases
  - tabs can be used after the # in an ATX header and between the markers in a
    thematic break
- Simplify and speed up brackets processing (links/images)
  - Improves the nested brackets pathological case (e.g. `[[[[a]]]]` with a lot
    of brackets)
  - Also contributed these changes upstream to
    [commonmark.js](https://talk.commonmark.org/t/ann-commonmark-0-26-cmark-0-26-0-commonmark-js-0-26-0/2165)
- Simplify merging of adjacent text nodes
- Extended `DelimiterProcessor` interface so that implementations get more
  information in `getDelimiterUse` and can reject delimiters by returning `0`
  from it. Also rename the methods:
  - `getOpeningDelimiterChar` -> `getOpeningCharacter`
  - `getClosingDelimiterChar` -> `getClosingCharacter`
  - `getMinDelimiterCount` -> `getMinLength`
### Fixed
- Fix max length for link labels (999, not 1000)
- autolink: Stop URLs at more invalid characters, notably '<' and '>'.
  According to RFC 3987, angle brackets are not allowed in URLs, and
  other linkers don't seem to allow them either.

## [0.5.1] - 2016-05-25
### Fixed
- Fix `StringIndexOutOfBoundsException` on line after tab (#52)

## [0.5.0] - 2016-04-22
### Added
- Add YAML front matter extension for document metadata blocks (#24), thanks to
  @chiwanpark 
- Add information about delimiter character and length to delimiter nodes (#10),
  thanks to @pcj
- Make HTML rendering for nodes extensible (#35)
- Add support for asymmetric delimiters (#17):
  `DelimiterProcessor#getDelimiterChar` was split into `getOpeningDelimiterChar`
  and `getClosingDelimiterChar`
### Changed
- Make `AttributeProvider` work for image and table nodes (#31)
- Update to CommonMark spec 0.25:
  - Changes how partially consumed tabs are handled.
- Add Android test project to build so that we won't break Android support
  (#38), thanks to @JinneeJ
- Replace `CustomHtmlRenderer` with `NodeRenderer` which also allows overriding
  rendering for built-in node types (#35)
### Fixed
- Fix blank line after empty list item to terminate list
- Fix nested bullet list indented with mix of tab and spaces (#41), thanks to
  @derari 
- Fix package name in Javadoc, thanks to @jiakuan
- autolink: Treat more special characters as trailing delimiters to not include
  `">`, `"/>` and `");` at the end of URLs
- autolink: Fix unexpected link end with unfinished delimiter pairs in URLs
- autolink: Fix Android incompatibility by not using `java.util.Objects`

## [0.4.1] - 2016-02-11
### Fixed
- Fix problematic regex that doesn't work on some Java versions and Android
- Fix problems with Android (usage of `java.util.Objects`, `StandardCharsets`,
  ProGuard, see #30), thanks to @JinneeJ!
### Changed
- autolink extension: Update to autolink 0.3.0. This stops recognizing
  "abc://foo" within "1abc://foo" as a link

## [0.4.0] - 2016-01-18
### Changed
Update to CommonMark spec 0.24 (#28):
- No longer allow whitespace between link text and link label
- Don't allow whitespace in link destination even with <>
- Don't use whitelist for schemes in autolinks, recognize all 2-32 length
  schemes (see [spec](http://spec.commonmark.org/0.24/#scheme))
- Allow multi-line content in setext headings

API breaking changes (caused by changes in spec):
- Rename `Header` to `Heading`
- Rename `HorizontalRule` to `ThematicBreak`
- Rename `HtmlTag` to `HtmlInline`
- Replace `MatchedBlockParser#getParagraphStartLine` with `#getParagraphContent`
  that returns the current content if the matched block is a paragraph

## [0.3.2] - 2016-01-07
### Fixed
- Add more bounds checks to internal Substring class (might affect extensions)

## [0.3.1] - 2015-12-01
### Fixed
-  Fix StringIndexOutOfBoundsException with unclosed inline link (#27)

## [0.3.0] - 2015-10-15
### Changed
- Update to spec 0.22 (#14)
- Allow block parsers from extensions to override core behavior (#18)
- Fix compilation without `install` (#19)
- Parent pom, build and README updates

## [0.2.0] - 2015-08-20
### Added
- Add method `Node parseReader(java.io.Reader)` to `Parser` (#2)
- Extend Javadoc and publish online (#4)
### Fixed
- Fix StringIndexOutOfBoundsException on some inputs (#13)
- ext-gfm-tables: Implement single-column tables (#7)

## [0.1.0] - 2015-07-22
### Added
Initial release of commonmark-java, a port of commonmark.js with extensions
for autolinking URLs, GitHub flavored strikethrough and tables.

[0.24.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.23.0...commonmark-parent-0.24.0
[0.23.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.22.0...commonmark-parent-0.23.0
[0.22.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.21.0...commonmark-parent-0.22.0
[0.21.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.20.0...commonmark-parent-0.21.0
[0.20.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.19.0...commonmark-parent-0.20.0
[0.19.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.18.2...commonmark-parent-0.19.0
[0.18.2]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.18.1...commonmark-parent-0.18.2
[0.18.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.18.0...commonmark-parent-0.18.1
[0.18.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.17.2...commonmark-parent-0.18.0
[0.17.2]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.17.1...commonmark-parent-0.17.2
[0.17.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.17.0...commonmark-parent-0.17.1
[0.17.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.16.1...commonmark-parent-0.17.0
[0.16.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.15.2...commonmark-parent-0.16.1
[0.15.2]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.15.1...commonmark-parent-0.15.2
[0.15.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.15.0...commonmark-parent-0.15.1
[0.15.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.14.0...commonmark-parent-0.15.0
[0.14.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.13.1...commonmark-parent-0.14.0
[0.13.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.13.0...commonmark-parent-0.13.1
[0.13.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.12.1...commonmark-parent-0.13.0
[0.12.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.11.0...commonmark-parent-0.12.1
[0.11.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.10.0...commonmark-parent-0.11.0
[0.10.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.9.0...commonmark-parent-0.10.0
[0.9.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.8.0...commonmark-parent-0.9.0
[0.8.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.7.1...commonmark-parent-0.8.0
[0.7.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.7.0...commonmark-parent-0.7.1
[0.7.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.6.0...commonmark-parent-0.7.0
[0.6.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.5.1...commonmark-parent-0.6.0
[0.5.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.5.0...commonmark-parent-0.5.1
[0.5.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.4.1...commonmark-parent-0.5.0
[0.4.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.4.0...commonmark-parent-0.4.1
[0.4.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.3.2...commonmark-parent-0.4.0
[0.3.2]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.3.1...commonmark-parent-0.3.2
[0.3.1]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.3.0...commonmark-parent-0.3.1
[0.3.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.2.0...commonmark-parent-0.3.0
[0.2.0]: https://github.com/commonmark/commonmark-java/compare/commonmark-parent-0.1.0...commonmark-parent-0.2.0
[0.1.0]: https://github.com/commonmark/commonmark-java/commits/commonmark-parent-0.1.0
