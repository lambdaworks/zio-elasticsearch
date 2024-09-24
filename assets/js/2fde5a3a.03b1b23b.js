"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5536],{4960:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>i,contentTitle:()=>c,default:()=>d,frontMatter:()=>o,metadata:()=>a,toc:()=>l});var n=r(4848),s=r(8453);const o={id:"elastic_query_constant_score",title:"Constant Score Query"},c=void 0,a={id:"overview/queries/elastic_query_constant_score",title:"Constant Score Query",description:"The ConstantScore query wraps a filter query and returns every matching document with a relevance score equal to the boost parameter value.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_constant_score.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_constant_score",permalink:"/zio-elasticsearch/overview/queries/elastic_query_constant_score",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_constant_score.md",tags:[],version:"current",frontMatter:{id:"elastic_query_constant_score",title:"Constant Score Query"},sidebar:"docs",previous:{title:"Boosting Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_boosting"},next:{title:"Disjunction max Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_disjunction_max"}},i={},l=[];function u(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,s.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(t.p,{children:["The ",(0,n.jsx)(t.code,{children:"ConstantScore"})," query wraps a filter query and returns every matching document with a relevance score equal to the boost parameter value."]}),"\n",(0,n.jsxs)(t.p,{children:["In order to use the ",(0,n.jsx)(t.code,{children:"ConstantScore"})," query import the following:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.query.ConstantScoreQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.code,{children:"ConstantScore"})," query with arbitrary query(",(0,n.jsx)(t.code,{children:"MatchPhrase"})," in this example) using the ",(0,n.jsx)(t.code,{children:"constantScore"})," method in the following manner:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val query: ConstantScoreQuery = constantScore(matchPhrase(field = "name", value = "test"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,n.jsx)(t.code,{children:"ConstantScore"})," query with arbitrary ",(0,n.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," query(",(0,n.jsx)(t.code,{children:"MatchPhrase"})," in this example) using the ",(0,n.jsx)(t.code,{children:"constantScore"})," method in the following manner:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val query: ConstantScoreQuery = constantScore(matchPhrase(field = Document.name, value = "test"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"boost"}),", you can use ",(0,n.jsx)(t.code,{children:"boost"})," method:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val queryWithBoost: ConstantScoreQuery = constantScore(matchPhrase(field = Document.name, value = "test")).boost(2.2)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can find more information about ",(0,n.jsx)(t.code,{children:"ConstantScore"})," query ",(0,n.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-constant-score-query.html",children:"here"}),"."]})]})}function d(e={}){const{wrapper:t}={...(0,s.R)(),...e.components};return t?(0,n.jsx)(t,{...e,children:(0,n.jsx)(u,{...e})}):u(e)}},8453:(e,t,r)=>{r.d(t,{R:()=>c,x:()=>a});var n=r(6540);const s={},o=n.createContext(s);function c(e){const t=n.useContext(o);return n.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function a(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:c(e.components),n.createElement(o.Provider,{value:t},e.children)}}}]);