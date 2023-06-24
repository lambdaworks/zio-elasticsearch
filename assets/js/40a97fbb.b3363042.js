"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8853],{8515:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>c,contentTitle:()=>n,default:()=>l,frontMatter:()=>i,metadata:()=>o,toc:()=>g});var r=s(5893),a=s(1151);const i={id:"elastic_request_aggregate",title:"Aggregation Request"},n=void 0,o={unversionedId:"overview/requests/elastic_request_aggregate",id:"overview/requests/elastic_request_aggregate",title:"Aggregation Request",description:"This request is used to create aggregations which summarizes your data as metrics, statistics, or other analytics.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_aggregate.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_aggregate",permalink:"/zio-elasticsearch/overview/requests/elastic_request_aggregate",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_aggregate.md",tags:[],version:"current",frontMatter:{id:"elastic_request_aggregate",title:"Aggregation Request"},sidebar:"docs",previous:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_request"},next:{title:"Bulk Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_bulk"}},c={},g=[];function u(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,a.ah)(),e.components);return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.p,{children:"This request is used to create aggregations which summarizes your data as metrics, statistics, or other analytics."}),"\n",(0,r.jsxs)(t.p,{children:["To create a ",(0,r.jsx)(t.code,{children:"Aggregate"})," request do the following:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticRequest.AggregateRequest\nimport zio.elasticsearch.ElasticRequest.aggregate\n// this import is required for using `IndexName`\nimport zio.elasticsearch._\nimport zio.elasticsearch.ElasticAggregation._\n\nval request: AggregateRequest = aggregate(name = IndexName("index"), aggregation = maxAggregation(name = "aggregation", field = "intField"))\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can find more information about ",(0,r.jsx)(t.code,{children:"Aggregate"})," request ",(0,r.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations.html",children:"here"}),"."]})]})}const l=function(e={}){const{wrapper:t}=Object.assign({},(0,a.ah)(),e.components);return t?(0,r.jsx)(t,Object.assign({},e,{children:(0,r.jsx)(u,e)})):u(e)}},1151:(e,t,s)=>{s.d(t,{Zo:()=>o,ah:()=>i});var r=s(7294);const a=r.createContext({});function i(e){const t=r.useContext(a);return r.useMemo((()=>"function"==typeof e?e(t):{...t,...e}),[t,e])}const n={};function o({components:e,children:t,disableParentContext:s}){let o;return o=s?"function"==typeof e?e({}):e||n:i(e),r.createElement(a.Provider,{value:o},t)}}}]);