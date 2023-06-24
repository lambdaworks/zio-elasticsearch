"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[3112],{2842:(e,t,a)=>{a.r(t),a.d(t,{assets:()=>n,contentTitle:()=>i,default:()=>u,frontMatter:()=>c,metadata:()=>h,toc:()=>o});var r=a(5893),s=a(1151);const c={id:"elastic_query_match_phrase",title:"Match Phrase Query"},i=void 0,h={unversionedId:"overview/queries/elastic_query_match_phrase",id:"overview/queries/elastic_query_match_phrase",title:"Match Phrase Query",description:"The MatchPhrase query analyzes the text and creates a phrase query out of the analyzed text.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match_phrase.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_match_phrase",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match_phrase.md",tags:[],version:"current",frontMatter:{id:"elastic_query_match_phrase",title:"Match Phrase Query"},sidebar:"docs",previous:{title:"Match All Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_all"},next:{title:"Nested Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_nested"}},n={},o=[];function l(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,s.ah)(),e.components);return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(t.p,{children:["The ",(0,r.jsx)(t.code,{children:"MatchPhrase"})," query analyzes the text and creates a ",(0,r.jsx)(t.code,{children:"phrase"})," query out of the analyzed text."]}),"\n",(0,r.jsxs)(t.p,{children:["In order to use the ",(0,r.jsx)(t.code,{children:"MatchPhrase"})," query import the following:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.query.MatchPhraseQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,r.jsxs)(t.p,{children:["You can create a ",(0,r.jsx)(t.code,{children:"MatchPhrase"})," query using the ",(0,r.jsx)(t.code,{children:"matchPhrase"})," method this way:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'val query: MatchPhraseQuery = matchPhrase(field = "stringField", value = "test")\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can create a ",(0,r.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,r.jsx)(t.code,{children:"MatchPhrase"})," query using the ",(0,r.jsx)(t.code,{children:"matchPhrase"})," method this way:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'val query: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")\n'})}),"\n",(0,r.jsxs)(t.p,{children:["If you want to change the ",(0,r.jsx)(t.code,{children:"boost"}),", you can use ",(0,r.jsx)(t.code,{children:"boost"})," method:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'val queryWithBoost: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")g.boost(2.0)\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can find more information about ",(0,r.jsx)(t.code,{children:"MatchPhrase"})," query ",(0,r.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-query-phrase.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:t}=Object.assign({},(0,s.ah)(),e.components);return t?(0,r.jsx)(t,Object.assign({},e,{children:(0,r.jsx)(l,e)})):l(e)}},1151:(e,t,a)=>{a.d(t,{Zo:()=>h,ah:()=>c});var r=a(7294);const s=r.createContext({});function c(e){const t=r.useContext(s);return r.useMemo((()=>"function"==typeof e?e(t):{...t,...e}),[t,e])}const i={};function h({components:e,children:t,disableParentContext:a}){let h;return h=a?"function"==typeof e?e({}):e||i:c(e),r.createElement(s.Provider,{value:h},t)}}}]);