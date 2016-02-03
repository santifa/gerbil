<%@page import="org.aksw.gerbil.web.ExperimentTaskStateHelper"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<head>
    <title>Overview</title>
    <link rel="stylesheet"
	        href="/gerbil/webjars/bootstrap/3.2.0/css/bootstrap.min.css">
    <link rel="icon" type="image/png"
	        href="/gerbil/webResources/gerbilicon_transparent.png">
    <script type="text/javascript" src="/gerbil/webjars/jquery/2.1.1/jquery.min.js"></script>
    <script type="text/javascript" src="/gerbil/webjars/bootstrap/3.2.0/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/gerbil/webResources/js/gerbil.color.js"></script>
    <script type="text/javascript" src="/gerbil/webResources/js/slidr.min.js"></script>
    <script type="text/javascript" src="/gerbil/webResources/js/highcharts.js"></script>
    <script type="text/javascript" src="/gerbil/webResources/js/highcharts-more.js"></script>
    <script type="text/javascript" src="/gerbil/webResources/js/charts.js""></script>
    <script type="text/javascript" src="/gerbil/webResources/js/highcharts-3d.js"></script>
</head>
<style>
 table {
	   table-layout: fixed;
 }
 
 .table>thead>tr>th {
	   vertical-align: middle !important;
	   height: 280px;
	   width: 20px !important;
 }

 .rotated_cell div {
	   display: block;
	   transform: rotate(270deg);
	   -moz-transform: rotate(270deg);
	   -ms-transform: rotate(270deg);
	   -o-transform: rotate(270deg);
	   -webkit-transform: rotate(270deg);
	   width: 250px;
	   position: relative;
	   top: -10;
	   left: -100;
 }

 .col-md-12 {
	   padding: 5px 0px;
 }

 .chartDiv {
     padding-top: 50px;
     left: 50px;
	   vertical-align: center;
 }
 
 .chartBody {
     vertical-align: center;
     overflow: hidden;
     width: 100%;
	   margin: 0;
     font-size: 14px;
	   font-family: "Helvetica Neue", Helvetica;
 }
</style>

