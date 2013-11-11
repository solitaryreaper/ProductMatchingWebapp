// Returns the common highcharts options
function getDefaultOptions()
{
	var options = {
	   colors: ['#50B432', 'red'],
	   tooltip: {
	       enabled:true
	   },
	   chart: {
		   plotBackgroundColor: null,
           plotBorderWidth: null,
           plotShadow: false
	   },
	   title: {
	      style: {
		 color: '#000',
		 font: 'bold 12px "Trebuchet MS", Verdana, sans-serif'
	      }
	   },
	   subtitle: {
	      style: {
		 color: '#666666'
	      }
	   },
	   labels: {
	      shared:true,
	      style: {
		 color: '#99b'
	      }
	   }
	};

    return options;	
}

// Returns the chart options for pie graphs
function getDefaultPieChartOptions()
{
    var options = getDefaultOptions();

    var pie_options = jQuery.extend(true, {}, options);
    pie_options.series = [{type: 'pie'}];
    pie_options.tooltip = {
                    enabled:true,
                    formatter: function () {
                        return '<b>'+ this.point.name +'</b>: '+ this.percentage.toFixed(2) +' %';
                    }                    
                };
    pie_options.plotOptions = {
                    pie: {
                    	size : '60%',
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: true,
                            color: '#000000',
                            connectorColor: '#000000',
                            formatter: function() {
                                return '<b>'+ this.point.name +'</b>: '+ this.percentage.toFixed(2) +' %';
                            }
                        }
                    }
                };
    
    return pie_options;

}