$(function(){
    $(document).delegate("input.processable-list-panel-select-all:checkbox", "click", function(){
        var selectAll = $(this);
        selectAll.closest("table")
            .find("input.processable-list-panel-select:checkbox:enabled")
            .attr("checked", selectAll.is(":checked"))
            .change();
    });
    
    $(document).delegate("input.processable-list-panel-select:checkbox:enabled", "click", function(){
       $(this).closest("table")
            .find("input.processable-list-panel-select-all:checkbox")
            .attr("checked", false);
    });
});