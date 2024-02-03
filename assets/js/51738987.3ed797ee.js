"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[1640],{6800:(e,r,a)=>{a.r(r),a.d(r,{assets:()=>n,contentTitle:()=>i,default:()=>u,frontMatter:()=>c,metadata:()=>h,toc:()=>o});var s=a(7624),t=a(2172);const c={id:"elastic_query_match_phrase",title:"Match Phrase Query"},i=void 0,h={id:"overview/queries/elastic_query_match_phrase",title:"Match Phrase Query",description:"The MatchPhrase query analyzes the text and creates a phrase query out of the analyzed text.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match_phrase.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_match_phrase",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match_phrase.md",tags:[],version:"current",frontMatter:{id:"elastic_query_match_phrase",title:"Match Phrase Query"},sidebar:"docs",previous:{title:"Match Boolean Prefix Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_boolean_prefix"},next:{title:"Match Phrase Prefix Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase_prefix"}},n={},o=[];function l(e){const r=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,t.M)(),e.components);return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(r.p,{children:["The ",(0,s.jsx)(r.code,{children:"MatchPhrase"})," query analyzes the text and creates a ",(0,s.jsx)(r.code,{children:"phrase"})," query out of the analyzed text."]}),"\n",(0,s.jsxs)(r.p,{children:["In order to use the ",(0,s.jsx)(r.code,{children:"MatchPhrase"})," query import the following:"]}),"\n",(0,s.jsx)(r.pre,{children:(0,s.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.MatchPhraseQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,s.jsxs)(r.p,{children:["You can create a ",(0,s.jsx)(r.code,{children:"MatchPhrase"})," query using the ",(0,s.jsx)(r.code,{children:"matchPhrase"})," method this way:"]}),"\n",(0,s.jsx)(r.pre,{children:(0,s.jsx)(r.code,{className:"language-scala",children:'val query: MatchPhraseQuery = matchPhrase(field = "stringField", value = "test")\n'})}),"\n",(0,s.jsxs)(r.p,{children:["You can create a ",(0,s.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,s.jsx)(r.code,{children:"MatchPhrase"})," query using the ",(0,s.jsx)(r.code,{children:"matchPhrase"})," method this way:"]}),"\n",(0,s.jsx)(r.pre,{children:(0,s.jsx)(r.code,{className:"language-scala",children:'val query: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")\n'})}),"\n",(0,s.jsxs)(r.p,{children:["If you want to change the ",(0,s.jsx)(r.code,{children:"boost"}),", you can use ",(0,s.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,s.jsx)(r.pre,{children:(0,s.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")g.boost(2.0)\n'})}),"\n",(0,s.jsxs)(r.p,{children:["You can find more information about ",(0,s.jsx)(r.code,{children:"MatchPhrase"})," query ",(0,s.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-query-phrase.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:r}=Object.assign({},(0,t.M)(),e.components);return r?(0,s.jsx)(r,Object.assign({},e,{children:(0,s.jsx)(l,e)})):l(e)}},2172:(e,r,a)=>{a.d(r,{M:()=>i});var s=a(1504);const t={},c=s.createContext(t);function i(e){const r=s.useContext(c);return s.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}}}]);