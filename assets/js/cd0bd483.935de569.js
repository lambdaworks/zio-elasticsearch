"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8588],{9152:(e,t,a)=>{a.r(t),a.d(t,{assets:()=>o,contentTitle:()=>g,default:()=>l,frontMatter:()=>s,metadata:()=>r,toc:()=>d});var n=a(7624),i=a(2172);const s={id:"elastic_aggregation_extended_stats",title:"Extended stats Aggregation"},g=void 0,r={id:"overview/aggregations/elastic_aggregation_extended_stats",title:"Extended stats Aggregation",description:"The Extended stats aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max, average, sum od squares, variance and std deviation of a field) over numeric values extracted from the aggregated documents.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_extended_stats.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_extended_stats",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_extended_stats",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_extended_stats.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_extended_stats",title:"Extended stats Aggregation"},sidebar:"docs",previous:{title:"Cardinality Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_cardinality"},next:{title:"Filter Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_filter"}},o={},d=[];function c(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,i.M)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(t.p,{children:["The ",(0,n.jsx)(t.code,{children:"Extended stats"})," aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max, average, sum od squares, variance and std deviation of a field) over numeric values extracted from the aggregated documents.\nThe ",(0,n.jsx)(t.code,{children:"Extended stats"})," aggregation is an extended version of the ",(0,n.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/aggregations/elastic_aggregation_stats",children:(0,n.jsx)(t.code,{children:"Stats"})})," aggregation."]}),"\n",(0,n.jsxs)(t.p,{children:["In order to use the ",(0,n.jsx)(t.code,{children:"Extended stats"})," aggregation import the following:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.ExtendedStatsAggregation\nimport zio.elasticsearch.ElasticAggregation.extendedStatsAggregation\n"})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.code,{children:"Extended stats"})," aggregation using the ",(0,n.jsx)(t.code,{children:"extendedStatsAggregation"})," method this way:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val aggregation: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = "intField")\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,n.jsx)(t.code,{children:"Extended stats"})," aggregation using the ",(0,n.jsx)(t.code,{children:"extendedStatsAggregation"})," method this way:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'// Document.intField must be number value, because of Stats aggregation\nval aggregation: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"missing"})," parameter, you can use ",(0,n.jsx)(t.code,{children:"missing"})," method:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val aggregationWithMissing: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField).missing(10.0)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"sigma"})," parameter, you can use ",(0,n.jsx)(t.code,{children:"sigma"})," method:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val aggregationWithSigma: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField).sigma(3.0)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,n.jsx)(t.code,{children:"withAgg"})," method:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = extendedStatsAggregation(name = "extendedStatsAggregation1", field = Document.intField).withAgg(extendedStatsAggregation(name = "extendedStatsAggregation2", field = Document.doubleField))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can find more information about ",(0,n.jsx)(t.code,{children:"Extended stats"})," aggregation ",(0,n.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-extendedstats-aggregation.html#search-aggregations-metrics-extendedstats-aggregation",children:"here"}),"."]})]})}function l(e={}){const{wrapper:t}={...(0,i.M)(),...e.components};return t?(0,n.jsx)(t,{...e,children:(0,n.jsx)(c,{...e})}):c(e)}},2172:(e,t,a)=>{a.d(t,{I:()=>r,M:()=>g});var n=a(1504);const i={},s=n.createContext(i);function g(e){const t=n.useContext(s);return n.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function r(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:g(e.components),n.createElement(s.Provider,{value:t},e.children)}}}]);