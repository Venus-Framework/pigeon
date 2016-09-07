Router.prototype.preHash = "";
Router.prototype.hashTree = new HashTree();
Router.prototype.register = function(url,handler){
    this.hashTree.add(url,handler);
};
function addEventLoad(func){
    var oldOnload = window.onload;
    if(typeof window.onload != 'function'){
        window.onload = func;
    }else{
        window.onload = function(){
            oldOnload();
            func();
        }
    }
}
Router.prototype.start = function () {
    $(window).on('hashchange',function(){}).triggerHandler('hashchange');
    if("onhashchange" in window) {
        var router = this;
        window.onhashchange = function(){
            var preHash = router.preHash;
            var hash = window.location.hash;
            // var prefix = commonPrefix(preHash,hash);
            // var commonNode = router.hashTree.findNode(prefix);
            // router.hashTree.call(commonNode,hash.substring(prefix.length,hash.length));
            router.call(preHash,hash);
            router.preHash = hash;
        }
    }else{

    }
}


Router.prototype.call = function(preHash,hash){
    console.log(preHash);
    console.log(hash);
    var prefix = commonPrefix(preHash,hash);
    console.log(prefix);
    var nowNode =   this.hashTree.findNode(prefix);
    var trimUrl = hash.substring(prefix.length,hash.length);
    for(var i = 0;i< trimUrl.length;i++){
        var c = trimUrl.charAt(i);
        if(nowNode.hasChild('*')){
            if(c!="/"){
                if(i==trimUrl.length-1){
                    // nowNode.childNodes['*'].bindFunction();
                    wrapperCall(nowNode.childNodes['*'],prefix+trimUrl.substring(0,i+1));
                    break;
                }
                continue;
            }else{
                nowNode = nowNode.childNodes['*'];
            }
        }
        if(nowNode.hasChild(c)){
            wrapperCall(nowNode.childNodes[c],prefix+trimUrl.substring(0,i+1));
            // nowNode.childNodes[c].bindFunction();//call the bind function
        }else{
            console.log(nowNode);
            break;//unregister url
        }
        nowNode = nowNode.childNodes[c];
    }
}
function Router() {
    this.mapper = {};
    this.preHash = "";
    this.hashTree = new HashTree();
    return this;
}

function commonPrefix(s1,s2){
    var params1 = s1.split('/');
    console.log(params1);
    var params2 = s2.split('/');
    console.log(params2);
    if(params1.length>params2.length){
        return "";
    }else{
        var common = "";
        for(var i = 0;i<params1.length ; i++){
            if(params1[i]==params2[i])
                common = common+params1[i]+"/";
            else
                break;
        }
        return common;
    }
}
// (function($) {
//     $.fn.router = function (options) {
//         var settings = $.extend(
//             {},
//             {
//
//             },
//             options);
//         var register =  {};
//         return this.each(function() {
//
//         });
//     }
// })(jQuery);
HashNode.prototype.fragment="";
HashNode.prototype.url="";
HashNode.prototype.bindFunction = function(){};
HashNode.prototype.childNodes={};
HashNode.prototype.hasChild = function (c) {
    if(this.childNodes.hasOwnProperty(c)){
        return true;
    }else return false;
}
HashNode.prototype.addChild = function (node){
    this.childNodes[node.fragment] =  node;
}

function HashNode(fragment,callback,childrens){
    this.fragment = fragment;
    this.bindFunction = callback;
    this.childNodes = childrens;
    this.url = "";
    return this;
}

function HashTree(){
    this.root = new HashNode("",function () {},{});
    return this;
}
//TODO refactor the root node;
HashTree.prototype.root = new HashNode("",function(){},{});
HashTree.prototype.add = function(url,callback){
    var nowNode = this.root;
    for(var i = 0;i<url.length;i++){
        var c = url.charAt(i);
        if(!nowNode.hasChild(c)) {
            var hashNode = new HashNode(c, function () {}, {});
            hashNode.url = nowNode.url+c;
            nowNode.addChild(hashNode);
        }

        nowNode = nowNode.childNodes[c];
    }
    nowNode.bindFunction = callback;
}
//refactor!!! for the '*' !!!
HashTree.prototype.findNode = function(url){
    var nowNode = this.root;
    for(var i = 0 ;i<url.length;i++){
        var c = url.charAt(i);
        if(nowNode.hasChild('*')){
            if(c!="/"){
                if(i==url.length-1)
                    return nowNode.childNodes['*'];
                continue;
            }else{
                nowNode = nowNode.childNodes['*'];
            }
        }
        if(!nowNode.hasChild(c)){
            console.log("Find failed start");
            console.log(nowNode.url);
            console.log(c);
            console.log("Find failed end");
            break;
        }
        nowNode = nowNode.childNodes[c];
    }
    return nowNode;
}
//refactor!!! for the '*' !!!
// HashTree.prototype.call =  function(commonNode,trimUrl){
//     var prefix = commonPrefix(preHash,hash);
//     var commonNode = this.hashTree.findNode(prefix);
//     var nowNode = commonNode;
//     console.log(commonNode);
//     console.log("trim url is : "+trimUrl);
//     for(var i = 0;i< trimUrl.length;i++){
//         var c = trimUrl.charAt(i);
//         if(nowNode.hasChild('*')){
//             if(c!="/"){
//                 if(i==trimUrl.length-1){
//                     nowNode.childNodes['*'].bindFunction();
//                     wrapperCall(nowNode.childNodes['*'],prefix+trimUrl.substring(0,i+1));
//                     break;
//                 }
//                 continue;
//             }
//         }
//         if(nowNode.hasChild(c)){
//             wrapperCall(nowNode.childNodes['*'],prefix+trimUrl.substring(0,i+1));
//             nowNode.childNodes[c].bindFunction();//call the bind function
//         }else{
//             console.log(nowNode);
//             break;//unregister url
//         }
//         nowNode = nowNode.childNodes[c];
//     }
// }

function wrapperCall(node,rawUrl){
    var normalizedUrl = node.url;
    var normalizedList = normalizedUrl.split("/");
    var rawList = rawUrl.split("/");
    var params = [];
    var j = 0;
    for(var i =0;i<normalizedList.length;i++){
        if(normalizedList[i]=='*'){
            params[j] = rawList[i];
            j++;
        }
    }
    node.bindFunction(params);
}






