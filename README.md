commonmark-android
===============

Android implementation of [CommonMark], a specification of the [Markdown] format for turning plain text into formatted text.

This library is based on [commonmark-java].
Added support for Android and rendering text as Spannable.

Requirements:

* Android SDK 9 or above

Usage
-----

#### SpannableRenderer

Render document to `SpannableString`.

```java
Parser parser = Parser.builder().extensions(extensions).build();
Node document = parser.parse("I'm a **bold string**");

Resources res = TeamCom.getContext().getResources();
SpannableRenderer renderer = SpannableRenderer.builder().quoteStripeColorResId(R.color.primary).build();
CharSequence formattedString = renderer.render(res, document);
```

The example above set the specified in this library spannables for each type of markdown.
It's possible to implement `org.commonmark.spannable.SpannableFactory` with custom spannables.

```java
Parser parser = Parser.builder().build();
SpannableRenderer renderer = SpannableRenderer.builder().factory(new SpannableFactory() {
    @Override
    public Object getSpan(int type) {
         switch (type) {
              case TYPE_BOLD:
              // return custom spannable for bold. The same for other types.
         }
    }
    @Override
    public Object getLinkSpan(String url) {
        // return custom spannable for links.
    }
}).build();
```

#### CleanRenderer

Render document without any formatting.

```java
Parser parser = Parser.builder().extensions(extensions).build();
Node document = parser.parse("I'm a **clean** string");

CleanRenderer renderer = new CleanRenderer();
CharSequence cleanedString = renderer.render(document);
```


#### Extensions

There were added 2 extensions:

- `AutolinkExtension` supports parsing links
- `ReplacementExtension` takes the map and use it to replace keys by values

```java
List<Extension> extensions = new ArrayList<>();

extensions.add(ReplacementExtension.create(replacementMap));
extensions.add(AutolinkExtension.create());

Parser parser = Parser.builder().extensions(extensions).build();
Node document = parser.parse(string);
```

License
-------

Copyright (c) 2015 Atlassian and others.

BSD (2-clause) licensed, see LICENSE.txt file.

[CommonMark]: http://commonmark.org/
[Markdown]: https://daringfireball.net/projects/markdown/
[commonmark.js]: https://github.com/jgm/commonmark.js
[commonmark-java]: https://github.com/atlassian/commonmark-java
