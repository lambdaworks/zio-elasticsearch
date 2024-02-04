"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[7916],{6088:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>d,contentTitle:()=>c,default:()=>a,frontMatter:()=>i,metadata:()=>o,toc:()=>u});var s=n(7624),r=n(2172);const i={id:"elastic_request_bulk",title:"Bulk Request"},c=void 0,o={id:"overview/requests/elastic_request_bulk",title:"Bulk Request",description:"The Bulk request performs multiple indexing or delete operations in a single API call. This reduces overhead and can greatly increase indexing speed.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_bulk.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_bulk",permalink:"/zio-elasticsearch/overview/requests/elastic_request_bulk",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_bulk.md",tags:[],version:"current",frontMatter:{id:"elastic_request_bulk",title:"Bulk Request"},sidebar:"docs",previous:{title:"Aggregation Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_aggregate"},next:{title:"Count Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_count"}},d={},u=[];function l(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,r.M)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(t.p,{children:["The ",(0,s.jsx)(t.code,{children:"Bulk"})," request performs multiple indexing or delete operations in a single API call. This reduces overhead and can greatly increase indexing speed."]}),"\n",(0,s.jsxs)(t.p,{children:["In order to use the ",(0,s.jsx)(t.code,{children:"Bulk"})," request import the following:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticRequest.BulkRequest\nimport zio.elasticsearch.ElasticRequest.bulk\n"})}),"\n",(0,s.jsxs)(t.p,{children:["You can create a ",(0,s.jsx)(t.code,{children:"Bulk"})," request using the ",(0,s.jsx)(t.code,{children:"bulk"})," method this way:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `IndexName` and `DocumentId`\nimport zio.elasticsearch._\n\nval index = Index("index")\n\nval document1 = new Document(id = DocumentId("111"), intField = 1, stringField = "stringField1")\nval document2 = new Document(id = DocumentId("222"), intField = 2, stringField = "stringField2")\n\nval request: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2))\n'})}),"\n",(0,s.jsxs)(t.p,{children:["If you want to change the ",(0,s.jsx)(t.code,{children:"refresh"}),", you can use ",(0,s.jsx)(t.code,{children:"refresh"}),", ",(0,s.jsx)(t.code,{children:"refreshFalse"})," or ",(0,s.jsx)(t.code,{children:"refreshTrue"})," method:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:'val requestWithRefresh: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).refresh(true)\nval requestWithRefreshFalse: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).refreshFalse\nval requestWithRefreshTrue: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).refreshTrue\n'})}),"\n",(0,s.jsxs)(t.p,{children:["If you want to change the ",(0,s.jsx)(t.code,{children:"routing"}),", you can use the ",(0,s.jsx)(t.code,{children:"routing"})," method:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `Routing` also\nimport zio.elasticsearch._\n\nval requestWithRouting: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).routing(Routing("routing"))\n'})}),"\n",(0,s.jsxs)(t.p,{children:["You can find more information about ",(0,s.jsx)(t.code,{children:"Bulk"})," request ",(0,s.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-bulk.html",children:"here"}),"."]})]})}function a(e={}){const{wrapper:t}={...(0,r.M)(),...e.components};return t?(0,s.jsx)(t,{...e,children:(0,s.jsx)(l,{...e})}):l(e)}},2172:(e,t,n)=>{n.d(t,{I:()=>o,M:()=>c});var s=n(1504);const r={},i=s.createContext(r);function c(e){const t=s.useContext(i);return s.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function o(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:c(e.components),s.createElement(i.Provider,{value:t},e.children)}}}]);