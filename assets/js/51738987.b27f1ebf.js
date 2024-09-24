"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9025],{6610:(e,r,t)=>{t.r(r),t.d(r,{assets:()=>h,contentTitle:()=>i,default:()=>u,frontMatter:()=>c,metadata:()=>n,toc:()=>o});var a=t(4848),s=t(8453);const c={id:"elastic_query_match_phrase",title:"Match Phrase Query"},i=void 0,n={id:"overview/queries/elastic_query_match_phrase",title:"Match Phrase Query",description:"The MatchPhrase query analyzes the text and creates a phrase query out of the analyzed text.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match_phrase.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_match_phrase",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match_phrase.md",tags:[],version:"current",frontMatter:{id:"elastic_query_match_phrase",title:"Match Phrase Query"},sidebar:"docs",previous:{title:"Match Boolean Prefix Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_boolean_prefix"},next:{title:"Match Phrase Prefix Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase_prefix"}},h={},o=[];function l(e){const r={a:"a",code:"code",p:"p",pre:"pre",...(0,s.R)(),...e.components};return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsxs)(r.p,{children:["The ",(0,a.jsx)(r.code,{children:"MatchPhrase"})," query analyzes the text and creates a ",(0,a.jsx)(r.code,{children:"phrase"})," query out of the analyzed text."]}),"\n",(0,a.jsxs)(r.p,{children:["In order to use the ",(0,a.jsx)(r.code,{children:"MatchPhrase"})," query import the following:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.MatchPhraseQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,a.jsxs)(r.p,{children:["You can create a ",(0,a.jsx)(r.code,{children:"MatchPhrase"})," query using the ",(0,a.jsx)(r.code,{children:"matchPhrase"})," method this way:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val query: MatchPhraseQuery = matchPhrase(field = "stringField", value = "test")\n'})}),"\n",(0,a.jsxs)(r.p,{children:["You can create a ",(0,a.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,a.jsx)(r.code,{children:"MatchPhrase"})," query using the ",(0,a.jsx)(r.code,{children:"matchPhrase"})," method this way:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val query: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")\n'})}),"\n",(0,a.jsxs)(r.p,{children:["If you want to change the ",(0,a.jsx)(r.code,{children:"boost"}),", you can use ",(0,a.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")g.boost(2.0)\n'})}),"\n",(0,a.jsxs)(r.p,{children:["You can find more information about ",(0,a.jsx)(r.code,{children:"MatchPhrase"})," query ",(0,a.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-query-phrase.html",children:"here"}),"."]})]})}function u(e={}){const{wrapper:r}={...(0,s.R)(),...e.components};return r?(0,a.jsx)(r,{...e,children:(0,a.jsx)(l,{...e})}):l(e)}},8453:(e,r,t)=>{t.d(r,{R:()=>i,x:()=>n});var a=t(6540);const s={},c=a.createContext(s);function i(e){const r=a.useContext(c);return a.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}function n(e){let r;return r=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:i(e.components),a.createElement(c.Provider,{value:r},e.children)}}}]);