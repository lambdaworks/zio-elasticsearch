"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[3353],{509:(e,i,s)=>{s.r(i),s.d(i,{assets:()=>o,contentTitle:()=>t,default:()=>d,frontMatter:()=>a,metadata:()=>r,toc:()=>c});var g=s(5893),n=s(1151);const a={id:"elastic_aggregation_missing",title:"Missing Aggregation"},t=void 0,r={unversionedId:"overview/aggregations/elastic_aggregation_missing",id:"overview/aggregations/elastic_aggregation_missing",title:"Missing Aggregation",description:"The Missing aggregation is a field data based single bucket aggregation, that creates a bucket of all documents in the current document set context that are missing a field value.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_missing.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_missing",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_missing",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_missing.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_missing",title:"Missing Aggregation"},sidebar:"docs",previous:{title:"Min Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_min"},next:{title:"Terms Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_terms"}},o={},c=[];function l(e){const i=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.ah)(),e.components);return(0,g.jsxs)(g.Fragment,{children:[(0,g.jsxs)(i.p,{children:["The ",(0,g.jsx)(i.code,{children:"Missing"})," aggregation is a field data based single bucket aggregation, that creates a bucket of all documents in the current document set context that are missing a field value."]}),"\n",(0,g.jsxs)(i.p,{children:["In order to use the ",(0,g.jsx)(i.code,{children:"Missing"})," aggregation import the following:"]}),"\n",(0,g.jsx)(i.pre,{children:(0,g.jsx)(i.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.MissingAggregation\nimport zio.elasticsearch.ElasticAggregation.missingAggregation\n"})}),"\n",(0,g.jsxs)(i.p,{children:["You can create a ",(0,g.jsx)(i.code,{children:"Missing"})," aggregation using the ",(0,g.jsx)(i.code,{children:"missingAggregation"})," method this way:"]}),"\n",(0,g.jsx)(i.pre,{children:(0,g.jsx)(i.code,{className:"language-scala",children:'val aggregation: MissingAggregation = missingAggregation(name = "missingAggregation", field = "stringField")\n'})}),"\n",(0,g.jsxs)(i.p,{children:["You can create a ",(0,g.jsx)(i.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,g.jsx)(i.code,{children:"Missing"})," aggregation using the ",(0,g.jsx)(i.code,{children:"missingAggregation"})," method this way:"]}),"\n",(0,g.jsx)(i.pre,{children:(0,g.jsx)(i.code,{className:"language-scala",children:'// Document.stringField must be string value, because of Missing aggregation\nval aggregation: MissingAggregation = missingAggregation(name = "missingAggregation", field = Document.stringField)\n'})}),"\n",(0,g.jsxs)(i.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,g.jsx)(i.code,{children:"withAgg"})," method:"]}),"\n",(0,g.jsx)(i.pre,{children:(0,g.jsx)(i.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = missingAggregation(name = "missingAggregation1", field = Document.stringField).withAgg(missingAggregation(name = "missingAggregation2", field = Document.stringField))\n'})}),"\n",(0,g.jsxs)(i.p,{children:["You can find more information about ",(0,g.jsx)(i.code,{children:"Missing"})," aggregation ",(0,g.jsx)(i.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-missing-aggregation.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:i}=Object.assign({},(0,n.ah)(),e.components);return i?(0,g.jsx)(i,Object.assign({},e,{children:(0,g.jsx)(l,e)})):l(e)}},1151:(e,i,s)=>{s.d(i,{Zo:()=>r,ah:()=>a});var g=s(7294);const n=g.createContext({});function a(e){const i=g.useContext(n);return g.useMemo((()=>"function"==typeof e?e(i):{...i,...e}),[i,e])}const t={};function r({components:e,children:i,disableParentContext:s}){let r;return r=s?"function"==typeof e?e({}):e||t:a(e),g.createElement(n.Provider,{value:r},i)}}}]);