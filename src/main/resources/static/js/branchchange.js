function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function findRef(path) {
    var pathArray = path.split('/');
    for (var i = 0; i < pathArray.length; i++) {
        if (pathArray[i] === 'tree') {
            return pathArray[i+1];
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
            var isFile = getParameterByName('file');

            if (path.endsWith('/tree')) {
                var newPath = path + '/' + this.value;
                if (isFile === 'true') {
                    newPath += '?file=true';
                }
                window.location.href = newPath;
            } else {
                var newPathWithBranch = path.replace('/tree/' + findRef(path), '/tree/' + this.value);
                if (isFile === 'true') {
                    newPathWithBranch += '?file=true';
                }
                window.location.href = newPathWithBranch;
            }
        }
    });
});