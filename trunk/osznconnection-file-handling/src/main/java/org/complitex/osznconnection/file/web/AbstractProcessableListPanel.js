function selectAll(cb){
    var items = document.getElementsByTagName('input');
    for (i=0; i< items.length; i++){
        if (items[i].type == 'checkbox' && items[i].id != null && ~items[i].id.indexOf("select") && !items[i].disabled){
            items[i].checked = cb.checked;
        }
    }
}