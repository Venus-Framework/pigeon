/*
*
*
*
* */
(function($) {
    $.fn.accordion = function(options) {
        var settings = $.extend(
            {},
            {
                initIndex:1,   //初始索引位置,在自动折叠中,暂时不考虑.
                wrapperClass:"pagination",//包裹元素的Class
                fold:false,//是否采取折叠或者完全展开模式
                viewNumber:5, //折叠模式下中部元素的个数
                autoFoldNumber:11,
                pageList:[]
            },
            options);
        return this.each(function() {
            var wrapperClass = settings.wrapperClass;
            $(this).append("<ul class="+wrapperClass+"></ul>");
            var wrapper = $(this).children(':first');
            if(settings.fold&&settings.size>settings.autoFoldNumber){
                wrapper.append("<li class='pagination-pre'><a href='#'><i class='ace-icon fa fa-angle-double-left'></i></a></li>");
                var viewNumber = settings.viewNumber;
                var size = settings.size;
                var i  = 0;
                while(i<viewNumber){
                    wrapper.append("<li><a>"+(i+1)+"</a></li>");
                    i++;
                }
                wrapper.append("<li class='disabled'><a>...</a></li>");
                wrapper.append("<li class='pagination-next'><a href='#'><i class='ace-icon fa fa-angle-double-right'></i></a></li>");
                var spreadFunction = function(){
                    var index = parseInt($(this).children().html());
                    settings.callback(index);
                    if(index%(viewNumber-1)==1){
                        $($(this).parent().children()).each(function (i,item) {
                            if(!$(this).hasClass('pagination-next')&&!$(this).hasClass('pagination-pre'))
                                $(this).remove();
                        });
                        i = 0;
                        while(i<viewNumber&&i+index<=size){
                            wrapper.children().eq(i).after("<li><a>"+(index+i)+"</a></li>");
                            wrapper.children().eq(i+1).bind("click",spreadFunction);
                            i++;
                        }
                        if(index!=1){
                            wrapper.children().eq(0).after("<li class='disabled'><a>...</a></li>");
                        }
                        if((index+viewNumber)<size){
                           wrapper.children().last().before("<li class='disabled'><a>...</a></li>");
                        }
                        wrapper.children().not('.pagination-pre').not('.disabled').eq(0).addClass('active');
                    }else{
                        wrapper.find('.active').removeClass('active');
                        $(this).addClass('active');
                    }
                    if(index==size)
                        wrapper.children().last().addClass('disabled');
                    else
                        wrapper.children().last().removeClass('disabled');
                    if(index==1)
                        wrapper.children().first().addClass('disabled');
                    else
                        wrapper.children().first().removeClass('disabled');
                };
                //Only choose the validate elements.
                wrapper.children().not(".pagination-next").not(".pagination-pre").bind('click',spreadFunction);
                wrapper.find('.pagination-next').bind('click',function () {
                    var nowPage =wrapper.find('.active');
                    if(!nowPage.next().hasClass('.pagination-next')){
                        nowPage.next().click();
                    }
                });
                var forwardSpread = function(obj){
                    var index = parseInt(obj.children().html());
                    settings.callback(index-1);
                    $(obj.parent().children()).each(function(i,item){
                        if(!$(this).hasClass('pagination-next')&&!$(this).hasClass('pagination-pre'))
                            $(this).remove();
                    });
                    i = 0;
                    while(i<viewNumber){
                        wrapper.children().eq(i).after("<li><a>"+(index-viewNumber+1+i)+"</a></li>");
                        wrapper.children().eq(i+1).bind("click",spreadFunction);
                        i++;
                    }
                    if(index!=viewNumber){
                        wrapper.children().eq(0).after("<li class='disabled'><a>...</a></li>");
                    }
                    wrapper.children().last().before("<li class='disabled'><a>...</a></li>");
                    wrapper.children().not('.pagination-next').not('.disabled').last().prev().addClass('active');
                };
                wrapper.find('.pagination-pre').bind('click',function(){
                    if(!$(this).hasClass('disabled')){
                        var nowPage = wrapper.find('.active');
                        if(!nowPage.prev().hasClass('pagination-pre')){
                            if(nowPage.prev().hasClass('disabled')){
                                forwardSpread(nowPage);
                            }else{
                                nowPage.prev().click();
                            }
                        }else{
                        }
                    }
                });
                wrapper.children().eq(1).addClass('active');
                wrapper.children().eq(0).addClass('disabled');
            }else
            {
                var pageList = settings.pageList;
                var size = pageList.length;
                if(size==0){
                    size = settings.size;
                    if(size!=0){
                        wrapper.append("<li class='pagination-pre'><a href='#'><i class='ace-icon fa fa-angle-double-left'></i></a></li>");
                        var i = 0;
                        while(i<size){
                            wrapper.append("<li><a>"+(i+1)+"</a></li>");
                            wrapper.children().eq(i+1).bind('click',function () {
                                $(this).siblings().filter('.active').removeClass('active')
                                var index  = parseInt($(this).children().html());
                                settings.callback(index);
                                $(this).addClass('active');
                                if(index == size){
                                    $(this).siblings().last().addClass('disabled');
                                }else
                                    $(this).siblings().last().removeClass('disabled');
                                if(index==1){
                                    $(this).siblings().first().addClass('disabled');
                                }else{
                                    $(this).siblings().first().removeClass('disabled');
                                }
                            });
                            i++;
                        }
                        wrapper.append("<li class ='pagination-next'><a href='#'><i class='ace-icon fa fa-angle-double-right'></i></a></li>");
                        wrapper.children().eq(0).bind('click',function(){
                            var activePage = wrapper.find('.active');
                            if(activePage.prev().hasClass('pagination-pre')){

                            }else
                                activePage.prev().click();
                        });
                        wrapper.children().eq(size+1).bind('click',function () {
                            var activePage = wrapper.find('.active');
                            if(activePage.next().hasClass('pagination-next')){

                            }else
                                activePage.next().click();
                        });
                        wrapper.children().eq(1).addClass('active');
                        wrapper.children().eq(0).addClass('disabled');
                        if(size==1){
                            wrapper.children().eq(2).addClass('disabled');
                        }
                    }
                }else{
                    {
                        wrapper.append("<li class='pagination-pre'><a href='#'><i class='ace-icon fa fa-angle-double-left'></i></a></li>");
                        $.each(pageList,function (i,item) {
                            wrapper.append("<li><a>"+item.data+"</a></li>");
                        });
                        wrapper.append("<li class='pagination-next'><a href='#'><i class='ace-icon fa fa-angle-double-right'></i></a></lic>");
                        wrapper.children().eq(settings.initIndex).addClass('active');
                        var initPage = wrapper.children().eq(settings.initIndex);
                        if(initPage.prev().hasClass('pagination-pre')){
                            initPage.prev().addClass('disabled')
                        }
                        if(initPage.next().hasClass('pagination-next')){
                            initPage.next().addClass('disabled');
                        }
                    }
                    $.each(pageList,function (i,item) {
                        var nowPage = wrapper.children().eq(i+1);
                        nowPage.bind("click",function () {
                            wrapper.find('li.active').removeClass('active');
                            nowPage.addClass('active');
                            if(nowPage.prev().hasClass('pagination-pre')){
                                nowPage.prev().addClass('disabled');
                            }else{
                                nowPage.parent().children().eq(0).removeClass('disabled');
                            }
                            if(nowPage.next().hasClass('pagination-next')){
                                nowPage.next().addClass('disabled');
                            }else{
                                nowPage.parent().children().eq(size+1).removeClass('disabled');
                            }
                            item.callback();
                        });
                    });
                    wrapper.children().eq(0).bind('click',function(){
                        var activePage = wrapper.find('.active');
                        if(activePage.prev().hasClass('pagination-pre')){

                        }else
                            activePage.prev().click();
                    });
                    wrapper.children().eq(size+1).bind('click',function () {
                        var activePage = wrapper.find('.active');
                        if(activePage.next().hasClass('pagination-next')){

                        }else
                            activePage.next().click();
                    });
                }
            }

        });
    }
})(jQuery);