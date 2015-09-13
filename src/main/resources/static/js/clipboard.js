$(document).ready(function() {
    $("time.timeago").timeago();

    $('[data-toggle="tooltip"]').tooltip();

    $('#logoutLink').on("click", function(e) {
        e.preventDefault();
        $('#logoutForm').submit();
    });

    new ZeroClipboard($(".copy-button"));
});