(function($) {
    $.fn.my_spin = function (options) {
        var settings = $.extend(
            {},
            {
                initVal:0,
                min:0,
                max:100,
                step:5,
                enable:false,
                callback:function(){}
            },
            options);
        return this.each(function () {
            var sub = $($(this).parent().prev().children()[0]);
            var plus =$($(this).parent().next().children()[0]);
            var submit = $($(this).parent().next().next().children()[0]);
            var input = $(this);
            var step = parseFloat(settings.step);
            //Init
            {
                if(!settings.enable){
                    sub.addClass("disabled");
                    plus.addClass("disabled");
                    input.attr("disabled","disabled");
                    submit.addClass("disabled");
                }
                input.val(settings.initVal);
                if(settings.initVal<settings.min+settings.step){
                    sub.addClass("disabled");
                }
                if(settings.initVal>settings.max-settings.step){
                    plus.addClass("disabled");
                }
            }
            sub.on('click',function () {
                var val = parseFloat(input.val());
                if(val-step>=settings.min){
                    input.val(val-step);
                }
                if(input.val()<settings.min+step)
                    sub.addClass("disabled");
                else sub.removeClass("disabled");
                if(input.val()>settings.max-step)
                    plus.addClass("disabled");
                else plus.removeClass("disabled");
            });
            plus.on('click',function(){
                var val = parseFloat(input.val());
                if(val+step<=settings.max){
                    input.val(val+step);
                }
                if(input.val()<settings.min+step)
                    sub.addClass("disabled");
                else sub.removeClass("disabled");
                if(input.val()>settings.max-step)
                    plus.addClass("disabled");
                else plus.removeClass("disabled");
            });
            input.on('input',function(e){
                var val = parseFloat(input.val());
                if(val>settings.max-step){
                    plus.addClass('disabled');
                }else plus.removeClass('disabled');
                if(val<settings.min+step){
                    sub.addClass('disabled');
                }else sub.removeClass("disabled");

            });
            submit.on('click',function(){
                var val = parseFloat(input.val());
                if(isNaN(val)||val>settings.max||val<settings.min){
                    alert("error input!!!");
                }else{
                    settings.callback(val);
                }
            });
        });
    }
})(jQuery);



