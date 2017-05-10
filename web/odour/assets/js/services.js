/* Services */

angular.module('odourCollectApp.services', [])

  .service('Position', function () {
    this.setted = false;

    this.set = function (lat, lng, zoom) {
      this.lat = lat;
      this.lng = lng;
      this.zoom = zoom;
      this.setted = true;
    }
  })

  .factory('ReportsService', function ($http, $rootScope, $q, SERVER, REPORT_EVENTS) {
    var reportsService = {};

    var reports = null;

    reportsService.selectedId = null;

    reportsService.getReports = function () {
      var deferred = $q.defer();

      if (reports === null) {
        $http.get(SERVER.baseUrl + '/getreports.php')
          .then(function (res) {
            reports = res.data;
            deferred.resolve(reports);
          }, function (error) {
            reports = null;
            deferred.reject(error);
          });
      } else {
        deferred.resolve(reports);
      }
      return deferred.promise;
    }

    reportsService.getNearbyReports = function (latitude, longitude) {
      return $http.get(SERVER.baseUrl + '/getreports.php?latitude=' + latitude + '&longitude=' + longitude)
        .then(function (res) {
          var reports = res.data;
          return reports;
        });
    }

    reportsService.getReport = function (reportId) {
      return $http.get(SERVER.baseUrl + '/getreport.php?report_id=' + reportId)
        .then(function (res) {
          var report = res.data;
          report.comments = JSON.parse(report.comments);
          return report;
        });
    }

    reportsService.getUserReports = function (username) {
      return $http.get(SERVER.baseUrl + '/getuser.php?user_id=' + username)
        .then(function (res) {
          var user = res.data;
          user.reports = JSON.parse(user.reports);
          return user;
        });
    }

    reportsService.addReport = function (report, lat, lng, user) {
      var reportData = angular.copy(report);
      reportData.latitude = lat;
      reportData.longitude = lng;
      reportData.user = user;
      return $http.post(SERVER.baseUrl + '/addreport.php', $.param(reportData))
        .then(function (res) {
          $rootScope.$broadcast(REPORT_EVENTS.reportAdded);
        });
    }

    reportsService.addComment = function (comment, reportId, user) {
      var commentData = {
        comment: comment,
        report_id: reportId,
        user: user,
      }
      return $http.post(SERVER.baseUrl + '/addcomment.php', $.param(commentData));
    }

    reportsService.clean = function () {
      reports = null;
      reportsService.selectedId = null;
      deferred = $q.defer;
    }

    return reportsService;
  })

  .factory('AuthService', function ($http, $rootScope, SERVER, AUTH_EVENTS, SessionStorage, Session) {
    var authService = {};

    authService.login = function (credentials) {
      return $http
        .post(SERVER.baseUrl + '/login.php', $.param(credentials))
        .then(function (res) {
          SessionStorage.create(res.data.id, res.data.username);
          $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
          return res.data.user;
        });
    };

    authService.logout = function () {
      SessionStorage.destroy();
      $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
    }

    authService.isAuthenticated = function () {
      return !!Session.user_id;
    };

    authService.signup = function (user) {
      return $http.post(SERVER.baseUrl + '/signup.php', $.param(user));
    }

    return authService;
  })

  .service('SessionStorage', function (localStorageService, Session) {
    this.load = function () {
      if (localStorageService.isSupported) {
        var savedSession = localStorageService.get('session');
        if (savedSession !== null) {
          Session.create(savedSession.user_id, savedSession.username);
          return true;
        }
      }
      return false;
    }

    this.create = function (user_id, username) {
      Session.create(user_id, username);
      if (localStorageService.isSupported) {
        var session = {user_id: Session.user_id, username: Session.username};
        return localStorageService.set('session', session)
      }
      return false;
    }

    this.destroy = function () {
      Session.destroy();
      if (localStorageService.isSupported) {
        return localStorageService.remove('session');
      }
      return false;
    }
  })

  .service('Session', function () {
    this.create = function (user_id, username) {
      this.user_id = user_id;
      this.username = username;
    };

    this.destroy = function () {
      this.user_id = null;
      this.username = null;
    };
  })

  .factory('Dialogs', function ($uibModal) {
    /* Helper factory to instantiate modal dialogs */
    var dialogs = {};

    dialogs.animation = true;
    dialogs.backdrop = 'static';

    dialogs.openLogin = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/login.html',
        controller: 'LoginCtrl',
      });
    }

    dialogs.openSignUp = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/signup.html',
        controller: 'SignUpCtrl',
      });
    }

    dialogs.openAddReport = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/addreport.html',
        controller: 'AddReportCtrl',
      });
    }

    dialogs.openAddComment = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/addcomment.html',
        controller: 'AddCommentCtrl',
      });
    }

    dialogs.openNoLogged = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/nologged.html',
        controller: 'NoLoggedCtrl',
      });
    }

    dialogs.openReportRecorded = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/report_recorded.html',
        controller: 'ReportRecordedCtrl'
      });
    }

    dialogs.openCommentRecorded = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/comment_recorded.html',
        controller: 'CommentRecordedCtrl'
      });
    }

    dialogs.openGeolocationError = function () {
      return $uibModal.open({
        animation: dialogs.animation,
        backdrop: dialogs.backdrop,
        templateUrl: 'assets/partials/geolocation_error.html',
        controller: 'GeolocationErrorCtrl'
      });
    }

    return dialogs;
  })

  .factory('Helpers', function() {
    var helpers = {};

    helpers.setFormFieldsDirty = function (form) {
      for (var i=0; i<form.$$controls.length; i++) {
        var control = form.$$controls[i];
        control.$setDirty();
      }
    }

    return helpers;
  });
