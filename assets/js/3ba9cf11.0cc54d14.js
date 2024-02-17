"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[1727],{181:(e,i,n)=>{n.r(i),n.d(i,{assets:()=>o,contentTitle:()=>a,default:()=>d,frontMatter:()=>g,metadata:()=>r,toc:()=>c});var s=n(4848),t=n(8453);const g={id:"elastic_aggregation_missing",title:"Missing Aggregation"},a=void 0,r={id:"overview/aggregations/elastic_aggregation_missing",title:"Missing Aggregation",description:"The Missing aggregation is a field data based single bucket aggregation, that creates a bucket of all documents in the current document set context that are missing a field value.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_missing.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_missing",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_missing",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_missing.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_missing",title:"Missing Aggregation"},sidebar:"docs",previous:{title:"Min Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_min"},next:{title:"Percentiles Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_percentile_ranks"}},o={},c=[];function l(e){const i={a:"a",code:"code",p:"p",pre:"pre",...(0,t.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(i.p,{children:["The ",(0,s.jsx)(i.code,{children:"Missing"})," aggregation is a field data based single bucket aggregation, that creates a bucket of all documents in the current document set context that are missing a field value."]}),"\n",(0,s.jsxs)(i.p,{children:["In order to use the ",(0,s.jsx)(i.code,{children:"Missing"})," aggregation import the following:"]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.MissingAggregation\nimport zio.elasticsearch.ElasticAggregation.missingAggregation\n"})}),"\n",(0,s.jsxs)(i.p,{children:["You can create a ",(0,s.jsx)(i.code,{children:"Missing"})," aggregation using the ",(0,s.jsx)(i.code,{children:"missingAggregation"})," method this way:"]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-scala",children:'val aggregation: MissingAggregation = missingAggregation(name = "missingAggregation", field = "stringField")\n'})}),"\n",(0,s.jsxs)(i.p,{children:["You can create a ",(0,s.jsx)(i.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,s.jsx)(i.code,{children:"Missing"})," aggregation using the ",(0,s.jsx)(i.code,{children:"missingAggregation"})," method this way:"]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-scala",children:'// Document.stringField must be string value, because of Missing aggregation\nval aggregation: MissingAggregation = missingAggregation(name = "missingAggregation", field = Document.stringField)\n'})}),"\n",(0,s.jsxs)(i.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,s.jsx)(i.code,{children:"withAgg"})," method:"]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = missingAggregation(name = "missingAggregation1", field = Document.stringField).withAgg(missingAggregation(name = "missingAggregation2", field = Document.stringField))\n'})}),"\n",(0,s.jsxs)(i.p,{children:["You can find more information about ",(0,s.jsx)(i.code,{children:"Missing"})," aggregation ",(0,s.jsx)(i.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-missing-aggregation.html",children:"here"}),"."]})]})}function d(e={}){const{wrapper:i}={...(0,t.R)(),...e.components};return i?(0,s.jsx)(i,{...e,children:(0,s.jsx)(l,{...e})}):l(e)}},8453:(e,i,n)=>{n.d(i,{R:()=>a,x:()=>r});var s=n(6540);const t={},g=s.createContext(t);function a(e){const i=s.useContext(g);return s.useMemo((function(){return"function"==typeof e?e(i):{...i,...e}}),[i,e])}function r(e){let i;return i=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:a(e.components),s.createElement(g.Provider,{value:i},e.children)}}}]);