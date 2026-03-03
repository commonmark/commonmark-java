# commonmark-ext-gfm-alerts

Extension for [commonmark-java](https://github.com/commonmark/commonmark-java) that adds support for [GitHub Flavored Markdown alerts](https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#alerts).

Enables highlighting important information using blockquote syntax with five standard alert types: NOTE, TIP, IMPORTANT, WARNING, and CAUTION.

## Usage

#### Markdown Syntax

```markdown
> [!NOTE]
> Useful information

> [!WARNING]
> Critical information
```

#### Standard GFM Types

```java
Extension extension = AlertsExtension.create();
Parser parser = Parser.builder().extensions(List.of(extension)).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(List.of(extension)).build();
```

#### Custom Alert Types

Add custom types beyond the five standard GFM types:

```java
Extension extension = AlertsExtension.builder()
        .addCustomType("INFO", "Information")
        .build();
```

Custom types must be UPPERCASE and cannot override standard types.

#### Styling

Alerts render as `<div>` elements with CSS classes:

```html
<div class="markdown-alert markdown-alert-note" data-alert-type="note">
  <p class="markdown-alert-title">Note</p>
  <p>Content</p>
</div>
```

Basic CSS example:

```css
.markdown-alert {
    padding: 0.5rem 1rem;
    margin-bottom: 1rem;
    border-left: 4px solid;
}

.markdown-alert-note { border-color: #0969da; background-color: #ddf4ff; }
.markdown-alert-tip { border-color: #1a7f37; background-color: #dcffe4; }
.markdown-alert-important { border-color: #8250df; background-color: #f6f0ff; }
.markdown-alert-warning { border-color: #9a6700; background-color: #fff8c5; }
.markdown-alert-caution { border-color: #cf222e; background-color: #ffebe9; }
```

Icons can be added using CSS `::before` pseudo-elements with GitHub's [Octicons](https://primer.style/octicons/) (info, light-bulb, report, alert, stop icons).

## License

See the main commonmark-java project for license information.
