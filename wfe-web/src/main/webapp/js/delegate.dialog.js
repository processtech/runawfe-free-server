$.delegateDialog = undefined;
function delegateTaskDialog(btn) {
    var button = $(btn);
    var taskId = parseInt(button.attr("data-taskid"));
    if($.delegateDialog == undefined) {
        var dialog = $("<div>", {id : "delegateDialog"});
        $('<input class="filter" placeholder="' + $.delegateDialogStatic.filter+'" />' +
        '<label><input class="keepCurrent" type="checkbox">' + $.delegateDialogStatic.addCurrentExecutors+'</label>' +
        '<table><tr>' +
        '<td>' +
        '<fieldset class="set search">' +
        '<legend>' + $.delegateDialogStatic.searchExecutors + '</legend>' +
        '<fieldset class="actor">' +
        '<legend>'+$.delegateDialogStatic.users+'</legend>' +
        '<div class="paging"></div>' +
        '<ul class="actor">' +
        '</ul>' +
        '</fieldset>' +
        '<fieldset class="group">' +
        '<legend>'+$.delegateDialogStatic.groups+'</legend>' +
        '<div class="paging"></div>' +
        '<ul class="group">' +
        '</ul>' +
        '</fieldset>' +
        '</fieldset>' +
        '</td>' +
        '<td>' +
        '<fieldset class="set selected">' +
        '<legend>'+$.delegateDialogStatic.selectedExecutors+'</legend>' +
        '<fieldset class="actor">' +
        '<legend>'+$.delegateDialogStatic.users+'</legend>' +
        '<ul class="actor">' +
        '</ul>' +
        '</fieldset>' +
        '<fieldset class="group">' +
        '<legend>'+$.delegateDialogStatic.groups+'</legend>' +
        '<ul class="group">' +
        '</ul>' +
        '</fieldset>' +
        '</fieldset>' +
        '</td>' +
        '</table>').appendTo(dialog);
        $.delegateDialog = dialog.dialog({
            width: 600,
            height: 500,
            buttons: [
                {
                    text: $.delegateDialogStatic.delegate,
                    click: function() {
                        submitDelegateDialog();
                    }
                },
            	{
                    text: $.delegateDialogStatic.unselect,
                    click: function() {
                        reset();
                    }
                }
            ]
        });
    }


    var selected = {};

    var tempData = {};

    var d = $.delegateDialog;
    var filter = d.find("input.filter");

    var searchListActor = d.find("fieldset.search").find("ul.actor");
    var searchListGroup = d.find("fieldset.search").find("ul.group");

    var selectedListActor = d.find("fieldset.selected").find("ul.actor");
    var selectedListGroup = d.find("fieldset.selected").find("ul.group");

    var revActor = 0;
    var revGroup = 0;
    
    var groupPage = 0;
    var actorPage = 0;

    filter.val("");
    var hint = undefined;

    function request(data, rev, callback) {
        data = $.extend({perPage: 10, page: 0, excludeme: true}, data);
        $.ajax({
            type:"POST",
            url:"/wfe/ajaxcmd?command=ajaxActorsList",
            data: JSON.stringify(data),
            dataType:"json",
            contentType:"application/json; charset=UTF-8",
            processData: false,
            success: function(data){callback(data, rev);}
        });
    }

    function createLI(id, type, fieldset, labelText, onchange) {
        var li = $("<li>");
        var label = $("<label>").appendTo(li);
        var checkbox = $("<input>", {type:"checkbox", "data-executorid": id, name : fieldset+"_"+type+"_"+id, checked : selected[id]!=undefined}).appendTo(label);
        var span = $("<span>").appendTo(label);
        checkbox.change(onchange);
        span.html(labelText);
        return li;
    }

    function sortBy(arr, field) {
        arr.sort(function(a,b){
            return a[field] > b[field] ? 1 : -1;
        });
    }

    function poppulateUL(data, ul, fieldset, field) {
        $.each(data, function(k,v) {
            tempData[v.id]=v;
            createLI(v.id, v.type, fieldset, v[field], onchange).appendTo(ul);
        })
    }

    function createPagingElement(page) {
        var text="";
        if(typeof page == "number") text=(page+1).toString();
        return $("<span>").html(text);
    }

    function updatePaging(block, page, totalPages, maxpages, callback) {
        block.empty();
        var side = (maxpages-1)/2;
        
        var start = page-side;
        if(start<0) start=0;
        
        var firstVisible = page>side;
        var lastVisible = (start+maxpages-1) < totalPages;
        var nextVisible = page+1<totalPages;
        var prevVisible = page>0;

        var i;

        if(totalPages<=1) return;

        function processLink(el, classes, value) {
            el.addClass(classes);
            if(value!=undefined) {
                el.addClass("paging-active");
                el.click(function() {callback(value);});
            }
            if(el.hasClass("ui-icon")) {
				var helper = $("<span>", {class:"icon-helper"});
				el.appendTo(helper);
				el=helper;
			}
            el.appendTo(block);
        }

        if(prevVisible) processLink(createPagingElement(""), "ui-icon ui-icon-seek-prev", page-1);
        if(firstVisible) processLink(createPagingElement(0), "", 0);

        for(i=0; (i<maxpages && start+i < totalPages); i++) {
            var p = start+i;
            var act = p;
            if(p==page) act=undefined;
            processLink(createPagingElement(p), "", act);
        }

        if(lastVisible) processLink(createPagingElement(totalPages-1), "", totalPages-1);
        if(nextVisible) processLink(createPagingElement(""), "ui-icon ui-icon-seek-next", page+1);
    }

    function updateSelectedList() {
        var data = {actor: [], group: []};
        $.each(selected, function(k,v) {
            data[v.type].push(v);
        });
        sortBy(data.actor, "fullname");
        sortBy(data.group, "name");

        selectedListActor.empty();
        selectedListGroup.empty();

        poppulateUL(data.actor, selectedListActor, "selected", "fullname");
        poppulateUL(data.group, selectedListGroup, "selected", "name");
    }

    function onchange() {
        var el = $(this);
        var checked = el.is(":checked");
        var name = el.attr("name");
        var opts = name.split("_");
        var fieldset = opts[0];
        var type = opts[1];
        var id = parseInt(opts[2]);

        if(checked) {
            if(!selected[id]) {
                selected[id] = tempData[id];
                updateSelectedList();
            }
        } else {
            if(selected[id]) {
                delete selected[id];
                d.find("input[data-executorid="+id+"]").attr("checked", false);
                updateSelectedList();
            }
        }
    }

    function processActorData(data, rev) {
        if(revActor != rev) return;
        searchListActor.empty();
        var paging = searchListActor.parent().find(".paging");
        updatePaging(paging, data.page, data.totalPages, 5, function(page) {
				actorPage = page;
				reloadActorList();
		});
        poppulateUL(data.data, searchListActor, "search", "fullname");
    }

    function processGroupData(data, rev) {
        if(revGroup != rev) return;
        searchListGroup.empty();
        var paging = searchListGroup.parent().find(".paging");
        updatePaging(paging, data.page, data.totalPages, 5, function(page) {
				groupPage = page;
                reloadGroupList();
		});
        poppulateUL(data.data, searchListGroup, "search", "name");
    }

	function reloadActorList() {
		request({target:"actor", hint: hint, page : actorPage}, ++revActor, processActorData);
	}
	
	function reloadGroupList() {
		request({target:"group", hint: hint, page : groupPage}, ++revGroup, processGroupData);
	}

    function reload() {
        var newHint = filter.val();
        if(newHint==hint) return;
        hint=newHint;
        tempData={};
        actorPage = 0;
        groupPage = 0;
        reloadActorList();
        reloadGroupList();
        
    }

	function submitDelegateDialog() {
		var data = {};
		data.executors = [];
		$.each(selected, function(k) {
			data.executors.push(parseInt(k));
		});
		data.keepCurrent = d.find("input.keepCurrent").is(":checked");
		data.taskId = taskId;
		
		$.ajax({
			type:"POST",
			url:"/wfe/delegateTask",
			data: JSON.stringify(data),
			dataType:"json", 
			contentType:"application/json; charset=UTF-8",
			success: function() {
					window.location = "/wfe/manage_tasks.do";
			}
		});
	}

	function reset() {
		hint = undefined;
		filter.val("");
		selected={};
		tempData={};
		searchListActor.empty();
		searchListGroup.empty();
		selectedListActor.empty();
		selectedListGroup.empty();
		d.find("input.keepCurrent").attr("checked", true);
		reload();
	}

    filter.on("keyup", reload);
    filter.on("change", reload);

    reset();
    
    $.delegateDialog.dialog("open");
}
