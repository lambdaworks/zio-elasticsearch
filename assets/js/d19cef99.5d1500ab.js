"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[4793],{1131:(e,s,a)=>{a.r(s),a.d(s,{assets:()=>o,contentTitle:()=>c,default:()=>m,frontMatter:()=>i,metadata:()=>n,toc:()=>l});const n=JSON.parse('{"id":"overview/elastic_query","title":"Overview","description":"In order to execute Elasticsearch query requests, both for searching and deleting by query,","source":"@site/../modules/docs/target/mdoc/overview/elastic_query.md","sourceDirName":"overview","slug":"/overview/elastic_query","permalink":"/zio-elasticsearch/overview/elastic_query","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/elastic_query.md","tags":[],"version":"current","frontMatter":{"id":"elastic_query","title":"Overview"},"sidebar":"docs","previous":{"title":"Usage","permalink":"/zio-elasticsearch/overview/overview_usage"},"next":{"title":"Boolean Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_bool"}}');var r=a(4848),t=a(8453);const i={id:"elastic_query",title:"Overview"},c=void 0,o={},l=[];function d(e){const s={code:"code",p:"p",pre:"pre",...(0,t.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(s.p,{children:["In order to execute Elasticsearch query requests, both for searching and deleting by query,\nyou first must specify the type of the query along with the corresponding parameters for that type.\nQueries are described with the ",(0,r.jsx)(s.code,{children:"ElasticQuery"})," data type, which can be constructed from the DSL methods found under the following import:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,r.jsxs)(s.p,{children:["Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.\nYou can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors.\nAn example with a ",(0,r.jsx)(s.code,{children:"term"})," query is shown below:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticQuery._\n\nfinal case class User(id: Int, name: String)\n\nobject User {\n  implicit val schema: Schema.CaseClass2[Int, String, User] =\n    DeriveSchema.gen[User]\n\n  val (id, name) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nterm("name", "John Doe")\n\n// type-safe method\nterm(field = User.name, value = "John Doe")\n'})}),"\n",(0,r.jsxs)(s.p,{children:["You can also represent a field from nested structures with type-safe query methods, using the ",(0,r.jsx)(s.code,{children:"/"})," operator on accessors:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'import zio._\nimport zio.elasticsearch._\nimport zio.elasticsearch.ElasticQuery._\nimport zio.schema.annotation.fieldName\nimport zio.schema.{DeriveSchema, Schema}\n\nfinal case class Name(\n  @fieldName("first_name")\n  firstName: String,\n  @fieldName("last_name")\n  lastName: String\n)\n\nobject Name {\n  implicit val schema: Schema.CaseClass2[String, String, Name] = DeriveSchema.gen[Name]\n\n  val (firstName, lastName) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nfinal case class User(id: String, name: Name, email: String, age: Int)\n\nobject User {\n  implicit val schema: Schema.CaseClass4[String, Name, String, Int, User] = \n    DeriveSchema.gen[User]\n\n  val (id, name, email, age) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nmatches(field = "name.first_name", value = "John")\n\n// type-safe method\nmatches(field = User.name / Name.firstName, value = "John")\n'})}),"\n",(0,r.jsxs)(s.p,{children:["Accessors also have a ",(0,r.jsx)(s.code,{children:"suffix"})," method, in case you want to use one in queries:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'ElasticQuery.term("email.keyword", "jane.doe@lambdaworks.io")\n\n// type-safe method\nElasticQuery.term(User.email.suffix("keyword"), "jane.doe@lambdaworks.io")\n'})}),"\n",(0,r.jsxs)(s.p,{children:["In case the suffix is ",(0,r.jsx)(s.code,{children:'"keyword"'})," or ",(0,r.jsx)(s.code,{children:'"raw"'})," you can use ",(0,r.jsx)(s.code,{children:"keyword"})," and ",(0,r.jsx)(s.code,{children:"raw"})," methods respectively."]}),"\n",(0,r.jsxs)(s.p,{children:["Now, after describing a query, you can pass it to the ",(0,r.jsx)(s.code,{children:"search"}),"/",(0,r.jsx)(s.code,{children:"deleteByQuery"})," method to obtain the ",(0,r.jsx)(s.code,{children:"ElasticRequest"})," corresponding to that query:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'ElasticRequest.search(IndexName("index"), term(field = "name.first_name.keyword", value = "John"))\n\n// type-safe method\nElasticRequest.search(IndexName("index"), term(field = User.name / Name.firstName.keyword, value = "John"))\n'})})]})}function m(e={}){const{wrapper:s}={...(0,t.R)(),...e.components};return s?(0,r.jsx)(s,{...e,children:(0,r.jsx)(d,{...e})}):d(e)}},8453:(e,s,a)=>{a.d(s,{R:()=>i,x:()=>c});var n=a(6540);const r={},t=n.createContext(r);function i(e){const s=n.useContext(t);return n.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function c(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:i(e.components),n.createElement(t.Provider,{value:s},e.children)}}}]);