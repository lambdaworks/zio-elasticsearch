"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9489],{497:(e,r,s)=>{s.r(r),s.d(r,{assets:()=>o,contentTitle:()=>i,default:()=>h,frontMatter:()=>n,metadata:()=>c,toc:()=>l});var t=s(4848),a=s(8453);const n={id:"overview_streaming",title:"Streaming"},i=void 0,c={id:"overview/overview_streaming",title:"Streaming",description:"ZIO Elasticsearch offers a few different API methods for creating ZIO streams out of search requests.",source:"@site/../modules/docs/target/mdoc/overview/streaming.md",sourceDirName:"overview",slug:"/overview/overview_streaming",permalink:"/zio-elasticsearch/overview/overview_streaming",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/streaming.md",tags:[],version:"current",frontMatter:{id:"overview_streaming",title:"Streaming"},sidebar:"docs",previous:{title:"Bulkable",permalink:"/zio-elasticsearch/overview/overview_bulkable"}},o={},l=[];function d(e){const r={a:"a",code:"code",p:"p",pre:"pre",...(0,a.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(r.p,{children:["ZIO Elasticsearch offers a few different API methods for creating ZIO streams out of search requests.\nThe library offers two different streaming modes relying on two different ways of retrieving paged results from Elasticsearch: ",(0,t.jsx)(r.code,{children:"scroll"})," and ",(0,t.jsx)(r.code,{children:"search_after"}),".\nWhen using the ",(0,t.jsx)(r.code,{children:"Elasticsearch.stream(...)"})," method you can provide your own configuration by creating the ",(0,t.jsx)(r.code,{children:"StreamConfig"})," object and providing\nit as a parameter for the method next to ",(0,t.jsx)(r.code,{children:"SearchRequest"}),". If you choose not to provide ",(0,t.jsx)(r.code,{children:"StreamConfig"})," then ",(0,t.jsx)(r.code,{children:"StreamConfig.Default"})," will be used."]}),"\n",(0,t.jsxs)(r.p,{children:[(0,t.jsx)(r.code,{children:"StreamConfig.Default"})," uses Scroll API by default (which is recommended for queries that have under 10,000 results), has keep_alive parameter set for ",(0,t.jsx)(r.code,{children:"1m"})," and\nuses Elasticsearch default page size."]}),"\n",(0,t.jsxs)(r.p,{children:[(0,t.jsx)(r.code,{children:"StreamConfig"})," also makes use of our fluent API, so you can use methods ",(0,t.jsx)(r.code,{children:"withPageSize"})," (used to determine how many documents to return per page)\nand ",(0,t.jsx)(r.code,{children:"keepAliveFor"})," (used to tell Elasticsearch how long should search be kept alive after every pagination using ",(0,t.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/8.6/api-conventions.html#time-units",children:"Time units"}),").\n",(0,t.jsx)(r.code,{children:"StreamConfig"})," has two predefined values for ",(0,t.jsx)(r.code,{children:"StreamConfig.Scroll"})," that uses ElasticSearch Scroll API and ",(0,t.jsx)(r.code,{children:"StreamConfig.SearchAfter"})," that uses Search After API with Point In Time."]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'StreamConfig(searchAfter = false, keepAlive = "5m", pageSize = Some(100))\n'})}),"\n",(0,t.jsxs)(r.p,{children:["When using the ",(0,t.jsx)(r.code,{children:"streamAs[A]"})," method, results are parsed into the desired type ",(0,t.jsx)(r.code,{children:"A"}),", relying on an implicit schema for ",(0,t.jsx)(r.code,{children:"A"}),"."]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'final case class User(id: Int, name: String)\n\nobject User {\n  implicit val schema: Schema.CaseClass2[Int, String, User] =\n    DeriveSchema.gen[User]\n\n  val (id, name) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nval request: SearchRequest =\n  ElasticRequest.search(IndexName("index"), ElasticQuery.range(User.id).gte(5))\n\nval searchAfterStream: ZStream[Elasticsearch, Throwable, User] =\n  Elasticsearch.streamAs[User](request, StreamConfig.SearchAfter)\n'})}),"\n",(0,t.jsxs)(r.p,{children:["Besides the type-safe ",(0,t.jsx)(r.code,{children:"streamAs[A]"})," method, the library offers a basic ",(0,t.jsx)(r.code,{children:"stream"})," method, which result will be a stream of type ",(0,t.jsx)(r.code,{children:"Item"})," which contains a ",(0,t.jsx)(r.code,{children:"raw"})," field that represents a document using the ",(0,t.jsx)(r.code,{children:"Json"})," type from the ZIO JSON library."]}),"\n",(0,t.jsx)(r.pre,{children:(0,t.jsx)(r.code,{className:"language-scala",children:'val request: SearchRequest =\n  ElasticRequest.search(IndexName("index"), ElasticQuery.range("id").gte(5))\n\nval defaultStream: ZStream[Elasticsearch, Throwable, Item] =\n  Elasticsearch.stream(request)\n\nval scrollStream: ZStream[Elasticsearch, Throwable, Item]  =\n  Elasticsearch.stream(request, StreamConfig.Scroll)\n'})})]})}function h(e={}){const{wrapper:r}={...(0,a.R)(),...e.components};return r?(0,t.jsx)(r,{...e,children:(0,t.jsx)(d,{...e})}):d(e)}},8453:(e,r,s)=>{s.d(r,{R:()=>i,x:()=>c});var t=s(6540);const a={},n=t.createContext(a);function i(e){const r=t.useContext(n);return t.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}function c(e){let r;return r=e.disableParentContext?"function"==typeof e.components?e.components(a):e.components||a:i(e.components),t.createElement(n.Provider,{value:r},e.children)}}}]);