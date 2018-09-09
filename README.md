# HtmlUnit - NekoHtml Parser

This is the code repository of the HTML parser used by HtmlUnit.

For a long time HtmlUnit uses the CyberNeko HTML Parser (http://nekohtml.sourceforge.net/). But because the development
seems to end somewhere around 2014 we started our own fork that now has many improvements.

## Getting Started

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

TODO
Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Deployment and Versioning

This part is intended for committer who are packaging a release.

* Check all your files are checked in
* Execute "mvn clean test" to be sure all tests are passing
* Update the version number in pom.xml
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

Many thanks to all of you contributing to HtmlUnit/CSSParser/Rhino in the past.