"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[996],{3404:(e,t,a)=>{a.r(t),a.d(t,{assets:()=>o,contentTitle:()=>r,default:()=>d,frontMatter:()=>g,metadata:()=>s,toc:()=>c});const s=JSON.parse('{"id":"overview/aggregations/elastic_aggregation_stats","title":"Stats Aggregation","description":"The Stats aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max and average of a field) over numeric values extracted from the aggregated documents.","source":"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_stats.md","sourceDirName":"overview/aggregations","slug":"/overview/aggregations/elastic_aggregation_stats","permalink":"/zio-elasticsearch/overview/aggregations/elastic_aggregation_stats","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_stats.md","tags":[],"version":"current","frontMatter":{"id":"elastic_aggregation_stats","title":"Stats Aggregation"},"sidebar":"docs","previous":{"title":"Percentiles Aggregation","permalink":"/zio-elasticsearch/overview/aggregations/elastic_aggregation_percentiles"},"next":{"title":"Sum Aggregation","permalink":"/zio-elasticsearch/overview/aggregations/elastic_aggregation_sum"}}');var i=a(4848),n=a(8453);const g={id:"elastic_aggregation_stats",title:"Stats Aggregation"},r=void 0,o={},c=[];function l(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,n.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(t.p,{children:["The ",(0,i.jsx)(t.code,{children:"Stats"})," aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max and average of a field) over numeric values extracted from the aggregated documents."]}),"\n",(0,i.jsxs)(t.p,{children:["In order to use the ",(0,i.jsx)(t.code,{children:"Stats"})," aggregation import the following:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.StatsAggregation\nimport zio.elasticsearch.ElasticAggregation.statsAggregation\n"})}),"\n",(0,i.jsxs)(t.p,{children:["You can create a ",(0,i.jsx)(t.code,{children:"Stats"})," aggregation using the ",(0,i.jsx)(t.code,{children:"statsAggregation"})," method this way:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val aggregation: StatsAggregation = statsAggregation(name = "statsAggregation", field = "intField")\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can create a ",(0,i.jsx)(t.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,i.jsx)(t.code,{children:"Stats"})," aggregation using the ",(0,i.jsx)(t.code,{children:"statsAggregation"})," method this way:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'// Document.intField must be number value, because of Stats aggregation\nval aggregation: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField)\n'})}),"\n",(0,i.jsxs)(t.p,{children:["If you want to change the ",(0,i.jsx)(t.code,{children:"missing"})," parameter, you can use ",(0,i.jsx)(t.code,{children:"missing"})," method:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val aggregationWithMissing: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField).missing(10.0)\n'})}),"\n",(0,i.jsxs)(t.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,i.jsx)(t.code,{children:"withAgg"})," method:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = statsAggregation(name = "statsAggregation1", field = Document.intField).withAgg(statsAggregation(name = "statsAggregation2", field = Document.doubleField))\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can find more information about ",(0,i.jsx)(t.code,{children:"Stats"})," aggregation ",(0,i.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-stats-aggregation.html",children:"here"}),"."]})]})}function d(e={}){const{wrapper:t}={...(0,n.R)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(l,{...e})}):l(e)}},8453:(e,t,a)=>{a.d(t,{R:()=>g,x:()=>r});var s=a(6540);const i={},n=s.createContext(i);function g(e){const t=s.useContext(n);return s.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function r(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:g(e.components),s.createElement(n.Provider,{value:t},e.children)}}}]);