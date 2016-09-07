(function($) {
    $.fn.selector_input = function (options) {
        var settings = $.extend(
            {},
            {
                submitObj:null,
                selectorObj:null,
                initSelectorIndex:0,
                inputObjs:[],
                callbacks:[]
            },
            options);
        var shownIndex = settings.initSelectorIndex;
        settings.submitObj.unbind('click');
        var selector = settings.selectorObj;
        selector.unbind('click');
        var initIndex = settings.initSelectorIndex;
        selector.val(initIndex);
        selector.trigger("chosen:updated");
        var inputs = settings.inputObjs;
        $(inputs).each(function(index){
            inputs[index].hide();
        });
        inputs[initIndex].show();
        selector.bind('change', function(evt, params) {
            var nowIndex = $(selector).val();

            inputs[shownIndex].hide();
            inputs[nowIndex].show();
            shownIndex = nowIndex;
        });
        settings.submitObj.bind('click',function(){
            settings.callbacks[shownIndex]();
        });

    }
})(jQuery);



