"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8279],{8577:(e,a,g)=>{g.r(a),g.d(a,{assets:()=>s,contentTitle:()=>r,default:()=>d,frontMatter:()=>n,metadata:()=>o,toc:()=>c});var t=g(5893),i=g(1151);const n={id:"elastic_aggregation_avg",title:"Avg Aggregation"},r=void 0,o={unversionedId:"overview/aggregations/elastic_aggregation_avg",id:"overview/aggregations/elastic_aggregation_avg",title:"Avg Aggregation",description:"The Avg aggregation is a single-value metrics aggregation that keeps track and returns the average value among the numeric values extracted from the aggregated documents.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_avg.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_avg",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_avg",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_avg.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_avg",title:"Avg Aggregation"},sidebar:"docs",previous:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_aggregation"},next:{title:"Bucket Selector Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_bucket_selector"}},s={},c=[];function l(e){const a=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,i.ah)(),e.components);return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(a.p,{children:["The ",(0,t.jsx)(a.code,{children:"Avg"})," aggregation is a single-value metrics aggregation that keeps track and returns the average value among the numeric values extracted from the aggregated documents."]}),"\n",(0,t.jsxs)(a.p,{children:["In order to use the ",(0,t.jsx)(a.code,{children:"Avg"})," aggregation import the following:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.AvgAggregation\nimport zio.elasticsearch.ElasticAggregation.avgAggregation\n"})}),"\n",(0,t.jsxs)(a.p,{children:["You can create a ",(0,t.jsx)(a.code,{children:"Avg"})," aggregation using the ",(0,t.jsx)(a.code,{children:"avgAggregation"})," method this way:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'val aggregation: AvgAggregation = avgAggregation(name = "avgAggregation", field = "intField")\n'})}),"\n",(0,t.jsxs)(a.p,{children:["You can create a ",(0,t.jsx)(a.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,t.jsx)(a.code,{children:"Avg"})," aggregation using the ",(0,t.jsx)(a.code,{children:"avgAggregation"})," method this way:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'// Document.intField must be number value, because of Avg aggregation\nval aggregation: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField)\n'})}),"\n",(0,t.jsxs)(a.p,{children:["If you want to change the ",(0,t.jsx)(a.code,{children:"missing"})," parameter, you can use ",(0,t.jsx)(a.code,{children:"missing"})," method:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'val aggregationWithMissing: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField).missing(10.0)\n'})}),"\n",(0,t.jsxs)(a.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,t.jsx)(a.code,{children:"withAgg"})," method:"]}),"\n",(0,t.jsx)(a.pre,{children:(0,t.jsx)(a.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = avgAggregation(name = "avgAggregation1", field = Document.intField).withAgg(avgAggregation(name = "avgAggregation2", field = Document.doubleField))\n'})}),"\n",(0,t.jsxs)(a.p,{children:["You can find more information about ",(0,t.jsx)(a.code,{children:"Avg"})," aggregation ",(0,t.jsx)(a.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-avg-aggregation.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:a}=Object.assign({},(0,i.ah)(),e.components);return a?(0,t.jsx)(a,Object.assign({},e,{children:(0,t.jsx)(l,e)})):l(e)}},1151:(e,a,g)=>{g.d(a,{Zo:()=>o,ah:()=>n});var t=g(7294);const i=t.createContext({});function n(e){const a=t.useContext(i);return t.useMemo((()=>"function"==typeof e?e(a):{...a,...e}),[a,e])}const r={};function o({components:e,children:a,disableParentContext:g}){let o;return o=g?"function"==typeof e?e({}):e||r:n(e),t.createElement(i.Provider,{value:o},a)}}}]);