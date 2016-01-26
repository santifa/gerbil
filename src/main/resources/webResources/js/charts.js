/*
  Draw different chart types
*/

// general convert from strings to float array
function convertToFloat (data) {
    return data.map(function (value) {
        if (value !== 'n.a.' && !value.startsWith('error')) {
            return parseFloat(value);
        } else {
            return 0.0;
        }
    });
};

// new draw spider chart function
function drawSpiderChart(data, categories, tagname, chartname) {
    $('#' + tagname).highcharts({
        chart: {
            polar: true,
            height: 700
        },
        credits: {enabled: false},
        title: {
            text: chartname,
            x: -80
        },
        xAxis: {
            categories: categories,
            tickmarkPlacement: 'on',
        },
        yAxis: {
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
        series: data
    });
};


// shows the medium scores for every filter
function drawScoreChart(categories, data, name, tagname) {
    $('#' + tagname).highcharts({
        chart: {
            type: 'scatter',
            widht: 700
        },
        credits: {enabled: false},
        title: {text: name},
        xAxis: {
            title: {text: 'Filters'},
            categories: categories
        },
        yAxis: {
            title: {text: 'F1 Score'}
        },
        series: data,
        tooltip: {
            borderWidth: 0
        }
    });
};

// shows the absolute amount of entities per filter type
function drawAbsolutePieChart(data, name, tagname) {
    $('#' + tagname).highcharts({
        chart: {
            type: 'pie',
            plotShadow: false,
            widht: 700
        },
        credits: {enabled: false},
        title: {
            text: name,
            x: -80
        },
        series: [{
            name: 'Entities',
            colorByPoint: true,
            data: data
        }],
        tooltip: {
            borderWidth: 0
        }
    });
};

// shows the amount of entities filtered per dataset
function drawAbsoluteDatasetChart(categories, data, name, tagname) {
    $('#' + tagname).highcharts({
        chart: {
            widht: 700,
            zoomType: 'xy',
            panning: true,
            panKey: 'shift'
        },
        credits: {enabled: false},
        title: {text: name},
        xAxis:{
            title: 'Datasets',
            categories: categories
        },
        yAxis: {
            title: 'Entities',
            type: 'logarithmic',
            minorTickInterval: 'auto'
        },
        plotOptions:{
            column: {
                stacking: 'normal'
            }
        },
        series: data,
        tooltip: {
            borderWidth: 0
        }
    });  
};

// shows the relative amount of entities filtered per dataset
function drawRelativeChart(filters, datasets, data, name, tagname) {
    // Set up the chart
    var chart = new Highcharts.Chart({
        chart: {
            renderTo: tagname,
            margin: 100,
            type: 'scatter',
            //zoomType: 'xy',
            options3d: {
                enabled: true,
                alpha: 10,
                beta: 30,
                depth: 400,
                viewDistance: 20,
                frame: {
                    bottom: {
                        size: 1,
                        color: 'rgba(0,0,0,0.05)'
                    }
                }
            }
        },
        credits: {enabled: false},
        title: {text: name},
        plotOptions: {
            scatter: {
                width: 10,
                height: 10,
                depth: 10
            }
        },
        yAxis: {
            min: 0,
            max: 1,
            allowDecimals: true,
            tickInterval: 0.1,
            title: 'F1 Score'
        },
        xAxis: {
            min: 0,
            max: filters.length,
            tickInterval: 1,
            title: 'Filters',
            gridLineWidth: 1,
            labels: {
                formatter: function () {
                    return filters[this.value];
                }
            }
        },
        zAxis: {
            min: 0,
            max: datasets.length,
            tickInterval: 1,
            title: 'Datasets',
            //showFirstLabel: false,
            labels: {
                formatter: function () {
                    return datasets[this.value];
                }
            }
        },
        legend: {enabled: false},
        series: data
    });
    
    // Add mouse events for rotation
    $(chart.container).bind('mousedown.hc touchstart.hc', function (eStart) {
        eStart = chart.pointer.normalize(eStart);
        
        var posX = eStart.pageX,
            posY = eStart.pageY,
            alpha = chart.options.chart.options3d.alpha,
            beta = chart.options.chart.options3d.beta,
            newAlpha,
            newBeta,
            sensitivity = 5; // lower is more sensitive
        
        $(document).bind({
            'mousemove.hc touchdrag.hc': function (e) {
                // Run beta
                newBeta = beta + (posX - e.pageX) / sensitivity;
                chart.options.chart.options3d.beta = newBeta;

                // Run alpha
                newAlpha = alpha + (e.pageY - posY) / sensitivity;
                chart.options.chart.options3d.alpha = newAlpha;

                chart.redraw(false);
            },
            'mouseup touchend': function () {
                $(document).unbind('.hc');
            }
        });
    });
};

// shows the amount of entities filtered per dataset
function drawAnnotationsPerWordChart(categories, data, name, tagname) {
    $('#' + tagname).highcharts({
        chart: {
            widht: 700,
            type: 'column'
        },
        credits: {enabled: false},
        title: {text: name},
        xAxis:{
            title: 'Datasets',
            categories: categories
        },
        yAxis: {
            title: 'Annotations Per Word',
            minorTickInterval: 'auto'
        },
        series: data,
        tooltip: {
            borderWidth: 0,
            pointFormatter: function() {
                return '<span style="color:{point.color}">\u25CF</span> ' + 
                    this.series.name + ': <b>' + this.y.toFixed(4) + '</b><br/>';
            }
        }
    });  
};

// shows the ambiguity of datasets
function drawAmbiguityChart(categories, data, name, tagname, xAxisName, yAxisName, type, subtitle) {
    $('#' + tagname).highcharts({
        chart: {
            type: 'column',
            widht: 700,
            zoomType: 'x',
            panning: true,
            panKey: 'shift'
        },
        credits: {enabled: false},
        title: {text: name},
        subtitle: {text: subtitle},
        xAxis:{
            title: xAxisName,
            categories: categories
        },
        yAxis: {
            title: yAxisName,
            type: type,
            minorTickInterval: 'auto'
       
        },
        series: data,
        tooltip: {
            borderWidth: 0,
            valueSuffix: yAxisName
        }
    });  
};


// prepare data for spider chart
function prepareDataForSpider (data) {
    return data.slice(1, data.length).map(function (value) {
        return {
            type: 'area',
            name: value[0],
            data: convertToFloat(value.slice(1, value.length)).map(curr => curr * 100),
            pointPlacement: 'on'
        };
    });
};

function drawMediumScoreChart(data) {
    var categories = data.map(function (value) {
        return value.filter;
    });
    drawScoreChart(categories, caclulateMediumScores(data),
                   'Medium Micro F1 Scores', 'mediumChart');                           
};

// we ignore 0.0 values because either the dataset is not run with an annotator or
// it's an error. in rare cases a value of 0.0 refers to a correct value but only if the dataset
// is empty and the annotator returns nothing;
function calculateMedium(data) {
    var counter = 0;
    return convertToFloat(data).reduce(function (pvalue, value, index, arr) {
        // devide by counter if last iteration is reached
        if (value === 0.0) {
            if ((arr.length -1) === index) {
                return counter === 0 ? 0.0 : pvalue / counter;
            } else {
                return pvalue;
            }
        } else {
            counter += 1;
            return (arr.length -1) === index ? (pvalue + value) / counter : pvalue + value;
        }
    }, 0.0)
};

function caclulateMediumScores(val) {
    var series = val[0].data[0].slice(1, val[0].data[0].length).map(function (value) {
        return {
            name: value[0],
            data: []
        }
    });

    for (var i = 0; i < val.length; i++) {
        var annotator = val[i].data[0].slice(1, val[i].data[0].length);             
        var row = annotator.map(function (value) {
            return calculateMedium(value.slice(1, value.length));
        });
        // add every score to the chart data
        for (var j = 0; j < series.length; j++) {
            series[j].data.push(row[j]);
        }             
    };
    return series;
};

// draw charts for the absolute and relative amount of entities per dataset
function drawAbsoluteEntityCharts(data, overallAmount) {
    var type_pie = [];
    var hit_pie = [];
    var page_pie = [];
    var datasets =[];
    var absoluteSeries = [];
    for (var key in data[0]) {
        if (!(key.startsWith('amount') || key.startsWith('filter'))) {
            datasets.push(key);
        }
    }
   
    for (var i = 0; i < data.length; i++) {
        if (data[i].filter === 'nofilter') {
            // we skip the nofilter because it refers to the unfiltered values for the pie charts
            absoluteSeries.push({
                type: 'column',
                name: 'Amount of Entities in general',
                data: getEntitiesPerDataset(datasets, data[i]),
                grouping: false,
                pointPlacement: -2
            });
            
        } else if (data[i].filter.indexOf('Hits') > -1) {
            // we have a hitscore filter
            hit_pie.push(createPieData(data[i]));
            absoluteSeries.push({
                type: 'column',
                name: data[i].filter,
                data: getEntitiesPerDataset(datasets, data[i]),
                stack: 'Hitscore',
                zIndex: 1
            });
            
        } else if (data[i].filter.indexOf('Page') > -1) {
            // we have a pagerank fiter
            page_pie.push(createPieData(data[i]));
            absoluteSeries.push({
                type: 'column',
                name: data[i].filter,
                data: getEntitiesPerDataset(datasets, data[i]),
                stack: 'Pagerank',
                zIndex: 1
            });
            
        } else {
            // we have a type filter
            type_pie.push(createPieData(data[i]));
            absoluteSeries.push({
                type: 'column',
                name: data[i].filter,
                data: getEntitiesPerDataset(datasets, data[i]),
                stack: 'Type',
                zIndex: 1
            });
        }
    }
    
    // add remaining entities
    type_pie.push(getRemaining(type_pie, overallAmount));
    hit_pie.push(getRemaining(hit_pie, overallAmount));
    page_pie.push(getRemaining(page_pie, overallAmount));
    
    drawAbsolutePieChart(type_pie, 'Absolute Entities  Per Filter (Amount ' + overallAmount + ')', 'entitiesabs2');
    drawAbsolutePieChart(hit_pie, 'Absolute Entities  Per Filter (Amount ' + overallAmount + ')', 'entitiesabs3');
    drawAbsolutePieChart(page_pie, 'Absolute Entities  Per Filter (Amount ' + overallAmount + ')', 'entitiesabs4');
    drawAbsoluteDatasetChart(datasets, absoluteSeries, 'Absolute Amount of Filtered Entities per Dataset', 'entitiesabs1');
};

function drawRelativeEntityChart(filterData, overallAmount) {
    var datasets = [];
    var filters = filterData.map(function (value) {
        return value.filter;
    });
    // remove no filter
    var index = filters.indexOf('nofilter');
    if (index > -1) {
        filters.splice(index, 1);
    }
    
    for (var key in filterData[0]) {
        if (!(key.startsWith('amount') || key.startsWith('filter'))) {
            datasets.push(key);
        }
    }

    // collect general entity amount
    var amount =[];
    for (var i = 0; i < filterData.length; i++) {
        if (filterData[i].filter.startsWith('nofilter')) {
            for (var j = 0; j < datasets.length; j++) {
                amount.push({
                    dataset: datasets[j],
                    amount: filterData[i][datasets[j]]
                });
            }
        }
    }
    
    var chartdata = [];
    if (index > -1) {
        filterData.splice(index, 1);
    }
    for (var i = 0; i < filterData.length; i++) {
        var s = []
        for (var j = 0; j < datasets.length; j++) {
            var relAmount = 0;
            for (var k = 0; k < amount.length; k++) {
                if (amount[k].dataset.startsWith(datasets[j])) {
                    relAmount = filterData[i][datasets[j]] / amount[k].amount;
                }
            }
            // [x,y,z]
            // filter, rel, dataset
            s.push([
                i,relAmount,j
            ]);
        }
        chartdata.push({
            name: 'Entities filtered by ' + filterData[i].filter,
            data: s,
            tooltip: {
                pointFormatter: function () {
                    return 'Filter: ' + filterData[this.x].filter + '<br/>'
                        + 'Dataset: ' + datasets[this.z] + '<br/>'
                        + 'relative Amount: ' + this.y.toFixed(4);
                }
            }
        });   
    }

    // add relative amount of filtered entities against amount of all entities
    var s = [];
    datasets.push('all Entities');
    for (var i = 0; i < filterData.length; i++) {
        s.push([
            i,
            filterData[i].amount / overallAmount,
            datasets.length -1
        ]);
        
    }
    chartdata.push({
        name: 'Relative Amount of Entities per Filter',
        data: s,
        tooltip: {
            pointFormatter: function () {
                return 'Filter: ' + filterData[this.x].filter + '<br/>'
                    + 'Dataset: All Entities (' + overallAmount + ')<br/>'
                    + 'relative Amount: ' + this.y.toFixed(4);
            }
        }
    });
    drawRelativeChart(filters, datasets, chartdata, 'Relative Amount of Filtered Entities per Dataset', 'entitiesrel');
    drawRelativeTable(filters, datasets, chartdata, 'relTable');
};

// draw experiment tables
function drawRelativeTable(filters, datasets, tableData, tableElementId) {
    var tbl_hd = '<tr><th class="rotated_cell"><div>Relative Amount</div></th>'
        + '<th class="rotated_cell"><div>' + datasets.join('</div></th><th class="rotated_cell"><div>') + '</div></th></tr>'; 

    var tbl_body;
    var length = tableData.length - 1 // last array is a column not a row
    for (var i = 0; i < length; i++) {
        tbl_body += '<tr><td>' + filters[i] + '</td>';
        tbl_body += tableData[i].data.reduce(function (p, v) {
            return p + '<td>' + v[1].toFixed(4) + '</td>' 
        }, '');
        // append quotient for all datasets
        tbl_body += '<td>' + tableData[length].data[i][1].toFixed(4) + '</td>';
        tbl_body += '</tr>';
    }

	  $("#" + tableElementId + " thead").html(tbl_hd);
		$("#" + tableElementId + " tbody").html(tbl_body);
};

function createPieData(data) {
    return {
        name: data.filter,
        y: data.amount
    };
};

function getEntitiesPerDataset(categories, data) {
    var chartdata = [];
    for (var j = 0; j < categories.length; j++) {
        chartdata.push(data[categories[j]]);
    }
    return chartdata;
};

function getRemaining(data, overallAmount) {
    return {
        name: 'Remaining Entities',
        y: (overallAmount - data.reduce(function (p, v) {
            return p + v.y;
        }, 0))
    };
};

function drawMetadataCharts(data) {
    var categories = []
    var s = [{
        name: 'Annotations Per Word Quotient',
        data: []
    }];
    for (var key in data.words) {
        categories.push(key);
        s[0].data.push(data.words[key]);
    }
    drawAnnotationsPerWordChart(categories, s, 'Densitiy Distribution', 'words');
}

function drawEntitiesAmbiguityCharts(data, parentDiv) {
    var html = '<div id="entitiesAmbigMedium" data-slidr="med" style="width: 900px"></div>'; 
    var datasets = []
    
    // create medium average and collect dataset names
    var values = [];
    for (var key in data.medium) {
        datasets.push(key);
        values.push(parseFloat(data.medium[key].toFixed(4)));
    }
    var mediumSeries = [{
        name: 'Medium Ambiguity',
        data: values
    }];
    // create chart divs with slidr id's
    for (var i = 0; i < datasets.length; i++) {
        html += '<div id="entitiesAmbig' + i + '" data-slidr="' + i + '" style="width: 900px"></div>';             
    }
    $('#ambiguityEntities').html(html);
    
    drawAmbiguityChart(datasets, mediumSeries, 'Medium Entity Ambiguity', 'entitiesAmbigMedium',
                       'Datasets', ' Surface Forms', 'logarithmic', 'Show the medium ambiguity of an entity within a dataset.');
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        var categories = [];
        values = [];
        for (var j = 0; j < data.data.length; j++) {
            if (data.data[j].hasOwnProperty(datasets[i])) {
                categories.push(data.data[j].entity);
                values.push(data.data[j]['Entity Ambiguity']);
            }
        }
        
        var series = [{
            name: datasets[i] + ' Ambiguity',
            data: values
        }];
        drawAmbiguityChart(categories, series, datasets[i] + ' Entity Ambiguity', 'entitiesAmbig' + i,
                           'Entities', ' Surface Forms', 'logarithmic', 'Scrollable and zoomable through <Shift>-<Left-Mouse> and <Left-Mouse>');
    }
};


