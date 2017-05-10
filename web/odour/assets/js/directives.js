/* Directives */

var USERNAME_REGEXP = /^[a-z]([0-9a-z_\s])+$/i;
var PASSWORD_REGEXP = /^([0-9a-zA-Z])+$/;

angular.module('odourCollectApp.directives', [])
  .directive('textstring', function() {
    /*
    Validator for input field.
    Username, report origin... may consist of a-z, 0-9, underscores, spaces and must begin with a letter
    */
    return {
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$validators.charset = function(modelValue, viewValue) {
          if (ctrl.$isEmpty(modelValue)) {
            // consider empty models to be valid
            return true;
          }

          if (USERNAME_REGEXP.test(viewValue)) {
            // it is valid
            return true;
          }

          // it is invalid
          return false;
        };
      }
    };
  })

  .directive('password', function() {
    /*
    Validator for input field.
    Password field only allow : a-z 0-9
    */
    return {
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$validators.charset = function(modelValue, viewValue) {
          if (ctrl.$isEmpty(modelValue)) {
            // consider empty models to be valid
            return true;
          }

          if (PASSWORD_REGEXP.test(viewValue)) {
            // it is valid
            return true;
          }

          // it is invalid
          return false;
        }
      }
    }
  });