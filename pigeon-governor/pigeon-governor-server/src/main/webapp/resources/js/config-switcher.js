(function($) {
    $.fn.switcher = function (options) {
        var settings = $.extend(
            {},
            {
                enable:false,
                initState:true,
                falseToTrueValidateText:"<span class='bigger-110'>是否开启Region路由</span>",
                falseToTrueType:"post",
                falseToTrueUrl:"",
                falseToTrueData:"",
                falseToTrueDataType:"html",
                trueToFalseValidateText:"<span class='bigger-110'>是否关闭Region路由</span>",
                trueToFalseType:"post",
                trueToFalseUrl:"",
                trueToFalseData:"",
                trueToFalseDataType:"html",
                name:"my-checkbox"
            },
            options);
        return this.each(function () {
            $.fn.bootstrapSwitch.defaults.onText =
                '<i class="ace-icon glyphicon glyphicon-play white"></i><span class="white">已开启</span>';
            $.fn.bootstrapSwitch.defaults.offText =
                '<i class="ace-icon glyphicon glyphicon-pause red"></i><span class="red">已关闭</span>';
            $.fn.bootstrapSwitch.defaults.size='mini';
            var switcher = $(this).find("[name="+settings.name+"]");
            switcher.bootstrapSwitch();
            if(settings.initState)
                switcher.bootstrapSwitch('state',true);
            else
                switcher.bootstrapSwitch('state',false);
            if(!settings.enable)
                switcher.bootstrapSwitch('disabled',true);
            else
                switcher.bootstrapSwitch('disabled',false);
            switcher.on('switchChange.bootstrapSwitch', function(event, state) {
                if(state){
                    switcher.bootstrapSwitch("state",false,true);
                    bootbox.dialog({
                        message: settings.falseToTrueValidateText,
                        buttons:
                        {
                            "confirm" :
                            {
                                "label" : "确认开启",
                                "className" : "btn-sm btn-danger",
                                "callback": function() {
                                    $.ajax({
                                        type: settings.falseToTrueType,
                                        url: settings.falseToTrueUrl,
                                        data: settings.falseToTrueData,
                                        dataType: settings.falseToTrueDataType,
                                        async: true,
                                        success: function (data) {
                                            //TODO handle set fail
                                            bootbox.dialog({
                                                message: "开启成功!",
                                                buttons: {
                                                    "success": {
                                                        "label": "<i class='ace-icon fa fa-check'></i> Confirm",
                                                        "className": "btn-sm btn-success"
                                                    }
                                                }
                                            });
                                            switcher.bootstrapSwitch("state",true,true);
                                        }
                                    });
                                }
                            },
                            "cancel" :
                            {
                                "label" : "保持关闭",
                                "className" : "btn-sm btn-success",
                                "callback": function() {
                                }
                            }
                        }
                    });
                }else{
                    switcher.bootstrapSwitch("state",true,true);
                    bootbox.dialog({
                        message: settings.trueToFalseValidateText,
                        buttons:
                        {
                            "confirm" :
                            {
                                "label" : "确认关闭",
                                "className" : "btn-sm btn-danger",
                                "callback": function() {
                                    //TODO
                                    $.ajax({
                                        type: settings.trueToFalseType,
                                        url: settings.trueToFalseUrl,
                                        data: settings.trueToFalseData,
                                        dataType: settings.trueToFalseDataType,
                                        async: true,
                                        success: function (data) {
                                            bootbox.dialog({
                                                message: "关闭成功!",
                                                buttons: {
                                                    "success": {
                                                        "label": "<i class='ace-icon fa fa-check'></i> Confirm",
                                                        "className": "btn-sm btn-success"
                                                    }
                                                }
                                            });
                                            switcher.bootstrapSwitch("state",false,true);
                                        }
                                    });
                                }
                            },
                            "cancel" :
                            {
                                "label" : "保持开启",
                                "className" : "btn-sm btn-success",
                                "callback": function() {
                                }
                            }
                        }
                    });

                }
            });

        });
    }
})(jQuery);



