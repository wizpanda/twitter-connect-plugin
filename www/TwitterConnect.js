var exec = require('cordova/exec');

var TwitterConnect = function(){};

TwitterConnect.prototype.login = function(successCallback, errorCallback){
	console.log("TwitterConnect.js: login");
	exec(function (result) {
		console.log("Twitter: Successful login! " +JSON.stringify(result));
		successCallback(result);
	}, function (result) {
		console.log("Twitter: Failed login: " + JSON.stringify(result));
		errorCallback(result);
	}, "TwitterConnect", 'login', []);
};

module.exports = TwitterConnect;