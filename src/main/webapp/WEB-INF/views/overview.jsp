<%@page import="org.aksw.gerbil.web.ExperimentTaskStateHelper"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<head>
    <link rel="stylesheet"
	        href="/gerbil/webjars/bootstrap/3.2.0/css/bootstrap.min.css">
    <title>Overview</title>
    <script type="text/javascript"
	          src="/gerbil/webjars/jquery/2.1.1/jquery.min.js"></script>
    <script type="text/javascript"
	          src="/gerbil/webjars/bootstrap/3.2.0/js/bootstrap.min.js"></script>
    <script type="text/javascript"
	          src="/gerbil/webResources/js/gerbil.color.js"></script>
     <script type="text/javascript"
	           src="/gerbil/webResources/js/slidr.min.js"></script>
     <script src="/gerbil/webResources/js/highcharts.js"></script>
     <script src="/gerbil/webResources/js/highcharts-more.js"></script>
    <link rel="icon" type="image/png"
	        href="/gerbil/webResources/gerbilicon_transparent.png">
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

 .chartDiv { /*position: absolute;*/
     //top: 50px;
     padding-top: 50px;
     left: 50px;
	   vertical-align: center;
	   //text-align:center;
 }

 .chartBody {
     //text-align: center;
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
        <c:url var="filtermetadata" value="/filtermetadata" />

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
						<button id="show" type="button" class="btn btn-default">Show
							table!</button>
					</div>
				</div>
			</div>
			<div class="col-md-12">
				  <h2>F1-measures</h2>
				  <p>The table as well as the diagram contain the micro
					    F1-measure.</p>
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
	 function loadMatchings() {
	     $.getJSON('${matchings}', {
	         experimentType : $('#expTypes input:checked').val(),
	         ajax : 'false'
       }, function(data) {
		       var htmlMatchings = "";
		       for ( var i = 0; i < data.Matching.length; i++) {
			         htmlMatchings += "<label class=\"btn btn-primary\" >";
			         htmlMatchings += " <input class=\"toggle\" type=\"radio\" name=\"matchingType\" id=\"" + data.Matching[i].name + "\" value=\"" + data.Matching[i].name + "\" >"
			                        + data.Matching[i].label;
			         htmlMatchings += "</label>";
		       }
		       $('#matching').html(htmlMatchings);
		       $('#matching input')[0].checked = true;
		       $('#matching label').each(function( index ) {
						   for ( var i = 0; i < data.Matching.length; i++) {
							     if(data.Matching[i].label==$( this ).find('input').val()){
					 						 $( this ).attr('data-toggle',		'tooltip')
					 									    .attr('data-placement',	'top')
					 									    .attr('title',			data.Matching[i].description);
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
       $.getJSON('${filters}', {ajax : false},            
                 function(data) {             
                     var htmlFilters = "";
                     for (var i = 0; i < data.Filters.length; i++) {
                         htmlFilters += "<label class=\"btn btn-primary\">";
                         htmlFilters += "<input class=\"toggle\" type=\"radio\" name=\"filter\" id=\"" + data.Filters[i] + "\" value=\"" + data.Filters[i] + "\">"
                                      + data.Filters[i];
                         htmlFilters += "</label>";
                     }
                     
                     // append compare view
                     htmlFilters += "<label class=\"btn btn-primary\">";
                     htmlFilters += "<input class=\"toggle\" type=\"radio\" name=\"filter\" id=\"Compare\" value=\"Compare\">Compare";
                     htmlFilters += "</label>";
                     
                     // append overview
                     htmlFilters += "<label class=\"btn btn-primary\">";
                     htmlFilters += "<input class=\"toggle\" type=\"radio\" name=\"filter\" id=\"Oveview\" value=\"Overview\">Overview";
                     htmlFilters += "</label>";
                     
                     
                     $('#filter').html(htmlFilters);
                     $('#filter input')[0].checked = true;

                     $('#filter label').each(function( index ) {
                         if ($(this).find('input').val() == "Compare") {
                             $(this).attr('data-toggle', 'tooltip')
                                    .attr('data-placement', 'top')
                                    .attr('title', 'Compare all filter together on two slides.');
                         } else if ($(this).find('input').val() == "Overview") {
                             $(this).attr('data-toogle', 'tooltip')
                                    .attr('data-placement', 'top')
                                    .attr('title', 'In deep overview with many different slidable charts');
                         }
                     });
                 });
   };
   
   function loadExperimentTypes() {
	     $.getJSON('${exptypes}', {ajax : false},
	               function(data) {
		                 var htmlExperimentTypes = "";
		                 for ( var i = 0; i < data.ExperimentType.length; i++) {
			                   htmlExperimentTypes += "<label class=\"btn btn-primary\" >";
			                   htmlExperimentTypes += " <input class=\"toggle\" type=\"radio\" name=\"experimentType\" id=\"" + data.ExperimentType[i].name + "\" value=\"" + data.ExperimentType[i].name + "\" >"
			                                        + data.ExperimentType[i].label;
			                   htmlExperimentTypes += "</label>";
		                 }
		                 $('#expTypes').html(htmlExperimentTypes);
		                 $('#expTypes input')[0].checked = true;
		                 // Add the listener for loading the matchings
		                 $("#expTypes input").change(loadMatchings);
		                 loadMatchings();
                     $('#expTypes input').change(loadFilters);
		                 loadFilters();
                     
		                 $('#expTypes label').each(function( index ) {
		        					   for ( var i = 0; i < data.ExperimentType.length; i++) {
		        					       if(data.ExperimentType[i].label==$( this ).find('input').val()){
 										             $( this ).attr('data-toggle',		'tooltip')
 													                .attr('data-placement',	'top')
 													                .attr('title',			data.ExperimentType[i].description);
 									           }
		       	 				}});
		                 
                		 $('[data-toggle="tooltip"]').tooltip();
                     
	               });
   };
   
   function loadTables() {
	     $.getJSON('${experimentoverview}', {
	         experimentType : $('#expTypes input:checked').val(),
	         matching : $('#matching input:checked').val(),
	         filter : $('#filter input:checked').val(),
	         ajax : 'false'
	     }, function(data){
  			   var chartname = $('#expTypes input:checked').val() + ' '
                         + $('#matching input:checked').val() + ' '
                         + $('#filter input:checked').val()
           showTable(data[0],"resultsTable");
           drawSpiderChart(data[0], 'resultsChart', chartname);
    		   showTable(data[1],"correlationsTable");
           drawSpiderChart(data[1], 'correlationsChart', 'Correlations Chart');
			 }).fail(function() {
		       console.log("error loading data for table");
	     });
   };
	 
	 function showTable(tableData, tableElementId) {
		   //http://stackoverflow.com/questions/1051061/convert-json-array-to-an-html-table-in-jquery
		   var tbl_body = "";
		   var tbl_hd = "";
       
		   $.each(tableData, function(i) {
			     var tbl_row = "";
			     if (i > 0) {
				       $.each(this, function(k, v) {
					         tbl_row += "<td>" + v + "</td>";
				       });
				       tbl_body += "<tr>" + tbl_row + "</tr>";
			     } else {
				       $.each(this, function(k, v) {
					         tbl_row += "<th class=\"rotated_cell\"><div >" + v + "</div></th>";
				       });
				       tbl_hd += "<tr>" + tbl_row + "</tr>";
			     }
		   });
		   $("#" + tableElementId + " thead").html(tbl_hd);
		   $("#" + tableElementId + " tbody").html(tbl_body);
	 } 
	
   // set all needed divs for slidr animation
   function prepareCompareCharts(filters) {
       var innerHtmlRes='';
       var innerHtmlCom='';
       for (var i = 0; i < filters.length; i++) {
           innerHtmlRes += '<div id="result' + i +'" data-slidr="'+ i +'">'+ filters[i] +'</div>';
           innerHtmlCom += '<div id="compare' + i +'" data-slidr="'+ i +'">' + filters[i] +'</div>';
       }

       $('#resultsChartBody').html('<div id="resultsChart" style="display: inline"></div><div id="compareChart" style="display: inline"></div>');
       $('#resultsChart').html(innerHtmlRes);
       $('#compareChart').html(innerHtmlCom);
       $('#resultsTable').html('<thead></thead><tbody></tbody');
   };

   // convert to float array
   function convertData(data) {
       return data.slice(1, data.length).map(function(currentValue) {
           if (currentValue != 'n.a.' && !currentValue.startsWith('error')) {
               return parseFloat(currentValue);
           } else {
               return 0.0;
           }
       });
   }
   
   // new draw spider chart function
   function drawSpiderChart(data, tagname, chartname) {
       var chartData = [];
       
       // fill json object with data
       for (var i = 1; i < data.length; i++) {    
           var dataset = {
               type: 'area',
               name: data[i][0],
               data: convertData(data[i]).map(curr => curr * 100),
               pointPlacement: 'on'
           }
           chartData.push(dataset);
       }
       
       $('#' + tagname).highcharts({
           chart: {
               polar: true,
               type: 'line',
               height: 700
           },
           credits: {enabled: false},
           title: {
               text: chartname,
               x: -80
           },
           xAxis: {
               categories: data[0].slice(1, data[0].length),
               tickmarkPlacement: 'on',
               lineWidth: 0
           },
           yAxis: {
               //gridLineInterpolation: 'polygon',
               lineWidth: 0,
               tickInterval: 10,
               labels: {
                   formatter: function() {
                       return this.value + '%'
                   }
               }
           },
           tooltip: {
               shared: true,
               pointFormat: '<span style="color:{series.color}">{series.name}: {point.y}% <br/>',
               borderWidth: 0
           },
           legend: {
               align: 'right',
               verticalAlign: 'top',
               y: 70,
               layout: 'vertical'
           },
           series: chartData
       });
   };
   
   // draw all compare charts 
   function compareChart() {
       $.getJSON('${filters}', {ajax : false},
                 function(data) {
                     prepareCompareCharts(data.Filters);
                     
                     // load data async
                     data.Filters.forEach(function(filter, n) {
                         $.getJSON('${experimentoverview}', {
                             experimentType : $('#expTypes input:checked').val(),
	                           matching : $('#matching input:checked').val(),
                             filter : filter,
	                           ajax : false
                         }).done(function(data) {
                             drawSpiderChart(data[0], "compare" + n, filter);
                             drawSpiderChart(data[0], "result" + n, filter);
                         });
                     })}).done(function(){
                         // initalize compare slides
                         slidr.create('resultsChart', {
                             breadcrumbs: true,
                             direction: 'horizontal',
                             keyboard: true,
                             overflow: true,
                             transition: 'fade',
                             theme: '#222',
                             fade: true
                         }).start();
                         
                         slidr.create('compareChart', {
                             breadcrumbs: true,
                             direction: 'horizontal',
                             keyboard: true,
                             overflow: true,
                             transition: 'fade',
                             theme: '#222',
                             fade: true
                         }).start(); 
            });
   };

   function prepareOverviewCharts() {
       $('#resultsChart').html('<div id="resultsChart" style="display: inline"></div>');
       $('#resultsChart').html('<div id="mediumChart" data-slidr="1"></div>'
                             + '<div id="peakChart" data-slidr="2"></div>'
                             + '<div id="lowChart" data-slidr="3"></div>'
                             + '<div id="metadata1" data-slidr="4"></div>'
                             + '<div id="metadata2" data-slidr="4"></div>');
       $('#resultsTable').html('<thead></thead><tbody></tbody');
   };

   function prepareDataForAnnotatorChart(data) {
       return data.slice(1, data[0].length).map(function(currentValue) {
           var convertedData = convertData(currentValue); 
           return [currentValue[0], // annotator name
                   // dataset medium
                   convertedData.reduce(function(previousValue, currentValue, currentIndex, arr) {
                       // if last iteration divide by length
                       if (currentIndex == arr.length -1) {
                           return (previousValue + currentValue) / arr.length;
                       } else {
                           return previousValue + currentValue;
                       }
                   }, 0),
                   // dataset peak
                   convertedData.reduce(function(previousValue, currentValue) {
                       return Math.max(previousValue, currentValue);
                   }),
                   // dataset low
                   convertedData.reduce(function(previousValue, currentValue) {
                       return Math.min(previousValue, currentValue);
                   }, 0)]; 
       });   
   };

   function drawFilterChart(xAxisNames, data, name, tagname, formatter) {
       $('#' + tagname).highcharts({
           chart: {
               type: 'line',
               widht: 700
           },
           credits: {enabled: false},
           title: {
               text: name,
               x: -80
           },
           xAxis: {
               categories: xAxisNames,
               //tickmarkPlacement: 'on',
               lineWidth: 0
           },
           yAxis: {
               //gridLineInterpolation: 'polygon',
               lineWidth: 0,
               tickInterval: 10,
               labels: formatter.formatter
           },
           tooltip: {
               shared: true,
               pointFormat: formatter.pointFormat,
               borderWidth: 0
           },
           series: data
        });
   };
   
   function overviewChart() {
       prepareOverviewCharts();
       
       $.getJSON('${filters}', {ajax : false},
                 function(data) {
                     var filters = data.Filters;
                     var dataChunks = [];
                     
                     // load data in sync
                     filters.forEach(function(filter, n) {
                         $.getJSON('${experimentoverview}', {
                             experimentType : $('#expTypes input:checked').val(),
	                           matching : $('#matching input:checked').val(),
                             filter : filter,
	                           ajax : false
                         }).done(function(data) {
                             // ignore dataset names
                             var chunk = {
                                 filter: filters[n],
                                 data: prepareDataForAnnotatorChart(data[0])
                             };
                             dataChunks.push(chunk);

                             var chartData1 = [];
                             var chartData2 = [];
                             var chartData3 = [];
                             if (n == filters.length -1) {
                                 for (var i = 0; i < dataChunks[0].data.length; i++) {
                                     var series = [];
                                     for (var j = 0; j < dataChunks.length; j++) {
                                         var value = {
                                             name: dataChunks[j].data[i][0],
                                             median: dataChunks[j].data[i][1],
                                             peak: dataChunks[j].data[i][2],
                                             low: dataChunks[j].data[i][3],
                                         };
                                         series.push(value);
                                     }
                                     var s = {
                                         name: series[0].name,
                                         type: 'line',
                                         data: series.map( curr => curr.median * 100)
                                     };
                                     chartData1.push(s);
                                     s = {
                                         name: series[0].name,
                                         type: 'line',
                                         data: series.map( curr => curr.peak * 100)
                                     };
                                     chartData2.push(s);
                                     s = {
                                         name: series[0].name,
                                         type: 'line',
                                         data: series.map( curr => curr.low * 100)
                                     };
                                     chartData3.push(s);
                                     
                                 }

                                 var formatter = {
                                     formatter: {
                                         formatter: function() {
                                             return this.value + '%'
                                         }},
                                     pointFormat: '<span style="color:{series.color}">{series.name}: {point.y}% <br/>'
                                 };
                                 drawFilterChart(filters, chartData1, 'Medium F1 Score per Filter', 'mediumChart', formatter);
                                 drawFilterChart(filters, chartData2, 'Highest F1 Score per Filter', 'peakChart', formatter);
                                 drawFilterChart(filters, chartData3, 'Lowest F1 Score per Filter', 'lowChart', formatter);
                             }
                         });
                     });
                 });

       $.getJSON('${filtermetadata}', {ajax : false},
                 function(data) {
                     var filters = data.map(function(currentValue) {
                         return currentValue.filter;
                     });

                     var formatter = {
                         formatter: {
                             formatter: function() {
                                 return this.value + " Entities"
                             }},
                         pointFormat: '<span style="color:{series.color}">{series.name}: {point.y} Entities <br/>'
                     };
                     
                      var amounts = [{
                         type: 'pie',
                         name: 'Amount of Entities',
                         data: data.map(function(currentValue) {
                             return {
                                 name: currentValue.filter,
                                 y: currentValue.amount
                             };
                         })
                     }];

                     var sum = data.reduce(function(previousValue, currentValue) {
                         return previousValue + parseInt(currentValue.amount);
                     }, 0);
                     drawFilterChart(filters, amounts, 'Amount of Entities per Filter (Sum ' + sum + ')', 'metadata1', formatter);

                     var chartData = [];
                     for (var i = 0; i < data[0].datasets.length; i++) {
                         var seriesData = [];
                         for (var j = 0; j < data.length; j++) {
                             seriesData.push(data[j].values[i]);
                         }
                         seriesData.unshift(data[0].datasets[i]);
                         chartData.push(seriesData);
                     }
                     
                     amounts = chartData.map(function(currentValue) {
                         return {
                             name: currentValue[0],
                             type: 'spline',
                             data: currentValue.slice(1, currentValue.length)
                         };
                     });
                     console.log(amounts);
                     drawFilterChart(filters, amounts, 'Amount of Entities per Dataset', 'metadata2', formatter);
                 });
   };

   // remove diagrams and tables
   function clearDiagrams() {
       $('#resultsChartBody').html('<div id="resultsChart" class="chartDiv"></div>');
       $('#correlationsChart').html('');
       $('#correlationsTable').html('<thead></thead><tbody></tbody>');
       $('#resultsTable').html('thead></thead><tbody></tbody>');
   };

   function isFilteredExperiment() {
       var exp = $('#exptypes input:checked').val();
       switch (exp) {
           case 'A2KB':
           case 'C2KB':
           case 'D2KB': return true;
               break;
           default: return false;
               break;
       }

   }
   $(document).ready(function() {
	     //++++++++++++
	     //creating the radioboxes
	     //++++++++++++
	     loadExperimentTypes();
       
	     $("#show").click(function(e) {
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
                   loadTables();
               }
           } else {
               clearDiagrams();
               loadTables();
           }
	     });
   });
	</script>
</body>