function drawSurfaceAmbiguityCharts(data) {
    var html = '<div id="surfaceAmbigMedium" data-slidr="med" style="width: 900px"></div>'; 
    var datasets = []
    
    // create medium average and collect dataset names
    var values = [];
    for (var key in data.medium) {
            datasets.push(key);
            values.push(parseFloat(data.medium[key].toFixed(4)));
    }
    var mediumSeries = [{
        name: 'Medium Ambiguity',
        data: values
    }];
    // create chart divs with slidr id's
    for (var i = 0; i < datasets.length; i++) {
        html += '<div id="surfaceAmbig' + i + '" data-slidr="' + i + '" style="width: 900px"></div>';             
    }
    $('#ambiguitySurface').html(html);
    
    drawAmbiguityChart(datasets, mediumSeries, 'Medium Surface Ambiguity', 'surfaceAmbigMedium',
                       'Datasets', ' Entities', 'logarithmic', 'Shows the medium ambiguity of a surface form within a dataset.');
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        var categories = [];
        values = [];
        for (var j = 0; j < data.data.length; j++) {
            if (data.data[j].hasOwnProperty(datasets[i])) {
                categories.push(data.data[j].surface);
                values.push(data.data[j]['Surface Form Ambiguity']);
            }
        }
        
        var series = [{
            name: datasets[i] + ' Ambiguity',
            data: values
        }];
        drawAmbiguityChart(categories, series, datasets[i] + ' Surface Form Ambiguity', 'surfaceAmbig' + i,
                           'Surface Forms', ' Entities', 'logarithmic',  'Scrollable and zoomable through <Shift>-<Left-Mouse> and <Left-Mouse>');
    }
};


