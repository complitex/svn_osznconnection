$(function(){
    (function(){
        $("table tr.data-row a.data-row-link").live("click", function(){
            var link = $(this);
            var row = link.closest("tr.data-row");
            row.addClass("data-row-hover");
        });
    })();
});