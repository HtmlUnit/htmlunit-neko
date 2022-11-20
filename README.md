# HtmlUnit - NekoHtml Parser

This is the code repository of the HTML parser used by HtmlUnit.

HtmlUnit has been using CyberNeko HTML parser (http://nekohtml.sourceforge.net/) for a long time.
But since the development was discontinued around 2014, we started our own fork, which now has many improvements.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sourceforge.htmlunit/neko-htmlunit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sourceforge.htmlunit/neko-htmlunit)

:heart: [Sponsor](https://github.com/sponsors/rbri)

### Project News
[HtmlUnit@Twitter][3]

### Latest release Version 2.67.0 / November 20, 2022

#### CVE-2022-29546
#### HtmlUnit - NekoHtml Parser suffers from a denial of service vulnerability on versions 2.60.0 and below. A specifically crafted input regarding the parsing of processing instructions leads to heap memory consumption. Please update to at least version 2.62.0.

For maven, you would add:

    <dependency>
        <groupId>net.sourceforge.htmlunit</groupId>
        <artifactId>neko-htmlunit</artifactId>
        <version>2.67.0</version>
    </dependency>

### Last CI build
The latest builds are available from our
[Jenkins CI build server][2]

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnit+-+Neko)](https://jenkins.wetator.org/view/HtmlUnit/job/HtmlUnit%20-%20Neko/)


If you use maven please add:

    <dependency>
        <groupId>net.sourceforge.htmlunit</groupId>
        <artifactId>neko-htmlunit</artifactId>
        <version>2.68.0-SNAPSHOT</version>
    </dependency>

You have to add the sonatype snapshot repository to your pom distributionManagement section also:

    <repository>
        <id>OSS Sonatype snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>


## Start NekoHtml Development

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
* Execute "mvn clean test" to be sure all tests are passing
* Update the version number in pom.xml and readme.md
* Execute "mvn clean test" to be sure all tests are passing
* Commit the changes


* Build and deploy the artifacts 

```
   mvn -up clean deploy
```

* Go to [Sonatype staging repositories](https://oss.sonatype.org/index.html#stagingRepositories) and process the deploy
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
