
// Open external links in new windows / tabs
$(document).ready(function () {
  $('a[href^="http://"], a[href^="https://"]').not('a[class*=internal]').attr('target', '_blank');
});