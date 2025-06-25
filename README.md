# Htmlunit-NekoHtml Parser

The **Htmlunit-NekoHtml** Parser is a HTML scanner and tag balancer that enables application programmers to parse HTML documents
and access the information using standard XML interfaces.  
The parser can scan HTML files and "fix up" many common mistakes that human (and computer) authors make in writing HTML documents.  
NekoHTML adds missing parent elements; automatically closes elements with optional end tags; and can handle mismatched inline element tags.

The **Htmlunit-NekoHtml** Parser has no external dependencies at all, requires Java 8 and works also on Android.  
The **Htmlunit-NekoHtml** Parser is used by Htmlunit.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.htmlunit/neko-htmlunit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.htmlunit/neko-htmlunit)

:heart: [Sponsor](https://github.com/sponsors/rbri)

### Project News

**[Developer Blog](https://htmlunit.github.io/htmlunit-blog/)**

[HtmlUnit@mastodon](https://fosstodon.org/@HtmlUnit) | [HtmlUnit@bsky](https://bsky.app/profile/htmlunit.bsky.social) | [HtmlUnit@Twitter](https://twitter.com/HtmlUnit)

### Latest release Version 4.13.0 / June 03, 2025

##### [CVE-2022-29546](https://nvd.nist.gov/vuln/detail/CVE-2022-29546)
Htmlunit-NekoHtml Parser suffers from a denial of service vulnerability on versions 2.60.0 and below. A specifically crafted input regarding the parsing of processing instructions leads to heap memory consumption.
##### [CVE-2022-28366](https://nvd.nist.gov/vuln/detail/CVE-2022-28366)
Htmlunit-NekoHtml Parser suffers from a denial of service via crafted Processing Instruction vulnerability on versions 2.26 and below.

## Get it!

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.htmlunit</groupId>
    <artifactId>neko-htmlunit</artifactId>
    <version>4.13.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.htmlunit', name: 'neko-htmlunit', version: '4.13.0'
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
The behavior of the scanner/parser can be influenced via a series of switches.

    parser.setFeature(HTMLScanner.PLAIN_ATTRIBUTE_VALUES, true);

Supported features:

* AUGMENTATIONS - Include infoset augmentations
* REPORT_ERRORS - Report errors

* SCRIPT_STRIP_COMMENT_DELIMS - Strip HTML comment delimiters ("&lt;!--" and "-->") from SCRIPT tag contents
* SCRIPT_STRIP_CDATA_DELIMS - Strip XHTML CDATA delimiters ("&lt;![CDATA[" and "]]>") from SCRIPT tag contents

* STYLE_STRIP_COMMENT_DELIMS - Strip HTML comment delimiters ("&lt;!--" and "-->") from STYLE tag contents
* STYLE_STRIP_CDATA_DELIMS - Strip XHTML CDATA delimiters ("&lt;![CDATA[" and "]]>") from STYLE tag contents

* IGNORE_SPECIFIED_CHARSET -Ignore specified charset found in the &lt;meta equiv='Content-Type' content='text/html;charset=...'> tag or in the &lt;?xml ... encoding='...'> processing instruction
* CDATA_SECTIONS - Scan CDATA sections
* CDATA_EARLY_CLOSING - '>' closes the cdata section (see html spec) - default enabled
* OVERRIDE_DOCTYPE - Override doctype declaration public and system identifiers
* INSERT_DOCTYPE - Insert document type declaration
* PARSE_NOSCRIPT_CONTENT - Parse &lt;noscript>...&lt;/noscript>' content

* ALLOW_SELFCLOSING_IFRAME - Allows self closing &lt;iframe/&gt; tag
* ALLOW_SELFCLOSING_SCRIPT - Allows self closing &lt;script/&gt; tag
* ALLOW_SELFCLOSING_TAGS - Allows self closing tags e.g. &lt;div/&gt; (XHTML)

* NORMALIZE_ATTRIBUTES - Normalize attribute values
* PLAIN_ATTRIBUTE_VALUES - Store the plain attribute values also


### Properties
The behavior of the scanner/parser can be influenced via a series of switches.

    parser.setProperty(HTMLScanner.ENCODING_TRANSLATOR, EncodingMap.INSTANCE);

Supported properties:

* NAMES_ELEMS - "upper", "lower", "default"
* NAMES_ATTRS - "upper", "lower", "default"

* DEFAULT_ENCODING

* ERROR_REPORTER - Error reporter; an instance of org.htmlunit.cyberneko.HTMLErrorReporter or null
* ENCODING_TRANSLATOR - an implementation of org.htmlunit.cyberneko.xerces.util.EncodingTranslator. Starting with version 4.0.0
  the default encoding translator is set to the StandardEncodingTranslator which provides a much better and standard compliant
  handling of encodings. You can switch back the EncodingMap, if you still need the old behavior.

* DOCTYPE_PUBID - Doctype declaration public identifier
* DOCTYPE_SYSID - Doctype declaration system identifier

* READER_BUFFER_SIZE - Size of the reader buffer used during content scanning



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
        <version>4.14.0-SNAPSHOT</version>
    </dependency>

You have to add the Central Portal snapshot repository to your pom `repositories` section also:

    <!-- for snapshots of our dependencies -->
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
    implementation group: 'org.htmlunit', name: 'neko-htmlunit', version: '4.14.0-SNAPSHOT'
  // ...
}
```


## Porting from 3.x to 4.x

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


## Porting from 2.x to 3.x

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

* Go to [Sonatype staging repositories](https://s01.oss.sonatype.org/index.html#stagingRepositories) and process the deploy
  - select the repository and close it - wait until the close is processed
  - release the package and wait until it is processed

* Create the version on Github
    * login to Github and open project https://github.com/HtmlUnit/htmlunit-neko
    * click Releases > Draft new release
    * fill the tag and title field with the release number (e.g. 4.0.0)
    * append 
        * neko-htmlunit-4.xx.jar
        * neko-htmlunit-4.xx.jar.asc
        * neko-htmlunit-4.xx.pom
        * neko-htmlunit-4.xx.pom.asc 
        * neko-htmlunit-4.xx-javadoc.jar
        * neko-htmlunit-4.xx-javadoc.jar.asc
        * neko-htmlunit-4.xx-sources.jar
        * neko-htmlunit-4.xx-sources.jar.asc
        * neko-htmlunit-4.xx-tests.jar
        * neko-htmlunit-4.xx-tests.jar.asc
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

Many thanks to all of you contributing to HtmlUnit/CSSParser/Rhino/NekoHtml in the past.
