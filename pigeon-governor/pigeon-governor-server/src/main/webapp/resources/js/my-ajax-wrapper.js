/*
 *
 *This function is used only for load the html data from sever and append the data into the container Object.
 *
 * 本插件仅仅负责在进行AJAX拉取数据时,在原始的外围Wrapper元素上添加一个Spinner.在AJAX完成后,删除Spinner.后续需要添加Spinner的配置接口.(若元素的原始
 * width较小,同样需要考虑到Spinner是否超出的问题)
 *
 * */
(function($) {
    $.fn.ajaxLoad= function(options) {
        var settings = $.extend(
            {},
            {
                type:"post",
                dataType:"html",
                async:true,
                successCallback:function(){}
            },
            options);
        var opts = {
            lines: 10, // The number of lines to draw
            length: 30, // The length of each line
            width: 12, // The line thickness
            radius: 40, // The radius of the inner circle
            corners: 0.8, // Corner roundness (0..1)
            rotate: 31, // The rotation offset
            color: '#4CCAFF', // #rgb or #rrggbb
            speed: 2.1, // Rounds per second
            trail: 64, // Afterglow percentage
            shadow: false, // Whether to render a shadow
            hwaccel: false, // Whether to use hardware acceleration
            className: 'spinner', // The CSS class to assign to the spinner
            zIndex: 2e9, // The z-index (defaults to 2000000000)
            top: 'auto', // Top position relative to parent in px
            left: 'auto' // Left position relative to parent in px
        };
        return this.each(function() {
            var wrapperObject = $(this);
            wrapperObject.html('');
            var originWidth = wrapperObject.width();
            wrapperObject.height(originWidth/2);
            var ajaxType = settings.type;
            var ajaxUrl = settings.url;
            var ajaxData = settings.data;
            var ajaxDataType = settings.dataType;
            var ajaxAsync = settings.async;
            wrapperObject.append("<div class='col-xs-12 col-sm-12'></div>");
            var spinWrapper = wrapperObject.children(':first');
            spinWrapper.height(spinWrapper.width()/2);
            new Spinner(opts).spin(spinWrapper[0]);
            $.ajax({
                type:ajaxType,
                url:ajaxUrl,
                data:ajaxData,
                dataType:ajaxDataType,
                async:ajaxAsync,
                success:function(data){
                    wrapperObject.height('auto');
                    settings.successCallback();
                    wrapperObject.html(data);
                }
            });
        });
    }
})(jQuery);