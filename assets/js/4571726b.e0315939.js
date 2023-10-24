"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[6503],{4262:(e,s,r)=>{r.r(s),r.d(s,{assets:()=>o,contentTitle:()=>i,default:()=>h,frontMatter:()=>n,metadata:()=>c,toc:()=>l});var t=r(5893),a=r(1151);const n={id:"overview_streaming",title:"Streaming"},i=void 0,c={id:"overview/overview_streaming",title:"Streaming",description:"ZIO Elasticsearch offers a few different API methods for creating ZIO streams out of search requests.",source:"@site/../modules/docs/target/mdoc/overview/streaming.md",sourceDirName:"overview",slug:"/overview/overview_streaming",permalink:"/zio-elasticsearch/overview/overview_streaming",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/streaming.md",tags:[],version:"current",frontMatter:{id:"overview_streaming",title:"Streaming"},sidebar:"docs",previous:{title:"Bulkable",permalink:"/zio-elasticsearch/overview/overview_bulkable"}},o={},l=[];function d(e){const s=Object.assign({p:"p",code:"code",a:"a",pre:"pre"},(0,a.a)(),e.components);return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsxs)(s.p,{children:["ZIO Elasticsearch offers a few different API methods for creating ZIO streams out of search requests.\nThe library offers two different streaming modes relying on two different ways of retrieving paged results from Elasticsearch: ",(0,t.jsx)(s.code,{children:"scroll"})," and ",(0,t.jsx)(s.code,{children:"search_after"}),".\nWhen using the ",(0,t.jsx)(s.code,{children:"Elasticsearch.stream(...)"})," method you can provide your own configuration by creating the ",(0,t.jsx)(s.code,{children:"StreamConfig"})," object and providing\nit as a parameter for the method next to ",(0,t.jsx)(s.code,{children:"SearchRequest"}),". If you choose not to provide ",(0,t.jsx)(s.code,{children:"StreamConfig"})," then ",(0,t.jsx)(s.code,{children:"StreamConfig.Default"})," will be used."]}),"\n",(0,t.jsxs)(s.p,{children:[(0,t.jsx)(s.code,{children:"StreamConfig.Default"})," uses Scroll API by default (which is recommended for queries that have under 10,000 results), has keep_alive parameter set for ",(0,t.jsx)(s.code,{children:"1m"})," and\nuses Elasticsearch default page size."]}),"\n",(0,t.jsxs)(s.p,{children:[(0,t.jsx)(s.code,{children:"StreamConfig"})," also makes use of our fluent API, so you can use methods ",(0,t.jsx)(s.code,{children:"withPageSize"})," (used to determine how many documents to return per page)\nand ",(0,t.jsx)(s.code,{children:"keepAliveFor"})," (used to tell Elasticsearch how long should search be kept alive after every pagination using ",(0,t.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/8.6/api-conventions.html#time-units",children:"Time units"}),").\n",(0,t.jsx)(s.code,{children:"StreamConfig"})," has two predefined values for ",(0,t.jsx)(s.code,{children:"StreamConfig.Scroll"})," that uses ElasticSearch Scroll API and ",(0,t.jsx)(s.code,{children:"StreamConfig.SearchAfter"})," that uses Search After API with Point In Time."]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'StreamConfig(searchAfter = false, keepAlive = "5m", pageSize = Some(100))\n'})}),"\n",(0,t.jsxs)(s.p,{children:["When using the ",(0,t.jsx)(s.code,{children:"streamAs[A]"})," method, results are parsed into the desired type ",(0,t.jsx)(s.code,{children:"A"}),", relying on an implicit schema for ",(0,t.jsx)(s.code,{children:"A"}),"."]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'final case class User(id: Int, name: String)\n\nobject User {\n  implicit val schema: Schema.CaseClass2[Int, String, User] =\n    DeriveSchema.gen[User]\n\n  val (id, name) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nval request: SearchRequest =\n  ElasticRequest.search(IndexName("index"), ElasticQuery.range(User.id).gte(5))\n\nval searchAfterStream: ZStream[Elasticsearch, Throwable, User] =\n  Elasticsearch.streamAs[User](request, StreamConfig.SearchAfter)\n'})}),"\n",(0,t.jsxs)(s.p,{children:["Besides the type-safe ",(0,t.jsx)(s.code,{children:"streamAs[A]"})," method, the library offers a basic ",(0,t.jsx)(s.code,{children:"stream"})," method, which result will be a stream of type ",(0,t.jsx)(s.code,{children:"Item"})," which contains a ",(0,t.jsx)(s.code,{children:"raw"})," field that represents a document using the ",(0,t.jsx)(s.code,{children:"Json"})," type from the ZIO JSON library."]}),"\n",(0,t.jsx)(s.pre,{children:(0,t.jsx)(s.code,{className:"language-scala",children:'val request: SearchRequest =\n  ElasticRequest.search(IndexName("index"), ElasticQuery.range("id").gte(5))\n\nval defaultStream: ZStream[Elasticsearch, Throwable, Item] =\n  Elasticsearch.stream(request)\n\nval scrollStream: ZStream[Elasticsearch, Throwable, Item]  =\n  Elasticsearch.stream(request, StreamConfig.Scroll)\n'})})]})}const h=function(e={}){const{wrapper:s}=Object.assign({},(0,a.a)(),e.components);return s?(0,t.jsx)(s,Object.assign({},e,{children:(0,t.jsx)(d,e)})):d(e)}},1151:(e,s,r)=>{r.d(s,{a:()=>i});var t=r(7294);const a={},n=t.createContext(a);function i(e){const s=t.useContext(n);return t.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}}}]);