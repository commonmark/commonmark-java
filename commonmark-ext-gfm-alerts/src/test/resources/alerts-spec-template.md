# Alerts

## Standard types

```````````````````````````````` example alert
> [!NOTE]
> This is a note
````````````````````````````````

```````````````````````````````` example alert
> [!TIP]
> This is a tip
````````````````````````````````

```````````````````````````````` example alert
> [!IMPORTANT]
> This is important
````````````````````````````````

```````````````````````````````` example alert
> [!WARNING]
> This is a warning
````````````````````````````````

```````````````````````````````` example alert
> [!CAUTION]
> This is a caution
````````````````````````````````

## Case insensitivity

Alert type matching is case-insensitive.

```````````````````````````````` example alert
> [!note]
> Content
````````````````````````````````

```````````````````````````````` example alert
> [!Note]
> Content
````````````````````````````````

## Alert content

Marker alone in first paragraph, blank line, then content:

```````````````````````````````` example alert
> [!NOTE]
>
> Content
````````````````````````````````

Multiple paragraphs:

```````````````````````````````` example alert
> [!NOTE]
> First paragraph
>
> Second paragraph
````````````````````````````````

Inline formatting:

```````````````````````````````` example alert
> [!TIP]
> This is **bold** and *italic*
````````````````````````````````

Code block inside alert:

```````````````````````````````` example alert
> [!TIP]
> Code:
>
>     function() { }
>
> End
````````````````````````````````

List inside alert:

```````````````````````````````` example alert
> [!IMPORTANT]
> Items:
> - First item
> - Second item
````````````````````````````````

Links inside alert:

```````````````````````````````` example alert
> [!NOTE]
> Check out [this link](https://example.com) for more info
````````````````````````````````

Heading inside alert:

```````````````````````````````` example alert
> [!IMPORTANT]
> ## Heading
> Content below heading
````````````````````````````````

Empty lines in middle of alert:

```````````````````````````````` example alert
> [!NOTE]
> First
>
>
> After empty lines
````````````````````````````````

## Not an alert

Text after marker on the same line:

```````````````````````````````` example alert
> [!NOTE] Some text
````````````````````````````````

Unknown type:

```````````````````````````````` example alert
> [!INVALID]
> Some text
````````````````````````````````

Unconfigured custom type is not an alert:

```````````````````````````````` example alert
> [!INFO]
> Should be blockquote
````````````````````````````````

Marker with no content:

```````````````````````````````` example alert
> [!NOTE]
````````````````````````````````

Whitespace-only content after marker:

```````````````````````````````` example alert
> [!TIP]
>   
>  
````````````````````````````````

Extra space inside marker:

```````````````````````````````` example alert
> [! NOTE]
> Should be blockquote
````````````````````````````````

Missing brackets:

```````````````````````````````` example alert
> !NOTE
> Should be blockquote
````````````````````````````````

Missing exclamation mark:

```````````````````````````````` example alert
> [NOTE]
> Should be blockquote
````````````````````````````````

Regular blockquote is not affected:

```````````````````````````````` example alert
> This is a regular blockquote
````````````````````````````````

## Boundaries

Trailing spaces after marker:

```````````````````````````````` example alert
> [!NOTE]  
> This is a note
````````````````````````````````

Trailing tabs after marker:

```````````````````````````````` example alert
> [!WARNING]→→
> Be careful
````````````````````````````````

Leading spaces before blockquote marker:

```````````````````````````````` example alert
   > [!IMPORTANT]
   > Content
````````````````````````````````

Blank line after marker ends the blockquote (not an alert):

```````````````````````````````` example alert
> [!NOTE]

Some text
````````````````````````````````

Alert followed by blockquote:

```````````````````````````````` example alert
> [!NOTE]
> This is an alert

> This is a blockquote
````````````````````````````````

Adjacent alerts:

```````````````````````````````` example alert
> [!NOTE]
> First alert

> [!WARNING]
> Second alert
````````````````````````````````

## Nesting and containers

Nested alert inside alert renders as blockquote:

```````````````````````````````` example alert
> [!NOTE]
> This is a note
>> [!WARNING]
>> Nested content
````````````````````````````````

Nested blockquote inside alert:

```````````````````````````````` example alert
> [!NOTE]
> This is a note
>> Nested blockquote
````````````````````````````````

Alert inside list item stays as blockquote:

```````````````````````````````` example alert
- > [!NOTE]
  > Test
````````````````````````````````

Alert marker in content is treated as text:

```````````````````````````````` example alert
> [!NOTE]
> This is a note
> [!WARNING]
> This is still part of the note
````````````````````````````````

## Continuation and interruption

Lazy continuation:

```````````````````````````````` example alert
> [!NOTE]
> First line
Lazy continuation
> Continues alert
````````````````````````````````

Alert type after regular blockquote content is not an alert:

```````````````````````````````` example alert
> Regular blockquote
> [!NOTE]
> More text
````````````````````````````````