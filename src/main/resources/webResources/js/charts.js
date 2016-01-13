/*
  Draw different chart types

*/

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
            lineWidth: 0
        },
        yAxis: {
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
