import{d as B,r as p,p as q,o as O,c as v,e as s,a6 as z,f as t,w as l,m as n,g as _,u as r,Z as A,ae as L,Y as k,j as H,t as i,X as R,ap as W,v as j,T as X,ar as Z,n as $,q as f,E as h}from"./index-C1E1J4Jo.js";import{request as T}from"./request-DLkGH8Nn.js";import{_ as F}from"./_plugin-vue_export-helper-DlAUqK2U.js";const G={class:"notice-detail-page"},J={class:"back-bar"},K={class:"notice-card"},P={class:"notice-header"},Q={key:0,class:"tags"},U={class:"notice-title"},ee={class:"notice-meta"},te={class:"meta-item"},ae={class:"meta-item"},oe={class:"meta-item"},se=["innerHTML"],le={class:"notice-footer"},ne={class:"footer-left"},ie={class:"update-time"},ce={class:"footer-right"},re=`
<p>尊敬的志愿者同学们</p>

<p>为进一步提升我校志愿服务水平，丰富志愿者的服务经验，现将有关事项通知如下</p>

<h3>一、活动背景</h3>
<p>志愿服务是培养大学生社会责任感、奉献精神的重要途径。本次活动旨在通过系统化的培训和实践，提升志愿者的专业服务能力。</p>

<h3>二、活动安排</h3>
<ul>
  <li><strong>时间：</strong>2026年2月10日至2026年3月10日</li>
  <li><strong>地点：</strong>各社区服务站点、校内活动中心</li>
  <li><strong>对象：</strong>全体注册志愿者</li>
</ul>

<h3>三、参与方式</h3>
<ol>
  <li>登录志愿者管理系统，完成在线报名</li>
  <li>关注系统通知，了解具体活动安排</li>
  <li>按时参加培训和活动，完成服务任务</li>
</ol>

<h3>四、奖励机制</h3>
<p>参与本次活动的志愿者将获得：</p>
<ul>
  <li>志愿服务时长认定</li>
  <li>积分奖励（可在积分商城兑换礼品）</li>
  <li>优秀志愿者证书（表现突出者）</li>
</ul>

<blockquote style="background: #f0f9eb; padding: 16px; border-left: 4px solid #67c23a; margin: 20px 0;">
  <p style="margin: 0; color: #67c23a;"><strong>温馨提示：</strong>请各位志愿者提前做好时间规划，积极参与活动，共同为社会贡献力量！</p>
</blockquote>

<p style="text-align: right; margin-top: 40px;">
  <strong>志愿者管理中心</strong><br/>
  2026年1月18日
</p>
`,de=B({__name:"NoticeDetail",setup(ue){const w=q(),N=$(),u=p(Number(w.params.id)),m=p(!1),a=p({id:0,title:"",createTime:""}),d=p(!1),y=e=>e?new Date(e).toLocaleDateString("zh-CN",{year:"numeric",month:"2-digit",day:"2-digit",hour:"2-digit",minute:"2-digit"}):"-",I=e=>({SYSTEM:"danger",ACTIVITY:"success",NOTICE:"info",NEWS:"warning"})[e||""]||"info",b=e=>({SYSTEM:"系统公告",ACTIVITY:"活动通知",NOTICE:"普通公告",NEWS:"新闻资讯"})[e||""]||"通知",x=async()=>{m.value=!0;try{const e=await T.get(`/notice/${u.value}`);e.code===200&&e.data&&(a.value=e.data)}catch(e){console.error("获取公告详情失败:",e),a.value={id:u.value,title:"关于2026年寒假志愿服务活动报名的通知",type:"ACTIVITY",department:"校团委志愿者工作部",isTop:!0,viewCount:1234,createTime:"2026-01-18T10:00:00",updateTime:"2026-01-18T14:30:00"}}finally{m.value=!1}},E=async()=>{try{const e=await T.get("/collection/check",{targetId:u.value,targetType:"NOTICE"});e.code===200&&(d.value=e.data===!0)}catch(e){console.error("检查收藏状态失败",e)}},D=async()=>{try{(await T.post("/collection/toggle",{targetId:u.value,targetType:"NOTICE"})).code===200&&(d.value=!d.value,h.success(d.value?"收藏成功":"已取消收藏"))}catch{h.error("操作失败")}},S=()=>{var o;const e=window.location.href;(o=navigator.clipboard)==null||o.writeText(e),h.success("链接已复制到剪贴板")},V=()=>{N.push("/notice/list")};return O(()=>{x(),E()}),(e,o)=>{const c=_("el-icon"),g=_("el-button"),C=_("el-tag"),M=_("el-divider"),Y=L("loading");return f(),v("div",G,[s("div",J,[t(g,{type:"primary",text:"",onClick:V},{default:l(()=>[t(c,null,{default:l(()=>[t(r(A))]),_:1}),o[0]||(o[0]=n(" 返回公告列表 ",-1))]),_:1})]),z((f(),v("div",K,[s("div",P,[a.value.isTop||a.value.type?(f(),v("div",Q,[a.value.isTop?(f(),H(C,{key:0,type:"danger",size:"small",effect:"dark"},{default:l(()=>[...o[1]||(o[1]=[n(" 置顶 ",-1)])]),_:1})):k("",!0),t(C,{type:I(a.value.type),size:"small"},{default:l(()=>[n(i(b(a.value.type)),1)]),_:1},8,["type"])])):k("",!0),s("h1",U,i(a.value.title),1),s("div",ee,[s("span",te,[t(c,null,{default:l(()=>[t(r(R))]),_:1}),n(" 发布时间："+i(y(a.value.createTime)),1)]),s("span",ae,[t(c,null,{default:l(()=>[t(r(W))]),_:1}),n(" 发布部门："+i(a.value.department||"志愿者管理中心"),1)]),s("span",oe,[t(c,null,{default:l(()=>[t(r(j))]),_:1}),n(" 阅读量："+i(a.value.viewCount||0),1)])])]),t(M),s("div",{class:"notice-content",innerHTML:a.value.content||re},null,8,se),s("div",le,[s("div",ne,[s("span",ie," 最后更新："+i(y(a.value.updateTime||a.value.createTime)),1)]),s("div",ce,[t(g,{type:"primary",plain:"",onClick:D},{default:l(()=>[t(c,null,{default:l(()=>[t(r(X))]),_:1}),n(" "+i(d.value?"已收藏":"收藏公告"),1)]),_:1}),t(g,{onClick:S},{default:l(()=>[t(c,null,{default:l(()=>[t(r(Z))]),_:1}),o[2]||(o[2]=n(" 分享 ",-1))]),_:1})])])])),[[Y,m.value]])])}}}),me=F(de,[["__scopeId","data-v-e1511e6a"]]);export{me as default};
