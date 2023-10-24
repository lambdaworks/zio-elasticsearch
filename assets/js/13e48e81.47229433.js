"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5288],{3008:(e,n,r)=>{r.r(n),r.d(n,{assets:()=>o,contentTitle:()=>c,default:()=>u,frontMatter:()=>s,metadata:()=>i,toc:()=>l});var a=r(5893),t=r(1151);const s={id:"elastic_query_range",title:"Range Query"},c=void 0,i={id:"overview/queries/elastic_query_range",title:"Range Query",description:"A query that matches documents that contain terms within a provided range.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_range.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_range",permalink:"/zio-elasticsearch/overview/queries/elastic_query_range",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_range.md",tags:[],version:"current",frontMatter:{id:"elastic_query_range",title:"Range Query"},sidebar:"docs",previous:{title:"Prefix Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_prefix"},next:{title:"Regexp Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_regexp"}},o={},l=[];function d(e){const n=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,t.a)(),e.components);return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsx)(n.p,{children:"A query that matches documents that contain terms within a provided range."}),"\n",(0,a.jsxs)(n.p,{children:["In order to use the ",(0,a.jsx)(n.code,{children:"Range"})," query import the following:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.RangeQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,a.jsxs)(n.p,{children:["You can create a ",(0,a.jsx)(n.code,{children:"Range"})," query using the ",(0,a.jsx)(n.code,{children:"range"})," method in the following manner:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:'val query: RangeQuery = range(field = "intField")\n'})}),"\n",(0,a.jsxs)(n.p,{children:["You can create a ",(0,a.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,a.jsx)(n.code,{children:"Range"})," query using the ",(0,a.jsx)(n.code,{children:"range"})," method in the following manner:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"val query: RangeQuery = range(field = Document.intField)\n"})}),"\n",(0,a.jsxs)(n.p,{children:["If you want to change ",(0,a.jsx)(n.code,{children:"boost"}),", you can use the ",(0,a.jsx)(n.code,{children:"boost"})," method:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"val queryWithBoost: RangeQuery = range(field = Document.intField).boost(2.0)\n"})}),"\n",(0,a.jsxs)(n.p,{children:["If you want to change ",(0,a.jsx)(n.code,{children:"format"}),", you can use the ",(0,a.jsx)(n.code,{children:"format"})," method:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:'val queryWithFormat: RangeQuery = range(field = Document.dateField).format("yyyy-MM-dd")\n'})}),"\n",(0,a.jsxs)(n.p,{children:["If you want to change ",(0,a.jsx)(n.code,{children:"gt"})," (greater than), you can use the ",(0,a.jsx)(n.code,{children:"gt"})," method:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"val queryWithGt: RangeQuery = range(field = Document.intField).gt(1)\n"})}),"\n",(0,a.jsxs)(n.p,{children:["If you want to change ",(0,a.jsx)(n.code,{children:"gte"})," (greater than or equal to), you can use the ",(0,a.jsx)(n.code,{children:"gte"})," method:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"val queryWithGte: RangeQuery = range(field = Document.intField).gte(1)\n"})}),"\n",(0,a.jsxs)(n.p,{children:["If you want to change ",(0,a.jsx)(n.code,{children:"lt"})," (less than), you can use the ",(0,a.jsx)(n.code,{children:"lt"})," method:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"val queryWithLt: RangeQuery = range(field = Document.intField).lt(100)\n"})}),"\n",(0,a.jsxs)(n.p,{children:["If you want to change ",(0,a.jsx)(n.code,{children:"lte"})," (less than or equal to), you can use the ",(0,a.jsx)(n.code,{children:"lte"})," method:"]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"val queryWithLte: RangeQuery = range(field = Document.intField).lte(100)\n"})}),"\n",(0,a.jsxs)(n.p,{children:["You can find more information about ",(0,a.jsx)(n.code,{children:"Range"})," query ",(0,a.jsx)(n.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-range-query.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:n}=Object.assign({},(0,t.a)(),e.components);return n?(0,a.jsx)(n,Object.assign({},e,{children:(0,a.jsx)(d,e)})):d(e)}},1151:(e,n,r)=>{r.d(n,{a:()=>c});var a=r(7294);const t={},s=a.createContext(t);function c(e){const n=a.useContext(s);return a.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}}}]);