(function(){window.XA||(XA={_url:"//xa.xingcloud.com/v4/",_actions:[],_updates:[],_sending:false,init:function(a){if(!a.app)throw Error("App is required.");XA._app=a.app;XA._uid=a.uid||"random"},setUid:function(a){XA._uid=a},action:function(){for(var a=0,b=arguments.length;a<b;a++)XA._actions.push(arguments[a]);XA._asyncSend()},update:function(){for(var a=0,b=arguments.length;a<b;a++)XA._updates.push(arguments[a]);XA._asyncSend()},_asyncSend:function(){setTimeout(function(){var a=XA._url+XA._app+
"/"+XA._uid+"?",b=null,c="",d=0;if(!(XA._updates.length+XA._actions.length==0||XA._sending)){for(XA._sending=true;b=XA._updates.shift();)if(c="update"+d++ +"="+encodeURIComponent(b)+"&",a.length+c.length>=1980){XA._updates.unshift(b);break}else a+=c;for(d=0;b=XA._actions.shift();)if(c="action"+d++ +"="+encodeURIComponent(b)+"&",a.length+c.length>=1980){XA._actions.unshift(b);break}else a+=c;(new Image).src=a+"_ts="+(new Date).getTime();XA._updates.length+XA._actions.length>0&&XA._asyncSend();XA._sending=
false}},0)}})})();

XA.init({app:"lp"});
XA.setUid('random');
function event_fire(e){
	m = /^\/(.+)\/(\d+)_(\d+)$/.exec(XA._lp);
	if(!m) throw new Error('invalid XA._lp: ' + XA._lp);
	console.log(m);
	XA.action([m[1], m[2], e, m[3]].join('.'));
}
