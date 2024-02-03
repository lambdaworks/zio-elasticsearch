"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8548],{1916:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>o,contentTitle:()=>n,default:()=>u,frontMatter:()=>c,metadata:()=>a,toc:()=>d});var i=r(7624),s=r(2172);const c={id:"elastic_query_terms_set",title:"Terms Set Query"},n=void 0,a={id:"overview/queries/elastic_query_terms_set",title:"Terms Set Query",description:"The TermsSet query returns documents that contain the minimum amount of exact terms in a provided field. The Terms set query is the same as [[zio.elasticsearch.query.TermsQuery]], except you can define the number of matching terms required to return a document.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_terms_set.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_terms_set",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms_set",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_terms_set.md",tags:[],version:"current",frontMatter:{id:"elastic_query_terms_set",title:"Terms Set Query"},sidebar:"docs",previous:{title:"Terms Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms"},next:{title:"Wildcard Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_wildcard"}},o={},d=[];function l(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,s.M)(),e.components);return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(t.p,{children:["The ",(0,i.jsx)(t.code,{children:"TermsSet"})," query returns documents that contain the minimum amount of exact terms in a provided field. The Terms set query is the same as [[zio.elasticsearch.query.TermsQuery]], except you can define the number of matching terms required to return a document."]}),"\n",(0,i.jsxs)(t.p,{children:["In order to use the ",(0,i.jsx)(t.code,{children:"TermsSet"})," query import the following:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.query.TermsSetQuery\nimport zio.elasticsearch.ElasticQuery.termsSetQuery\n"})}),"\n",(0,i.jsxs)(t.p,{children:["You can create a ",(0,i.jsx)(t.code,{children:"TermsSet"})," query with defined ",(0,i.jsx)(t.code,{children:"minimumShouldMatchField"})," using the ",(0,i.jsx)(t.code,{children:"termsSet"})," method this way:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val query: TermsSetQuery = termsSet(field = "stringField", minimumShouldMatchField = "intField", terms = "a", "b", "c")\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can create a ",(0,i.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,i.jsx)(t.code,{children:"TermsSet"})," query with defined ",(0,i.jsx)(t.code,{children:"minimumShouldMatchField"})," using the ",(0,i.jsx)(t.code,{children:"termsSet"})," method this way:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val query: TermsSetQuery = termsSet(field = Document.name, minimumShouldMatchField = Document.intField, terms = "a", "b", "c")\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can create a ",(0,i.jsx)(t.code,{children:"TermsSet"})," query with defined ",(0,i.jsx)(t.code,{children:"minimumShouldMatchScript"})," using the ",(0,i.jsx)(t.code,{children:"termsSetScript"})," method this way:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.script.Script\n\nval query: TermsSetQuery = termsSetScript(field = "stringField", minimumShouldMatchScript = Script("doc[\'intField\'].value"), terms = "a", "b", "c")\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can create a ",(0,i.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,i.jsx)(t.code,{children:"TermsSet"})," query with defined ",(0,i.jsx)(t.code,{children:"minimumShouldMatchScript"})," using the ",(0,i.jsx)(t.code,{children:"termsSetScript"})," method this way:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.script.Script\n\nval query: TermsSetQuery = termsSetScript(field = Document.name, minimumShouldMatchScript = Script("doc[\'intField\'].value"), terms = "a", "b", "c")\n'})}),"\n",(0,i.jsxs)(t.p,{children:["If you want to change the ",(0,i.jsx)(t.code,{children:"boost"}),", you can use ",(0,i.jsx)(t.code,{children:"boost"})," method:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val queryWithBoostAndMinimumShouldMatchField: TermsSetQuery = termsSet(field = "booleanField", minimumShouldMatchField = "intField", terms = true, false).boost(2.0)\nval queryWithBoostAndMinimumShouldMatchScript: TermsSetQuery = termsSetScript(field = "booleanField", minimumShouldMatchScript = Script("doc[\'intField\'].value"), terms = true, false).boost(2.0)\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can find more information about ",(0,i.jsx)(t.code,{children:"TermsSet"})," query ",(0,i.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-terms-set-query.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:t}=Object.assign({},(0,s.M)(),e.components);return t?(0,i.jsx)(t,Object.assign({},e,{children:(0,i.jsx)(l,e)})):l(e)}},2172:(e,t,r)=>{r.d(t,{M:()=>n});var i=r(1504);const s={},c=i.createContext(s);function n(e){const t=i.useContext(c);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}}}]);