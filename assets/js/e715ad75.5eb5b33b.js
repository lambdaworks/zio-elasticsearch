"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[2617],{179:(e,a,t)=>{t.r(a),t.d(a,{assets:()=>o,contentTitle:()=>s,default:()=>u,frontMatter:()=>g,metadata:()=>r,toc:()=>c});var i=t(5893),n=t(1151);const g={id:"elastic_aggregation_sum",title:"Sum Aggregation"},s=void 0,r={unversionedId:"overview/aggregations/elastic_aggregation_sum",id:"overview/aggregations/elastic_aggregation_sum",title:"Sum Aggregation",description:"The Sum aggregation is a single-value metrics aggregation that keeps track and returns the sum value among the numeric values extracted from the aggregated documents.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_sum.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_sum",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_sum",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_sum.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_sum",title:"Sum Aggregation"},sidebar:"docs",previous:{title:"Terms Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_terms"},next:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_request"}},o={},c=[];function l(e){const a=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.ah)(),e.components);return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(a.p,{children:["The ",(0,i.jsx)(a.code,{children:"Sum"})," aggregation is a single-value metrics aggregation that keeps track and returns the sum value among the numeric values extracted from the aggregated documents."]}),"\n",(0,i.jsxs)(a.p,{children:["In order to use the ",(0,i.jsx)(a.code,{children:"Sum"})," aggregation import the following:"]}),"\n",(0,i.jsx)(a.pre,{children:(0,i.jsx)(a.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.SumAggregation\nimport zio.elasticsearch.ElasticAggregation.sumAggregation\n"})}),"\n",(0,i.jsxs)(a.p,{children:["You can create a ",(0,i.jsx)(a.code,{children:"Sum"})," aggregation using the ",(0,i.jsx)(a.code,{children:"sumAggregation"})," method this way:"]}),"\n",(0,i.jsx)(a.pre,{children:(0,i.jsx)(a.code,{className:"language-scala",children:'val aggregation: SumAggregation = sumAggregation(name = "sumAggregation", field = "intField")\n'})}),"\n",(0,i.jsxs)(a.p,{children:["You can create a ",(0,i.jsx)(a.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,i.jsx)(a.code,{children:"Sum"})," aggregation using the ",(0,i.jsx)(a.code,{children:"sumAggregation"})," method this way:"]}),"\n",(0,i.jsx)(a.pre,{children:(0,i.jsx)(a.code,{className:"language-scala",children:'// Document.intField must be number value, because of Sum aggregation\nval aggregation: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField)\n'})}),"\n",(0,i.jsxs)(a.p,{children:["If you want to change the ",(0,i.jsx)(a.code,{children:"missing"})," parameter, you can use ",(0,i.jsx)(a.code,{children:"missing"})," method:"]}),"\n",(0,i.jsx)(a.pre,{children:(0,i.jsx)(a.code,{className:"language-scala",children:'val aggregationWithMissing: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField).missing(10.0)\n'})}),"\n",(0,i.jsxs)(a.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,i.jsx)(a.code,{children:"withAgg"})," method:"]}),"\n",(0,i.jsx)(a.pre,{children:(0,i.jsx)(a.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = sumAggregation(name = "sumAggregation1", field = Document.intField).withAgg(sumAggregation(name = "sumAggregation2", field = Document.doubleField))\n'})}),"\n",(0,i.jsxs)(a.p,{children:["You can find more information about ",(0,i.jsx)(a.code,{children:"Sum"})," aggregation ",(0,i.jsx)(a.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-sum-aggregation.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:a}=Object.assign({},(0,n.ah)(),e.components);return a?(0,i.jsx)(a,Object.assign({},e,{children:(0,i.jsx)(l,e)})):l(e)}},1151:(e,a,t)=>{t.d(a,{Zo:()=>r,ah:()=>g});var i=t(7294);const n=i.createContext({});function g(e){const a=i.useContext(n);return i.useMemo((()=>"function"==typeof e?e(a):{...a,...e}),[a,e])}const s={};function r({components:e,children:a,disableParentContext:t}){let r;return r=t?"function"==typeof e?e({}):e||s:g(e),i.createElement(n.Provider,{value:r},a)}}}]);