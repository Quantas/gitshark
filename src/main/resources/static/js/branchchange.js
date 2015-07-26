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

            if (path.endsWith('/tree')) {
                window.location.href = path + '/' + this.value;
            } else {
                window.location.href = path.replace('/tree/' + findRef(path), '/tree/' + this.value);
            }
        }
    });
});