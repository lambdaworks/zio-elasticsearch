"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5128],{3200:(e,s,r)=>{r.r(s),r.d(s,{assets:()=>c,contentTitle:()=>i,default:()=>u,frontMatter:()=>n,metadata:()=>o,toc:()=>d});var a=r(5893),t=r(3905);const n={id:"overview_zio_prelude_schema",title:"Use of ZIO Prelude and Schema"},i=void 0,o={unversionedId:"overview/overview_zio_prelude_schema",id:"overview/overview_zio_prelude_schema",title:"Use of ZIO Prelude and Schema",description:"ZIO Prelude is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day-to-day problems.",source:"@site/../modules/docs/target/mdoc/overview/zio_prelude_schema.md",sourceDirName:"overview",slug:"/overview/overview_zio_prelude_schema",permalink:"/zio-elasticsearch/overview/overview_zio_prelude_schema",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/zio_prelude_schema.md",tags:[],version:"current",frontMatter:{id:"overview_zio_prelude_schema",title:"Use of ZIO Prelude and Schema"},sidebar:"docs",previous:{title:"Update By Query Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_update_by_query"},next:{title:"Executing Requests",permalink:"/zio-elasticsearch/overview/overview_elastic_executor"}},c={},d=[{value:"Type-safety with ZIO Prelude&#39;s Newtype",id:"type-safety-with-zio-preludes-newtype",level:3},{value:"Usage of ZIO Schema and its accessors for type-safety",id:"usage-of-zio-schema-and-its-accessors-for-type-safety",level:3}];function l(e){const s=Object.assign({p:"p",a:"a",h3:"h3",code:"code",pre:"pre"},(0,t.ah)(),e.components);return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsxs)(s.p,{children:[(0,a.jsx)(s.a,{href:"https://zio.github.io/zio-prelude/docs/overview/overview_index",children:"ZIO Prelude"})," is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day-to-day problems."]}),"\n",(0,a.jsx)(s.h3,{id:"type-safety-with-zio-preludes-newtype",children:"Type-safety with ZIO Prelude's Newtype"}),"\n",(0,a.jsxs)(s.p,{children:["Newtypes provide zero overhead new types and refined new types to allow you to increase the type-safety of your code base with zero overhead and minimal boilerplate.\nThe library uses ZIO Prelude's Newtype for ",(0,a.jsx)(s.code,{children:"IndexName"}),", ",(0,a.jsx)(s.code,{children:"DocumentId"}),", and ",(0,a.jsx)(s.code,{children:"Routing"})," in order to preserve type-safety when these types are being created."]}),"\n",(0,a.jsx)(s.pre,{children:(0,a.jsx)(s.code,{className:"language-scala",children:'val indexName: IndexName   = IndexName("index")\nval documentId: DocumentId = DocumentId("documentId")\n'})}),"\n",(0,a.jsx)(s.h3,{id:"usage-of-zio-schema-and-its-accessors-for-type-safety",children:"Usage of ZIO Schema and its accessors for type-safety"}),"\n",(0,a.jsxs)(s.p,{children:[(0,a.jsx)(s.a,{href:"https://zio.dev/zio-schema/",children:"ZIO Schema"})," is a ZIO-based library for modeling the schema of data structures as first-class values.\nTo provide type-safety in your requests ZIO Elasticsearch uses ZIO Schema."]}),"\n",(0,a.jsx)(s.p,{children:"Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.\nYou can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors."}),"\n",(0,a.jsxs)(s.p,{children:["Here is an example of creating a schema for the custom type ",(0,a.jsx)(s.code,{children:"User"})," and using implicit schema to create accessors which results in type-safe query methods.\nYou can also represent a field from nested structures with type-safe query methods, using the ",(0,a.jsx)(s.code,{children:"/"})," operator on accessors, as shown below."]}),"\n",(0,a.jsxs)(s.p,{children:["If your field name has different naming in Elasticsearch's index then you can use the ",(0,a.jsx)(s.code,{children:'@fieldName("...")'})," annotation, in which case the library\nwill use the name from the annotation when making the request."]}),"\n",(0,a.jsx)(s.pre,{children:(0,a.jsx)(s.code,{className:"language-scala",children:'final case class Address(street: String, number: Int)\n\nobject Address {\n  implicit val schema: Schema.CaseClass2[String, Int, Address] =\n    DeriveSchema.gen[Address]\n\n  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nfinal case class User(\n  @fieldName("_id")\n  id: Int,\n  email: String,\n  address: Address\n)\n\nobject User {\n  implicit val schema: Schema.CaseClass3[Int, String, Address, User] =\n    DeriveSchema.gen[User]\n\n  val (id, email, address) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nval query: BoolQuery[User] =\n  ElasticQuery\n    .must(ElasticQuery.range(User.id).gte(7).lt(10))\n    .should(ElasticQuery.startsWith(User.address / Address.street, "ZIO"))\n\nval aggregation: TermsAggregation =\n  ElasticAggregation\n    .termsAggregation("termsAgg", User.address / Address.street)\n\nval request: SearchAndAggregateRequest =\n  ElasticRequest\n    .search(IndexName("index"), query)\n    .aggregate(aggregation)\n\nval result: RIO[Elasticsearch, SearchResult] = Elasticsearch.execute(request)\n'})}),"\n",(0,a.jsxs)(s.p,{children:["Accessors also have a ",(0,a.jsx)(s.code,{children:"suffix"})," method, in case you want to use one in queries:"]}),"\n",(0,a.jsx)(s.pre,{children:(0,a.jsx)(s.code,{className:"language-scala",children:'ElasticQuery.term("email.keyword", "jane.doe@lambdaworks.io")\n\n// type-safe method\nElasticQuery.term(User.email.suffix("keyword"), "jane.doe@lambdaworks.io")\n'})}),"\n",(0,a.jsxs)(s.p,{children:["In case the suffix is ",(0,a.jsx)(s.code,{children:'"keyword"'})," or ",(0,a.jsx)(s.code,{children:'"raw"'})," you can use ",(0,a.jsx)(s.code,{children:"keyword"})," and ",(0,a.jsx)(s.code,{children:"raw"})," methods respectively:"]}),"\n",(0,a.jsx)(s.pre,{children:(0,a.jsx)(s.code,{className:"language-scala",children:'ElasticQuery.term(User.email.keyword, "jane.doe@lambdaworks.io")\nElasticQuery.term(User.email.raw, "jane.doe@lambdaworks.io")\n'})})]})}const u=function(e={}){const{wrapper:s}=Object.assign({},(0,t.ah)(),e.components);return s?(0,a.jsx)(s,Object.assign({},e,{children:(0,a.jsx)(l,e)})):l(e)}},3905:(e,s,r)=>{r.d(s,{ah:()=>d});var a=r(7294);function t(e,s,r){return s in e?Object.defineProperty(e,s,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[s]=r,e}function n(e,s){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);s&&(a=a.filter((function(s){return Object.getOwnPropertyDescriptor(e,s).enumerable}))),r.push.apply(r,a)}return r}function i(e){for(var s=1;s<arguments.length;s++){var r=null!=arguments[s]?arguments[s]:{};s%2?n(Object(r),!0).forEach((function(s){t(e,s,r[s])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):n(Object(r)).forEach((function(s){Object.defineProperty(e,s,Object.getOwnPropertyDescriptor(r,s))}))}return e}function o(e,s){if(null==e)return{};var r,a,t=function(e,s){if(null==e)return{};var r,a,t={},n=Object.keys(e);for(a=0;a<n.length;a++)r=n[a],s.indexOf(r)>=0||(t[r]=e[r]);return t}(e,s);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);for(a=0;a<n.length;a++)r=n[a],s.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(t[r]=e[r])}return t}var c=a.createContext({}),d=function(e){var s=a.useContext(c),r=s;return e&&(r="function"==typeof e?e(s):i(i({},s),e)),r},l={inlineCode:"code",wrapper:function(e){var s=e.children;return a.createElement(a.Fragment,{},s)}},u=a.forwardRef((function(e,s){var r=e.components,t=e.mdxType,n=e.originalType,c=e.parentName,u=o(e,["components","mdxType","originalType","parentName"]),h=d(r),m=t,p=h["".concat(c,".").concat(m)]||h[m]||l[m]||n;return r?a.createElement(p,i(i({ref:s},u),{},{components:r})):a.createElement(p,i({ref:s},u))}));u.displayName="MDXCreateElement"}}]);