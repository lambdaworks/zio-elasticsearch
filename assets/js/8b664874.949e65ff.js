"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[168],{5953:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>a,default:()=>u,frontMatter:()=>o,metadata:()=>i,toc:()=>l});var n=r(4848),s=r(8453);const o={id:"overview_fluent_api",title:"Fluent API"},a=void 0,i={id:"overview/overview_fluent_api",title:"Fluent API",description:"Both Elastic requests and queries offer a fluent API so that we could provide optional parameters in chained method calls for each request or query.",source:"@site/../modules/docs/target/mdoc/overview/fluent_api.md",sourceDirName:"overview",slug:"/overview/overview_fluent_api",permalink:"/zio-elasticsearch/overview/overview_fluent_api",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/fluent_api.md",tags:[],version:"current",frontMatter:{id:"overview_fluent_api",title:"Fluent API"},sidebar:"docs",previous:{title:"Executing Requests",permalink:"/zio-elasticsearch/overview/overview_elastic_executor"},next:{title:"Bulkable",permalink:"/zio-elasticsearch/overview/overview_bulkable"}},c={},l=[];function d(e){const t={code:"code",p:"p",pre:"pre",...(0,s.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(t.p,{children:["Both Elastic requests and queries offer a fluent API so that we could provide optional parameters in chained method calls for each request or query.\nIf you are creating a ",(0,n.jsx)(t.code,{children:"Bool"})," query that can possibly contain ",(0,n.jsx)(t.code,{children:"must"}),", ",(0,n.jsx)(t.code,{children:"mustNot"}),", ",(0,n.jsx)(t.code,{children:"should"}),", and ",(0,n.jsx)(t.code,{children:"filter"})," queries, you can just use one of the methods from the ",(0,n.jsx)(t.code,{children:"ElasticQuery"})," object to create any of them and then just fluently chain any other to the original one."]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'ElasticQuery.must(ElasticQuery.range("version").gte(7).lt(10)).should(ElasticQuery.startsWith("name", "ZIO"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["And if we wanted to specify lower and upper bounds for a ",(0,n.jsx)(t.code,{children:"range"})," query:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:"ElasticQuery.range(User.age).gte(18).lt(100)\n"})}),"\n",(0,n.jsxs)(t.p,{children:["Fluent API is also supported for parameters like ",(0,n.jsx)(t.code,{children:"routing"})," and ",(0,n.jsx)(t.code,{children:"refresh"}),", for example, if we wanted to add routing and refresh parameters to a ",(0,n.jsx)(t.code,{children:"deleteById"})," request:\nMethods ",(0,n.jsx)(t.code,{children:"refreshTrue"})," and ",(0,n.jsx)(t.code,{children:"refreshFalse"})," are just shortcuts for using ",(0,n.jsx)(t.code,{children:"refresh(true)"})," or ",(0,n.jsx)(t.code,{children:"refresh(false)"}),"."]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'ElasticRequest.deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue\n'})}),"\n",(0,n.jsxs)(t.p,{children:["When creating aggregations we can also use ",(0,n.jsx)(t.code,{children:"withAgg"})," method to add another aggregation and return the ",(0,n.jsx)(t.code,{children:"MultipleAggregations"})," type that contains both aggregations."]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'ElasticAggregation.termsAggregation(name = "firstAggregation", field = "name")\n  .withAgg(ElasticAggregation.termsAggregation(name = "secondAggregation", field = "age"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["Creating ",(0,n.jsx)(t.code,{children:"sort"})," also supports fluent API, as it is shown in the code below:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'ElasticSort.sortBy("age").mode(SortMode.Avg)\nElasticSort.sortBy("first_name").missing(Missing.First)\nElasticSort.sortBy("created_at").format("strict_date_optional_time_nanos")\n'})})]})}function u(e={}){const{wrapper:t}={...(0,s.R)(),...e.components};return t?(0,n.jsx)(t,{...e,children:(0,n.jsx)(d,{...e})}):d(e)}},8453:(e,t,r)=>{r.d(t,{R:()=>a,x:()=>i});var n=r(6540);const s={},o=n.createContext(s);function a(e){const t=n.useContext(o);return n.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function i(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:a(e.components),n.createElement(o.Provider,{value:t},e.children)}}}]);