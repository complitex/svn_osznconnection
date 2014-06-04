$(function(){
    (function(){
        $("table tr.data-row").on('click', 'a.data-row-link',function(){
            var link = $(this);
            var row = link.closest("tr.data-row");
            row.addClass("data-row-hover");
        });

        $("table tr.data-row-link").bind("click", function(){
            var row = $(this);
            row.addClass("data-row-hover");
        });
    })();
});