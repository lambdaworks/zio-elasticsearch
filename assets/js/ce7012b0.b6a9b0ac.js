"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[3224],{9201:(e,r,a)=>{a.r(r),a.d(r,{assets:()=>d,contentTitle:()=>c,default:()=>u,frontMatter:()=>s,metadata:()=>n,toc:()=>l});var i=a(5893),t=a(1151);const s={id:"elastic_query_wildcard",title:"Wildcard Query"},c=void 0,n={unversionedId:"overview/queries/elastic_query_wildcard",id:"overview/queries/elastic_query_wildcard",title:"Wildcard Query",description:"The Wildcard query returns documents that contain terms matching a wildcard pattern. You can combine wildcard operators with other characters to create a wildcard pattern.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_wildcard.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_wildcard",permalink:"/zio-elasticsearch/overview/queries/elastic_query_wildcard",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_wildcard.md",tags:[],version:"current",frontMatter:{id:"elastic_query_wildcard",title:"Wildcard Query"},sidebar:"docs",previous:{title:"Terms Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms"},next:{title:"IDs Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_ids"}},d={},l=[];function o(e){const r=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,t.ah)(),e.components);return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(r.p,{children:["The ",(0,i.jsx)(r.code,{children:"Wildcard"})," query returns documents that contain terms matching a wildcard pattern. You can combine wildcard operators with other characters to create a wildcard pattern."]}),"\n",(0,i.jsxs)(r.p,{children:["In order to use the ",(0,i.jsx)(r.code,{children:"Wildcard"})," query import the following:"]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.WildcardQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,i.jsxs)(r.p,{children:["The ",(0,i.jsx)(r.code,{children:"Wildcard"})," query can be created with ",(0,i.jsx)(r.code,{children:"contains"}),", ",(0,i.jsx)(r.code,{children:"startsWith"})," or ",(0,i.jsx)(r.code,{children:"wildcard"})," method.\nThe ",(0,i.jsx)(r.code,{children:"contains"})," method is adjusted ",(0,i.jsx)(r.code,{children:"wildcard"})," method, that returns documents that contain terms containing provided text.\nThe ",(0,i.jsx)(r.code,{children:"startsWith"})," method is adjusted ",(0,i.jsx)(r.code,{children:"wildcard"})," method that returns documents that contain terms starting with provided text."]}),"\n",(0,i.jsxs)(r.p,{children:["To create a ",(0,i.jsx)(r.code,{children:"Wildcard"})," query use one of the following methods:"]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-scala",children:'val query: WildcardQuery = contains(field = "name", value = "a")\nval query: WildcardQuery = startsWith(field = "name", value = "a")\nval query: WildcardQuery = wildcard(field = "name", value = "a*a*")\n'})}),"\n",(0,i.jsxs)(r.p,{children:["To create a ",(0,i.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,i.jsx)(r.code,{children:"Wildcard"})," query use one of the following methods:"]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-scala",children:'val query: WildcardQuery = contains(field = Document.name, value = "a")\nval query: WildcardQuery = startsWith(field = Document.name, value = "a")\nval query: WildcardQuery = wildcard(field = Document.name, value = "a*a*")\n'})}),"\n",(0,i.jsxs)(r.p,{children:["If you want to change the ",(0,i.jsx)(r.code,{children:"boost"}),", you can use ",(0,i.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: WildcardQuery = contains(field = Document.name, value = "test").boost(2.0)\n'})}),"\n",(0,i.jsxs)(r.p,{children:["If you want to change the ",(0,i.jsx)(r.code,{children:"case_insensitive"}),", you can use ",(0,i.jsx)(r.code,{children:"caseInsensitive"}),", ",(0,i.jsx)(r.code,{children:"caseInsensitiveFalse"})," or ",(0,i.jsx)(r.code,{children:"caseInsensitiveTrue"}),"  method:"]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-scala",children:'val queryWithCaseInsensitive: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitive(true)\nval queryWithCaseInsensitiveFalse: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitiveFalse\nval queryWithCaseInsensitiveTrue: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitiveTrue\n'})}),"\n",(0,i.jsxs)(r.p,{children:["You can find more information about ",(0,i.jsx)(r.code,{children:"Wildcard"})," query ",(0,i.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-wildcard-query.html#query-dsl-wildcard-query",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:r}=Object.assign({},(0,t.ah)(),e.components);return r?(0,i.jsx)(r,Object.assign({},e,{children:(0,i.jsx)(o,e)})):o(e)}},1151:(e,r,a)=>{a.d(r,{Zo:()=>n,ah:()=>s});var i=a(7294);const t=i.createContext({});function s(e){const r=i.useContext(t);return i.useMemo((()=>"function"==typeof e?e(r):{...r,...e}),[r,e])}const c={};function n({components:e,children:r,disableParentContext:a}){let n;return n=a?"function"==typeof e?e({}):e||c:s(e),i.createElement(t.Provider,{value:n},r)}}}]);