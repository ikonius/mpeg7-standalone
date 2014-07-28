var transTitle = 'MPEG-7 Transformation';
var procComplete = 'Processing complete';
var procError = 'Processing error occured';
var uploadTitle = 'X3D Upload';
var bar = $('.progress-bar');
var percentVal = '0%';
var x3dBrowser;
var xmlData;
var timeout = 4000;
$(document).ready(function() {
    x3dBrowser = $(".x3dbrowser");
    x3dBrowser.hide();
    $.getJSON("modules/ajax.xql",
            function(data) {
                $('#location').typeahead({
                    items: 'all',
                    source: function(query, process) {
                        collections = [];
                        map = {};
                        $.each(data, function(i, collection) {
                            map[collection.name] = collection;
                            collections.push(collection.name);
                        });

                        process(collections);
                    },
                    matcher: function(item) {
                        if (item.toLowerCase().indexOf(this.query.trim().toLowerCase()) !== -1) {
                            return true;
                        }
                    },
                    sorter: function(items) {
                        return items.sort();
                    },
                    highlighter: function(item) {
                        var regex = new RegExp('(' + this.query + ')', 'gi');
                        return item.replace(regex, "<strong>$1</strong>");
                    },
                    updater: function(item) {
                        selectedCollection = map[item].path;
                        return selectedCollection;
                    }
                });
            });
    //+ '<span align="right" class="hint-small">'+collection.path + '</span>'
    initDirectory();
    $("#load").on('click', function() {
        initDirectory();
    });

    $('.btn-file :file').on('change', function() {
        var input = $(this),
                numFiles = input.get(0).files ? input.get(0).files.length : 1,
                label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label])
    })
            .on('fileselect', function(event, numFiles, label) {
                $("#fileSelected").val(label);
            });

    $("#querySelector").on("change", function() {
        $("#searchTerm").val(this.value);
    });



    $("#file").on('dblclick', function(e) {
        e.preventDefault();
        selectText('file');

    });

    $("#logRow").slideUp();
    $(".close").on('click', function() {
        $(this).closest(".row").slideUp("slow");
    });
    $("#logForm").ajaxForm({
        complete: function(response, statusText, xhr, $form) {
            var logTable = $("#logTable");
            logTable.html(response.responseText);
            logTable.find("table").addClass("table table-hover");
            $("#logRow").slideDown("slow");
        }
    });

    $('#uploader').ajaxForm({
        beforeSubmit: function() {
            if (CheckFileName() === false) {
                return false;
            }
            percentVal = '0%';
            bar.css('width', percentVal).attr('aria-valuenow', 0);
            bar.html(percentVal);
            $.growlUI(uploadTitle, 'File is being uploaded', timeout);
        },
        uploadProgress: function(event, position, total, percentComplete) {
            percentVal = percentComplete + '%';
            bar.css('width', percentVal).attr('aria-valuenow', percentComplete);
            bar.html(percentVal);
            if (percentComplete === 100) {
                $.blockUI({
                    message: '<h1>Please wait</h1><img src="resources/css/images/loader.gif" alt="loading"/><p>File being processed on the server. </p>'
                });
            }
        },
        success: function() {
            percentVal = '100%';
            bar.width(percentVal);
            bar.attr('aria-valuenow', 100);
            bar.html(percentVal);
        },
        complete: function(response, statusText, xhr, $form) {
            $.unblockUI();
            percentVal = '100%';
            bar.css('width', percentVal).attr('aria-valuenow', 100);
            bar.html(percentVal);

            if (statusText === "success") {
                $.growlUI(uploadTitle, procComplete + ' - File contents successfully extracted into the database collection', timeout);
                $.blockUI({
                    message: '<h1>Please wait</h1><img src="resources/css/images/loader.gif" alt="loading"/><p>Transformation of extracted X3D resources on server.</p>'
                });
                var options = {
                    success: showResponse,
                    data: {
                        collection: response.responseText
                    }
                };
                $('#transformer').ajaxSubmit(options);
            } else {
                $.growlUI(transTitle, procError + ': ' + response.responseText, timeout);
            }
            initDirectory();
            percentVal = '0%';
            bar.css('width', percentVal).attr('aria-valuenow', 0);
            bar.html("Server ready.");
            $('#logger').removeClass("hidden");
        }
    });

    $('#queryForm').ajaxForm({
        beforeSend: function() {
            $('#file').empty();
        },
        success: function(response) {
            $('#file').html(response);
            $('#fileComps').removeClass("hidden");
            $('#openFileWin').addClass("hidden");
            $('#selFileName').addClass("hidden");
        }
    });
});

