# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).
This project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html),
with the exception that 0.x versions can break between minor versions.

## Unreleased
### Changed
- Add `Automatic-Module-Name` manifest entries so that library can be used
  nicely in Java 9 modules. The module names correspond to the root
  package name: `org.commonmark`, `org.commonmark.ext.autolink`, etc.

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
  that returns the current content if the the matched block is a paragraph

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


[0.10.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.9.0...commonmark-parent-0.10.0
[0.9.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.8.0...commonmark-parent-0.9.0
[0.8.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.7.1...commonmark-parent-0.8.0
[0.7.1]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.7.0...commonmark-parent-0.7.1
[0.7.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.6.0...commonmark-parent-0.7.0
[0.6.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.5.1...commonmark-parent-0.6.0
[0.5.1]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.5.0...commonmark-parent-0.5.1
[0.5.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.4.1...commonmark-parent-0.5.0
[0.4.1]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.4.0...commonmark-parent-0.4.1
[0.4.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.3.2...commonmark-parent-0.4.0
[0.3.2]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.3.1...commonmark-parent-0.3.2
[0.3.1]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.3.0...commonmark-parent-0.3.1
[0.3.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.2.0...commonmark-parent-0.3.0
[0.2.0]: https://github.com/atlassian/commonmark-java/compare/commonmark-parent-0.1.0...commonmark-parent-0.2.0
[0.1.0]: https://github.com/atlassian/commonmark-java/commits/commonmark-parent-0.1.0
