"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[7464],{4873:(e,s,t)=>{t.r(s),t.d(s,{assets:()=>a,contentTitle:()=>o,default:()=>d,frontMatter:()=>c,metadata:()=>i,toc:()=>l});const i=JSON.parse('{"id":"overview/queries/elastic_query_exists","title":"Exists Query","description":"The Exists query is used for returning documents that contain an indexed value for a field.","source":"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_exists.md","sourceDirName":"overview/queries","slug":"/overview/queries/elastic_query_exists","permalink":"/zio-elasticsearch/overview/queries/elastic_query_exists","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_exists.md","tags":[],"version":"current","frontMatter":{"id":"elastic_query_exists","title":"Exists Query"},"sidebar":"docs","previous":{"title":"Disjunction max Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_disjunction_max"},"next":{"title":"Function Score Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_function_score"}}');var r=t(4848),n=t(8453);const c={id:"elastic_query_exists",title:"Exists Query"},o=void 0,a={},l=[];function u(e){const s={a:"a",code:"code",p:"p",pre:"pre",...(0,n.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(s.p,{children:["The ",(0,r.jsx)(s.code,{children:"Exists"})," query is used for returning documents that contain an indexed value for a field."]}),"\n",(0,r.jsxs)(s.p,{children:["In order to use the ",(0,r.jsx)(s.code,{children:"Exists"})," query import the following:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.query.ExistsQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,r.jsxs)(s.p,{children:["You can create an ",(0,r.jsx)(s.code,{children:"Exists"})," query using the ",(0,r.jsx)(s.code,{children:"exists"})," method this way:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val query: ExistsQuery = exists(field = "name")\n'})}),"\n",(0,r.jsxs)(s.p,{children:["Also, you can create a ",(0,r.jsx)(s.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,r.jsx)(s.code,{children:"Exists"})," query using the ",(0,r.jsx)(s.code,{children:"exists"})," method this way:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"val query: ExistsQuery = exists(field = Document.name)\n"})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change the ",(0,r.jsx)(s.code,{children:"boost"}),", you can use ",(0,r.jsx)(s.code,{children:"boost"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val queryWithBoost: ExistsQuery = exists(field = "name").boost(2.0)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["You can find more information about ",(0,r.jsx)(s.code,{children:"Exists"})," query ",(0,r.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-exists-query.html#query-dsl-exists-query",children:"here"}),"."]})]})}function d(e={}){const{wrapper:s}={...(0,n.R)(),...e.components};return s?(0,r.jsx)(s,{...e,children:(0,r.jsx)(u,{...e})}):u(e)}},8453:(e,s,t)=>{t.d(s,{R:()=>c,x:()=>o});var i=t(6540);const r={},n=i.createContext(r);function c(e){const s=i.useContext(n);return i.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function o(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:c(e.components),i.createElement(n.Provider,{value:s},e.children)}}}]);