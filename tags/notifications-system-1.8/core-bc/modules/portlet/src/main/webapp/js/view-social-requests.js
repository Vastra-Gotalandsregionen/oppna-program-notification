AUI().ready('io', function(A) {
    var test = A.all('a.request-choice');
    A.all('a.request-choice').on('click', function(e) {
        e.halt();
        var url = e.target.get('href');
        A.io.request(url, {
            cache: false,
            sync: true,
            timeout: 5000,
            method: 'post',
            on: {
                success: function(event, id, xhr) {
                    var msg = xhr.responseText;
                    e.target.ancestor().ancestor().html(msg);
                },
                failure: function() {

                }
            }
        });
    });
});