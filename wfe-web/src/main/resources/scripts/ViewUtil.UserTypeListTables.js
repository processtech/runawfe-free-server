$(document).ready(function() {
	$('#UNIQUENAME').tablesorter({
		HEADERS
		sortList: [[SORTCOLUMN,0]]
	});
	
	$('#UNIQUENAME input[name="check_all"]').on('click', function(){
	      $('#UNIQUENAME tbody input[type="checkbox"]').prop('checked', this.checked);
	   });
	
	$('#UNIQUENAME tbody').on('change', 'input[type="checkbox"]', function(){
	      if(!this.checked){
	         var el = $('#UNIQUENAME input[name="check_all"]').get(0);
	         if(el && el.checked && ('indeterminate' in el)){
	            el.indeterminate = true;
	         }
	      }
	   }); 
});
