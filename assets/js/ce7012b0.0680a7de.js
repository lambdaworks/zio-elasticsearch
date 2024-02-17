"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[65],{2582:(e,r,a)=>{a.r(r),a.d(r,{assets:()=>d,contentTitle:()=>s,default:()=>u,frontMatter:()=>c,metadata:()=>n,toc:()=>l});var t=a(4848),i=a(8453);const c={id:"elastic_query_wildcard",title:"Wildcard Query"},s=void 0,n={id:"overview/queries/elastic_query_wildcard",title:"Wildcard Query",description:"The Wildcard query returns documents that contain terms matching a wildcard pattern. You can combine wildcard operators with other characters to create a wildcard pattern.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_wildcard.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_wildcard",permalink:"/zio-elasticsearch/overview/queries/elastic_query_wildcard",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_wildcard.md",tags:[],version:"current",frontMatter:{id:"elastic_query_wildcard",title:"Wildcard Query"},sidebar:"docs",previous:{title:"Terms Set Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms_set"},next:{title:"IDs Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_ids"}},d={},l=[];function o(e){const r={a:"a",code:"code",p:"p",pre:"pre",...(0,i.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(r.p,{children:["The ",(0,t.jsx)(r.code,{children:"Wildcard"})," query returns documents that contain terms matching a wildcard pattern. You can combine wildcard operators with other characters to create a wildcard pattern."]}),"\n",(0,t.jsxs)(r.p,{children:["In order to use the ",(0,t.jsx)(r.code,{children:"Wildcard"})," query import the following:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.WildcardQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,t.jsxs)(r.p,{children:["The ",(0,t.jsx)(r.code,{children:"Wildcard"})," query can be created with ",(0,t.jsx)(r.code,{children:"contains"}),", ",(0,t.jsx)(r.code,{children:"startsWith"})," or ",(0,t.jsx)(r.code,{children:"wildcard"})," method.\nThe ",(0,t.jsx)(r.code,{children:"contains"})," method is adjusted ",(0,t.jsx)(r.code,{children:"wildcard"})," method, that returns documents that contain terms containing provided text.\nThe ",(0,t.jsx)(r.code,{children:"startsWith"})," method is adjusted ",(0,t.jsx)(r.code,{children:"wildcard"})," method that returns documents that contain terms starting with provided text."]}),"\n",(0,t.jsxs)(r.p,{children:["To create a ",(0,t.jsx)(r.code,{children:"Wildcard"})," query use one of the following methods:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val query: WildcardQuery = contains(field = "name", value = "a")\nval query: WildcardQuery = startsWith(field = "name", value = "a")\nval query: WildcardQuery = wildcard(field = "name", value = "a*a*")\n'})}),"\n",(0,t.jsxs)(r.p,{children:["To create a ",(0,t.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(r.code,{children:"Wildcard"})," query use one of the following methods:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val query: WildcardQuery = contains(field = Document.name, value = "a")\nval query: WildcardQuery = startsWith(field = Document.name, value = "a")\nval query: WildcardQuery = wildcard(field = Document.name, value = "a*a*")\n'})}),"\n",(0,t.jsxs)(r.p,{children:["If you want to change the ",(0,t.jsx)(r.code,{children:"boost"}),", you can use ",(0,t.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: WildcardQuery = contains(field = Document.name, value = "test").boost(2.0)\n'})}),"\n",(0,t.jsxs)(r.p,{children:["If you want to change the ",(0,t.jsx)(r.code,{children:"case_insensitive"}),", you can use ",(0,t.jsx)(r.code,{children:"caseInsensitive"}),", ",(0,t.jsx)(r.code,{children:"caseInsensitiveFalse"})," or ",(0,t.jsx)(r.code,{children:"caseInsensitiveTrue"}),"  method:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val queryWithCaseInsensitive: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitive(true)\nval queryWithCaseInsensitiveFalse: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitiveFalse\nval queryWithCaseInsensitiveTrue: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitiveTrue\n'})}),"\n",(0,t.jsxs)(r.p,{children:["You can find more information about ",(0,t.jsx)(r.code,{children:"Wildcard"})," query ",(0,t.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-wildcard-query.html#query-dsl-wildcard-query",children:"here"}),"."]})]})}function u(e={}){const{wrapper:r}={...(0,i.R)(),...e.components};return r?(0,t.jsx)(r,{...e,children:(0,t.jsx)(o,{...e})}):o(e)}},8453:(e,r,a)=>{a.d(r,{R:()=>s,x:()=>n});var t=a(6540);const i={},c=t.createContext(i);function s(e){const r=t.useContext(c);return t.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}function n(e){let r;return r=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:s(e.components),t.createElement(c.Provider,{value:r},e.children)}}}]);