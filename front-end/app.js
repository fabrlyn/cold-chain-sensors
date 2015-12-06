'use strict';

angular.module('plantIT', ['ngRoute', 'angularMoment', 'nvd3'])
.config(function ($routeProvider) {
    $routeProvider
      .when('/', {
            templateUrl: 'views/package.html',
            controller: 'PackageController'
      })
      .when('/package', {
		templateUrl: 'views/main.html',
		controller: 'MainController'
      })
      
      .otherwise({
      	redirectTo: '/'
      });
});
