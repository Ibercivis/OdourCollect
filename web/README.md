OdourCollect WebApp
===================

* SPA (Single Page Application)
* webservice (API)


## Translations

Prerequisites *(only for extract/compile translations)*:

* Install node.js and npm
* Install the dependencies with `npm install`

To extract the translations in a .pot file:

    grunt extract

This command will generate a file `web/translations/po/template.pot` with the annotated strings.

To compile the .pom files in a .js file:

    grunt compile

That will generate a javascript file with the strings translated `web/odour/assets/js/translations.js`