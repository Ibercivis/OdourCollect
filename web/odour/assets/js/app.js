// Declare app level module which depends on filters, and services
angular.module('odourCollectApp', [
  'odourCollectApp.controllers',
  'odourCollectApp.services',
  'odourCollectApp.directives',
  'ngRoute',
  'gettext',
  'ui.bootstrap',
  'ui-leaflet',
  'ui.slider',
  'ui.bootstrap.datetimepicker',
  'angularUtils.directives.dirPagination',
  'LocalStorageModule',
])

  .constant('SERVER', {
    baseUrl: 'http://lxbifi86.bifi.unizar.es:8080/webservice',
  })

  .constant('AUTH_EVENTS', {
    loginSuccess: 'auth-login-success',
    logoutSuccess: 'auth-logout-success',
  })

  .constant('REPORT_EVENTS', {
    reportAdded: 'report-added',
  })

  .config(function ($routeProvider, $httpProvider, $locationProvider, $qProvider) {
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
    $httpProvider.defaults.xsrfCookieName = 'csrftoken';
    $httpProvider.defaults.xsrfHeaderName = 'X-CSRFToken';

    //disable IE ajax request caching
    $httpProvider.defaults.headers.common['Cache-Control'] = 'no-cache';
    $httpProvider.defaults.headers.common['Pragma'] = 'no-cache';

    $routeProvider
      .when('/', {
        templateUrl: 'assets/partials/map.html',
        controller: 'MapCtrl'
      })

      .when('/lastentries', {
        templateUrl: 'assets/partials/lastentries.html',
        controller: 'LastEntriesCtrl'
      })

      .when('/project', {
        templateUrl: 'assets/partials/project.html',
        controller: 'ProjectCtrl'
      })

      .when('/methodology', {
        templateUrl: 'assets/partials/methodology.html',
        controller: 'MethodologyCtrl'
      })

      .when('/entry/:entryId', {
        templateUrl: 'assets/partials/report.html',
        controller: 'EntryCtrl'
      })

      .when('/user/:userId', {
        templateUrl: 'assets/partials/user.html',
        controller: 'UserCtrl'
      })

      .otherwise({
        redirectTo: '/'
      });

    // use the HTML5 History API & set HTML5 mode true
    //$locationProvider.html5Mode(true);

    // Silent unhandled rejections
    $qProvider.errorOnUnhandledRejections(false);
  })

  .run(function ($window, gettextCatalog, SessionStorage) {
    var lang = $window.navigator.languages
        ? $window.navigator.languages[0]
        : ($window.navigator.language || $window.navigator.userLanguage);

    gettextCatalog.setCurrentLanguage(lang);

    SessionStorage.load();
  });