function initDirectory() {
    $(".scroller").empty();
    $('#openFileWin').addClass("hidden");
    $("#fileComps").addClass("hidden");
    x3dBrowser.hide();
    $("#directory").fileTree({
        root: '/db/3dData/x3d',
        script: "modules/filetree.xql"
    },
    function(file) {
        var isX3d = file.indexOf(".x3d") > -1;
        if (isX3d || file.indexOf(".mp7") > -1) {
            $.ajax({
                url: file,
                dataType: "text",
                success: function(data) {
                    xml_neat = formatXml(data);
                    $('#file').html(xml_neat);
                    document.getElementById("openFileWin").href = file;
                    $('#selFileName').html(file.split("/").pop());
                    $('#selFileName').removeClass("hidden");
                    $('#fileComps').removeClass("hidden");
                    $('#openFileWin').removeClass("hidden");
                    if (isX3d) {
                        xmlData = $.parseXML(data);
                        $(".x3dbrowser").html("");
                        $(xmlData).find("X3D").appendTo(".x3dbrowser");
                        x3dBrowser.show();
                        x3dom.reload();
                    }
                }
            });
        } else {
            window.open(file);
        }
    }
    );

    $.contextMenu({
        selector: "li.file.ext_x3d a",
        items: {
            transform: {
                name: "Generate MPEG-7",
                icon: "edit",
                callback: function(key, opt) {
                    $.blockUI({
                        message: '<h1>Please wait</h1><img src="resources/css/images/loader.gif" alt="loading"/><p>Transformation of X3D resource on server.</p>'
                    });
                    $.post("modules/singletransform.xql", {
                        file: opt.$trigger.data("filename"),
                        collection: opt.$trigger.data("path")
                    },
                    function(data) {
                        $.unblockUI();
                        if (data === "true") {
                            $.growlUI(transTitle, procComplete, timeout);
                        } else {
                            $.growlUI(transTitle, procError, timeout);
                        }
                    });
                }
            }
        }
    });
}

function showResponse(responseText, statusText, xhr, $form) {
    $.unblockUI();
    if (statusText === "success") {
        $.growlUI(transTitle, procComplete);
    } else {
        $.growlUI(transTitle, procError + ': ' + responseText);
    }
}

function CheckFileName() {
    var fileNameExt = document.getElementById("uploadFile").value.split(/[.]+/).pop();
    if (fileNameExt.toUpperCase() === "ZIP") {
        if (GetFileSize("uploadFile") < parseFloat(2.50)) {
            return true;
        }
        $.growlUI('Upload Error', 'Maximum upload size is 2.5 MB.');
    } else {
        $.growlUI('Upload Error', 'Please browse to upload a valid File with zip extension');
    }
    $('#reset').click();
    return false;
}

function GetFileSize(fileid) {
    try {
        var fileSize = 0;
        //for IE
        if (navigator.appVersion.indexOf('.NET') > -1) {
            //before making an object of ActiveXObject, 
            //please make sure ActiveX is enabled in your IE browser
            var objFSO = new ActiveXObject("Scripting.FileSystemObject");
            var filePath = $("#" + fileid)[0].value;
            var objFile = objFSO.getFile(filePath);
            var fileSize = objFile.size; //size in kb
        }
        //for FF, Safari, Opera and Others
        else {
            fileSize = $("#" + fileid)[0].files[0].size //size in kb
        }
        fileSize = fileSize / 1048576; //size in mb 

    } catch (e) {
        console.log("Error is :" + e);
    }
    return parseFloat(fileSize.toFixed(2));
}

function formatXml(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match(/.+<\/\w[^>]*>$/)) {
            indent = 0;
        } else if (node.match(/^<\/\w/)) {
            if (pad !== 0) {
                pad -= 1;
            }
        } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
            indent = 1;
        } else {
            indent = 0;
        }
        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }
        formatted += padding + node + '\r\n';
        pad += indent;
    });
    return formatted.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;').replace(/\n/g, '<br />');
}

function selectText(containerid) {
    if (document.selection) {
        var range = document.body.createTextRange();
        range.moveToElementText(document.getElementById(containerid));
        range.select();
    } else if (window.getSelection) {
        var range = document.createRange();
        range.selectNode(document.getElementById(containerid));
        window.getSelection().addRange(range);
    }
}

$(function() {

    $.getJSON("modules/statistics.xql",
            function(data) {
                var stats = data;
                $('.count').highcharts({
                    chart: {
                        plotBackgroundColor: null,
                        plotBorderWidth: 0,
                        plotShadow: false
                    },
                    title: {
                        text: 'Document<br>count',
                        align: 'center',
                        verticalAlign: 'middle',
                        y: 50
                    },
                    tooltip: {
                        pointFormat: '{series.name}: <b>{point.y}</b><br/>Percentage: <b>{point.percentage:.1f}%</b>'
                    },
                    plotOptions: {
                        pie: {
                            dataLabels: {
                                enabled: true,
                                distance: -40,
                                style: {
                                    fontWeight: 'bold',
                                    color: 'white',
                                    textShadow: '0px 1px 2px black'
                                }
                            },
                            startAngle: -90,
                            endAngle: 90,
                            center: ['50%', '75%']
                        }
                    },
                    series: [{
                            type: 'pie',
                            name: 'Documents',
                            innerSize: '50%',
                            data: [{
                                    name: Object.keys(stats.counters[0])[0],
                                    y: Number(stats.counters[0].X3D[0].count),
                                    color: '#8085e9'
                                }, {
                                    name: Object.keys(stats.counters[0])[1],
                                    y: Number(stats.counters[0].MPEG7[0].count),
                                    color: '#f15c80'
                                }

                            ]
                        }]
                });
            });

});