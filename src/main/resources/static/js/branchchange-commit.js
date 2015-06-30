function getCommitsRoot() {
	var path = window.location.pathname;
	return path.substring(0, path.indexOf('/commits'));
}

var ogBranch;
$(document).ready(function() {
	ogBranch = $('#branchDropdown').val();
	$('#branchDropdown').on('change', function() {
		if (this.value !== "") {
			window.location.href = getCommitsRoot() + '/commits?selected=' + this.value;
		} else {
			window.location.href = getCommitsRoot() + '/commits';
		}
	});
});