"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8853],{8515:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>c,contentTitle:()=>n,default:()=>d,frontMatter:()=>i,metadata:()=>g,toc:()=>o});var a=s(5893),r=s(1151);const i={id:"elastic_request_aggregate",title:"Aggregation Request"},n=void 0,g={id:"overview/requests/elastic_request_aggregate",title:"Aggregation Request",description:"This request is used to create aggregations which summarizes your data as metrics, statistics, or other analytics.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_aggregate.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_aggregate",permalink:"/zio-elasticsearch/overview/requests/elastic_request_aggregate",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_aggregate.md",tags:[],version:"current",frontMatter:{id:"elastic_request_aggregate",title:"Aggregation Request"},sidebar:"docs",previous:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_request"},next:{title:"Bulk Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_bulk"}},c={},o=[];function l(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,r.a)(),e.components);return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsx)(t.p,{children:"This request is used to create aggregations which summarizes your data as metrics, statistics, or other analytics."}),"\n",(0,a.jsxs)(t.p,{children:["To create a ",(0,a.jsx)(t.code,{children:"Aggregate"})," request do the following:"]}),"\n",(0,a.jsx)(t.pre,{children:(0,a.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticRequest.AggregateRequest\nimport zio.elasticsearch.ElasticRequest.aggregate\n// this import is required for using `IndexName`, `IndexPattern` and `MultiIndex`\nimport zio.elasticsearch._\nimport zio.elasticsearch.ElasticAggregation._\n\nval request: AggregateRequest = aggregate(selectors = IndexName("index"), aggregation = maxAggregation(name = "aggregation", field = "intField"))\n'})}),"\n",(0,a.jsxs)(t.p,{children:["If you want to create ",(0,a.jsx)(t.code,{children:"Aggregate"})," request with ",(0,a.jsx)(t.code,{children:"IndexPattern"}),", do the following:"]}),"\n",(0,a.jsx)(t.pre,{children:(0,a.jsx)(t.code,{className:"language-scala",children:'val requestWithIndexPattern: AggregateRequest = aggregate(selectors = IndexPattern("index*"), aggregation = maxAggregation(name = "aggregation", field = "intField"))\n'})}),"\n",(0,a.jsxs)(t.p,{children:["If you want to create ",(0,a.jsx)(t.code,{children:"Aggregate"})," request with ",(0,a.jsx)(t.code,{children:"MultiIndex"}),", do the following:"]}),"\n",(0,a.jsx)(t.pre,{children:(0,a.jsx)(t.code,{className:"language-scala",children:'val requestWithMultiIndex: AggregateRequest = aggregate(selectors = MultiIndex.names(IndexName("index1"), IndexName("index2")), aggregation = maxAggregation(name = "aggregation", field = "intField"))\n'})}),"\n",(0,a.jsxs)(t.p,{children:["You can find more information about ",(0,a.jsx)(t.code,{children:"Aggregate"})," request ",(0,a.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:t}=Object.assign({},(0,r.a)(),e.components);return t?(0,a.jsx)(t,Object.assign({},e,{children:(0,a.jsx)(l,e)})):l(e)}},1151:(e,t,s)=>{s.d(t,{a:()=>n});var a=s(7294);const r={},i=a.createContext(r);function n(e){const t=a.useContext(i);return a.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}}}]);