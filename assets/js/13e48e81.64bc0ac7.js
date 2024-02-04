"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5048],{3588:(e,n,r)=>{r.r(n),r.d(n,{assets:()=>o,contentTitle:()=>c,default:()=>u,frontMatter:()=>s,metadata:()=>i,toc:()=>l});var t=r(7624),a=r(2172);const s={id:"elastic_query_range",title:"Range Query"},c=void 0,i={id:"overview/queries/elastic_query_range",title:"Range Query",description:"A query that matches documents that contain terms within a provided range.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_range.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_range",permalink:"/zio-elasticsearch/overview/queries/elastic_query_range",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_range.md",tags:[],version:"current",frontMatter:{id:"elastic_query_range",title:"Range Query"},sidebar:"docs",previous:{title:"Prefix Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_prefix"},next:{title:"Regexp Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_regexp"}},o={},l=[];function d(e){const n={a:"a",code:"code",p:"p",pre:"pre",...(0,a.M)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(n.p,{children:"A query that matches documents that contain terms within a provided range."}),"\n",(0,t.jsxs)(n.p,{children:["In order to use the ",(0,t.jsx)(n.code,{children:"Range"})," query import the following:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.RangeQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,t.jsxs)(n.p,{children:["You can create a ",(0,t.jsx)(n.code,{children:"Range"})," query using the ",(0,t.jsx)(n.code,{children:"range"})," method in the following manner:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:'val query: RangeQuery = range(field = "intField")\n'})}),"\n",(0,t.jsxs)(n.p,{children:["You can create a ",(0,t.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(n.code,{children:"Range"})," query using the ",(0,t.jsx)(n.code,{children:"range"})," method in the following manner:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"val query: RangeQuery = range(field = Document.intField)\n"})}),"\n",(0,t.jsxs)(n.p,{children:["If you want to change ",(0,t.jsx)(n.code,{children:"boost"}),", you can use the ",(0,t.jsx)(n.code,{children:"boost"})," method:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"val queryWithBoost: RangeQuery = range(field = Document.intField).boost(2.0)\n"})}),"\n",(0,t.jsxs)(n.p,{children:["If you want to change ",(0,t.jsx)(n.code,{children:"format"}),", you can use the ",(0,t.jsx)(n.code,{children:"format"})," method:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:'val queryWithFormat: RangeQuery = range(field = Document.dateField).format("yyyy-MM-dd")\n'})}),"\n",(0,t.jsxs)(n.p,{children:["If you want to change ",(0,t.jsx)(n.code,{children:"gt"})," (greater than), you can use the ",(0,t.jsx)(n.code,{children:"gt"})," method:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"val queryWithGt: RangeQuery = range(field = Document.intField).gt(1)\n"})}),"\n",(0,t.jsxs)(n.p,{children:["If you want to change ",(0,t.jsx)(n.code,{children:"gte"})," (greater than or equal to), you can use the ",(0,t.jsx)(n.code,{children:"gte"})," method:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"val queryWithGte: RangeQuery = range(field = Document.intField).gte(1)\n"})}),"\n",(0,t.jsxs)(n.p,{children:["If you want to change ",(0,t.jsx)(n.code,{children:"lt"})," (less than), you can use the ",(0,t.jsx)(n.code,{children:"lt"})," method:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"val queryWithLt: RangeQuery = range(field = Document.intField).lt(100)\n"})}),"\n",(0,t.jsxs)(n.p,{children:["If you want to change ",(0,t.jsx)(n.code,{children:"lte"})," (less than or equal to), you can use the ",(0,t.jsx)(n.code,{children:"lte"})," method:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-scala",children:"val queryWithLte: RangeQuery = range(field = Document.intField).lte(100)\n"})}),"\n",(0,t.jsxs)(n.p,{children:["You can find more information about ",(0,t.jsx)(n.code,{children:"Range"})," query ",(0,t.jsx)(n.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-range-query.html",children:"here"}),"."]})]})}function u(e={}){const{wrapper:n}={...(0,a.M)(),...e.components};return n?(0,t.jsx)(n,{...e,children:(0,t.jsx)(d,{...e})}):d(e)}},2172:(e,n,r)=>{r.d(n,{I:()=>i,M:()=>c});var t=r(1504);const a={},s=t.createContext(a);function c(e){const n=t.useContext(s);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function i(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(a):e.components||a:c(e.components),t.createElement(s.Provider,{value:n},e.children)}}}]);