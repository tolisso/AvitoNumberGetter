Working ONLY on linux

## About

This program print phones from avito's page to stdout.

There is also a garbage in the stderr, so you can redirect it to
`/dev/null` with: 

`#command_starts_program# 2> /dev/null`

## Requirements

You need tesseract to run this script.
It can be installed with:

`sudo apt install tesseract-ocr`
or 

`sudo apt-get install tesseract-ocr`

Also you need to have **chrome** and **selenium plugin to chrome**.

## Build

To build project you need to add additional modules to it. 
They are locating in packages

*plugins/selenium-java-@version* and 

*plugins/selenium-java-@version/libs*

## How to use

Launch with arguments: 
1) -link (link)
2) -page (link) (numberOfLinks-opt)
3) -pages (link) (numberOfPages)

(link) must point to
1) housing page
2) page with list of housings
3) first page with list of housings

(numberOfLinks-opt) set how many links from beginning 
of list of a housings WITH NUMBERS will be loaded. 
For page with one housing it's useless. 