"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[2584],{5148:(e,t,a)=>{a.r(t),a.d(t,{assets:()=>n,contentTitle:()=>c,default:()=>u,frontMatter:()=>g,metadata:()=>o,toc:()=>s});var i=a(7624),r=a(2172);const g={id:"elastic_aggregation_bucket_selector",title:"Bucket Selector Aggregation"},c=void 0,o={id:"overview/aggregations/elastic_aggregation_bucket_selector",title:"Bucket Selector Aggregation",description:"This aggregation is a parent pipeline aggregation which executes a script which determines whether the current bucket will be retained in the parent multi-bucket aggregation.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_bucket_selector.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_bucket_selector",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_bucket_selector",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_bucket_selector.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_bucket_selector",title:"Bucket Selector Aggregation"},sidebar:"docs",previous:{title:"Avg Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_avg"},next:{title:"Bucket Sort Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_bucket_sort"}},n={},s=[];function l(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,r.M)(),e.components);return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.p,{children:"This aggregation is a parent pipeline aggregation which executes a script which determines whether the current bucket will be retained in the parent multi-bucket aggregation."}),"\n",(0,i.jsxs)(t.p,{children:["To create a ",(0,i.jsx)(t.code,{children:"BucketSelector"})," aggregation do the following:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.aggregation.BucketSelectorAggregation\nimport zio.elasticsearch.ElasticAggregation.bucketSelectorAggregation\nimport zio.elasticsearch.script.Script\n\nval aggregation: BucketSelectorAggregation = bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation"))\n'})}),"\n",(0,i.jsxs)(t.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,i.jsx)(t.code,{children:"withAgg"})," method:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation")).withAgg(maxAggregation(name = "maxAggregation", field = Document.doubleField))\n'})}),"\n",(0,i.jsxs)(t.p,{children:["You can find more information about ",(0,i.jsx)(t.code,{children:"BucketSelector"})," aggregation ",(0,i.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-bucket-selector-aggregation.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:t}=Object.assign({},(0,r.M)(),e.components);return t?(0,i.jsx)(t,Object.assign({},e,{children:(0,i.jsx)(l,e)})):l(e)}},2172:(e,t,a)=>{a.d(t,{M:()=>c});var i=a(1504);const r={},g=i.createContext(r);function c(e){const t=i.useContext(g);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}}}]);