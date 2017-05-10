/* Controllers */

angular.module('odourCollectApp.controllers', [])

  .controller('NavCtrl', function ($scope, $rootScope, $location, AUTH_EVENTS, AuthService, Session, Dialogs) {
    $scope.loggedUser = null;

    if (AuthService.isAuthenticated()) {
      $scope.loggedUser = {id: Session.user_id, name: Session.username};
    }

    $rootScope.$on(AUTH_EVENTS.loginSuccess, function() {
      $scope.loggedUser = {id: Session.user_id, name: Session.username};
    });

    $rootScope.$on(AUTH_EVENTS.logoutSuccess, function() {
      $scope.loggedUser = null;
    });

    $scope.isActive = function (viewLocation) {
      return viewLocation === $location.path();
    };

    $scope.openLogin = function () {
      Dialogs.openLogin();
    }

    $scope.logout = function () {
      AuthService.logout();
    }
  })

  .controller('MapCtrl', function ($scope, $rootScope, $timeout, $q, leafletData, gettextCatalog, REPORT_EVENTS, Position, ReportsService, AuthService, Dialogs) {
    $scope.filters = {
      type: '',
    }

    var baseIcon = {
      shadowUrl: 'assets/vendor/leaflet/images/marker-shadow.png',
      iconSize: [35, 35], // size of the icon
      iconAnchor: [10, 35],    // point of the icon which will correspond to marker's location
      shadowAnchor: [10, 35],  // the same for the shadow
    }

    var goodIcon = angular.copy(baseIcon)
    var badIcon = angular.copy(baseIcon);
    var midIcon = angular.copy(baseIcon);

    goodIcon.iconUrl = 'assets/img/good_odour.png';
    badIcon.iconUrl = 'assets/img/bad_odour.png';
    midIcon.iconUrl = 'assets/img/mid_odour.png';

    if (Position.setted) {
      $scope.center = {
        lat: Position.lat,
        lng: Position.lng,
        zoom: Position.zoom
      }
    } else {
      $scope.center = {
        lat: 0,
        lng: 0,
        zoom: 2,
        autoDiscover: true,
      }
    }

    var geolocationAvailable = false;

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        function (position) {
          $scope.$apply(function() {
            geolocationAvailable = true;

            $scope.center = {
              lat: 0,
              lng: 0,
              zoom: 13,
              autoDiscover: true,
            }
          });
        },
        function (error) {
          $scope.$apply(function() {
            $scope.center.lat = 0;
            $scope.center.lng = 0;
            $scope.center.zoom = 2;

            Dialogs.openGeolocationError();
          });
        }
      );
    } else {
      Dialogs.openGeolocationError();
    }


    var southWest = L.latLng(-89.98155760646617, -180);
    var northEast = L.latLng(89.99346179538875, 180);
    var bounds = L.latLngBounds(southWest, northEast);

    $scope.defaults = {
      maxBounds: bounds,
    }

    /*$scope.controls = {
      custom: [
        L.control.locate({ follow: true })
      ]
    };*/


    // Add report button (get the location from the navigator location)
    $scope.controls = {
      custom: [],
    }

    var addReportControl = L.control();
    addReportControl.setPosition('bottomleft');
    addReportControl.onAdd = function () {
      var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-custom');
      container.innerHTML = gettextCatalog.getString('Add report');

      container.style.background = '#fff';
      container.style.padding = '5px';
      container.style.cursor = 'pointer';

      L.DomEvent.addListener(container, 'click', function (e) {
        $scope.$apply(function() {
          if ( !geolocationAvailable ) {
            Dialogs.openGeolocationError();
          } else {
            getGeoLocation().then(function(latlng) {
              addReport();
            }, function (error) {
              Dialogs.openGeolocationError();
            });
          }
        });

        e.stopPropagation();
      });

      return container;
    }

    $scope.controls.custom.push(addReportControl);

    $scope.events = {
      map: {
        enable: ['click', 'moveend', 'drag'],
        logic: 'emit'
      }
    }

    $scope.$on('leafletDirectiveMap.map.click', function (event, args) {
      Position.set(args.leafletEvent.latlng.lat, args.leafletEvent.latlng.lng, $scope.center.zoom);
      addReport();
    });

    $scope.$on('leafletDirectiveMap.map.moveend', function (event, args) {
      Position.set($scope.center.lat, $scope.center.lng, $scope.center.zoom);
    });

    $scope.$on('leafletDirectiveMap.map.drag', function (event, args) {
      // avoid infinite scrolling
      leafletData.getMap('map').then(function(map) {
        map.panInsideBounds(bounds, { animate: false });
      });
    });

    $scope.layers = {
      baselayers: {
        osm: {
          name: 'OpenStreetMap',
          type: 'xyz',
          url: 'https://api.mapbox.com/v4/mapbox.streets/{z}/{x}/{y}.png?access_token={accessToken}',
          layerOptions: {
            attribution: '',
            minZoom: 1,
            maxZoom: 18,
            accessToken: 'pk.eyJ1IjoiZWR1YmlmaSIsImEiOiJjaW9sbG9zaXUwMDJvdzlseWkxMHQyb2MwIn0.e-J4brbdwiVbdOEummCCWw',
          }
        }
      },
      overlays: {
        odours: {
          name: 'Odours',
          type: 'markercluster',
          layerOptions: {
            spiderfyOnMaxZoom: false,    // When you click a cluster at the bottom zoom level we spiderfy it so you can see all of its markers
            showCoverageOnHover: false,  // When you mouse over a cluster it shows the bounds of its markers
            disableClusteringAtZoom: 18, // On max zoom, markers will not be clustered
          },
          visible: true
        }
      }
    }

    $scope.markers = {};


    function refresh () {
      ReportsService.getReports().then(function (reports) {
        $timeout(function() { // wait until render
          drawMarkers(reports);
        });
      });
    }

    $rootScope.$on(REPORT_EVENTS.reportAdded, function() {
      ReportsService.clean();
      refresh();
    });

    function getGeoLocation () {
      var deferred = $q.defer();
      leafletData.getMap('map').then(function(map) {
        map.locate({setView: true, maxZoom: 16, watch: true, enableHighAccuracy: true});
        map.on('locationfound', function (e) {
          deferred.resolve(e.latlng);
        });
      });
      return deferred.promise;
    }

    function addReport () {
      if (AuthService.isAuthenticated()) {
        Dialogs.openAddReport();
      } else {
        Dialogs.openNoLogged();
      }
    }

    function drawMarkers(reports) {
      var markers = {};

      for (var i=0; i<reports.length; i++) {
        var report = reports[i]

        var marker = {
          layer: 'odours',
          lat: parseFloat(report.latitude),
          lng: parseFloat(report.longitude),
          getMessageScope: function () { return $scope; },
          message: '<strong>' + gettextCatalog.getString('User') + '</strong>: <strong>' + report.username + '</strong><br>' +
                    '<a href="#!entry/' + report.report_id + '" class="report-link">' +
                    '<strong>' + gettextCatalog.getString('Type') + '</strong>: ' + report.type + '<br>' +
                    '<strong>' + gettextCatalog.getString('Date') + '</strong>: ' + report.report_date + '<br>' +
                    '<strong>' + gettextCatalog.getString('Intensity') + '</strong> (1 - 6): ' + report.intensity + '<br>' +
                    '<strong>' + gettextCatalog.getString('Annoyance (-4 - 4)') + '</strong>: ' + report.annoyance + '<br>' +
                    '<strong>' + gettextCatalog.getString('Number of comments') + '</strong>: ' + report.number_comments +
                    '</a><br>' +
                    '<a href="" class="btn btn-xs btn-default btn-comment" ng-click="comment(' + report.report_id + ')">' + gettextCatalog.getString('Comment') + '</a>',
          compileMessage: true,
        }

        if (report.annoyance < -2) {
          marker.icon = goodIcon;
        } else if (report.annoyance > 2) {
          marker.icon = badIcon;
        } else {
          marker.icon = midIcon;
        }

        markers[report.report_id] = marker;
      }

      $scope.markers = markers;
    }

    $scope.filter = function () {
      if ($scope.filters.type == '') {
        return; // Do nothing
      }

      ReportsService.getReports().then(function (reports) {
        $timeout(function() { // wait until render
          var filteredReports = [];
          var definedTypes = ['Garbage', 'Sewage', 'Chemica', 'I do not know'];

          for (var i=0; i<reports.length; i++) {
            var report = reports[i];

            if ($scope.filters.type == 'All') {
              filteredReports.push(report);
            } else if ($scope.filters.type == report.type) {
              filteredReports.push(report);
            } else if ($scope.filters.type == 'Other' && definedTypes.indexOf(report.type) == -1) {
              filteredReports.push(report);
            }
          }

          drawMarkers(filteredReports);
        });
      });
    }

    $scope.comment = function (reportId) {
      if (AuthService.isAuthenticated()) {
        ReportsService.selectedId = reportId;
        Dialogs.openAddComment();
      } else {
        Dialogs.openNoLogged();
      }
    }


    refresh(); // Refresh on load
  })

  .controller('LastEntriesCtrl', function ($scope, $rootScope, REPORT_EVENTS, Position, ReportsService) {
    $scope.reports = [];
    $scope.nearbyfilter = 'all';

    $scope.refresh = function() {
      if ($scope.nearbyfilter == 'all') {
        var request = ReportsService.getReports();
      } else {
        var request = ReportsService.getNearbyReports(Position.lat, Position.lng);
      }

      request.then(function (reports) {
        $scope.reports = reports;
      });
    }

    $rootScope.$on(REPORT_EVENTS.reportAdded, function() {
      ReportsService.clean();
      refresh();
    });


    $scope.refresh(); // Refresh on load
  })

  .controller('EntryCtrl', function ($scope, $routeParams, ReportsService, AuthService, Dialogs) {
    $scope.report = null;

    function refreshReport() {
      ReportsService.getReport($routeParams.entryId).then(function (report) {
        $scope.report = report;
      }, function (error) {
        console.log(error);
      });
    }

    $scope.addComment = function () {
      if (AuthService.isAuthenticated()) {
        ReportsService.selectedId = $routeParams.entryId;
        Dialogs.openAddComment().closed.then(function() {
          refreshReport();
        });
      } else {
        Dialogs.openLogin();
      }
    }

    // Refresh on load
    refreshReport();
  })

  .controller('UserCtrl', function ($scope, $routeParams, ReportsService) {
    $scope.user = null;

    ReportsService.getUserReports($routeParams.userId).then(function (user) {
      $scope.user = user;
    }, function (error) {
      console.log(error);
    })
  })

  .controller('ProjectCtrl', function ($scope) {
  })

  .controller('MethodologyCtrl', function ($scope) {
  })

  .controller('LoginCtrl', function ($scope, $uibModalInstance, gettextCatalog, AuthService, Dialogs, Helpers) {
    $scope.credentials = {
      username: '',
      password: '',
    }

    $scope.errors = [];

    $scope.signup = function () {
      Dialogs.openSignUp().rendered.then(function () {
        $scope.close();
      });
    }

    $scope.login = function () {
      if ($scope.loginForm.$valid) {
        AuthService.login($scope.credentials).then(function () {
          $scope.close();
        }, function (error) {
          if (error.data.message == "Error! Username and password do not match.") {
            $scope.errors.push(gettextCatalog.getString('Username and password do not match.'));
          } else if (error.data.message == "Wrong username") {
            $scope.errors.push(gettextCatalog.getString('Wrong username.'));
          } else {
            $scope.errors.push(gettextCatalog.getString('Unexpected error.'));
          }
        });
      } else {
        Helpers.setFormFieldsDirty($scope.loginForm);
      }
    }

    $scope.close = function () {
      $uibModalInstance.dismiss();
    };
  })

  .controller('SignUpCtrl', function ($scope, $uibModalInstance, gettextCatalog, AuthService, Helpers) {
    $scope.user = {
      username: '',
      email: '',
      password: '',
      age: '',
      gender: '',
    };

    $scope.errors = [];

    $scope.signup = function () {
      if ($scope.signupForm.$valid) {
        AuthService.signup($scope.user).then(function () {
          // Signup ok -> login
          var credentials = {username: $scope.user.username, password: $scope.user.password};
          AuthService.login(credentials).then(function () {
            $scope.close();
          });
        }, function (error) {
          if (error.data.message == "Error! Username already exists.") {
            $scope.errors.push(gettextCatalog.getString('Username already exists.'));
          } else {
            $scope.errors.push(gettextCatalog.getString('Unexpected error.'));
          }
        });
      } else {
        Helpers.setFormFieldsDirty($scope.signupForm);
      }
    }

    $scope.close = function () {
      $uibModalInstance.dismiss();
    };
  })

  .controller('AddReportCtrl', function ($scope, $uibModalInstance, gettextCatalog, ReportsService, Session, Position, Dialogs, Helpers) {
    $scope.report = {
      type: '',
      other_type: '',
      intensity: 1,
      annoyance: -4,
      cloud: 1,
      rain: 0,
      wind: 0,
      origin: '',
      duration: '',
      datetime: '',
    }

    $scope.errors = [];

    $scope.add = function () {
      if ($scope.reportForm.$valid) {
        ReportsService.addReport($scope.report, Position.lat, Position.lng, Session.username).then(function () {
          Dialogs.openReportRecorded().rendered.then(function () {
            $scope.close();
          });
        }, function (error) {
          $scope.errors.push(gettextCatalog.getString('Unexpected error.'));
        });
      } else {
        Helpers.setFormFieldsDirty($scope.reportForm);
      }
    }

    $scope.close = function () {
      $uibModalInstance.dismiss();
    }
  })

  .controller('AddCommentCtrl', function ($scope, $uibModalInstance, gettextCatalog, ReportsService, Session, Dialogs, Helpers) {
    $scope.comment = '';
    $scope.errors = [];

    $scope.add = function () {
      if ($scope.commentForm.$valid) {
        ReportsService.addComment($scope.comment, ReportsService.selectedId, Session.username).then(function () {
          Dialogs.openCommentRecorded().rendered.then(function () {
            $scope.close();
          });
        }, function (error) {
          $scope.errors.push(gettextCatalog.getString('Unexpected error.'));
        });
      } else {
        Helpers.setFormFieldsDirty($scope.commentForm);
      }
    }

    $scope.close = function () {
      $uibModalInstance.dismiss();
    }
  })

  .controller('NoLoggedCtrl', function ($scope, $uibModalInstance, Dialogs) {
    $scope.close = function () {
      Dialogs.openLogin().rendered.then(function () {
        $uibModalInstance.dismiss();
      });
    }
  })

  .controller('ReportRecordedCtrl', function ($scope, $uibModalInstance) {
    $scope.close = function () {
      $uibModalInstance.dismiss();
    }
  })

  .controller('CommentRecordedCtrl', function ($scope, $uibModalInstance) {
    $scope.close = function () {
      $uibModalInstance.dismiss();
    }
  })

  .controller('GeolocationErrorCtrl', function ($scope, $uibModalInstance) {
    $scope.close = function () {
      $uibModalInstance.dismiss();
    }
  });
