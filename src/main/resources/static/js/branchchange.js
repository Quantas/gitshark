function findRefIndex(pathArray) {
	for (var i = 0; i < pathArray.length; i++) {
        if (pathArray[i] === 'tree' || pathArray[i] === 'history') {
        	return i + 1;
        }
    }
}

if (!String.prototype.endsWith) {
    String.prototype.endsWith = function (pattern) {
        var d = this.length - pattern.length;
        return d >= 0 && this.lastIndexOf(pattern) === d;
    };
}

$(document).ready(function () {
    var $dropdown = $('#branchDropdown');

    $dropdown.on('change', function () {
        if (this.value !== "") {
            var path = window.location.pathname;

            if (path.endsWith('/tree')) {
                window.location.href = path + '/' + this.value;
            } else {
            	var pathArray = path.split('/');
            	var refIndex = findRefIndex(pathArray);
                var newLocation = '';
                
            	// yes i starts at 1, browsers are dumb...
                for (var i = 1; i < pathArray.length; i++) {
                	if (i == refIndex) {
                		newLocation += '/' + this.value;
                	} else {
                		newLocation += '/' + pathArray[i];
                	}
                }
                
                window.location.href = newLocation;
            }
        }
    });
});