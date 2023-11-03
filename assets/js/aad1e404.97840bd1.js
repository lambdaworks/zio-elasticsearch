"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[4708],{9514:(e,i,t)=>{t.r(i),t.d(i,{assets:()=>c,contentTitle:()=>s,default:()=>d,frontMatter:()=>r,metadata:()=>g,toc:()=>o});var n=t(5893),a=t(1151);const r={id:"elastic_aggregation_percentiles",title:"Percentiles Aggregation"},s=void 0,g={id:"overview/aggregations/elastic_aggregation_percentiles",title:"Percentiles Aggregation",description:"The Percentiles aggregation is a multi-value metrics aggregation that calculates one or more percentiles over numeric values extracted from the aggregated documents.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_percentiles.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_percentiles",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_percentiles",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_percentiles.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_percentiles",title:"Percentiles Aggregation"},sidebar:"docs",previous:{title:"Missing Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_missing"},next:{title:"Sum Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_sum"}},c={},o=[];function l(e){const i=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,a.a)(),e.components);return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(i.p,{children:["The ",(0,n.jsx)(i.code,{children:"Percentiles"})," aggregation is a multi-value metrics aggregation that calculates one or more percentiles over numeric values extracted from the aggregated documents."]}),"\n",(0,n.jsxs)(i.p,{children:["In order to use the ",(0,n.jsx)(i.code,{children:"Percentiles"})," aggregation import the following:"]}),"\n",(0,n.jsx)(i.pre,{children:(0,n.jsx)(i.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.PercentilesAggregation\nimport zio.elasticsearch.ElasticAggregation.percentilesAggregation\n"})}),"\n",(0,n.jsxs)(i.p,{children:["You can create a ",(0,n.jsx)(i.code,{children:"Percentiles"})," aggregation using the ",(0,n.jsx)(i.code,{children:"percentilesAggregation"})," method this way:"]}),"\n",(0,n.jsx)(i.pre,{children:(0,n.jsx)(i.code,{className:"language-scala",children:'val aggregation: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = "intField")\n'})}),"\n",(0,n.jsxs)(i.p,{children:["You can create a ",(0,n.jsx)(i.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,n.jsx)(i.code,{children:"Percentiles"})," aggregation using the ",(0,n.jsx)(i.code,{children:"percentilesAggregation"})," method this way:"]}),"\n",(0,n.jsx)(i.pre,{children:(0,n.jsx)(i.code,{className:"language-scala",children:'// Document.intField must be number value\nval aggregation: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = Document.intField)\n'})}),"\n",(0,n.jsxs)(i.p,{children:["If you want to specify the percentiles you want to calculate, you can use ",(0,n.jsx)(i.code,{children:"percents"})," method:"]}),"\n",(0,n.jsx)(i.pre,{children:(0,n.jsx)(i.code,{className:"language-scala",children:'val aggregationWithPercents: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = Document.intField).percents(15, 50, 70)\n'})}),"\n",(0,n.jsxs)(i.p,{children:["If you want to change the ",(0,n.jsx)(i.code,{children:"missing"}),", you can use ",(0,n.jsx)(i.code,{children:"missing"})," method:"]}),"\n",(0,n.jsx)(i.pre,{children:(0,n.jsx)(i.code,{className:"language-scala",children:'val aggregationWithMissing: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = Document.intField).missing(10.0)\n'})}),"\n",(0,n.jsxs)(i.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,n.jsx)(i.code,{children:"withAgg"})," method:"]}),"\n",(0,n.jsx)(i.pre,{children:(0,n.jsx)(i.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = percentilesAggregation(name = "percentilesAggregation1", field = Document.intField).withAgg(percentilesAggregation(name = "percentilesAggregation2", field = Document.doubleField))\n'})}),"\n",(0,n.jsxs)(i.p,{children:["You can find more information about ",(0,n.jsx)(i.code,{children:"Percentiles"})," aggregation ",(0,n.jsx)(i.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-percentile-aggregation.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:i}=Object.assign({},(0,a.a)(),e.components);return i?(0,n.jsx)(i,Object.assign({},e,{children:(0,n.jsx)(l,e)})):l(e)}},1151:(e,i,t)=>{t.d(i,{a:()=>s});var n=t(7294);const a={},r=n.createContext(a);function s(e){const i=n.useContext(r);return n.useMemo((function(){return"function"==typeof e?e(i):{...i,...e}}),[i,e])}}}]);