AUI().ready('io', function(A) {
    A.all('.usd-issues a').on('click', function(e) {
        e.halt();
        var url = e.target.get('href');
        if (e.target.getDOM().tagName != 'A') {
            url = e.target.ancestor('a').get('href');
        }
        A.io.request('${bopsIdUrl}', {
            cache: false,
            sync: true,
            timeout: 5000,
            dataType: 'json',
            method: 'get',
            on: {
                success: function() {
                    url += this.get("responseData");
                    window.open(url);
                },
                failure: function() {
                    window.open(url);
                }
            }
        });
    });
});