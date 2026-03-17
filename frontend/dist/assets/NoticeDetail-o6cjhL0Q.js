const __vite__mapDeps=(i,m=__vite__mapDeps,d=(m.f||(m.f=["./clipboard-RwK3GWKn.js","./index-3mE7C7gs.js","./index-7b11wJqI.css"])))=>i.map(i=>d[i]);
import{d as B,r as _,l as O,o as Y,e as v,f as o,p as L,g as t,w as s,j as p,ah as q,a5 as C,s as z,t as i,A as n,D as h,h as r,a6 as R,a3 as H,au as P,y as W,$,aw as j,k as F,m as f,E as k,ax as G}from"./index-3mE7C7gs.js";import{_ as J}from"./_plugin-vue_export-helper-DlAUqK2U.js";const K={class:"notice-detail-page"},Q={class:"back-bar"},U={class:"notice-card"},X={class:"notice-header"},Z={key:0,class:"tags"},ee={class:"notice-title"},te={class:"notice-meta"},ae={class:"meta-item"},oe={class:"meta-item"},se={class:"meta-item"},le=["innerHTML"],ne={class:"notice-footer"},ie={class:"footer-left"},ce={class:"update-time"},re={class:"footer-right"},de=`
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
`,ue=B({__name:"NoticeDetail",setup(_e){const w=O(),I=F(),u=_(Number(w.params.id)),m=_(!1),a=_({id:0,title:"",createTime:""}),d=_(!1),T=e=>e?new Date(e).toLocaleDateString("zh-CN",{year:"numeric",month:"2-digit",day:"2-digit",hour:"2-digit",minute:"2-digit"}):"-",N=e=>({SYSTEM:"danger",ACTIVITY:"success",NOTICE:"info",NEWS:"warning"})[e||""]||"info",b=e=>({SYSTEM:"系统公告",ACTIVITY:"活动通知",NOTICE:"普通公告",NEWS:"新闻资讯"})[e||""]||"通知",x=async()=>{m.value=!0;try{const e=await h.get(`/notice/${u.value}`);e.code===200&&e.data&&(a.value=e.data)}catch(e){console.error("获取公告详情失败:",e),a.value={id:u.value,title:"关于2026年寒假志愿服务活动报名的通知",type:"ACTIVITY",department:"校团委志愿者工作部",isTop:!0,viewCount:1234,createTime:"2026-01-18T10:00:00",updateTime:"2026-01-18T14:30:00"}}finally{m.value=!1}},E=async()=>{try{const e=await h.get("/collection/check",{targetId:u.value,targetType:"NOTICE"});e.code===200&&(d.value=e.data===!0)}catch(e){console.error("检查收藏状态失败",e)}},D=async()=>{try{(await h.post("/collection/toggle",{targetId:u.value,targetType:"NOTICE"})).code===200&&(d.value=!d.value,k.success(d.value?"收藏成功":"已取消收藏"))}catch{k.error("操作失败")}},S=()=>{G(async()=>{const{copyToClipboard:e}=await import("./clipboard-RwK3GWKn.js");return{copyToClipboard:e}},__vite__mapDeps([0,1,2]),import.meta.url).then(({copyToClipboard:e})=>{e(window.location.href,"链接已复制到剪贴板")})},V=()=>{I.push("/notice/list")};return Y(()=>{x(),E()}),(e,l)=>{const c=p("el-icon"),g=p("el-button"),y=p("el-tag"),M=p("el-divider"),A=q("loading");return f(),v("div",K,[o("div",Q,[t(g,{type:"primary",text:"",onClick:V},{default:s(()=>[t(c,null,{default:s(()=>[t(r(R))]),_:1}),l[0]||(l[0]=n(" 返回公告列表 ",-1))]),_:1})]),L((f(),v("div",U,[o("div",X,[a.value.isTop||a.value.type?(f(),v("div",Z,[a.value.isTop?(f(),z(y,{key:0,type:"danger",size:"small",effect:"dark"},{default:s(()=>[...l[1]||(l[1]=[n(" 置顶 ",-1)])]),_:1})):C("",!0),t(y,{type:N(a.value.type),size:"small"},{default:s(()=>[n(i(b(a.value.type)),1)]),_:1},8,["type"])])):C("",!0),o("h1",ee,i(a.value.title),1),o("div",te,[o("span",ae,[t(c,null,{default:s(()=>[t(r(H))]),_:1}),n(" 发布时间："+i(T(a.value.createTime)),1)]),o("span",oe,[t(c,null,{default:s(()=>[t(r(P))]),_:1}),n(" 发布部门："+i(a.value.department||"志愿者管理中心"),1)]),o("span",se,[t(c,null,{default:s(()=>[t(r(W))]),_:1}),n(" 阅读量："+i(a.value.viewCount||0),1)])])]),t(M),o("div",{class:"notice-content",innerHTML:a.value.content||de},null,8,le),o("div",ne,[o("div",ie,[o("span",ce," 最后更新："+i(T(a.value.updateTime||a.value.createTime)),1)]),o("div",re,[t(g,{type:"primary",plain:"",onClick:D},{default:s(()=>[t(c,null,{default:s(()=>[t(r($))]),_:1}),n(" "+i(d.value?"已收藏":"收藏公告"),1)]),_:1}),t(g,{onClick:S},{default:s(()=>[t(c,null,{default:s(()=>[t(r(j))]),_:1}),l[2]||(l[2]=n(" 分享 ",-1))]),_:1})])])])),[[A,m.value]])])}}}),me=J(ue,[["__scopeId","data-v-3a2567bc"]]);export{me as default};
