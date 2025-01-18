"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8596],{653:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>l,contentTitle:()=>c,default:()=>d,frontMatter:()=>u,metadata:()=>r,toc:()=>a});const r=JSON.parse('{"id":"overview/requests/elastic_request_delete_by_query","title":"Delete By Query Request","description":"The DeleteByQuery request deletes documents that match the specified query.","source":"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_delete_by_query.md","sourceDirName":"overview/requests","slug":"/overview/requests/elastic_request_delete_by_query","permalink":"/zio-elasticsearch/overview/requests/elastic_request_delete_by_query","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_delete_by_query.md","tags":[],"version":"current","frontMatter":{"id":"elastic_request_delete_by_query","title":"Delete By Query Request"},"sidebar":"docs","previous":{"title":"Delete By ID Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_delete_by_id"},"next":{"title":"Delete Index Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_delete_index"}}');var n=s(4848),i=s(8453);const u={id:"elastic_request_delete_by_query",title:"Delete By Query Request"},c=void 0,l={},a=[];function o(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,i.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(t.p,{children:["The ",(0,n.jsx)(t.code,{children:"DeleteByQuery"})," request deletes documents that match the specified query."]}),"\n",(0,n.jsxs)(t.p,{children:["To create a ",(0,n.jsx)(t.code,{children:"DeleteById"})," request do the following:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticRequest.DeleteByQueryRequest\nimport zio.elasticsearch.ElasticRequest.deleteByQuery\n// this import is required for using `IndexName`\nimport zio.elasticsearch._\nimport zio.elasticsearch.ElasticQuery._\n\nval request: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"refresh"}),", you can use ",(0,n.jsx)(t.code,{children:"refresh"}),", ",(0,n.jsx)(t.code,{children:"refreshFalse"})," or ",(0,n.jsx)(t.code,{children:"refreshTrue"})," method:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val requestWithRefresh: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refresh(true)\nval requestWithRefreshFalse: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refreshFalse\nval requestWithRefreshTrue: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refreshTrue\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"routing"}),", you can use the ",(0,n.jsx)(t.code,{children:"routing"})," method:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'// this import is required for `Routing` also\nimport zio.elasticsearch._\n\nval requestWithRouting: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).routing(Routing("routing"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can find more information about ",(0,n.jsx)(t.code,{children:"DeleteByQuery"})," request ",(0,n.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-delete-by-query.html",children:"here"}),"."]})]})}function d(e={}){const{wrapper:t}={...(0,i.R)(),...e.components};return t?(0,n.jsx)(t,{...e,children:(0,n.jsx)(o,{...e})}):o(e)}},8453:(e,t,s)=>{s.d(t,{R:()=>u,x:()=>c});var r=s(6540);const n={},i=r.createContext(n);function u(e){const t=r.useContext(i);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function c(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(n):e.components||n:u(e.components),r.createElement(i.Provider,{value:t},e.children)}}}]);