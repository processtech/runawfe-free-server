if (!window.console) {
	window.console = {};
}

if (!window.console.log) {
	window.console.log = function(msg) {};
}

if (!window.console.info) {
	window.console.info = function(msg) {};
}
if (!window.console.debug) {
	window.console.debug = function(msg) {};
}
if (!window.console.error) {
	window.console.error = function(msg) {};
}