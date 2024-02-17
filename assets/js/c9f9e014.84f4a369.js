"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8636],{1384:(e,s,r)=>{r.r(s),r.d(s,{assets:()=>o,contentTitle:()=>c,default:()=>h,frontMatter:()=>i,metadata:()=>a,toc:()=>l});var t=r(4848),n=r(8453);const i={id:"elastic_request_refresh",title:"Refresh Request"},c=void 0,a={id:"overview/requests/elastic_request_refresh",title:"Refresh Request",description:"This request is used for refreshing Elasticsearch index.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_refresh.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_refresh",permalink:"/zio-elasticsearch/overview/requests/elastic_request_refresh",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_refresh.md",tags:[],version:"current",frontMatter:{id:"elastic_request_refresh",title:"Refresh Request"},sidebar:"docs",previous:{title:"Get By ID Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_get_by_id"},next:{title:"Search Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_search"}},o={},l=[];function d(e){const s={a:"a",code:"code",p:"p",pre:"pre",...(0,n.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(s.p,{children:"This request is used for refreshing Elasticsearch index."}),"\n",(0,t.jsxs)(s.p,{children:["In order to use the ",(0,t.jsx)(s.code,{children:"Refresh"})," request import the following:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticRequest.RefreshRequest\nimport zio.elasticsearch.ElasticRequest.refresh\n// this import is required for using `IndexName`, `IndexPattern` and `MultiIndex`\nimport zio.elasticsearch._\n"})}),"\n",(0,t.jsxs)(s.p,{children:["You can create a ",(0,t.jsx)(s.code,{children:"Refresh"})," request using the ",(0,t.jsx)(s.code,{children:"refresh"})," method in the following manner:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'val request: RefreshRequest = refresh(selectors = IndexName("index"))\n'})}),"\n",(0,t.jsxs)(s.p,{children:["If you want to refresh more indices, you can use ",(0,t.jsx)(s.code,{children:"refresh"})," method this way:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'val requestWithMultiIndex: RefreshRequest = refresh(selectors = MultiIndex.names(IndexName("index1"), IndexName("index2")))\n'})}),"\n",(0,t.jsxs)(s.p,{children:["If you want to refresh all indices, you can use ",(0,t.jsx)(s.code,{children:"refresh"})," method with ",(0,t.jsx)(s.code,{children:"IndexPattern"})," this way:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'val requestWithIndexPattern: RefreshRequest = refresh(selectors = IndexPattern("_all"))\n'})}),"\n",(0,t.jsxs)(s.p,{children:["You can find more information about ",(0,t.jsx)(s.code,{children:"Refresh"})," request ",(0,t.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-refresh.html",children:"here"}),"."]})]})}function h(e={}){const{wrapper:s}={...(0,n.R)(),...e.components};return s?(0,t.jsx)(s,{...e,children:(0,t.jsx)(d,{...e})}):d(e)}},8453:(e,s,r)=>{r.d(s,{R:()=>c,x:()=>a});var t=r(6540);const n={},i=t.createContext(n);function c(e){const s=t.useContext(i);return t.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function a(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(n):e.components||n:c(e.components),t.createElement(i.Provider,{value:s},e.children)}}}]);