<body>
	  <div class="container">
		    <!-- mappings to URLs in back-end controller -->
		    <c:url var="experimentoverview" value="/experimentoverview" />
		    <c:url var="matchings" value="/matchings" />
		    <c:url var="exptypes" value="/exptypes" />
        <c:url var="filters" value="/filters" />
        <c:url var="compare" value="/compare" />
        <c:url var="filtermetadata" value="/filtermetadata" />
        <c:url var="ambiguityEntities" value="/ambiguityEntities" />
        <c:url var="ambiguitySurface" value="/ambiguitySurface" />
        <c:url var="diversityEntities" value="/diversityEntities" />
        <c:url var="diversitySurface" value="/diversitySurface" />
        
		    <%@include file="navbar.jsp"%>
		    <h1>GERBIL Experiment Overview</h1>
		    <div class="form-horizontal">
			      <div class="col-md-12">
				        <div class="control-group">
					          <label class="col-md-4 control-label">Experiment Type</label>
					          <div id="expTypes" class="col-md-8"></div>
				        </div>
			      </div>

			      <div class="col-md-12">
				        <div class="control-group">
					          <label class="col-md-4 control-label">Matching</label>
					          <div id="matching" class="col-md-8"></div>
				        </div>
			      </div>
			      <!-- filter selection -->
			      <div class="col-md-12">
			          <div class="control-group">
			              <label class="col-md-4 control-label">Filter</label>
			              <div id="filter" class="col-md-8"></div>
			          </div>
			      </div>
			      <div class="col-md-12">
				        <div class="control-group">
					          <label class="col-md-4 control-label"></label>
					          <div class="col-md-8">
						            <button id="show" type="button" class="btn btn-default">Show table!</button>
					          </div>
				        </div>
			      </div>
			      <div class="col-md-12">
				        <h2>F1-measures</h2>
				        <p>The table as well as the diagram contain the micro F1-measure.</p>
			      </div>
            
			      <div class="container-fluid">
				        <div id="resultsChartBody" class="chartBody"></div>
			      </div>
		    </div>
	  </div>
	  <div class="container-fluid">
		    <table id="resultsTable" class="table table-hover table-condensed">
			      <thead></thead>
			      <tbody></tbody>
		    </table>
	  </div>
	  <div class="container">
		    <div class="form-horizontal">
			      <div class="col-md-12">
				        <h2>Annotator &ndash; Dataset feature correlations</h2>
				        <p>The table as well as the diagram contain the pearson correlations between the annotators and the dataset features. Note that the diagram only shows the absolute values of the correlation. For the correlation type, you should take a look at the table.</p>
			      </div>
			      <div class="container-fluid">
				        <div id="correlationsChartBody" class="chartBody">
					          <div id="correlationsChart" class="chartDiv"></div>
				        </div>
			      </div>
		    </div>
	  </div>
	  <div class="container-fluid">
		    <table id="correlationsTable" class="table table-hover table-condensed">
			      <thead></thead>
			      <tbody></tbody>
		    </table>
	  </div>

    <script type="text/javascript">
     
     function loadMatchings () {
	       $.getJSON('${matchings}', {
	           experimentType : $('#expTypes input:checked').val(),
	          // ajax : 'false'
         }, function (data) {
		         $('#matching').html(data.Matching.reduce(function (pvalue, value) {
                 return pvalue + '<label class="btn btn-primary"><input class="toggle" type="radio" name="matchingType" id="'
                      + value.label + '" value="' + value.label + '">' + value.label + '</label>';
             }, ''));

	           $('#matching input')[0].checked = true;
		         $('#matching label').map(function () {
						     $(this).attr('data-toggle', 'tooltip')
					 			        .attr('data-placement', 'top');
                 
                 for ( var i = 0; i < data.Matching.length; i++ ) {
							       if ( data.Matching[i].label == $(this).find('input').val() ) {
					 						   $(this).attr('title', data.Matching[i].description);
                     }
                 }
             });					 
			       $('[data-toggle="tooltip"]').tooltip();
	       });
     };

     function loadFilters() {
         if (!isFilteredExperiment()) {
             $('#filter').html('');
             return;
         }
         $.getJSON('${filters}', /*{ajax : false},*/ function (data) {             
             var filters = data.Filters.reduce(function (pvalue, value) {
                 return pvalue + '<label class="btn btn-primary"><input class="toggle" type="radio" name="filter" id="'
                      + value + '" value="' + value + '">' + value + '</label>';
             }, '');
                     
             // compare and metadata overview
             filters += '<label class="btn btn-primary">';
             filters += '<input class="toggle" type="radio" name="filter" id="Compare" value="Compare">Compare';
             filters += '</label>';   
             filters += '<label class="btn btn-primary">';
             filters += '<input class="toggle" type="radio" name="filter" id="Oveview" value="Overview">Overview';
             filters += '</label>';

             $('#filter').html(filters);
             $('#filter input')[0].checked = true;
                       
             $('#filter label').map(function () {
                 $(this).attr('data-toggle', 'tooltip')
                        .attr('data-placement', 'top');
                 
                 if ( $(this).find('input').val() === "Compare" ) {
                     $(this).attr('title', 'Compare all filter together on two slides.');
                 } else if ( $(this).find('input').val() === "Overview" ) {
                     $(this).attr('title', 'In deep overview with many different slidable charts');
                 }
             });
			       $('[data-toggle="tooltip"]').tooltip();
         });
     };
     
     function loadExperimentTypes() {
	       $.getJSON('${exptypes}', /*{ajax : false},*/ function (data) {
             $('#expTypes').html(data.ExperimentType.reduce(function (pvalue, value) {
                 return pvalue + '<label class="btn btn-primary"><input class="toggle" type="radio" name="experimentType"  id="'
                      + value.name + '" value="' + value.name + '">' + value.name + '</label>';
             }, ''));
             
             $('#expTypes input')[0].checked = true;
		         loadMatchings();
		         loadFilters();
             // Add the listener for loading the matchings and filters
		         $("#expTypes input").change(loadMatchings);
             $('#expTypes input').change(loadFilters);
		         
		         $('#expTypes label').map(function () {
                 $(this).attr('data-toggle', 'tooltip')
 								        .attr('data-placement',	'top');
                 
                 for ( var i = 0; i < data.ExperimentType.length; i++ ) {
		        				 if(data.ExperimentType[i].label === $(this).find('input').val()){
 												 $(this).attr('title', data.ExperimentType[i].description);
 									   }
		       	 		 }});
             $('[data-toggle="tooltip"]').tooltip();
	       });
     };
     
     function loadExperiment() {
	       $.getJSON('${experimentoverview}', {
	           experimentType : $('#expTypes input:checked').val(),
	           matching : $('#matching input:checked').val(),
	           filter : $('#filter input:checked').val(),
	           //ajax : 'false'
	       }, function (data){
             var filtername = $('#filter input:checked').val() || '';
             var chartname = $('#expTypes input:checked').val() + ' '
                           + $('#matching input:checked').val() + ' '
                           + filtername;
             
             showTable(data[0],'resultsTable');
             drawSpiderChart(prepareDataForSpider(data[0]), 
                             data[0][0].slice(1, data[0][0].length), 'resultsChart', chartname);
             showTable(data[1],'correlationsTable');
             drawSpiderChart(prepareDataForSpider(data[1]), 
                             data[1][1].slice(1, data[1][1].length), 'correlationsChart', 'Correlations Chart');
			   }).fail(function() {
		         console.log("error loading data for table");
	       });
     };
     
     // draw experiment tables
	   function showTable(tableData, tableElementId) {
		     var tbl_body = tableData.slice(1, tableData.length).reduce(function (pvalue, value) {
             return pvalue + '<tr><td>' + value.join('</td><td>') + '</td></tr>';
         }, '');      
         var tbl_hd = tableData[0].reduce(function (pvalue, value) {
             return pvalue + '<th class="rotated_cell"><div>' + value + '</div></th>';
         }, '<tr>');
         tbl_hd += '</tr>';
         
		     $("#" + tableElementId + " thead").html(tbl_hd);
		     $("#" + tableElementId + " tbody").html(tbl_body);
	   }
	   
     

     // set all needed divs for slidr animation
     function prepareCompareCharts(filters) {
         var innerHtmlRes='';
         var innerHtmlCom='';
         for (var i = 0; i < filters.length; i++) {
             innerHtmlRes += '<div id="result' + i +'" data-slidr="'+ i +'" style="width: 900px">'+ filters[i] +'</div>';
             innerHtmlCom += '<div id="compare' + i +'" data-slidr="'+ i +'" style="width: 900px">' + filters[i] +'</div>';
         }
         
         $('#resultsChartBody').html('<div id="resultsChart" style="display: inline"></div><div id="compareChart" style="display: inline"></div>');
         $('#resultsChart').html(innerHtmlRes);
         $('#compareChart').html(innerHtmlCom);
         $('#resultsTable').html('<thead></thead><tbody></tbody>');
     };
     
     // draw all compare charts 
     function compareChart() {
         $.getJSON('${compare}', {
             experimentType : $('#expTypes input:checked').val(),
	           matching : $('#matching input:checked').val(),
	       }, function (data) {
             prepareCompareCharts(data.map(function (value) {
                 return value.filter;
             }));

             for (var i = 0; i < data.length; i++) {
                 var categories = data[i].data[0][0].slice(1, data[i].data[0][0].length);
                 var chartData = prepareDataForSpider(data[i].data[0]);
                 drawSpiderChart(chartData, categories, "result" + i, data[i].filter);
                 drawSpiderChart(chartData, categories, "compare" + i, data[i].filter);
             }
             
             createSlidr('resultsChart');
             createSlidr('compareChart');
         });
     };
     
     function prepareOverviewCharts() {
         $('#resultsChartBody').html('<div id="fscore" style="display: inline; width: 900px"></div>'
                                   + '<div id="entities" style=display: inline></div>'
                                   + '<div id="entityMetadata" style="display: inline"></div>'
                                   + '<div id="emptydocs" style="display: inline; width: 900px"></div>'
                                   + '<div id="words" style="display: inline; width: 900px"></div>'
                                   + '<div id="ambiguityEntities" style="display: inline"></div>'
                                   + '<div id="ambiguitySurface" style="display: inline"></div>'
                                   + '<div id="diversityEntities" style="display: inline"></div>'
                                   + '<div id="diversitySurface" style="display: inline"></div>');
         $('#entities').html('<div id="entitiesabs1" data-slidr="1" style="width: 900px"></div>'
                           + '<div id="entitiesabs2" data-slidr="2" style="width: 900px"></div>'
                           + '<div id="entitiesabs3" data-slidr="3" style="width: 900px"></div>'
                           + '<div id="entitiesabs4" data-slidr="4" style="width: 900px"></div>');
         $('#entityMetadata').html('<div id="entitiesrel" style="width: 900px"></div>'
                                 + '<table id="relTable" class="table table-hover table-condensed"><thead></thead><tbody></tbody></div>');
         $('#resultsTable').html('<thead></thead><tbody></tbody');
     };




     function overviewChart() {
         prepareOverviewCharts();
         
         $.getJSON('${filtermetadata}', {
             experimentType : $('#expTypes input:checked').val(),
	           matching : $('#matching input:checked').val(),
         }, function (data) {
             drawMediumScoreChart(data.scores);
             drawAbsoluteEntityCharts(data.filters, data.overallAmount);
             drawRelativeEntityChart(data.filters, data.overallAmount);
             drawMetadataCharts(data);
             createSlidr('entities');
         });

         $.getJSON('${ambiguityEntities}',
                   function (data) {
                       drawEntitiesAmbiguityCharts(data);
                       createSlidr('ambiguityEntities');
                   });

         $.getJSON('${ambiguitySurface}',
                   function (data) {
                       drawSurfaceAmbiguityCharts(data);
                       createSlidr('ambiguitySurface');
                 });
        
         $.getJSON('${diversityEntities}',
                   function (data) {
                       drawEntitiesDiversityCharts(data);
                       createSlidr('diversityEntities');
                   });


         $.getJSON('${diversitySurface}',
                   function (data) {
                       drawSurfaceDiversityCharts(data);
                       createSlidr('diversitySurface');
                   });
     };

     function createSlidr(name) {
         slidr.create(name, {
             breadcrumbs: true,
             direction: 'horizontal',
             keyboard: true,
             overflow: true,
             transition: 'fade',
             theme: '#222',
             fade: true
         }).start();  
     };


     
     // remove diagrams and tables
     function clearDiagrams() {
         $('#resultsChartBody').html('<div id="resultsChart" class="chartDiv"></div>');
         $('#correlationsChart').html('');
         $('#correlationsTable').html('<thead></thead><tbody></tbody>');
         $('#resultsTable').html('<thead></thead><tbody></tbody>');
     };
     
     function isFilteredExperiment() {
         switch ($('#expTypes input:checked').val()) {
             case 'A2KB':
             case 'C2KB':
             case 'D2KB': return true;
                 break;
             default: return false;
                 break;
         }
     };
     
     $(document).ready(function () {
         //++++++++++++
         //creating the radioboxes
         //++++++++++++
         loadExperimentTypes();
         
	       $("#show").click(function (e) {
	           if (isFilteredExperiment()) {
                 if ($('#filter input:checked').val() == "Compare") {
                     console.log("compare");
                     clearDiagrams();
                     compareChart();
                 } else if ($('#filter input:checked').val() == "Overview") {
                     console.log("overview");
                     clearDiagrams();
                     overviewChart();
                 } else {
                     console.log("else");
                     clearDiagrams();
                     loadExperiment();
                 }
             } else {
                 clearDiagrams();
                 loadExperiment();
             }
         });
     });
    </script>  
</body>
