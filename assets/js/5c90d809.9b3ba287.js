"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9237],{7098:(e,t,c)=>{c.r(t),c.d(t,{assets:()=>o,contentTitle:()=>i,default:()=>h,frontMatter:()=>l,metadata:()=>r,toc:()=>n});const r=JSON.parse('{"id":"overview/queries/elastic_query_match_all","title":"Match All Query","description":"The most simple query, which matches all documents, giving them all a score of 1.0.","source":"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match_all.md","sourceDirName":"overview/queries","slug":"/overview/queries/elastic_query_match_all","permalink":"/zio-elasticsearch/overview/queries/elastic_query_match_all","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match_all.md","tags":[],"version":"current","frontMatter":{"id":"elastic_query_match_all","title":"Match All Query"},"sidebar":"docs","previous":{"title":"Match Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_match"},"next":{"title":"Match Boolean Prefix Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_match_boolean_prefix"}}');var s=c(4848),a=c(8453);const l={id:"elastic_query_match_all",title:"Match All Query"},i=void 0,o={},n=[];function u(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,a.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(t.p,{children:["The most simple query, which matches all documents, giving them all a ",(0,s.jsx)(t.code,{children:"score"})," of ",(0,s.jsx)(t.code,{children:"1.0"}),"."]}),"\n",(0,s.jsxs)(t.p,{children:["To create a ",(0,s.jsx)(t.code,{children:"MatchAll"})," query do the following:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.query.MatchAllQuery\nimport zio.elasticsearch.ElasticQuery._\n\nval query: MatchAllQuery = matchAll\n"})}),"\n",(0,s.jsxs)(t.p,{children:["If you want to change the ",(0,s.jsx)(t.code,{children:"boost"}),", you can use the ",(0,s.jsx)(t.code,{children:"boost"})," method:"]}),"\n",(0,s.jsx)(t.pre,{children:(0,s.jsx)(t.code,{className:"language-scala",children:"val queryWithBoost: MatchAllQuery = matchAll.boost(1.2)\n"})}),"\n",(0,s.jsxs)(t.p,{children:["You can find more information about ",(0,s.jsx)(t.code,{children:"MatchAll"})," query ",(0,s.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-all-query.html",children:"here"}),"."]})]})}function h(e={}){const{wrapper:t}={...(0,a.R)(),...e.components};return t?(0,s.jsx)(t,{...e,children:(0,s.jsx)(u,{...e})}):u(e)}},8453:(e,t,c)=>{c.d(t,{R:()=>l,x:()=>i});var r=c(6540);const s={},a=r.createContext(s);function l(e){const t=r.useContext(a);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function i(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:l(e.components),r.createElement(a.Provider,{value:t},e.children)}}}]);