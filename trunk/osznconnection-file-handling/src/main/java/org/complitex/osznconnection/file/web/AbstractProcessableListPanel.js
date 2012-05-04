$(function(){
    $("input.processable-list-panel-select-all:checkbox").live("click", function(){
        var selectAll = $(this);
        selectAll.closest("table")
            .find("input.processable-list-panel-select:checkbox:enabled")
            .attr("checked", selectAll.is(":checked"))
            .change();
    });
});