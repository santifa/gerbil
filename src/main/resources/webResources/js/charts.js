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

// a general column chart, provide a type in the data for changing the chart type
function drawGeneralChart(data, name, tagname, xAxis, yAxis, subtitle, plotOptions) {
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
        plotOptions: plotOptions,
        xAxis: xAxis,
        yAxis: yAxis,
        series: data,
        tooltip: {
            borderWidth: 0,
            valueSuffix: ' ' + yAxis.title
        }
    });  
};

// shows the relative amount of entities filtered per dataset in 3d
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



function drawMediumScoreChart(data) {
    var axis = getAxis(data.map(curr => curr.filter), 'Filters', 'F1 Score', null);
    drawGeneralChart(caclulateMediumScores(data), 'Medium Micro F1 Scores', 'fscore',
                       axis.x, axis.y, 'Scores of 0 are not included in calculation, they indicate an error or an empty dataset', {});
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
            type: 'scatter',
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
    var type_pie = [{
        type: 'pie',
        name: 'Entities',
        colorByPoint: true,
        data: []
    }];
    var hit_pie = [{
        type: 'pie',
        name: 'Entities',
        colorByPoint: true,
        data: []
    }];
    var page_pie = [{
        type: 'pie',
        name: 'Entities',
        colorByPoint: true,
        data: []
    }];
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
            //hit_pie.push(createPieData(data[i]));
            hit_pie[0].data.push({
                name: data[i].filter,
                y: data[i].amount
            });

            absoluteSeries.push({
                type: 'column',
                name: data[i].filter,
                data: getEntitiesPerDataset(datasets, data[i]),
                stack: 'Hitscore',
                zIndex: 1
            });
            
        } else if (data[i].filter.indexOf('Page') > -1) {
            // we have a pagerank fiter
            //page_pie.push(createPieData(data[i]));
            page_pie[0].data.push({
                name: data[i].filter,
                y: data[i].amount
            });
            
            absoluteSeries.push({
                type: 'column',
                name: data[i].filter,
                data: getEntitiesPerDataset(datasets, data[i]),
                stack: 'Pagerank',
                zIndex: 1
            });
            
        } else {
            // we have a type filter
            //type_pie.push(createPieData(data[i]));
            type_pie[0].data.push({
                name: data[i].filter,
                y: data[i].amount
            });

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
    type_pie[0].data.push(getRemaining(type_pie[0].data, overallAmount));
    hit_pie[0].data.push(getRemaining(hit_pie[0].data, overallAmount));
    page_pie[0].data.push(getRemaining(page_pie[0].data, overallAmount));

    var yAxis = {title: 'Entities'};
    
    drawGeneralChart(type_pie, 'Absolute Entities Per Filter (Amount ' + overallAmount + ')', 'entitiesabs2',
                       {}, yAxis, '', {});
    drawGeneralChart(hit_pie, 'Absolute Entities Per Filter (Amount ' + overallAmount + ')', 'entitiesabs3',
                       {}, yAxis, '', {});
    drawGeneralChart(page_pie, 'Absolute Entities Per Filter (Amount ' + overallAmount + ')', 'entitiesabs4',
                       {}, yAxis, '', {});

    var axis = getAxis(datasets, 'Datasets', 'Entities', 'logarithmic');
    var plotOptions = {
        column: {
            stacking: 'normal'
        }
    };
    drawGeneralChart(absoluteSeries, 'Absolute Amount of Filtered Entities per Dataset', 'entitiesabs1',
                       axis.x, axis.y, '', plotOptions);
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
    var densityCategories = []
    var emptyCategories = [];
    var density = [{
        name: 'Annotations Per Word Quotient',
        data: []
    }];

    var empty = [{
        name: 'Percantage of empty documents within a dataset',
        data: []
    }];

    for (var key in data.words) {
        densityCategories.push(key);
        var value = data.words[key].toFixed(4);
        density[0].data.push(parseFloat(value));
        
        if (data.empty.hasOwnProperty(key)) {
            emptyCategories.push(key);
            value = data.empty[key].toFixed(4);
            empty[0].data.push(parseFloat(value));
        }
    }

    var axis = getAxis(densityCategories, 'Datasets', 'Annotations per Word', null);
    drawGeneralChart(density, 'Densitiy Distribution', 'words',
                       axis.x, axis.y, 'Higher is better, to high is nonsense.', {});

    axis.x.categories = emptyCategories;
    axis.y.title = 'Percent';
    drawGeneralChart(empty, 'Empty Documents', 'emptydocs', axis.x, axis.y,
                       '0 indicates all documents have annotations.', {});
    
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
    $('#ambiguityEntities').html(html + createDivs('entitiesAmbig', datasets));
    var axis = getAxis(datasets, 'Datasets', 'Surface Forms used', 'logarithmic');
    drawGeneralChart(mediumSeries, 'Medium Entity Ambiguity', 'entitiesAmbigMedium',
                       axis.x, axis.y, 'Show the medium ambiguity of all entities used within a dataset.', {});
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        chartData = createChartData(datasets[i], ' Ambiguity', data, 'entity', 'Entity Ambiguity');
        axis.x.categories = chartData.cat;
        axis.x.title = 'Entities';
        drawGeneralChart(chartData.series, datasets[i] + ' Entity Ambiguity', 'entitiesAmbig' + i,
                           axis.x, axis.y, '', {});
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
    $('#ambiguitySurface').html(html + createDivs('surfaceAmbig', datasets));
    var axis = getAxis(datasets, 'Datasets', 'Entities used', 'logarithmic');
    drawGeneralChart(mediumSeries, 'Medium Surface Ambiguity', 'surfaceAmbigMedium',
                       axis.x, axis.y, 'Shows the medium ambiguity of all surface forms used within a dataset.', {});
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        chartData = createChartData(datasets[i], ' Ambiguity', data, 'surface', 'Surface Form Ambiguity');
        axis.x.categories = chartData.cat;
        axis.x.title = 'Surface Forms'
        drawGeneralChart(chartData.series, datasets[i] + ' Surface Form Ambiguity', 'surfaceAmbig' + i,
                           axis.x, axis.y,  '', {});
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
    $('#diversityEntities').html(html + createDivs('entitiesDivers', datasets));
    var axis = getAxis(datasets, 'Datasets', 'Surface Forms used', null);
    drawGeneralChart(mediumSeries, 'Medium Entity Diversity', 'entitiesDiversMedium',
                       axis.x, axis.y, 'Shows the relative amount of surface forms used for every entity.', {});
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        chartData = createChartData(datasets[i], ' Diversity', data, 'entity', datasets[i]);
        axis.x.categories = chartData.cat;
        axis.x.title = 'Entities';
        drawGeneralChart(chartData.series, datasets[i] + ' Entity Diversity', 'entitiesDivers' + i,
                           axis.x, axis.y, 'Values above 1 indicates missing informations in the knowledgebase.', {});
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
    $('#diversitySurface').html(html + createDivs('surfaceDivers', datasets));
    var axis = getAxis(datasets, 'Datasets', 'Entities', null);
    drawGeneralChart(mediumSeries, 'Medium Surface Form Diversity', 'surfaceDiversMedium',
                       axis.x, axis.y, 'Shows the relative amount of entities used for every surface form.', {});
    
    // creat all single charts
    for (var i = 0; i < datasets.length; i++) {
        chartData = createChartData(datasets[i], ' Diversity', data, 'surface', datasets[i]);
        axis.x.categories = chartData.cat;
        axis.x.title = 'Surface Forms';
        drawGeneralChart(chartData.series, datasets[i] + ' Surface Form Diversity', 'surfaceDivers' + i,
                         axis.x, axis.y, 'Values above 1 indicates missing informations in the knowledgebase.', {});
    }
};

// create chart data for ambiguity and diversity charts
function createChartData(dataset, name, data, key1, key2) {
//    console.log(data);
    var categories = [];
    values = [];
    for (var j = 0; j < data.data.length; j++) {
        if (data.data[j].hasOwnProperty(dataset)) {     
            categories.push(data.data[j][key1]);
            values.push(data.data[j][key2]);
        }
    }
    return {
        cat: categories,
        series: [{
            name: dataset + name,
            data: values
        }]
    };   
};

// create divs
function createDivs(tag, datasets) {
    var html = '';
    for (var i = 0; i < datasets.length; i++) {
        html += '<div id="' + tag + i + '" data-slidr="' + i + '" style="width: 900px"></div>';             
    }
    return html;
};

// create chart axis
function getAxis(categories, xName, yName, type) {
    var axis = {
        x: {
            title: xName,
            categories: categories
        },
        y: {title: yName}
    };
    if (type != null) {
        axis.y.type = type;
    }
    return axis;
};
