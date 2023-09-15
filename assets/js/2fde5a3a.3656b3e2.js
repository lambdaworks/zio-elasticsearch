"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[4682],{7811:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>i,contentTitle:()=>a,default:()=>h,frontMatter:()=>o,metadata:()=>c,toc:()=>l});var s=r(5893),n=r(1151);const o={id:"elastic_query_constant_score",title:"Constant Score Query"},a=void 0,c={id:"overview/queries/elastic_query_constant_score",title:"Constant Score Query",description:"The ConstantScore query wraps a filter query and returns every matching document with a relevance score equal to the boost parameter value.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_constant_score.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_constant_score",permalink:"/zio-elasticsearch/overview/queries/elastic_query_constant_score",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_constant_score.md",tags:[],version:"current",frontMatter:{id:"elastic_query_constant_score",title:"Constant Score Query"},sidebar:"docs",previous:{title:"Boolean Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_bool"},next:{title:"Exists Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_exists"}},i={},l=[];function u(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.ah)(),e.components);return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(t.p,{children:["The ",(0,s.jsx)(t.code,{children:"ConstantScore"})," query wraps a filter query and returns every matching document with a relevance score equal to the boost parameter value."]}),"\n",(0,s.jsxs)(t.p,{children:["In order to use the ",(0,s.jsx)(t.code,{children:"ConstantScore"})," query import the following:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.query.ConstantScoreQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,s.jsxs)(t.p,{children:["You can create a ",(0,s.jsx)(t.code,{children:"ConstantScore"})," query with arbitrary query(",(0,s.jsx)(t.code,{children:"MatchPhrase"})," in this example) using the ",(0,s.jsx)(t.code,{children:"constantScore"})," method in the following manner:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:'val query: ConstantScoreQuery = constantScore(matchPhrase(field = "name", value = "test"))\n'})}),"\n",(0,s.jsxs)(t.p,{children:["You can create a ",(0,s.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,s.jsx)(t.code,{children:"ConstantScore"})," query with arbitrary ",(0,s.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," query(",(0,s.jsx)(t.code,{children:"MatchPhrase"})," in this example) using the ",(0,s.jsx)(t.code,{children:"constantScore"})," method in the following manner:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:'val query: ConstantScoreQuery = constantScore(matchPhrase(field = Document.name, value = "test"))\n'})}),"\n",(0,s.jsxs)(t.p,{children:["If you want to change the ",(0,s.jsx)(t.code,{children:"boost"}),", you can use ",(0,s.jsx)(t.code,{children:"boost"})," method:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:'val queryWithBoost: ConstantScoreQuery = constantScore(matchPhrase(field = Document.name, value = "test")).boost(2.2)\n'})}),"\n",(0,s.jsxs)(t.p,{children:["You can find more information about ",(0,s.jsx)(t.code,{children:"ConstantScore"})," query ",(0,s.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-constant-score-query.html",children:"here"}),"."]})]})}const h=function(e={}){const{wrapper:t}=Object.assign({},(0,n.ah)(),e.components);return t?(0,s.jsx)(t,Object.assign({},e,{children:(0,s.jsx)(u,e)})):u(e)}},1151:(e,t,r)=>{r.d(t,{Zo:()=>c,ah:()=>o});var s=r(7294);const n=s.createContext({});function o(e){const t=s.useContext(n);return s.useMemo((()=>"function"==typeof e?e(t):{...t,...e}),[t,e])}const a={};function c({components:e,children:t,disableParentContext:r}){let c;return c=r?"function"==typeof e?e({}):e||a:o(e),s.createElement(n.Provider,{value:c},t)}}}]);