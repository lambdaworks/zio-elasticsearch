"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9311],{4100:(e,r,s)=>{s.r(r),s.d(r,{assets:()=>o,contentTitle:()=>c,default:()=>d,frontMatter:()=>i,metadata:()=>a,toc:()=>l});var t=s(5893),n=s(1151);const i={id:"elastic_query_term",title:"Term Query"},c=void 0,a={id:"overview/queries/elastic_query_term",title:"Term Query",description:"The Term query returns documents that contain an exact term in the provided field.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_term.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_term",permalink:"/zio-elasticsearch/overview/queries/elastic_query_term",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_term.md",tags:[],version:"current",frontMatter:{id:"elastic_query_term",title:"Term Query"},sidebar:"docs",previous:{title:"Regexp Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_regexp"},next:{title:"Terms Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms"}},o={},l=[];function u(e){const r=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.ah)(),e.components);return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(r.p,{children:["The ",(0,t.jsx)(r.code,{children:"Term"})," query returns documents that contain an exact term in the provided field."]}),"\n",(0,t.jsxs)(r.p,{children:["In order to use the ",(0,t.jsx)(r.code,{children:"Term"})," query import the following:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.TermQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,t.jsxs)(r.p,{children:["You can create a ",(0,t.jsx)(r.code,{children:"Term"})," query using the ",(0,t.jsx)(r.code,{children:"term"})," method this way:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val query: TermQuery = term(field = Document.name, value = "test")\n'})}),"\n",(0,t.jsxs)(r.p,{children:["You can create a ",(0,t.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(r.code,{children:"Term"})," query using the ",(0,t.jsx)(r.code,{children:"term"})," method this way:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val query: TermQuery = term(field = Document.name, value = "test")\n'})}),"\n",(0,t.jsxs)(r.p,{children:["If you want to change the ",(0,t.jsx)(r.code,{children:"boost"}),", you can use ",(0,t.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: TermQuery = term(field = Document.name, value = "test").boost(2.0)\n'})}),"\n",(0,t.jsxs)(r.p,{children:["If you want to change the ",(0,t.jsx)(r.code,{children:"case_insensitive"}),", you can use ",(0,t.jsx)(r.code,{children:"caseInsensitive"}),", ",(0,t.jsx)(r.code,{children:"caseInsensitiveFalse"})," or ",(0,t.jsx)(r.code,{children:"caseInsensitiveTrue"})," method:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val queryWithCaseInsensitive: TermQuery = term(field = Document.name, value = "test").caseInsensitive(true)\nval queryWithCaseInsensitiveFalse: TermQuery = term(field = Document.name, value = "test").caseInsensitiveFalse\nval queryWithCaseInsensitiveTrue: TermQuery = term(field = Document.name, value = "test").caseInsensitiveTrue\n'})}),"\n",(0,t.jsxs)(r.p,{children:["You can find more information about ",(0,t.jsx)(r.code,{children:"Term"})," query ",(0,t.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-term-query.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:r}=Object.assign({},(0,n.ah)(),e.components);return r?(0,t.jsx)(r,Object.assign({},e,{children:(0,t.jsx)(u,e)})):u(e)}},1151:(e,r,s)=>{s.d(r,{Zo:()=>a,ah:()=>i});var t=s(7294);const n=t.createContext({});function i(e){const r=t.useContext(n);return t.useMemo((()=>"function"==typeof e?e(r):{...r,...e}),[r,e])}const c={};function a({components:e,children:r,disableParentContext:s}){let a;return a=s?"function"==typeof e?e({}):e||c:i(e),t.createElement(n.Provider,{value:a},r)}}}]);