function drawEntitiesDiversityCharts(data) {
    var html = '<div id="entitiesDiversMedium" data-slidr="med" style="width: 900px"></div>'; 
    var datasets = []
    
    // create medium average and collect dataset names
    var values = [];
    for (var key in data.medium) {
        datasets.push(key);
        values.push(parseFloat(data.medium[key].toFixed(4)));
    }
    var mediumSeries = [{
        name: 'Medium Diversity',
        data: values
    }];
    // create chart divs with slidr id's
    for (var i = 0; i < datasets.length; i++) {
        html += '<div id="entitiesDivers' + i + '" data-slidr="' + i + '" style="width: 900px"></div>';             
    }
    $('#diversityEntities').html(html);
    
    drawAmbiguityChart(datasets, mediumSeries, 'Medium Entity Diversity', 'entitiesDiversMedium',
                       'Datasets', ' Surface Forms used', 'linear', 'Shows the medium diversity of an entity has within a dataset');
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        var categories = [];
        values = [];
        for (var j = 0; j < data.data.length; j++) {
            if (data.data[j].hasOwnProperty(datasets[i])) {
                categories.push(data.data[j].entity);
                values.push(data.data[j][datasets[i]]);
            }
        }
        
        var series = [{
            name: datasets[i] + ' Diversity',
            data: values
        }];
        drawAmbiguityChart(categories, series, datasets[i] + ' Entity Diversity', 'entitiesDivers' + i,
                           'Entities', ' Surface Forms used', 'linear',  'Scrollable and zoomable through <Shift>-<Left-Mouse> and <Left-Mouse>');
    }
};

