"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[4767],{9989:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>a,contentTitle:()=>u,default:()=>d,frontMatter:()=>n,metadata:()=>c,toc:()=>l});var r=s(5893),i=s(1151);const n={id:"elastic_request_delete_by_query",title:"Delete By Query Request"},u=void 0,c={id:"overview/requests/elastic_request_delete_by_query",title:"Delete By Query Request",description:"The DeleteByQuery request deletes documents that match the specified query.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_delete_by_query.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_delete_by_query",permalink:"/zio-elasticsearch/overview/requests/elastic_request_delete_by_query",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_delete_by_query.md",tags:[],version:"current",frontMatter:{id:"elastic_request_delete_by_query",title:"Delete By Query Request"},sidebar:"docs",previous:{title:"Delete By ID Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_delete_by_id"},next:{title:"Delete Index Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_delete_index"}},a={},l=[];function o(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,i.a)(),e.components);return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(t.p,{children:["The ",(0,r.jsx)(t.code,{children:"DeleteByQuery"})," request deletes documents that match the specified query."]}),"\n",(0,r.jsxs)(t.p,{children:["To create a ",(0,r.jsx)(t.code,{children:"DeleteById"})," request do the following:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticRequest.DeleteByQueryRequest\nimport zio.elasticsearch.ElasticRequest.deleteByQuery\n// this import is required for using `IndexName`\nimport zio.elasticsearch._\nimport zio.elasticsearch.ElasticQuery._\n\nval request: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test"))\n'})}),"\n",(0,r.jsxs)(t.p,{children:["If you want to change the ",(0,r.jsx)(t.code,{children:"refresh"}),", you can use ",(0,r.jsx)(t.code,{children:"refresh"}),", ",(0,r.jsx)(t.code,{children:"refreshFalse"})," or ",(0,r.jsx)(t.code,{children:"refreshTrue"})," method:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'val requestWithRefresh: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refresh(true)\nval requestWithRefreshFalse: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refreshFalse\nval requestWithRefreshTrue: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refreshTrue\n'})}),"\n",(0,r.jsxs)(t.p,{children:["If you want to change the ",(0,r.jsx)(t.code,{children:"routing"}),", you can use the ",(0,r.jsx)(t.code,{children:"routing"})," method:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'// this import is required for `Routing` also\nimport zio.elasticsearch._\n\nval requestWithRouting: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).routing(Routing("routing"))\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can find more information about ",(0,r.jsx)(t.code,{children:"DeleteByQuery"})," request ",(0,r.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-delete-by-query.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:t}=Object.assign({},(0,i.a)(),e.components);return t?(0,r.jsx)(t,Object.assign({},e,{children:(0,r.jsx)(o,e)})):o(e)}},1151:(e,t,s)=>{s.d(t,{a:()=>u});var r=s(7294);const i={},n=r.createContext(i);function u(e){const t=r.useContext(n);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}}}]);