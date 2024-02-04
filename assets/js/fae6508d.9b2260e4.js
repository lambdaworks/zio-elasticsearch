"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8192],{6076:(e,s,a)=>{a.r(s),a.d(s,{assets:()=>c,contentTitle:()=>i,default:()=>u,frontMatter:()=>n,metadata:()=>o,toc:()=>d});var r=a(7624),t=a(2172);const n={id:"overview_zio_prelude_schema",title:"Use of ZIO Prelude and Schema"},i=void 0,o={id:"overview/overview_zio_prelude_schema",title:"Use of ZIO Prelude and Schema",description:"ZIO Prelude is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day-to-day problems.",source:"@site/../modules/docs/target/mdoc/overview/zio_prelude_schema.md",sourceDirName:"overview",slug:"/overview/overview_zio_prelude_schema",permalink:"/zio-elasticsearch/overview/overview_zio_prelude_schema",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/zio_prelude_schema.md",tags:[],version:"current",frontMatter:{id:"overview_zio_prelude_schema",title:"Use of ZIO Prelude and Schema"},sidebar:"docs",previous:{title:"Update By Query Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_update_by_query"},next:{title:"Executing Requests",permalink:"/zio-elasticsearch/overview/overview_elastic_executor"}},c={},d=[{value:"Type-safety with ZIO Prelude&#39;s Newtype",id:"type-safety-with-zio-preludes-newtype",level:3},{value:"Usage of ZIO Schema and its accessors for type-safety",id:"usage-of-zio-schema-and-its-accessors-for-type-safety",level:3}];function l(e){const s={a:"a",code:"code",h3:"h3",p:"p",pre:"pre",...(0,t.M)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(s.p,{children:[(0,r.jsx)(s.a,{href:"https://zio.github.io/zio-prelude/docs/overview/overview_index",children:"ZIO Prelude"})," is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day-to-day problems."]}),"\n",(0,r.jsx)(s.h3,{id:"type-safety-with-zio-preludes-newtype",children:"Type-safety with ZIO Prelude's Newtype"}),"\n",(0,r.jsxs)(s.p,{children:["Newtypes provide zero overhead new types and refined new types to allow you to increase the type-safety of your code base with zero overhead and minimal boilerplate.\nThe library uses ZIO Prelude's Newtype for ",(0,r.jsx)(s.code,{children:"IndexName"}),", ",(0,r.jsx)(s.code,{children:"DocumentId"}),", and ",(0,r.jsx)(s.code,{children:"Routing"})," in order to preserve type-safety when these types are being created."]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val indexName: IndexName   = IndexName("index")\nval documentId: DocumentId = DocumentId("documentId")\n'})}),"\n",(0,r.jsx)(s.h3,{id:"usage-of-zio-schema-and-its-accessors-for-type-safety",children:"Usage of ZIO Schema and its accessors for type-safety"}),"\n",(0,r.jsxs)(s.p,{children:[(0,r.jsx)(s.a,{href:"https://zio.dev/zio-schema/",children:"ZIO Schema"})," is a ZIO-based library for modeling the schema of data structures as first-class values.\nTo provide type-safety in your requests ZIO Elasticsearch uses ZIO Schema."]}),"\n",(0,r.jsx)(s.p,{children:"Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.\nYou can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors."}),"\n",(0,r.jsxs)(s.p,{children:["Here is an example of creating a schema for the custom type ",(0,r.jsx)(s.code,{children:"User"})," and using implicit schema to create accessors which results in type-safe query methods.\nYou can also represent a field from nested structures with type-safe query methods, using the ",(0,r.jsx)(s.code,{children:"/"})," operator on accessors, as shown below."]}),"\n",(0,r.jsxs)(s.p,{children:["If your field name has different naming in Elasticsearch's index then you can use the ",(0,r.jsx)(s.code,{children:'@fieldName("...")'})," annotation, in which case the library\nwill use the name from the annotation when making the request."]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'final case class Address(street: String, number: Int)\n\nobject Address {\n  implicit val schema: Schema.CaseClass2[String, Int, Address] =\n    DeriveSchema.gen[Address]\n\n  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nfinal case class User(\n  @fieldName("_id")\n  id: Int,\n  email: String,\n  address: Address\n)\n\nobject User {\n  implicit val schema: Schema.CaseClass3[Int, String, Address, User] =\n    DeriveSchema.gen[User]\n\n  val (id, email, address) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nval query: BoolQuery[User] =\n  ElasticQuery\n    .must(ElasticQuery.range(User.id).gte(7).lt(10))\n    .should(ElasticQuery.startsWith(User.address / Address.street, "ZIO"))\n\nval aggregation: TermsAggregation =\n  ElasticAggregation\n    .termsAggregation("termsAgg", User.address / Address.street)\n\nval request: SearchAndAggregateRequest =\n  ElasticRequest\n    .search(IndexName("index"), query)\n    .aggregate(aggregation)\n\nval result: RIO[Elasticsearch, SearchResult] = Elasticsearch.execute(request)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["Accessors also have a ",(0,r.jsx)(s.code,{children:"suffix"})," method, in case you want to use one in queries:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'ElasticQuery.term("email.keyword", "jane.doe@lambdaworks.io")\n\n// type-safe method\nElasticQuery.term(User.email.suffix("keyword"), "jane.doe@lambdaworks.io")\n'})}),"\n",(0,r.jsxs)(s.p,{children:["In case the suffix is ",(0,r.jsx)(s.code,{children:'"keyword"'})," or ",(0,r.jsx)(s.code,{children:'"raw"'})," you can use ",(0,r.jsx)(s.code,{children:"keyword"})," and ",(0,r.jsx)(s.code,{children:"raw"})," methods respectively:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'ElasticQuery.term(User.email.keyword, "jane.doe@lambdaworks.io")\nElasticQuery.term(User.email.raw, "jane.doe@lambdaworks.io")\n'})})]})}function u(e={}){const{wrapper:s}={...(0,t.M)(),...e.components};return s?(0,r.jsx)(s,{...e,children:(0,r.jsx)(l,{...e})}):l(e)}},2172:(e,s,a)=>{a.d(s,{I:()=>o,M:()=>i});var r=a(1504);const t={},n=r.createContext(t);function i(e){const s=r.useContext(n);return r.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function o(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:i(e.components),r.createElement(n.Provider,{value:s},e.children)}}}]);