# Htmlunit-NekoHtml Parser

[![Maven Central Version](https://img.shields.io/maven-central/v/org.htmlunit/neko-htmlunit)](https://central.sonatype.com/artifact/org.htmlunit/neko-htmlunit)
[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnit+-+Neko)](https://jenkins.wetator.org/view/HtmlUnit/job/HtmlUnit%20-%20Neko/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)

The **Htmlunit-NekoHtml** Parser is a HTML scanner and tag balancer that enables application programmers to parse HTML documents
and access the information using standard XML interfaces.  
The parser can scan HTML files and "fix up" many common mistakes that human (and computer) authors make in writing HTML documents.  
NekoHTML adds missing parent elements; automatically closes elements with optional end tags; and can handle mismatched inline element tags.

## Key Features

✅ **Error Tolerant** - Handles malformed HTML gracefully  
✅ **Standards Compliant** - Follows HTML parsing specifications  
✅ **Well Tested** - Over 10,000 test cases  
✅ **No External Dependencies** - Pure Java implementation  
✅ **Java 17 Compatible** - Works with Java 17, 21 and beyond  
✅ **Android Support** - Runs on Android platforms  

The **Htmlunit-NekoHtml** Parser is used by [HtmlUnit](https://htmlunit.sourceforge.io/).

:heart: [Sponsor this project](https://github.com/sponsors/rbri)

## Contents

- [Get it!](#get-it)
- [HowTo use](#howto-use) (DOMParser, SAXParser)
- [Features](#features)
  - [Limits of the tag balancer](#limits-of-the-tag-balancer)
  - [Choosing the self-closing flags](#choosing-the-self-closing-flags)
  - [Security considerations](#security-considerations)
- [Properties](#properties)
- [Upgrade notes within 5.x](#upgrade-notes-within-5x)
- [Migrating from 4.x to 5.x](#migrating-from-4x-to-5x) / [3.x to 4.x](#migrating-from-3x-to-4x) / [2.x to 3.x](#migrating-from-2x-to-3x)
- [Development](#start-nekohtml-development)
- [History](#history)

### Project News

**[Developer Blog](https://htmlunit.github.io/htmlunit-blog/)**

[HtmlUnit@mastodon](https://fosstodon.org/@HtmlUnit) | [HtmlUnit@bsky](https://bsky.app/profile/htmlunit.bsky.social) | [HtmlUnit@Twitter](https://twitter.com/HtmlUnit)

#### Version 5

Starting with version 5.0.0, **JDK 17 or higher is required**.  
If you are still on JDK 8, see [Legacy Support (JDK 8)](#legacy-support-jdk-8) below.


#### Important behavior changes since 5.5.0

The handling of self-closing tags (`<iframe/>`, `<script/>`, `<textarea/>`, ...) and of NUL characters was
tightened to match the HTML5 parsing rules. This also applies to **fragment parsing**: a fragment of an HTML document
has to be parsed with the HTML defaults (the slash is ignored), a fragment of an XHTML document with all three
self-closing flags enabled. If you set `ALLOW_SELFCLOSING_TAGS`, parse XHTML or parse fragments, please read
[Choosing the self-closing flags](#choosing-the-self-closing-flags) and the
[upgrade notes](#upgrade-notes-within-5x).

### Latest release Version 5.6.0 / October 11, 2026

##### Security Advisories

[CVE-2026-104828](https://nvd.nist.gov/vuln/detail/CVE-2026-104828): Fixed in versions 5.5.5+  
Htmlunit-NekoHtml Parser suffers from a mutation XSS (mXSS) vulnerability on versions below 5.5.5.
`HTMLWriterFilter` wrote the content of `<textarea>` and `<title>` without entity encoding
because both elements carry the `SPECIAL` flag in `HTMLElements`. As these elements contain escapable raw text,
the parser decodes entity references inside them, and the decoded content was written back raw.
A parse-serialize round trip therefore turned entity-encoded input into live HTML markup.

[CVE-2022-29546](https://nvd.nist.gov/vuln/detail/CVE-2022-29546): Fixed in versions 2.61.0+  
Htmlunit-NekoHtml Parser suffers from a denial of service vulnerability on versions 2.60.0 and below.
A specifically crafted input regarding the parsing of processing instructions leads to heap memory consumption.

[CVE-2022-28366](https://nvd.nist.gov/vuln/detail/CVE-2022-28366): Fixed in versions 2.27+  
Htmlunit-NekoHtml Parser suffers from a denial of service via crafted Processing Instruction vulnerability on versions 2.26 and below.

## Get it!

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.htmlunit</groupId>
    <artifactId>neko-htmlunit</artifactId>
    <version>5.6.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.htmlunit', name: 'neko-htmlunit', version: '5.6.0'
```

## HowTo use

### DOMParser

The DOMParser can be used together with the simple build in DOM implementation or with your own.

    final String html =
                " <!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "<h1>NekoHtml</h1>\n"
                + "</body>\n"
                + "</html>";

    final StringReader sr = new StringReader(html);
    final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);

    // use the provided simple DocumentImpl
    final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
    parser.parse(in);

    HTMLDocumentImpl doc = (HTMLDocumentImpl) parser.getDocument();
    NodeList headings = doc.getElementsByTagName("h1");

### SAXParser

Using the SAXParser is straigtforward - simple provide your own org.xml.sax.ContentHandler implementation.

    final String html =
                " <!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "<h1>NekoHtml</h1>\n"
                + "</body>\n"
                + "</html>";

    final StringReader sr = new StringReader(html);
    final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);

    final SAXParser parser = new SAXParser();

    ContentHandler myContentHandler = new MyContentHandler();
    parser.setContentHandler(myContentHandler);

    parser.parse(in);


### Features

The behavior of the scanner/parser can be influenced via a series of feature switches that control how the parser handles various HTML constructs and edge cases.

```java
parser.setFeature(HTMLScanner.PLAIN_ATTRIBUTE_VALUES, true);
```

#### General Processing Features

| Feature | Default | Description |
|---------|---------|-------------|
| **AUGMENTATIONS** | `false` | Include infoset augmentations in the parsing output. When enabled, provides additional metadata about the parsed elements including location information (line numbers, column numbers, character offsets). |
| **REPORT_ERRORS** | `false` | Enable detailed error reporting during parsing. When enabled, the parser will report syntax errors, malformed markup, and other parsing issues through the configured error reporter. |

#### Script and Style Processing

| Feature | Default | Description |
|---------|---------|-------------|
| **SCRIPT_STRIP_COMMENT_DELIMS** | `false` | Automatically strip HTML comment delimiters (`<!--` and `-->`) from `<script>` tag contents. Useful for handling legacy JavaScript wrapped in HTML comments. |
| **SCRIPT_STRIP_CDATA_DELIMS** | `false` | Strip XHTML CDATA delimiters (`<![CDATA[` and `]]>`) from `<script>` tag contents. Enables clean processing of XHTML-style JavaScript sections. |
| **STYLE_STRIP_COMMENT_DELIMS** | `false` | Strip HTML comment delimiters (`<!--` and `-->`) from `<style>` tag contents. Handles CSS code wrapped in HTML comments for backward compatibility. |
| **STYLE_STRIP_CDATA_DELIMS** | `false` | Strip XHTML CDATA delimiters (`<![CDATA[` and `]]>`) from `<style>` tag contents. Processes XHTML-style CSS sections cleanly. |

#### Character Encoding Features

| Feature | Default | Description |
|---------|---------|-------------|
| **IGNORE_SPECIFIED_CHARSET** | `false` | Ignore charset specifications found in `<meta http-equiv='Content-Type' content='text/html;charset=...'>` tags or `<?xml ... encoding='...'>` processing instructions. Forces the parser to use the default or manually specified encoding. |

#### CDATA and Comment Processing

| Feature | Default | Description |
|---------|---------|-------------|
| **CDATA_SECTIONS** | `false` | **Controls how CDATA sections (`<![CDATA[...]]>`) are processed:**<br/><br/>**When `true` (XML-style processing):**<br/>• CDATA sections follow strict XML parsing rules<br/>• Fires three distinct events: `startCDATA()`, `characters()`, `endCDATA()`<br/>• CDATA delimiters are NOT included in character content<br/>• Only properly closed sections (ending with `]]>`) are recognized<br/><br/>**When `false` (HTML-style processing - default):**<br/>• CDATA sections are treated as HTML comments<br/>• Only a single `comment()` event is fired<br/>• Opening delimiter `[CDATA[` is included in comment content<br/>• More lenient parsing for malformed sections |
| **CDATA_EARLY_CLOSING** | `true` | Allow CDATA sections to be closed by a single `>` character, following HTML specification behavior. When disabled, CDATA sections must be properly closed with `]]>`. |

#### Document Structure Features

| Feature | Default | Description |
|---------|---------|-------------|
| **OVERRIDE_DOCTYPE** | `false` | Override any existing DOCTYPE declaration with the values specified in the `DOCTYPE_PUBID` and `DOCTYPE_SYSID` properties. Useful for forcing a specific document type. |
| **INSERT_DOCTYPE** | `false` | Automatically insert a DOCTYPE declaration if none is present in the document. Uses the values from `DOCTYPE_PUBID` and `DOCTYPE_SYSID` properties. |

#### Tag and Element Processing

| Feature | Default | Description |
|---------|---------|-------------|
| **PARSE_NOSCRIPT_CONTENT** | `true` | Parse the content within `<noscript>...</noscript>` tags as regular HTML markup. When disabled, noscript content is treated as raw text up to `</noscript>` (like a browser with scripting enabled). A self-closed `<noscript/>` is only treated as an empty element if `ALLOW_SELFCLOSING_TAGS` is enabled. |
| **ALLOW_SELFCLOSING_IFRAME** | `false` | Honor the trailing slash on `<iframe/>`. When `false` (default) the slash is ignored as in HTML5: `<iframe/>` opens an element and everything up to `</iframe>` is raw text, never markup. When `true` the element is empty and the following content is parsed as markup. Independent of `ALLOW_SELFCLOSING_TAGS`. **Do not enable for untrusted HTML**, see [Security considerations](#security-considerations). |
| **ALLOW_SELFCLOSING_SCRIPT** | `false` | Honor the trailing slash on `<script/>`. When `false` (default) the slash is ignored as in HTML5 and everything up to `</script>` is script text. When `true` the element is empty and the following content is parsed as markup. Independent of `ALLOW_SELFCLOSING_TAGS`. **Do not enable for untrusted HTML**, see [Security considerations](#security-considerations). |
| **ALLOW_SELFCLOSING_TAGS** | `false` | Honor the XML-style trailing slash on all elements except `iframe` and `script` (e.g., `<div/>`, `<p/>`). This includes the elements with raw text or RCDATA content: a self-closed `noembed`, `noframes`, `noscript`, `plaintext`, `title`, `textarea`, `style` or `xmp` is an empty element and does not swallow the rest of the document. `iframe` and `script` are controlled by their own flags above; this flag does not close them. |

#### Attribute Processing

| Feature | Default | Description |
|---------|---------|-------------|
| **NORMALIZE_ATTRIBUTES** | `false` | Normalize attribute values by collapsing consecutive whitespace characters into single spaces and trimming leading/trailing whitespace. Follows XML attribute value normalization rules. |
| **PLAIN_ATTRIBUTE_VALUES** | `false` | Store both the normalized and original (plain) attribute values. When enabled, provides access to attribute values exactly as they appear in the source, before any entity resolution or normalization. |

#### Usage Examples

```java
// Enable comprehensive error reporting and augmentations
parser.setFeature(HTMLScanner.REPORT_ERRORS, true);
parser.setFeature(HTMLScanner.AUGMENTATIONS, true);

// Process CDATA sections as proper XML constructs
parser.setFeature(HTMLScanner.CDATA_SECTIONS, true);

// Handle legacy script/style sections with HTML comments
parser.setFeature(HTMLScanner.SCRIPT_STRIP_COMMENT_DELIMS, true);
parser.setFeature(HTMLScanner.STYLE_STRIP_COMMENT_DELIMS, true);

// Parse XHTML/XML input: honor the slash on every element (see "Choosing the self-closing flags")
parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);
parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_SCRIPT, true);

// Normalize and preserve original attribute values
parser.setFeature(HTMLScanner.NORMALIZE_ATTRIBUTES, true);
parser.setFeature(HTMLScanner.PLAIN_ATTRIBUTE_VALUES, true);
```

#### Limits of the tag balancer

The tag balancer repairs misnested and unclosed markup. Doing this takes searches on the stack of open elements, and a
crafted document can make these searches walk the whole stack for every single tag (quadratic run time), or can nest
elements so deep that every recursive tree walker overflows its stack. To keep parsing time linear and the generated tree
walkable, the balancer has a few **fixed limits**. They are constants in `HTMLTagBalancer` and cannot be configured.

**All limits described here were introduced in version 5.7.0.** Earlier versions have none of them.

For real documents the limits are never reached: they count **open elements** (the entries of the element stack, counted
from the innermost one), not characters or tags, and real pages are nested a few dozen levels deep. Only pathological input
(generated documents, fuzzer output, attacks) is affected, and it degrades to a well defined behavior: an element is nested
instead of closed, an end tag is ignored, or an element is not nested any deeper.

| Limit | Value | Applies to | If the limit is hit |
|-------|:-----:|------------|---------------------|
| `MAX_ADOPTION_DEPTH` | 100 | un-nesting of `<a>` and `<nobr>` | the new element is nested inside the open one (the old behavior) |
| `MAX_PARENT_SEARCH_DEPTH` | 100 | search for the required parent of table row/cell elements (`tr`, `td`, `th`, ...) | the parent is treated as missing and is synthesized |
| `MAX_CLOSE_SEARCH_DEPTH` | 100 | search for the open elements a start tag closes implicitly (`li`, `td`, `option`, ...) | the open element is not closed, the new element is nested |
| `MAX_CONTEXT_SEARCH_DEPTH` | 100 | the element an **end tag** closes; the check whether a `<table>`/`<form>` start tag is in table structure | the end tag is ignored like an end tag without start tag; the start tag is treated as not being in table structure |
| `MAX_ELEMENT_DEPTH` | 512 | nesting depth of the generated document | the element is reported as an empty element and does not nest any deeper |

All limits are inclusive of `html` and `body`, which are on the stack too. A distance of "100" means that an element is
found if there are at most 99 other open elements above it; with 100 or more it is out of reach.

##### Un-nesting of `<a>` and `<nobr>` (`MAX_ADOPTION_DEPTH`)

Like browsers, the balancer does not nest an `<a>` (or `<nobr>`) inside an open `<a>`: the open one is closed, the new one
is opened next to it and the elements in between are re-opened (a simplified version of the HTML5 *adoption agency* algorithm).

```html
<a href="1">1<div><a href="2">2</a></div>
```

If the first `<a>` is **100 or more** open elements away, nothing is un-nested and the second anchor is nested inside the first
one as before. This bounds the number of elements that are closed and re-opened (and so the number of synthesized events)
that one start tag can cause.

*Effect:* only visible for anchors separated by 100 or more unclosed elements; the result is the pre-5.x nested tree, not an error.

##### Required parents of table elements (`MAX_PARENT_SEARCH_DEPTH`)

A `<td>` needs a `<tr>`, a `<tr>` needs a `<tbody>`, and so on. If the parent is not directly on top, the balancer searches
down the stack, but not beyond the enclosing `<table>` (a nested table never sees the rows of the outer one).
The search looks at most 99 entries below the top.

```html
<table><tr> ... 100 unclosed <div> ... <td>x</td></table>
```

Here the `<tr>` is out of reach, so the balancer synthesizes a second `<tr>` (and `<tbody>`) for the cell. With up to 99 unclosed
elements in between the original `<tr>` is found and nothing is added.

*Effect:* a table cell that is buried deeper than the limit ends up in an additional, synthesized row. The cell is not lost.
Table structure nested like this is not meaningful in real documents.

##### Implicit closing (`MAX_CLOSE_SEARCH_DEPTH`)

Many start tags close an open element: `<li>` the previous `<li>`, `<td>` the previous `<td>`, `<option>` the previous
`<option>`. The search for the element to close stops at block level elements and at the
parents of the new element, but other elements (`<span>`, `<b>`, ...) do not stop it.

```html
<ul><li>a ... 100 unclosed <span> ... <li>b</ul>
```

With up to 99 elements in between the second `<li>` closes the first one (they become siblings). With 100 or more, the first
`<li>` is out of reach and the second one is **nested** inside the spans of the first.

*Effect:* a different shape of the tree for sloppy documents that leave 100 or more inline elements open before the next
list item, table cell or option. Nothing is removed.

##### End tags and table/form context (`MAX_CONTEXT_SEARCH_DEPTH`)

An end tag closes its matching start tag and all unclosed elements above it:

```html
<div><span><span>x</div>y
```

closes both spans and the div, and `y` is outside of the div. If the matching start tag is **100 or more** open elements
below the top, the end tag is treated like an end tag **without** start tag: it is ignored, nothing is closed and following
content stays inside all the open elements.

```html
<div> ... 100 unclosed <span> ... x</div>y     <!-- the </div> is ignored, y is inside the div and the spans -->
```

The end tags of `html` and `body` are exempt: both are unique and sit at the bottom of the stack, so they always work.

The same limit is used for the check whether a `<table>` or `<form>` start tag is inside table structure but outside a cell
(a new `<table>` there closes the surrounding table, a `<form>` there is closed immediately and stays empty). If the enclosing table
structure is out of reach the tag is treated as not being in a table.

*Effect:* this is the limit most likely to be noticed. A document that opens 100 or more elements and relies on a single end
tag to close all of them gets a different tree than before: the end tag does nothing and the rest of the document is nested in the
open elements. All elements are still closed at the end of the document, and the tree stays balanced. Browsers would close the elements.

##### Maximum nesting depth (`MAX_ELEMENT_DEPTH`)

Elements that are never closed nest forever: `<s><s><s>...` builds a tree as deep as the input is long. A few kilobytes are
enough to overflow the stack of any recursive tree walker (serializers, visitors, `toString()`, ...). Browsers limit the depth
of the tree, too (Chrome: 512), and so does the balancer.

Starting with 512 open elements (including `html` and `body`) a new element does **not** become a parent any more:

- it is reported as an *empty element* (SAX: `startElement` immediately followed by `endElement`, DOM: an element without children)
- its content is added to the deepest open element
- its end tag is an ordinary unmatched end tag and is ignored

```html
<div><div><div> ... 600 times ... x
```

The tree contains 600 `div` elements, but only the first ones nest; the others are leaves, and `x` is a child of the
last element that was still able to nest. The maximum depth of the generated tree is 512 plus the one level of the last
empty element.

*Effect:* none for documents that are less than 512 levels deep. For deeper documents the structure below the limit is
flattened. Elements that need a parent of their own (for example a `<td>` without an open `<tr>`) can get lost, because their
synthesized parent cannot nest either. If your application really needs deeper trees (generated documents, for example),
please open an issue.

##### Other limits

Two further constants only concern memory and have no influence on the result: the element stacks grow geometrically and
switch to linear growth in steps of 256k entries (`MAX_STACK_GROWTH`), and stacks that grew beyond 4096 entries are shrunk
again when the parser is reset (`MAX_RETAINED_STACK_CAPACITY`), so one huge document does not keep its memory.

The limits are covered by `HTMLTagBalancerDosTest` which parses the attacks above with large sizes and checks run time,
balance of the result and the boundaries (99 entries still work, 100 do not).

#### Important Notes

- **CDATA_SECTIONS**: This is one of the most important features for XML/XHTML compatibility. Enable it when processing XHTML documents or when you need to distinguish between CDATA sections and comments.
- **Error Reporting**: Enable `REPORT_ERRORS` during development to catch malformed HTML early.
- **Self-Closing Tags**: Browsers ignore the slash on non-void HTML elements, XML parsers honor it. Pick the flags that match the kind of input, see [Choosing the self-closing flags](#choosing-the-self-closing-flags). The defaults are the HTML5 behavior and are the safe choice.
- **NUL characters**: see [NUL characters](#nul-characters) for the replacement rules.
- **Attribute Normalization**: When `NORMALIZE_ATTRIBUTES` is enabled, whitespace handling follows XML rules, which may differ from browser behavior.

#### Choosing the self-closing flags

In HTML5 the slash of `<div/>` is ignored (the div stays open), and `<iframe/>`, `<script/>`, `<textarea/>`, `<title/>`,
`<style/>` and a few more open an element whose content is **text, not markup**, up to the matching end tag.
An XML parser (XHTML) honors the slash on every element instead. The flags select which behavior you get.

**Which flag controls which element**

| Element | The slash is honored if |
|---------|-------------------------|
| `iframe` | `ALLOW_SELFCLOSING_IFRAME` is `true` |
| `script` | `ALLOW_SELFCLOSING_SCRIPT` is `true` |
| every other element, including `noembed`, `noframes`, `noscript`, `plaintext`, `title`, `textarea`, `style`, `xmp` | `ALLOW_SELFCLOSING_TAGS` is `true` |

The `iframe` and `script` flags work on their own: `ALLOW_SELFCLOSING_TAGS=true` does **not** close `iframe` and `script`, and
`ALLOW_SELFCLOSING_IFRAME=true` also works if `ALLOW_SELFCLOSING_TAGS` is `false`.

**Recommended combinations**

| Input | `ALLOW_SELFCLOSING_TAGS` | `ALLOW_SELFCLOSING_IFRAME` | `ALLOW_SELFCLOSING_SCRIPT` |
|-------|:---:|:---:|:---:|
| **HTML document** (browser compatible, the default) | `false` | `false` | `false` |
| **XHTML / XML document** | `true` | `true` | `true` |
| **Fragment in an HTML context** (what a browser does for `innerHTML`) | `false` | `false` | `false` |
| **Fragment in an XHTML context** | `true` | `true` | `true` |
| Lenient HTML, `<div/>` should be closed | `true` | `false` | `false` |

Notes:

- A fragment follows the kind of the document it will be inserted into, not the kind of the fragment text. Use the two
  fragment rows according to that document: HTML defaults for an HTML document, all three flags for an XHTML document.
- The lenient row closes every self-closed element except `iframe` and `script`, including `<textarea/>` and `<title/>`.
  Browsers would treat these as open elements. Only use it if you know your input relies on it.
- For XHTML also set the CDATA features, as HtmlUnit does:

```java
parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);
parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_SCRIPT, true);
parser.setFeature(HTMLScanner.CDATA_SECTIONS, true);
parser.setFeature(HTMLScanner.CDATA_EARLY_CLOSING, false);
parser.setFeature(HTMLScanner.SCRIPT_STRIP_CDATA_DELIMS, true);
parser.setFeature(HTMLScanner.STYLE_STRIP_CDATA_DELIMS, true);
```

**Example**: the input `<iframe/><img src="x" onerror="alert(1)"></iframe>`

| `ALLOW_SELFCLOSING_IFRAME` | Result |
|:---:|--------|
| `false` (default) | one `iframe` element, the `img` markup is its text content and stays inert, exactly as in a browser |
| `true` | an empty `iframe` followed by a live `img` element with its `onerror` attribute |

**Known limitations in XML mode.** Neko always uses the HTML scanner and tag balancer, also for XHTML. The flags make the slash
behave like in XML, but elements with raw text content are still scanned as text when they are *not* self-closed:
`<title>a<b>b</b>c</title>` in an XHTML document produces one text node `a<b>b</b>c`, not a `b` element, and the HTML content
model of the balancer still applies (for example for elements inside `head`). Use a real XML parser if you need strict XML.

#### NUL characters

A NUL character (U+0000) in the input is handled like in the HTML5 tokenizer and tree construction:

| Where | Result |
|-------|--------|
| content of `title`, `textarea`, `style`, `script`, `xmp`, `iframe`, `noframes`, `noembed`, `plaintext`, and `noscript` (if `PARSE_NOSCRIPT_CONTENT` is `false`) | replaced by U+FFFD |
| a NUL right after a `<` in one of these elements | replaced by U+FFFD |
| comments | replaced by U+FFFD |
| attribute values | replaced by U+FFFD |
| regular text content (for example inside a `div`) | ignored (dropped) |
| character reference `&#0;` | U+FFFD (unchanged) |

#### Security considerations

- **Parse with the same configuration that you sanitize with.** A different treatment of the slash on `iframe`, `script`,
  `textarea`, `style`, `title`, ... turns text in one parser into live markup (including event handler attributes) in another.
  Such a parser differential is the typical way a sanitizer is bypassed.
- **Keep the defaults for untrusted HTML.** `ALLOW_SELFCLOSING_IFRAME`, `ALLOW_SELFCLOSING_SCRIPT` and `ALLOW_SELFCLOSING_TAGS` are only for
  input that really is XML/XHTML. If you cannot tell, use the HTML defaults, they match what browsers do.
- **Serializer round trips.** If you serialize parsed content with `HTMLWriterFilter`, use a version that contains the fix for
  [CVE-2026-104828](https://nvd.nist.gov/vuln/detail/CVE-2026-104828) (x.y.z+). Older versions wrote the decoded content of
  `textarea` and `title` back without entity encoding, so serializing the result of a parse could turn encoded text into markup.
- **Denial of service.** The tag balancer limits the work per tag and the nesting depth of the generated tree, see
  [Limits of the tag balancer](#limits-of-the-tag-balancer). The limits are fixed; parsing time of crafted input stays linear in the input size.
- Neko is a parser, not a sanitizer. Use a dedicated HTML sanitizer for untrusted content and re-parse its output with the
  settings of the consumer.

### Properties

The behavior of the scanner/parser can be influenced via a series of property switches that control various aspects of parsing behavior and output formatting.

```java
parser.setProperty(HTMLScanner.ENCODING_TRANSLATOR, EncodingMap.INSTANCE);
```

#### Element and Attribute Name Handling

| Property | Values | Default | Description |
|----------|--------|---------|-------------|
| **NAMES_ELEMS** | `"upper"`, `"lower"`, `"default"` | `"default"` | Controls how HTML element names are modified during parsing. `"upper"` converts all element names to uppercase, `"lower"` converts to lowercase, and `"default"` preserves the original case. |
| **NAMES_ATTRS** | `"upper"`, `"lower"`, `"default"` | `"default"` | Controls how HTML attribute names are modified during parsing. Similar to `NAMES_ELEMS` but applies to attribute names only. |

#### Character Encoding Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| **DEFAULT_ENCODING** | `String` | `"Windows-1252"` | Sets the default character encoding to use when no encoding is specified in the document or input source. Should be a valid IANA encoding name. |
| **ENCODING_TRANSLATOR** | `EncodingTranslator` | `StandardEncodingTranslator.INSTANCE` | Provides encoding name translation from HTML meta tag labels to Java encoding names. Starting with version 4.0.0, uses `StandardEncodingTranslator` for spec-compliant behavior. Can be set to `EncodingMap.INSTANCE` for legacy behavior. |

#### Error and Debugging Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| **ERROR_REPORTER** | `HTMLErrorReporter` | `null` | Error reporter instance for handling parsing errors and warnings. Must implement `org.htmlunit.cyberneko.HTMLErrorReporter` interface. When `null`, error reporting is disabled. |

#### Document Type Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| **DOCTYPE_PUBID** | `String` | `"-//W3C//DTD HTML 4.01 Transitional//EN"` | DOCTYPE declaration public identifier used when `INSERT_DOCTYPE` or `OVERRIDE_DOCTYPE` features are enabled. |
| **DOCTYPE_SYSID** | `String` | `"http://www.w3.org/TR/html4/loose.dtd"` | DOCTYPE declaration system identifier used when `INSERT_DOCTYPE` or `OVERRIDE_DOCTYPE` features are enabled. |

#### Performance Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| **READER_BUFFER_SIZE** | `Integer` | `616` | Size of the internal character buffer used during content scanning. Larger values may improve performance for large documents but use more memory. Default is optimized for cache efficiency (10 × 64 - 24 bytes). |

#### Usage Examples

```java
// Configure element and attribute name casing
parser.setProperty(HTMLScanner.NAMES_ELEMS, "upper");
parser.setProperty(HTMLScanner.NAMES_ATTRS, "lower");

// Set custom encoding handling
parser.setProperty(HTMLScanner.DEFAULT_ENCODING, "UTF-8");
parser.setProperty(HTMLScanner.ENCODING_TRANSLATOR, EncodingMap.INSTANCE);

// Configure DOCTYPE for inserted declarations
parser.setProperty(HTMLScanner.DOCTYPE_PUBID, "-//W3C//DTD XHTML 1.0 Strict//EN");
parser.setProperty(HTMLScanner.DOCTYPE_SYSID, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

// Set up error reporting
HTMLErrorReporter errorReporter = new MyCustomErrorReporter();
parser.setProperty(HTMLScanner.ERROR_REPORTER, errorReporter);

// Optimize buffer size for large documents
parser.setProperty(HTMLScanner.READER_BUFFER_SIZE, 2048);
```

### Tag Case Handling

By default, tags inserted by the parser to fix the DOM tree are created in **lowercase** to maintain XHTML compatibility. 
For example, when the parser automatically inserts missing elements like `<html>`, `<head>`, or `<body>`, these tags will be in lowercase form.

However, you can control the case of element names using the property:
```java
parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");
```

When this property is set to `"upper"`, the parser will create all tag names in **uppercase** instead. 
This includes both tags present in the source HTML and tags automatically inserted to balance the DOM tree.

#### Important Notes

- **Encoding Translator**: The `StandardEncodingTranslator` provides WHATWG-compliant encoding name mapping. Use `EncodingMap` only if you need legacy behavior compatibility.
- **Buffer Size**: The default buffer size is optimized for cache efficiency. Increasing it may help with very large documents but will use more memory.
- **Error Reporter**: Implementing a custom error reporter allows you to handle parsing errors according to your application's needs.
- **Name Casing**: Changing element/attribute name casing affects the output and may impact CSS selectors or JavaScript that relies on specific casing.


<a name="legacy-support-jdk-8"></a>
### Legacy Support (JDK 8)

If you need to continue using **JDK 8**, versions 4.x remain available as-is.
Ongoing maintenance and fixes for JDK 8 are only available through sponsorship —
please contact me via email to discuss options. Sponsorship provides:

- Backporting security and bug fixes to the 4.x branch
- Compatibility maintenance with older Java versions
- Timely releases for critical issues

Without sponsorship, the 4.x branch will not receive further updates.


### Last CI build
The latest builds are available from our
[Jenkins CI build server](https://jenkins.wetator.org/job/HtmlUnit%20-%20Neko/ "HtmlUnit -Neko CI")

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnit+-+Neko)](https://jenkins.wetator.org/view/HtmlUnit/job/HtmlUnit%20-%20Neko/)


Read on if you want to try the latest bleeding-edge snapshot.

### Maven

Add the dependency to your `pom.xml`:

    <dependency>
        <groupId>org.htmlunit</groupId>
        <artifactId>neko-htmlunit</artifactId>
        <version>5.6.0-SNAPSHOT</version>
    </dependency>

You have to add the sonatype-central snapshot repository to your pom `repositories` section also:

    <repositories>
        <repository>
            <name>Central Portal Snapshots</name>
            <id>central-portal-snapshots</id>
            <url>https://central.sonatype.com/repository/maven-snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

### Gradle

Add the snapshot repository and dependency to your `build.gradle`:

```groovy
repositories {
  maven { url "https://central.sonatype.com/repository/maven-snapshots" }
  // ...
}
// ...
dependencies {
    implementation group: 'org.htmlunit', name: 'neko-htmlunit', version: '5.6.0-SNAPSHOT'
  // ...
}
```


## Upgrade notes within 5.x

### Tag balancer limits and browser like un-nesting (since 5.7.0)

- **`<a>` and `<nobr>` are un-nested** like browsers do: `<a href="1">1<div><a href="2">2</a></div>` no longer nests the second
  anchor in the first one.
- **The balancer has fixed limits** for the search on the stack of open elements (100 entries) and for the nesting depth of the
  generated tree (512 elements). Documents that leave 100 or more elements open and rely on one end tag (or an implicit close) to
  close them all get a different tree than before; documents nested deeper than 512 levels are flattened. Details and examples:
  [Limits of the tag balancer](#limits-of-the-tag-balancer).

### Self-closing flags and NUL handling (since 5.6.0)

- **`ALLOW_SELFCLOSING_TAGS` no longer closes `iframe` and `script`.** Before, the general flag also closed `<iframe/>` and
  `<script/>` (a self-closed `script` was even left half closed). Now the slash on these two elements is only honored if
  `ALLOW_SELFCLOSING_IFRAME` / `ALLOW_SELFCLOSING_SCRIPT` is enabled; otherwise the content is raw text, as in HTML5.
  **If you parse XHTML/XML**, enable all three flags (see [Choosing the self-closing flags](#choosing-the-self-closing-flags)).
- **Self-closed special elements are empty elements with `ALLOW_SELFCLOSING_TAGS`.** `<noembed/>`, `<noframes/>`,
  `<noscript/>`, `<plaintext/>`, `<title/>`, `<textarea/>`, `<style/>` and `<xmp/>` no longer swallow the rest of the document as
  raw text when the flag is enabled. Without the flag nothing changes: the slash is ignored and the content is raw text.
- **NUL characters** are replaced by U+FFFD in raw text, RCDATA, comments and attribute values, and dropped in regular text
  (see [NUL characters](#nul-characters)). Before, they were passed on unchanged.
- **Fragment parsing** (`innerHTML`-like use): the flags have to match the document the fragment belongs to.
  For fragments of an **HTML** document use the HTML defaults; if you enabled `ALLOW_SELFCLOSING_TAGS` for all fragments only to
  be lenient (HtmlUnit, for example, did so before), switch it off to get the browser behavior. For fragments of an **XHTML**
  document enable all three self-closing flags, exactly as for a full XHTML page. See the table above.

## Migrating from 4.x to 5.x

Version 5.0.0 is a major release. The changes below cover everything you need to update when upgrading from any 4.x release.

### Java version requirement

5.x requires **JDK 17 or higher**. Java 8 and Java 11 are no longer supported.
If you cannot upgrade your JDK, stay on the [4.x branch](#legacy-support-jdk-8).

### HTMLElements thread-safety fix (since 4.17.0)

The shared `HTMLElements` instance no longer caches unknown elements because that cache was not thread-safe. If your code relied on the unknown-element cache, switch to `HTMLElementsWithCache` and create a new instance per parse run:

```java
// 4.x — relied on shared cache in HTMLElements (not thread-safe)
DOMParser parser = new DOMParser(HTMLDocumentImpl.class);

// 5.x — use a fresh HTMLElementsWithCache per parse run if you need the cache
HTMLElementsProvider provider = new HTMLElementsWithCache();
DOMParser parser = new DOMParser(HTMLDocumentImpl.class, provider);
```

### HTMLScanner document handler is now required (since 4.12.0)

`HTMLScanner` now enforces that a document handler is set before parsing. The null check was moved to the setter, so passing `null` will throw immediately rather than failing silently mid-parse. Ensure you always call `setContentHandler` (SAXParser) or provide a handler via the `DOMParser` constructor before calling `parse()`.

### Tag name casing for auto-inserted tags (since 4.20.0 / 4.21.0)

Auto-inserted tags (e.g. `<html>`, `<head>`, `<body>`) are now consistently produced in **lowercase** by default. If your code compared tag names assuming uppercase auto-inserted tags, update those comparisons or set the `NAMES_ELEMS` property explicitly:

```java
// Force uppercase if your code depends on it
parser.setProperty(HTMLScanner.NAMES_ELEMS, "upper");

// Or update comparisons to be case-insensitive
String name = element.getTagName().toLowerCase(Locale.ROOT);
```

### EOF handling changed from exceptions to return codes (since 4.19.0)

EOF conditions inside the scanner are now signalled via return codes rather than exceptions. This is an internal change that should not affect typical parser usage, but if you have custom `HTMLScanner` subclasses that override scanning methods and catch internal exceptions for EOF detection, review those overrides.

### SAXParser factory added (since 4.15.0)

`NekoSAXParserFactory` was added as a standard `javax.xml.parsers.SAXParserFactory` entry point. If you were constructing `SAXParser` directly and want to align with the standard JAXP pattern going forward:

```java
// New in 4.15 / available in 5.x
SAXParserFactory factory = new NekoSAXParserFactory();
javax.xml.parsers.SAXParser saxParser = factory.newSAXParser();
```

### Removed and unsupported features

- **`XMLLocator`** was simplified and some internal fields removed in 4.5.0. Custom subclasses that accessed internal locator fields will need to be updated.
- **`XMLAttributesImpl`** was simplified in 4.5.0; direct field access patterns in custom subclasses should be replaced with the public API.


## Migrating from 3.x to 4.x

Version 4.x introduces a major change in the handling of encodings - the mapping from the encoding
label found in the meta tag to the encoding to be used for parsing the document got some significant
changes. Starting with version 4.0 the mapping is now in sync with the [spec](https://encoding.spec.whatwg.org/#names-and-labels).

For this also

 * a new interface org.htmlunit.cyberneko.xerces.util.EncodingTranslator was introduced
 * a new property 'http://cyberneko.org/html/properties/encoding-translator' is now supported
   * set the property to a different EncodingTranslator if you like
 * the new class org.htmlunit.cyberneko.xerces.util.StandardEncodingTranslator implements the new mapping
 * the (old) class org.htmlunit.cyberneko.xerces.util.EncodingMap is still there; you can use this as
   encoding translator if you like to have the old translation behavior (parser.setProperty(HTMLScanner.ENCODING_TRANSLATOR, EncodingMap.INSTANCE))


## Migrating from 2.x to 3.x

Usually the upgrade should be simple:

 * change your pom.xml to switch to the new artifact
 * adjust the imports
   * net.sourceforge.htmlunit.cyberneko -> org.htmlunit.cyberneko
   * net.sourceforge.htmlunit.xerces -> org.htmlunit.cyberneko.xerces

But we have removed some features and some classes in version 3.
If you have any problems or if you miss something important for your project, please open an issue.


## Start NekoHtml Development

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You simply only need a local maven installation.


### Building

Create a local clone of the repository and you are ready to start.

Open a command line window from the root folder of the project and call

```
mvn compile
```

### Running the tests

```
mvn test
```

## Contributing

Pull Requests and and all other Community Contributions are essential for open source software.
Every contribution - from bug reports to feature requests, typos to full new features - are greatly appreciated.

## Deployment and Versioning

This part is intended for committer who are packaging a release.

* Check all your files are checked in
* Execute these mvn commands to be sure all tests are passing and everything is up to data

```
   mvn versions:display-plugin-updates
   mvn versions:display-dependency-updates
   mvn -U clean test
```

* Update the version number in pom.xml and README.md
* Commit the changes


* Build and deploy the artifacts 

```
   mvn -up clean deploy
```

* Go to [Maven Central Portal](https://central.sonatype.com/) and process the deploy
  - publish the package and wait until it is processed

* Create the version on Github
    * login to Github and open project https://github.com/HtmlUnit/htmlunit-neko
    * click Releases > Draft new release
    * fill the tag and title field with the release number (e.g. 5.0.0)
    * append 
        * neko-htmlunit-5.xx.jar
        * neko-htmlunit-5.xx.jar.asc
        * neko-htmlunit-5.xx.pom
        * neko-htmlunit-5.xx.pom.asc 
        * neko-htmlunit-5.xx-javadoc.jar
        * neko-htmlunit-5.xx-javadoc.jar.asc
        * neko-htmlunit-5.xx-sources.jar
        * neko-htmlunit-5.xx-sources.jar.asc
        * neko-htmlunit-5.xx-tests.jar
        * neko-htmlunit-5.xx-tests.jar.asc
    * and publish the release 

* Update the version number in pom.xml to start next snapshot development
* Update the htmlunit pom to use the new release

## History

HtmlUnit has been using CyberNeko HTML parser (http://nekohtml.sourceforge.net/) for a long time.
But since the development was discontinued around 2014, we started our own fork, which now has many improvements.

As of version 2.68.0, neko-htmlunit also uses its own fork of Xerces (https://github.com/apache/xerces2-j).
This forked code is integrated into the code base to further reduce the external dependencies.  
This made it possible to remove many unneeded parts and dependencies to ensure e.g. compatibility with Android.

## Authors

* **Andy Clark** (author of CyberNeko)
* **Marc Guillemot** (CyberNeko and NekoHtml)
* **Ahmed Ashour** (NekoHtml)
* **RBRi** (NekoHtml)

## License

This project is licensed under the Apache 2.0 License

## Acknowledgments

Many thanks to all of you contributing to CyberNeko/NekoHtml in the past.

## Development Tools

Special thanks to:

<a href="https://www.jetbrains.com/community/opensource/"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains" width="42"></a>
<a href="https://www.jetbrains.com/idea/"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg" alt="IntelliJ IDEA" width="42"></a>  
**[JetBrains](https://www.jetbrains.com/)** for providing IntelliJ IDEA under their [open source development license](https://www.jetbrains.com/community/opensource/) and

<a href="https://www.eclipse.org/"><img src="https://www.eclipse.org/eclipse.org-common/themes/solstice/public/images/logo/eclipse-foundation-grey-orange.svg" alt="Eclipse Foundation" width="80"></a>  
Eclipse Foundation for their Eclipse IDE

<a href="https://www.syntevo.com/smartgit/"><img src="https://www.syntevo.com/assets/images/logos/smartgit-8c1aa1e2.svg" alt="SmartGit" width="54"></a>  
to **[Syntevo](https://www.syntevo.com/)** for their excellent [SmartGit](https://www.smartgit.dev/)!