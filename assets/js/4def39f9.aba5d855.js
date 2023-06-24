"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5870],{1706:(e,s,t)=>{t.r(s),t.d(s,{assets:()=>o,contentTitle:()=>i,default:()=>u,frontMatter:()=>c,metadata:()=>a,toc:()=>d});var r=t(5893),n=t(1151);const c={id:"elastic_query_nested",title:"Nested Query"},i=void 0,a={unversionedId:"overview/queries/elastic_query_nested",id:"overview/queries/elastic_query_nested",title:"Nested Query",description:"The Nested query searches nested field objects as if they were indexed as separate documents. If an object matches the search, the Nested query returns the root parent document.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_nested.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_nested",permalink:"/zio-elasticsearch/overview/queries/elastic_query_nested",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_nested.md",tags:[],version:"current",frontMatter:{id:"elastic_query_nested",title:"Nested Query"},sidebar:"docs",previous:{title:"Match Phrase Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase"},next:{title:"Range Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_range"}},o={},d=[];function l(e){const s=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.ah)(),e.components);return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(s.p,{children:["The ",(0,r.jsx)(s.code,{children:"Nested"})," query searches nested field objects as if they were indexed as separate documents. If an object matches the search, the Nested query returns the root parent document."]}),"\n",(0,r.jsxs)(s.p,{children:["In order to use the ",(0,r.jsx)(s.code,{children:"Nested"})," query import the following:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.query.NestedQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,r.jsxs)(s.p,{children:["You can create a ",(0,r.jsx)(s.code,{children:"Nested"})," query using the ",(0,r.jsx)(s.code,{children:"nested"})," method in the following manner:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val query: NestedQuery = nested(path = "testField", query = matchAll)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["You can create a ",(0,r.jsx)(s.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,r.jsx)(s.code,{children:"Nested"})," query using the ",(0,r.jsx)(s.code,{children:"nested"})," method in the following manner:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"val query: NestedQuery = nested(path = Document.subDocumentList, query = matchAll)\n"})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change the ",(0,r.jsx)(s.code,{children:"ignore_unmapped"}),", you can use ",(0,r.jsx)(s.code,{children:"ignoreUnmapped"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"val queryWithIgnoreUnmapped: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).ignoreUnmapped(true)\n"})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change the ",(0,r.jsx)(s.code,{children:"inner_hits"}),", you can use ",(0,r.jsx)(s.code,{children:"innerHits"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.query.InnerHits\n\nval queryWithInnerHits: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).innerHits(innerHits = InnerHits.from(5))\n"})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change the ",(0,r.jsx)(s.code,{children:"score_mode"}),", you can use ",(0,r.jsx)(s.code,{children:"scoreMode"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.query.ScoreMode\n\nval queryWithScoreMode: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).scoreMode(ScoreMode.Avg)\n"})}),"\n",(0,r.jsxs)(s.p,{children:["You can find more information about ",(0,r.jsx)(s.code,{children:"Nested"})," query ",(0,r.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-nested-query.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:s}=Object.assign({},(0,n.ah)(),e.components);return s?(0,r.jsx)(s,Object.assign({},e,{children:(0,r.jsx)(l,e)})):l(e)}},1151:(e,s,t)=>{t.d(s,{Zo:()=>a,ah:()=>c});var r=t(7294);const n=r.createContext({});function c(e){const s=r.useContext(n);return r.useMemo((()=>"function"==typeof e?e(s):{...s,...e}),[s,e])}const i={};function a({components:e,children:s,disableParentContext:t}){let a;return a=t?"function"==typeof e?e({}):e||i:c(e),r.createElement(n.Provider,{value:a},s)}}}]);