function drawSurfaceDiversityCharts(data) {
    var html = '<div id="surfaceDiversMedium" data-slidr="med" style="width: 900px"></div>'; 
    var datasets = []
    
    // create medium average and collect dataset names
    var values = [];
    for (var key in data.medium) {
        datasets.push(key);
        values.push(parseFloat(data.medium[key].toFixed(4)));
    }
    var mediumSeries = [{
        name: 'Medium Diversity',
        data: values
    }];
    // create chart divs with slidr id's
    for (var i = 0; i < datasets.length; i++) {
        html += '<div id="surfaceDivers' + i + '" data-slidr="' + i + '" style="width: 900px"></div>';             
    }
    $('#diversitySurface').html(html);
    
    drawAmbiguityChart(datasets, mediumSeries, 'Medium Surface Form Diversity', 'surfaceDiversMedium',
                       'Datasets', ' Entities used', 'linear', 'Shows the medium diversity of a surface form has within tha datasets.');
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        var categories = [];
        values = [];
        for (var j = 0; j < data.data.length; j++) {
            if (data.data[j].hasOwnProperty(datasets[i])) {
                categories.push(data.data[j].entity);
                values.push(data.data[j][datasets[i]]);
            }
        }
        
        var series = [{
            name: datasets[i] + ' Diversity',
            data: values
        }];
        drawAmbiguityChart(categories, series, datasets[i] + ' Surface Form Diversity', 'surfaceDivers' + i,
                           'Surface Forms', ' Entities used', 'linear',  'Scrollable and zoomable through <Shift>-<Left-Mouse> and <Left-Mouse>');
    }
};
