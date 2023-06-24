"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[7557],{8083:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>o,contentTitle:()=>i,default:()=>u,frontMatter:()=>c,metadata:()=>n,toc:()=>l});var a=r(5893),s=r(1151);const c={id:"elastic_query_match",title:"Match Query"},i=void 0,n={unversionedId:"overview/queries/elastic_query_match",id:"overview/queries/elastic_query_match",title:"Match Query",description:"The Match query is a type of query that searches for a provided text, number, date or boolean value.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_match",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match.md",tags:[],version:"current",frontMatter:{id:"elastic_query_match",title:"Match Query"},sidebar:"docs",previous:{title:"Has Parent Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_parent"},next:{title:"Match All Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_all"}},o={},l=[];function h(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,s.ah)(),e.components);return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsxs)(t.p,{children:["The ",(0,a.jsx)(t.code,{children:"Match"})," query is a type of query that searches for a provided text, number, date or boolean value.\nThis is the standard query for performing a full-text search, including options for fuzzy matching."]}),"\n",(0,a.jsxs)(t.p,{children:["In order to use the ",(0,a.jsx)(t.code,{children:"Match"})," query import the following:"]}),"\n",(0,a.jsx)(t.pre,{children:(0,a.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.query.MatchQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,a.jsxs)(t.p,{children:["You can create a ",(0,a.jsx)(t.code,{children:"Match"})," query using the ",(0,a.jsx)(t.code,{children:"matches"})," method in the following manner:"]}),"\n",(0,a.jsx)(t.pre,{children:(0,a.jsx)(t.code,{className:"language-scala",children:'val query: MatchQuery = matches(field = "message", value = "this is a test")\n'})}),"\n",(0,a.jsxs)(t.p,{children:["You can create a ",(0,a.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,a.jsx)(t.code,{children:"Match"})," query using the ",(0,a.jsx)(t.code,{children:"matches"})," method in the following manner:"]}),"\n",(0,a.jsx)(t.pre,{children:(0,a.jsx)(t.code,{className:"language-scala",children:'val query: MatchQuery = matches(field = Document.message, value = "this is a test")\n'})}),"\n",(0,a.jsxs)(t.p,{children:["You can find more information about ",(0,a.jsx)(t.code,{children:"Match"})," query ",(0,a.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html#query-dsl-match-query",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:t}=Object.assign({},(0,s.ah)(),e.components);return t?(0,a.jsx)(t,Object.assign({},e,{children:(0,a.jsx)(h,e)})):h(e)}},1151:(e,t,r)=>{r.d(t,{Zo:()=>n,ah:()=>c});var a=r(7294);const s=a.createContext({});function c(e){const t=a.useContext(s);return a.useMemo((()=>"function"==typeof e?e(t):{...t,...e}),[t,e])}const i={};function n({components:e,children:t,disableParentContext:r}){let n;return n=r?"function"==typeof e?e({}):e||i:c(e),a.createElement(s.Provider,{value:n},t)}}}]);