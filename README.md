# Labradoodle

Gitlab API utilites.

Currently just supports exporting issues from a project into an Excel workbook called "punchlist.xlsx".

Built using "org.gitlab/java-gitlab-api" and "dk.ative/docjure".

Tested against Gitlab 7.1.1.

## Installation

 1. Clone
 2. Copy config.edn.sample to config.edn
 3. Edit config.edn
    * replace :url value with your Gitlab URL
    * replace :private-token value with your Gitlab private token, which should be listed on your
    [Profile](https://$HOST/profile/account) page.

## Usage

Using bash script from command line:

    $ ./scripts/export-punchlist.sh "foo"
    $ open punchlist.xlsx

From REPL:

    $ lein repl
    labradoodle.core=> (export-punchlist "foo")
    labradoodle.core=> CTRL-D
    $ open punchlist.xlsx

## License

Copyright © 2015 Roan O'Sullivan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
