"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[6868],{1951:(e,r,s)=>{s.r(r),s.d(r,{assets:()=>o,contentTitle:()=>a,default:()=>d,frontMatter:()=>c,metadata:()=>t,toc:()=>l});const t=JSON.parse('{"id":"overview/queries/elastic_query_term","title":"Term Query","description":"The Term query returns documents that contain an exact term in the provided field.","source":"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_term.md","sourceDirName":"overview/queries","slug":"/overview/queries/elastic_query_term","permalink":"/zio-elasticsearch/overview/queries/elastic_query_term","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_term.md","tags":[],"version":"current","frontMatter":{"id":"elastic_query_term","title":"Term Query"},"sidebar":"docs","previous":{"title":"Regexp Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_regexp"},"next":{"title":"Terms Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_terms"}}');var n=s(4848),i=s(8453);const c={id:"elastic_query_term",title:"Term Query"},a=void 0,o={},l=[];function u(e){const r={a:"a",code:"code",p:"p",pre:"pre",...(0,i.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(r.p,{children:["The ",(0,n.jsx)(r.code,{children:"Term"})," query returns documents that contain an exact term in the provided field."]}),"\n",(0,n.jsxs)(r.p,{children:["In order to use the ",(0,n.jsx)(r.code,{children:"Term"})," query import the following:"]}),"\n",(0,n.jsx)(r.pre,{children:(0,n.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.TermQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,n.jsxs)(r.p,{children:["You can create a ",(0,n.jsx)(r.code,{children:"Term"})," query using the ",(0,n.jsx)(r.code,{children:"term"})," method this way:"]}),"\n",(0,n.jsx)(r.pre,{children:(0,n.jsx)(r.code,{className:"language-scala",children:'val query: TermQuery = term(field = "stringField", value = "test")\n'})}),"\n",(0,n.jsxs)(r.p,{children:["You can create a ",(0,n.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,n.jsx)(r.code,{children:"Term"})," query using the ",(0,n.jsx)(r.code,{children:"term"})," method this way:"]}),"\n",(0,n.jsx)(r.pre,{children:(0,n.jsx)(r.code,{className:"language-scala",children:'val query: TermQuery = term(field = Document.name, value = "test")\n'})}),"\n",(0,n.jsxs)(r.p,{children:["If you want to change the ",(0,n.jsx)(r.code,{children:"boost"}),", you can use ",(0,n.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,n.jsx)(r.pre,{children:(0,n.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: TermQuery = term(field = Document.name, value = "test").boost(2.0)\n'})}),"\n",(0,n.jsxs)(r.p,{children:["If you want to change the ",(0,n.jsx)(r.code,{children:"case_insensitive"}),", you can use ",(0,n.jsx)(r.code,{children:"caseInsensitive"}),", ",(0,n.jsx)(r.code,{children:"caseInsensitiveFalse"})," or ",(0,n.jsx)(r.code,{children:"caseInsensitiveTrue"})," method:"]}),"\n",(0,n.jsx)(r.pre,{children:(0,n.jsx)(r.code,{className:"language-scala",children:'val queryWithCaseInsensitive: TermQuery = term(field = Document.name, value = "test").caseInsensitive(true)\nval queryWithCaseInsensitiveFalse: TermQuery = term(field = Document.name, value = "test").caseInsensitiveFalse\nval queryWithCaseInsensitiveTrue: TermQuery = term(field = Document.name, value = "test").caseInsensitiveTrue\n'})}),"\n",(0,n.jsxs)(r.p,{children:["You can find more information about ",(0,n.jsx)(r.code,{children:"Term"})," query ",(0,n.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-term-query.html",children:"here"}),"."]})]})}function d(e={}){const{wrapper:r}={...(0,i.R)(),...e.components};return r?(0,n.jsx)(r,{...e,children:(0,n.jsx)(u,{...e})}):u(e)}},8453:(e,r,s)=>{s.d(r,{R:()=>c,x:()=>a});var t=s(6540);const n={},i=t.createContext(n);function c(e){const r=t.useContext(i);return t.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}function a(e){let r;return r=e.disableParentContext?"function"==typeof e.components?e.components(n):e.components||n:c(e.components),t.createElement(i.Provider,{value:r},e.children)}}}]);