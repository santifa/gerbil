/*
  Draw different chart types
*/

// general convert from strings to float array
function convertToFloat (data) {
    return data.slice(1, data.length).map(function (value) {
        if (value != 'n.a.' && !value.startsWith('error')) {
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
        title: {
            text: name,
            x: -80
        },
        xAxis:{
            categories: categories
        },
        yAxis: {
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

// shows the medium scores for every filter
function drawScoreChart(categories, data, name, tagname) {
    $('#' + tagname).highcharts({
        chart: {
            type: 'scatter',
            widht: 700
        },
        credits: {enabled: false},
        title: {
            text: name,
            x: -80
        },
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
}

// we ignore 0.0 values because either the dataset is not run with an annotator or
// it's an error. in rare cases a value of 0.0 refers to a correct value but only if the dataset
// is empty and the annotator returns nothing;
function calculateMedium(data) {
    var counter = 0;
    return convertToFloat(data).reduce(function (pvalue, value, index, arr) {
        // device by counter if last iteration is reached
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
    var series = val[0].data.slice(1, val[0].data.length).map(function (value) {
        return {
            name: value[0],
            data: []
        }
    });
    
    for (var i = 0; i < val.length; i++) {
        var annotator = val[i].data.slice(1, val[i].data.length);             
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

function drawAbsoluteCharts(data, overallAmount) {
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
    console.log(absoluteSeries);
    
    // add remaining entities
    type_pie.push(getRemaining(type_pie, overallAmount));
    hit_pie.push(getRemaining(hit_pie, overallAmount));
    page_pie.push(getRemaining(page_pie, overallAmount));
    
    drawAbsolutePieChart(type_pie, 'Absolute Entities  Per Filter (Amount ' + overallAmount + ')', 'entitiesabs2');
    drawAbsolutePieChart(hit_pie, 'Absolute Entities  Per Filter (Amount ' + overallAmount + ')', 'entitiesabs3');
    drawAbsolutePieChart(page_pie, 'Absolute Entities  Per Filter (Amount ' + overallAmount + ')', 'entitiesabs4');
    drawAbsoluteDatasetChart(datasets, absoluteSeries, 'Absolute Amount of Filtered Entities per Dataset', 'entitiesabs1');
};

function createPieData(data) {
    return {
        name: data.filter,
        y: data.amount
    };
}

function getEntitiesPerDataset(categories, data) {
    var chartdata = [];
    for (var j = 0; j < categories.length; j++) {
        chartdata.push(data[categories[j]]);
    }
    return chartdata;
}

function getRemaining(data, overallAmount) {
    return {
        name: 'Remaining Entities',
        y: (overallAmount - data.reduce(function (p, v) {
            return p + v.y;
        }, 0))
    };
};
