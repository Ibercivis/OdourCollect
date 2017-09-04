module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    nggettext_extract: {
      pot: {
        files: {
          'translations/po/template.pot': [
            'odour/index.html',
            'odour/assets/partials/**/*.html',
            'odour/assets/js/**/*.js',
          ]
        }
      },
    },

    nggettext_compile: {
      all: {
        files: {
          'odour/assets/js/translations.js': ['translations/po/*.po']
        }
      },
    },

  });

  grunt.loadNpmTasks('grunt-angular-gettext');

  grunt.registerTask('extract', ['nggettext_extract']);
  grunt.registerTask('compile', ['nggettext_compile']);
};
