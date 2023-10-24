"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9189],{6450:(e,a,i)=>{i.r(a),i.d(a,{assets:()=>o,contentTitle:()=>r,default:()=>d,frontMatter:()=>n,metadata:()=>s,toc:()=>c});var t=i(5893),g=i(1151);const n={id:"elastic_aggregation_max",title:"Max Aggregation"},r=void 0,s={id:"overview/aggregations/elastic_aggregation_max",title:"Max Aggregation",description:"The Max aggregation is a single-value metrics aggregation that keeps track and returns the maximum value among the numeric values extracted from the aggregated documents.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_max.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_max",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_max",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_max.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_max",title:"Max Aggregation"},sidebar:"docs",previous:{title:"Cardinality Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_cardinality"},next:{title:"Min Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_min"}},o={},c=[];function l(e){const a=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,g.a)(),e.components);return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(a.p,{children:["The ",(0,t.jsx)(a.code,{children:"Max"})," aggregation is a single-value metrics aggregation that keeps track and returns the maximum value among the numeric values extracted from the aggregated documents."]}),"\n",(0,t.jsxs)(a.p,{children:["In order to use the ",(0,t.jsx)(a.code,{children:"Max"})," aggregation import the following:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.MaxAggregation\nimport zio.elasticsearch.ElasticAggregation.maxAggregation\n"})}),"\n",(0,t.jsxs)(a.p,{children:["You can create a ",(0,t.jsx)(a.code,{children:"Max"})," aggregation using the ",(0,t.jsx)(a.code,{children:"maxAggregation"})," method this way:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'val aggregation: MaxAggregation = maxAggregation(name = "maxAggregation", field = "intField")\n'})}),"\n",(0,t.jsxs)(a.p,{children:["You can create a ",(0,t.jsx)(a.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(a.code,{children:"Max"})," aggregation using the ",(0,t.jsx)(a.code,{children:"maxAggregation"})," method this way:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'// Document.intField must be number value, because of Max aggregation\nval aggregation: MaxAggregation = maxAggregation(name = "maxAggregation", field = Document.intField)\n'})}),"\n",(0,t.jsxs)(a.p,{children:["If you want to change the ",(0,t.jsx)(a.code,{children:"missing"}),", you can use ",(0,t.jsx)(a.code,{children:"missing"})," method:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'val aggregationWithMissing: MaxAggregation = maxAggregation(name = "maxAggregation", field = Document.intField).missing(10.0)\n'})}),"\n",(0,t.jsxs)(a.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,t.jsx)(a.code,{children:"withAgg"})," method:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = maxAggregation(name = "maxAggregation1", field = Document.intField).withAgg(maxAggregation(name = "maxAggregation2", field = Document.doubleField))\n'})}),"\n",(0,t.jsxs)(a.p,{children:["You can find more information about ",(0,t.jsx)(a.code,{children:"Max"})," aggregation ",(0,t.jsx)(a.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-max-aggregation.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:a}=Object.assign({},(0,g.a)(),e.components);return a?(0,t.jsx)(a,Object.assign({},e,{children:(0,t.jsx)(l,e)})):l(e)}},1151:(e,a,i)=>{i.d(a,{a:()=>r});var t=i(7294);const g={},n=t.createContext(g);function r(e){const a=t.useContext(n);return t.useMemo((function(){return"function"==typeof e?e(a):{...a,...e}}),[a,e])}}}]);