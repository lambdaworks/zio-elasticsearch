"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[82],{5124:(e,r,s)=>{s.r(r),s.d(r,{assets:()=>o,contentTitle:()=>a,default:()=>d,frontMatter:()=>c,metadata:()=>n,toc:()=>l});var t=s(5893),i=s(1151);const c={id:"elastic_query_terms",title:"Terms Query"},a=void 0,n={id:"overview/queries/elastic_query_terms",title:"Terms Query",description:"The Terms query returns documents that contain one or more exact terms in a provided field.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_terms.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_terms",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_terms.md",tags:[],version:"current",frontMatter:{id:"elastic_query_terms",title:"Terms Query"},sidebar:"docs",previous:{title:"Term Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_term"},next:{title:"Terms Set Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms_set"}},o={},l=[];function u(e){const r=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,i.a)(),e.components);return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(r.p,{children:["The ",(0,t.jsx)(r.code,{children:"Terms"})," query returns documents that contain one or more exact terms in a provided field.\nThis query is the same as the Term query, except you can search for multiple values."]}),"\n",(0,t.jsxs)(r.p,{children:["In order to use the ",(0,t.jsx)(r.code,{children:"Terms"})," query import the following:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.query.TermsQuery\nimport zio.elasticsearch.ElasticQuery.terms\n"})}),"\n",(0,t.jsxs)(r.p,{children:["You can create a ",(0,t.jsx)(r.code,{children:"Terms"})," query using the ",(0,t.jsx)(r.code,{children:"terms"})," method this way:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val query: TermsQuery = terms(field = "name", "a", "b", "c")\n'})}),"\n",(0,t.jsxs)(r.p,{children:["You can create a ",(0,t.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(r.code,{children:"Terms"})," query using the ",(0,t.jsx)(r.code,{children:"terms"})," method this way:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val query: TermQuery = terms(field = Document.name, "a", "b", "c")\n'})}),"\n",(0,t.jsxs)(r.p,{children:["If you want to change the ",(0,t.jsx)(r.code,{children:"boost"}),", you can use ",(0,t.jsx)(r.code,{children:"boost"})," method:"]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val queryWithBoost: TermsQuery = terms(field = "name", "a", "b", "c").boost(2.0)\n'})}),"\n",(0,t.jsxs)(r.p,{children:["You can find more information about ",(0,t.jsx)(r.code,{children:"Terms"})," query ",(0,t.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-terms-query.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:r}=Object.assign({},(0,i.a)(),e.components);return r?(0,t.jsx)(r,Object.assign({},e,{children:(0,t.jsx)(u,e)})):u(e)}},1151:(e,r,s)=>{s.d(r,{a:()=>a});var t=s(7294);const i={},c=t.createContext(i);function a(e){const r=t.useContext(c);return t.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}}}]);