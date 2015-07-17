function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

if (!String.prototype.endsWith) {
	String.prototype.endsWith = function(pattern) {
		var d = this.length - pattern.length;
		return d >= 0 && this.lastIndexOf(pattern) === d;
	};
}

var ogBranch;

$(document).ready(function() {
	var $dropdown = $('#branchDropdown');
	ogBranch = $dropdown.val();

	$dropdown.on('change', function() {
		if (this.value !== "") {
			var path = window.location.pathname;
			var isFile = getParameterByName('file');

			if (path.endsWith('/tree')) {
				var newPath = path + '/' + this.value;
				if (isFile === 'true') {
					newPath += '?file=true';
				}
				window.location.href = newPath;
			} else {
				var newPathWithBranch = path.replace('/tree/' + ogBranch, '/tree/' + this.value);
				if (isFile === 'true') {
					newPathWithBranch += '?file=true';
				}
				window.location.href = newPathWithBranch;
			}
		}
	});
});