<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link href="/ztree/ztree.css" rel="stylesheet" type="text/css"/>
  <link href="/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
  <link href="/jquery/jquery-ui.css" rel="stylesheet" type="text/css"/>
  <script src="/jquery/jquery-1.7.2.min.js"></script>
  <script src="/jquery/jquery-ui.js"></script>
  <script src="/jquery/jquery.easy-confirm-dialog.js"></script>
  <script src="/ztree/jquery.ztree.core-3.3.min.js"></script>
	<SCRIPT type="text/javascript">
		<!--
		var setting = {
			data: {
				key: {
					title:"title"
				},
				simpleData: {
					enable: true
				},
			},
			callback: {
				onClick: function(e, treeId, treeNode, clickFlag){
					var content = $("#content").empty().show();
					var result = $("#result").empty().hide();
					if(treeNode.pId == null){
						content.hide();
						return;
					}
					if('${env}'=='product'){
						content.append("<p>verification code(you can get it from pigeon log):<input type='text' id='token' value=''/></p>");
					}
					for(var i = 0; i < treeNode.parameters; i++){
						content.append("<p><input type='text' value=''/></p>");
					}
					content.append("<button id='invokeBtn' class='submit'>invoke</button>");
					$("button.submit", content).on("click", function(){
						var pdata = {};
						pdata.parameters = [];
						$("input", content).each(function(){
							if($(this).attr("id")!='token'){
								pdata.parameters.push($(this).val());
							}
						});
						pdata.url = treeNode.data_url;
						pdata.method = treeNode.data_method;
						pdata.parameterTypes = treeNode.data_parameterTypes.split(',');
						$.ajax({
							url:"/invoke.json?validate=true&token=" + $("#token").val(),
							data: pdata,
							success: function(m){
								result.text(m).show();
							}
						});
					});
					if('${env}'=='product'){
						$("#invokeBtn").easyconfirm({locale: {
							title: '警告',
							text: '是否确认执行该调用?在线上请务必谨慎执行！！！',
							button: ['取消','确认'],
							closeText: '取消'
						}});
					}
				}
			}
		};

		var zNodes =[
			<#list services as x>
			{ id:${x_index + 1}, pId:0, name:"${x.name}", title:"${x.type.canonicalName}", isParent:true}<#if (x.methods?size>0) >,<#elseif x_has_next>,</#if>
				<#list x.methods as m>
			{ id:${x_index + 1}${m_index + 1}, pId:${x_index + 1}, 
				data_url:"${x.name}",
				data_method:"${m.name}",
				data_parameterTypes: "<#list m.parameterTypes as p>${p.name}<#if p_has_next>,</#if></#list>",
				name:"${m.name}(<#list m.parameterTypes as p>${p.canonicalName}<#if p_has_next>,</#if></#list>)", 
				parameters:"${m.parameterTypes?size}"}<#if m_has_next>,<#elseif x_has_next>,</#if>
				</#list>
			</#list> 
		];

		$(document).ready(function(){
			$.fn.zTree.init($("#treeDemo"), setting, zNodes);
		});
		//-->
	</SCRIPT>
</head>
<body style="font-size:62.5%;padding-top:10px;padding-left:10px;">
	<div class="row">
	  <div class="span6"  style="overflow:hidden">
		<div>
		<p>pigeon services registered at port ${port?c}/${httpPort?c}:</p>
		<p>version: ${version}</p>
		<p>env: ${env}</p>
		</div>
		<div>
			<div class="zTreeDemoBackground left">
				<ul id="treeDemo" class="ztree"></ul>
			</div>
		</div>  
	  </div>
	  <div class="span6">
	  	<div id="content"></div>
	  	<div id='result' class='' style="display:none;border:1px solid #ddd;width:100%;height:100px;"></div>
	  </div>
	</div>

</body>
</html>