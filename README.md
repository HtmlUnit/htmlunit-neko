# HtmlUnit - NekoHtml Parser

This is the code repository of the HTML parser used by HtmlUnit.

HtmlUnit has been using CyberNeko HTML parser (http://nekohtml.sourceforge.net/) for a long time.
But since the development was discontinued around 2014, we started our own fork, which now has many improvements.

As of version 2.68.0, neko-htmlunit also uses its own fork of Xerces (https://github.com/apache/xerces2-j).
This made it possible to remove many unneeded parts and dependencies to ensure e.g. compatibility with Android.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sourceforge.htmlunit/neko-htmlunit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sourceforge.htmlunit/neko-htmlunit)

:heart: [Sponsor](https://github.com/sponsors/rbri)

### Project News
[HtmlUnit@Twitter][3]

### Latest release Version 3.1.0 / April 12, 2023

#### CVE-2022-29546 / CVE-2022-28366
#### HtmlUnit - NekoHtml Parser suffers from a denial of service vulnerability on versions 2.60.0 and below. A specifically crafted input regarding the parsing of processing instructions leads to heap memory consumption. Please update to at least version 2.62.0.

#### CVE-2023-26119
#### HtmlUnit - NekoHtml Parser suffers from a remote code execution vulnerability on versions 2.70.0 and below.Please update to at least version 3.1.0.

## Get it!

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.htmlunit</groupId>
    <artifactId>neko-htmlunit</artifactId>
    <version>3.1.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.htmlunit', name: 'neko-htmlunit', version: '3.1.0'
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


### Last CI build
The latest builds are available from our
[Jenkins CI build server][2]

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnit+-+Neko)](https://jenkins.wetator.org/view/HtmlUnit/job/HtmlUnit%20-%20Neko/)


If you use maven please add:

    <dependency>
        <groupId>org.htmlunit</groupId>
        <artifactId>neko-htmlunit</artifactId>
        <version>3.2.0-SNAPSHOT</version>
    </dependency>

You have to add the sonatype snapshot repository to your pom distributionManagement section also:

    <repository>
        <id>OSS Sonatype snapshots</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>


## Porting from 2.x to 3.x

    * change your pom.xml to switch to the new artifact
    * adjust the imports
        * net.sourceforge.htmlunit.cyberneko -> org.htmlunit.cyberneko
        * net.sourceforge.htmlunit.xerces -> org.htmlunit.cyberneko.xerces

For version 3 we have removed some features and some classes. If you have any problems or if
you miss something important for your project, please open an issue.


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
* Execute "mvn -U clean test" to be sure all tests are passing
* Update the version number in pom.xml and readme.md
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
    * fill the tag and title field with the release number (e.g. 1.1.0)
    * append 
        * neko-htmlunit-2.xx-javadoc.jar
        * neko-htmlunit-2.xx-javadoc.jar.asc
        * neko-htmlunit-2.xx-sources.jar
        * neko-htmlunit-2.xx-sources.jar.asc
        * neko-htmlunit-2.xx-tests.jar
        * neko-htmlunit-2.xx-tests.jar.asc
        * neko-htmlunit-2.xx.jar
        * neko-htmlunit-2.xx.jar.asc
        * neko-htmlunit-2.xx.pom
        * neko-htmlunit-2.xx.pom.asc 
    * and publish the release 

* Update the version number in pom.xml to start next snapshot development
* Update the htmlunit pom to use the new release

## Authors

* **Andy Clark** (author of CyberNeko)
* **Marc Guillemot** (CyberNeko and NekoHtml)
* **Ahmed Ashour** (NekoHtml)
* **RBRi** (NekoHtml)

## License

This project is licensed under the Apache 2.0 License

## Acknowledgments

Many thanks to all of you contributing to HtmlUnit/CSSParser/Rhino/NekoHtml in the past.


[2]: https://jenkins.wetator.org/job/HtmlUnit%20-%20Neko/ "HtmlUnit -Neko CI"
[3]: https://twitter.com/HtmlUnit "https://twitter.com/HtmlUnit"
