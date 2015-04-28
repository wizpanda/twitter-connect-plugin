var exec = require('cordova/exec');

var TwitterConnect = {
	login: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterConnect', 'login', []);
	},
	logout: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterConnect', 'logout', []);
	},
	compose: function(successCallback, errorCallback, message, image, url) {
		exec(successCallback, errorCallback, 'TwitterConnect', 'compose', [message, image, url]);
	}
};

module.exports = TwitterConnect;