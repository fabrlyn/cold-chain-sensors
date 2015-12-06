'use strict';

angular.module('plantIT').controller('MainController', function ($scope, $http, $location, $interval) {
	$scope.transports = [];
	$scope.rawTransports = [];
	$scope.active = 'Moving';
	$scope.archived = 'Delivered';
	$scope.all = 'All';
	$scope.currentTab = $scope.active;
	$scope.isTrue = true;
	$scope.minTempColor = '#00B0FF';
	$scope.maxTempColor = '#D50000';
	$scope.avgTempColor = '#76FF03';
	$scope.data = [];
	$scope.lineColors = ['#673AB7', '#607D8B', '#FF5722', '#2196F3', '#43A047'];
	var waitTime = 12000;
	
	$scope.navigateTo = function ( path ) {          
                $location.path( path );
        };
    $scope.dashboardLink = '/';
    $scope.businessLink = '/package';
	
	function pollData(){
		if($scope.data && $scope.readings.length > 0){
			updateGraph();
		}
	}
	
    $interval(pollData, waitTime);

    $scope.$on('$destroy', function () {
      // cancel the interval
      $interval.cancel(waitTime);
    });
	
	function parseTransports(rawTransports){
		var transports = []
		for(var i = 0; i < rawTransports.length; i++){
			var transport = {};
			transport.id = rawTransports[i].id;
			transport.transportId = rawTransports[i].name;
			transport.startDate = rawTransports[i].start_date;
			transport.startDateHumanized = moment(rawTransports[i].start_date).fromNow();
			transport.endDate = rawTransports[i].end_date;
			if(moment(transport.endDate).isValid()){
				transport.endDateHumanized = moment(rawTransports[i].end_date).fromNow();	
			}
			transport.startLocation = rawTransports[i].location_start;
			transport.endLocation = rawTransports[i].location_end;
			transport.packages = [
			
			];
			/*
			<td>{{package.device_id}}</td>
                                            <td>{{package.name}}</td>
                                            <td>{{package.amount}}</td>
                                            <td>{{package.requirement}}</td>
                                            <td>
                                                <span ng-class="{'package-status-tbl': isTrue, 'error': package.alarm === 3, 'warning': package.alarm === 2, 'success': package.alarm === 1}" 
                                                data-tooltip="Requirement not satisfied!"></span>
                                           </td>
			*/
			transport.alarm = 1;
			addPackagesToTransport(transport);
			transports.push(transport);
		}
		return transports;
	}
	
	function addPackagesToTransport(transport){
		$http.get('http://openhack-plantit.mybluemix.net/transport/' + transport.id).success(function(packageQueryResults){
			transport.packages = packageQueryResults.result;
			
			addMedicinesToPackages(transport);
		});
	}
	
	function addMedicinesToPackages(transport){
		for(var i = 0; i < transport.packages.length; i++){
			addMedicinesToPackage(transport, transport.packages[i]);
		}
		transport.packages.push({
					device_id: '98:12:51:23:12',
					name: 'Amoxlin',
					requirement: 'F',
					dosage: '300mg',
					alarm: 1
				});
				transport.packages.push({
					device_id: '98:12:14:41:12',
					name: 'Noflaxin',
					requirement: 'A',
					dosage: '200mg',
					alarm: 1
				});
	}
	
	function addMedicinesToPackage(transport, packageVar){
		$http.get('http://openhack-plantit.mybluemix.net/package/' + packageVar.id).success(function(medicineQueryResults){
			var packageInfo = medicineQueryResults.result[0];
			packageVar.alarm = packageInfo.alarm;
			packageVar.amount = packageInfo.amount;
			packageVar.name = packageInfo.name;
			packageVar.requirement = packageInfo.requirement;
			packageVar.startDate = packageInfo.start_date;
			if(packageVar.alarm > transport.alarm){
				transport.alarm = packageVar.alarm;
			}
		});
	}
	
	function filterTransports(transports){
		var filteredTransports = []
		for(var i = 0; i < transports.length; i++){
			if(!shouldFilterTransport(transports[i])){
				filteredTransports.push(transports[i]);
			}
		}
		return filteredTransports;
	}
	
	function shouldFilterTransport(transport){
		return shouldFilterByTab(transport);
	}
	
	function shouldFilterByTab(transport){
		if($scope.currentTab === $scope.all){
			return false;
		} else if($scope.currentTab === $scope.active && transport.endDate){
			return true;
		} else if($scope.currentTab === $scope.archived && !transport.endDate){
			return true;
		}
		return false;
	}
	
	/*$scope.$watch('rawTransports', function(newFilter){
      $scope.transports = filterTransports($scope.rawTransports);
    });*/
	
	$scope.$watch('currentTab', function(newFilter){
      $scope.transports = filterTransports($scope.rawTransports);
    });
	
	$http.get('http://openhack-plantit.mybluemix.net/list/transport').success(function(transportQueryResults){
		$scope.rawTransports = parseTransports(transportQueryResults.result);
		$scope.transports = filterTransports($scope.rawTransports);
	});
	
	$scope.readings = [];
	
	$scope.options = {
            chart: {
                type: 'lineChart',
                height: 200,
                margin : {
                    top: 20,
                    right: 20,
                    bottom: 40,
                    left: 55
                },
				noData: 'Loading data...',
                x: function(d){ return d.x; },
                y: function(d){ return d.y; },
                useInteractiveGuideline: false, //TODO: currently does not work correctly turned on
                dispatch: {
                    stateChange: function(e){ console.log("stateChange"); },
                    changeState: function(e){ console.log("changeState"); },
                    tooltipShow: function(e){ console.log("tooltipShow"); },
                    tooltipHide: function(e){ console.log("tooltipHide"); }
                },
                xAxis: {
                    axisLabel: 'Time',
					tickFormat: function(d){
					//	return parseUnixTime(d);
						return d3.time.format('%m/%d %X')(new Date(d)); //%d-%m-%y
					},
					ticks: 5,
					showMaxMin: false
                },
                yAxis: {
                    axisLabel: 'Temperature (°C)',
					axisLabelDistance: -10
					/*,
                    tickFormat: function(d){
                        return d3.format('.02f')(d);
                    },
                    axisLabelDistance: -10*/
                },
				forceY: [20],
                callback: function(chart){
                    console.log("!!! lineChart callback !!!");
                }
            },
            title: {
                enable: false,
                text: 'Title for Line Chart'
            },
            subtitle: {
                enable: false,
                text: 'Subtitle for simple line chart. Lorem ipsum dolor sit amet, at eam blandit sadipscing, vim adhuc sanctus disputando ex, cu usu affert alienum urbanitas.',
                css: {
                    'text-align': 'center',
                    'margin': '10px 13px 0px 7px'
                }
            },
            caption: {
                enable: false,
                html: '<b>Figure 1.</b> Lorem ipsum dolor sit amet, at eam blandit sadipscing, <span style="text-decoration: underline;">vim adhuc sanctus disputando ex</span>, cu usu affert alienum urbanitas. <i>Cum in purto erat, mea ne nominavi persecuti reformidans.</i> Docendi blandit abhorreant ea has, minim tantas alterum pro eu. <span style="color: darkred;">Exerci graeci ad vix, elit tacimates ea duo</span>. Id mel eruditi fuisset. Stet vidit patrioque in pro, eum ex veri verterem abhorreant, id unum oportere intellegam nec<sup>[1, <a href="https://github.com/krispo/angular-nvd3" target="_blank">2</a>, 3]</sup>.',
                css: {
                    'text-align': 'justify',
                    'margin': '10px 13px 0px 7px'
                }
            }
        };
		$scope.packageData = [];
		
		function pushData(readings){
			$scope.deviceIds = {};
			
	       /* var minPoints = [];
        	minPoints.length = readings.length;
			var maxPoints = [];
			minPoints.length = readings.length;
			var avgPoints = [];
			avgPoints.length = readings.length;*/
			var idCounter = 0;
			//console.log(readings);
			for(var i = 0; i < readings.length; i++){
				var dateTime = readings[i].date;//moment(readings[i].date).toDate();
				var deviceId = readings[i].device_id;
				var index = $scope.deviceIds[deviceId];
				if(!index && index !== 0){
					index = idCounter++;
					$scope.deviceIds[deviceId] = index;
					$scope.packageData[index] = {minPoints: [], maxPoints: [], avgPoints: []};
				//	packageData[index].minPoints.length = readings.length;
				//	packageData[index].minPoints.length = readings.length;
				//	packageData[index].avgPoints.length = readings.length;
				}
				var avgString = numeral(readings[i].value_avg).format('0[.]00 a');
				var avgNum = numeral().unformat(avgString);
				$scope.packageData[index].minPoints.push({x: dateTime, y: readings[i].value_min});
				$scope.packageData[index].maxPoints.push({x: dateTime, y: readings[i].value_max});
				$scope.packageData[index].avgPoints.push({x: dateTime, y: avgNum});
		
				$scope.packageData[index].deviceId = readings[i].device_id;
			}
		//	console.log('ids');
		//	console.log(deviceIds);
		//	console.log(packageData);
			var data = [];
			for(var j = 0; j < idCounter; j++){
		//		console.log(packageData[j].avgPoints.length);
				data.push({
					values: $scope.packageData[j].avgPoints,
					key: $scope.packageData[j].deviceId,
					color: $scope.lineColors[j % $scope.lineColors.length]
				});
			}
			
		}

        function placeData(readings){
			$scope.deviceIds = {};
			var packageData = [];
			
	       /* var minPoints = [];
        	minPoints.length = readings.length;
			var maxPoints = [];
			minPoints.length = readings.length;
			var avgPoints = [];
			avgPoints.length = readings.length;*/
			var idCounter = 0;
			//console.log(readings);
			for(var i = 0; i < readings.length; i++){
				var dateTime = readings[i].date;//moment(readings[i].date).toDate();
				var deviceId = readings[i].device_id;
				var index = $scope.deviceIds[deviceId];
				if(!index && index !== 0){
					index = idCounter++;
					$scope.deviceIds[deviceId] = index;
					packageData[index] = {minPoints: [], maxPoints: [], avgPoints: []};
				//	packageData[index].minPoints.length = readings.length;
				//	packageData[index].minPoints.length = readings.length;
				//	packageData[index].avgPoints.length = readings.length;
				}
				var avgString = numeral(readings[i].value_avg).format('0[.]00 a');
				var avgNum = numeral().unformat(avgString);
				packageData[index].minPoints.push({x: dateTime, y: readings[i].value_min});
				packageData[index].maxPoints.push({x: dateTime, y: readings[i].value_max});
				packageData[index].avgPoints.push({x: dateTime, y: avgNum});
		
				packageData[index].deviceId = readings[i].device_id;
			}
			$scope.packageData = packageData;
		//	console.log('ids');
		//	console.log(deviceIds);
		//	console.log(packageData);
			var data = [];
			for(var j = 0; j < idCounter; j++){
		//		console.log(packageData[j].avgPoints.length);
				data.push({
					values: packageData[j].avgPoints,
					key: packageData[j].deviceId,
					color: $scope.lineColors[j % $scope.lineColors.length]
				});
			}
			$scope.data = data;
			/*$scope.data = [
				{
					values: minPoints,
					key: 'Minimum temperatures',
					color: '#00B0FF'
				},
				{
					values: maxPoints,
					key: 'Maximum temperatures',
					color: '#D50000'
				},
				{
					values: avgPoints,
					key: 'Average temperatures',
					color: '#76FF03'
				}
			];*/
		}        		
		
	var readingQuery = {
		"selector": {
			"date": {
			"$gt": 0
			}
		},
		"fields": [
			"_id",
			"device_id",
			"date",
			"value_avg",
			"value_min",
			"value_max"
		],
		"sort": [
			{
			"date": "desc"
			}
		]
	};
	
	function pushDataToGraph(){
		$http.post('https://ba28d985-d76e-4878-bedb-b702b24202d7-bluemix.cloudant.com/sensors/_find', readingQuery).success(function(readingsQueryResult){
			var newReadings = [];
			var offset = readingsQueryResult.docs.length - $scope.readings.length;
			if(offset > 0){
				$scope.readings = readingsQueryResult.docs;
				var start = $scope.readings.length;
				for(var i = 0; i < offset; i++){
					var newReading = readingsQueryResult.docs[start + i];
					newReadings.push(newReading);
					$scope.readings.push(newReading)
				}
				pushData(newReadings);
			}
			
		});
	}
	
	function updateGraph(){
		$http.post('https://ba28d985-d76e-4878-bedb-b702b24202d7-bluemix.cloudant.com/sensors/_find', readingQuery).success(function(readingsQueryResult){
			$scope.readings = readingsQueryResult.docs;
			placeData($scope.readings);
		});
	}
	
	updateGraph();
	
});