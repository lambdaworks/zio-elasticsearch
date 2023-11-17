"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5082],{5454:(e,s,i)=>{i.r(s),i.d(s,{assets:()=>o,contentTitle:()=>c,default:()=>d,frontMatter:()=>n,metadata:()=>a,toc:()=>u});var t=i(5893),r=i(1151);const n={id:"elastic_query_exists",title:"Exists Query"},c=void 0,a={id:"overview/queries/elastic_query_exists",title:"Exists Query",description:"The Exists query is used for returning documents that contain an indexed value for a field.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_exists.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_exists",permalink:"/zio-elasticsearch/overview/queries/elastic_query_exists",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_exists.md",tags:[],version:"current",frontMatter:{id:"elastic_query_exists",title:"Exists Query"},sidebar:"docs",previous:{title:"Disjunction max Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_disjunction_max"},next:{title:"Function Score Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_function_score"}},o={},u=[];function l(e){const s=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,r.a)(),e.components);return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(s.p,{children:["The ",(0,t.jsx)(s.code,{children:"Exists"})," query is used for returning documents that contain an indexed value for a field."]}),"\n",(0,t.jsxs)(s.p,{children:["In order to use the ",(0,t.jsx)(s.code,{children:"Exists"})," query import the following:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.query.ExistsQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,t.jsxs)(s.p,{children:["You can create an ",(0,t.jsx)(s.code,{children:"Exists"})," query using the ",(0,t.jsx)(s.code,{children:"exists"})," method this way:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'val query: ExistsQuery = exists(field = "name")\n'})}),"\n",(0,t.jsxs)(s.p,{children:["Also, you can create a ",(0,t.jsx)(s.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(s.code,{children:"Exists"})," query using the ",(0,t.jsx)(s.code,{children:"exists"})," method this way:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:"val query: ExistsQuery = exists(field = Document.name)\n"})}),"\n",(0,t.jsxs)(s.p,{children:["If you want to change the ",(0,t.jsx)(s.code,{children:"boost"}),", you can use ",(0,t.jsx)(s.code,{children:"boost"})," method:"]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'val queryWithBoost: ExistsQuery = exists(field = "name").boost(2.0)\n'})}),"\n",(0,t.jsxs)(s.p,{children:["You can find more information about ",(0,t.jsx)(s.code,{children:"Exists"})," query ",(0,t.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-exists-query.html#query-dsl-exists-query",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:s}=Object.assign({},(0,r.a)(),e.components);return s?(0,t.jsx)(s,Object.assign({},e,{children:(0,t.jsx)(l,e)})):l(e)}},1151:(e,s,i)=>{i.d(s,{a:()=>c});var t=i(7294);const r={},n=t.createContext(r);function c(e){const s=t.useContext(n);return t.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}}}]);