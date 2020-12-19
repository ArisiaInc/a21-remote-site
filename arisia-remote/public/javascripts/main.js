$('.confirmModal').on('show.bs.modal', function (event) {
  var button = $(event.relatedTarget) // Button that triggered the modal
  var recipient = button.data('confirmid') // Extract info from data-* attributes
  // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
  // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
  var modal = $(this)
  modal.find('.modal-body-id').text(recipient)
  modal.find('.confirmed-button').data('who', recipient)
})

$('#remove-admin-button').click(function(event) {
  var button = $('#remove-admin-button'); // Button that triggered the modal
  var who = button.data('who');
  console.log("The person we are removing is " + who);
  $.ajax({
    url: '/admin/manageAdmins/' + who,
    type: 'DELETE',
    complete: function() {
      var url = window.location.href;
      window.location.href = url;
    }
  });
});

$('#remove-button').click(function(event) {
  var button = $('#remove-button'); // Button that triggered the modal
  var who = button.data('who');
  var baseUrl = button.data('baseurl')
  console.log("The person we are removing is " + who);
  $.ajax({
    url: baseUrl + who,
    type: 'DELETE',
    complete: function() {
      var url = window.location.href;
      window.location.href = url;
    }
  });
});
