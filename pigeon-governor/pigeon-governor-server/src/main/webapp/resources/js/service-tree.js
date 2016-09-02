var DataSourceTree = function(options) {
    this._data 	= options.data;
}






DataSourceTree.prototype.data = function(options, callback) {
    var $data = null;
    if(!("name" in options) && !("type" in options)){
        $data = this._data;//the root tree
        callback({ data: $data });
        return;
    }
    else if("type" in options && options.type == "folder") {
        if("additionalParameters" in options && "children" in options.additionalParameters)
            $data = options.additionalParameters.children;
        else $data = {}//no data
    }
    if($data!=null){
        callback({data:$data});
        return;
    }
};
//
//
// var ace_icon = ace.vars['icon'];
// //class="'+ace_icon+' fa fa-file-text grey"
// //becomes
// //class="ace-icon fa fa-file-text grey"
// var tree_data_2 = {
// 	'pictures' : {name: 'Pictures', type: 'folder', 'icon-class':'red'}	,
// 	'music' : {name: 'Music', type: 'folder', 'icon-class':'orange'}	,
// 	'video' : {name: 'Video', type: 'folder', 'icon-class':'blue'}	,
// 	'documents' : {name: 'Documents', type: 'folder', 'icon-class':'green'}	,
// 	'backup' : {name: 'Backup', type: 'folder'}	,
// 	'readme' : {name: '<i class="'+ace_icon+' fa fa-file-text grey"></i> ReadMe.txt', type: 'item'},
// 	'manual' : {name: '<i class="'+ace_icon+' fa fa-book blue"></i> Manual.html', type: 'item'}
// }
// tree_data_2['music']['additionalParameters'] = {
// 	'children' : [
// 		{name: '<i class="'+ace_icon+' fa fa-music blue"></i> song1.ogg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-music blue"></i> song2.ogg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-music blue"></i> song3.ogg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-music blue"></i> song4.ogg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-music blue"></i> song5.ogg', type: 'item'}
// 	]
// }
// tree_data_2['video']['additionalParameters'] = {
// 	'children' : [
// 		{name: '<i class="'+ace_icon+' fa fa-film blue"></i> movie1.avi', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-film blue"></i> movie2.avi', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-film blue"></i> movie3.avi', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-film blue"></i> movie4.avi', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-film blue"></i> movie5.avi', type: 'item'}
// 	]
// }
// tree_data_2['pictures']['additionalParameters'] = {
// 	'children' : {
// 		'wallpapers' : {name: 'Wallpapers', type: 'folder', 'icon-class':'pink'},
// 		'camera' : {name: 'Camera', type: 'folder', 'icon-class':'pink'}
// 	}
// }
// tree_data_2['pictures']['additionalParameters']['children']['wallpapers']['additionalParameters'] = {
// 	'children' : [
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> wallpaper1.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> wallpaper2.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> wallpaper3.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> wallpaper4.jpg', type: 'item'}
// 	]
// }
// tree_data_2['pictures']['additionalParameters']['children']['camera']['additionalParameters'] = {
// 	'children' : [
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> photo1.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> photo2.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> photo3.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> photo4.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> photo5.jpg', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-picture-o green"></i> photo6.jpg', type: 'item'}
// 	]
// }
//
//
// tree_data_2['documents']['additionalParameters'] = {
// 	'children' : [
// 		{name: '<i class="'+ace_icon+' fa fa-file-text red"></i> document1.pdf', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-file-text grey"></i> document2.doc', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-file-text grey"></i> document3.doc', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-file-text red"></i> document4.pdf', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-file-text grey"></i> document5.doc', type: 'item'}
// 	]
// }
//
// tree_data_2['backup']['additionalParameters'] = {
// 	'children' : [
// 		{name: '<i class="'+ace_icon+' fa fa-archive brown"></i> backup1.zip', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-archive brown"></i> backup2.zip', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-archive brown"></i> backup3.zip', type: 'item'},
// 		{name: '<i class="'+ace_icon+' fa fa-archive brown"></i> backup4.zip', type: 'item'}
// 	]
// }
// var treeDataSource2 = new DataSourceTree({data: tree_data_2});