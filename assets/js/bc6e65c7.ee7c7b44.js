"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[1678],{6771:(e,a,t)=>{t.r(a),t.d(a,{assets:()=>c,contentTitle:()=>r,default:()=>d,frontMatter:()=>s,metadata:()=>o,toc:()=>g});var n=t(5893),i=t(1151);const s={id:"elastic_aggregation",title:"Overview"},r=void 0,o={unversionedId:"overview/elastic_aggregation",id:"overview/elastic_aggregation",title:"Overview",description:"In order to execute Elasticsearch aggregation requests, you first must specify the type of the aggregation along with the corresponding parameters for that type.",source:"@site/../modules/docs/target/mdoc/overview/elastic_aggregation.md",sourceDirName:"overview",slug:"/overview/elastic_aggregation",permalink:"/zio-elasticsearch/overview/elastic_aggregation",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/elastic_aggregation.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation",title:"Overview"},sidebar:"docs",previous:{title:"IDs Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_ids"},next:{title:"Avg Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_avg"}},c={},g=[];function l(e){const a=Object.assign({p:"p",code:"code",pre:"pre"},(0,i.ah)(),e.components);return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(a.p,{children:["In order to execute Elasticsearch aggregation requests, you first must specify the type of the aggregation along with the corresponding parameters for that type.\nAggregations are described with the ",(0,n.jsx)(a.code,{children:"ElasticAggregation"})," data type, which can be constructed from the DSL methods found under the following import:"]}),"\n",(0,n.jsx)(a.pre,{children:(0,n.jsx)(a.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticAggregation._\n"})}),"\n",(0,n.jsxs)(a.p,{children:["Aggregation DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.\nYou can pass field names simply as strings, or you can use the type-safe aggregation methods that make use of ZIO Schema's accessors.\nAn example with a ",(0,n.jsx)(a.code,{children:"max"})," aggregation is shown below:"]}),"\n",(0,n.jsx)(a.pre,{children:(0,n.jsx)(a.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticAggregation._\n\nfinal case class User(id: Int, name: String, age: Int)\n\nobject User {\n  implicit val schema: Schema.CaseClass3[Int, String, Int, User] =\n    DeriveSchema.gen[User]\n\n  val (id, name, age) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nmaxAggregation(name = "maxAggregation", field = "age")\n\n// type-safe method\nmaxAggregation(name = "maxAggregation", field = User.age)\n'})}),"\n",(0,n.jsxs)(a.p,{children:["You can also represent a field from nested structures with type-safe aggregation methods, using the ",(0,n.jsx)(a.code,{children:"/"})," operator on accessors:"]}),"\n",(0,n.jsx)(a.pre,{children:(0,n.jsx)(a.code,{className:"language-scala",children:'import zio._\nimport zio.elasticsearch._\nimport zio.elasticsearch.ElasticAggregation._\nimport zio.schema.annotation.fieldName\nimport zio.schema.{DeriveSchema, Schema}\n\nfinal case class Name(\n  @fieldName("first_name")\n  firstName: String,\n  @fieldName("last_name")\n  lastName: String\n)\n\nobject Name {\n  implicit val schema: Schema.CaseClass2[String, String, Name] = DeriveSchema.gen[Name]\n\n  val (firstName, lastName) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\nfinal case class User(id: String, name: Name, email: String, age: Int)\n\nobject User {\n  implicit val schema: Schema.CaseClass4[String, Name, String, Int, User] = \n    DeriveSchema.gen[User]\n\n  val (id, name, email, age) = schema.makeAccessors(FieldAccessorBuilder)\n}\n\ntermsAggregation(name = "termsAggregation", field = "name.first_name")\n\n// type-safe method\ntermsAggregation(name = "termsAggregation", field = User.name / Name.firstName)\n'})}),"\n",(0,n.jsxs)(a.p,{children:["Accessors also have a ",(0,n.jsx)(a.code,{children:"suffix"})," method, in case you want to use one in aggregations:"]}),"\n",(0,n.jsx)(a.pre,{children:(0,n.jsx)(a.code,{className:"language-scala",children:'ElasticAggregation.cardinality(name = "cardinalityAggregation", field = "email.keyword")\n\n// type-safe method\nElasticAggregation.cardinality(name = "cardinalityAggregation", field = User.email.suffix("keyword"))\n'})}),"\n",(0,n.jsxs)(a.p,{children:["In case the suffix is ",(0,n.jsx)(a.code,{children:'"keyword"'})," or ",(0,n.jsx)(a.code,{children:'"raw"'})," you can use ",(0,n.jsx)(a.code,{children:"keyword"})," and ",(0,n.jsx)(a.code,{children:"raw"})," methods respectively."]}),"\n",(0,n.jsxs)(a.p,{children:["Now, after describing an aggregation, you can pass it to the ",(0,n.jsx)(a.code,{children:"aggregate"}),"/",(0,n.jsx)(a.code,{children:"search"})," method to obtain the ",(0,n.jsx)(a.code,{children:"ElasticRequest"})," corresponding to that aggregation:"]}),"\n",(0,n.jsx)(a.pre,{children:(0,n.jsx)(a.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticAggregation._\nimport zio.elasticsearch.ElasticQuery._\n\nElasticRequest.aggregate(index = IndexName("index"), aggregation = termsAggregation(name = "termsAggregation", field = "name.first_name.keyword"))\nElasticRequest.search(IndexName("index"), query = matchAll, aggregation = termsAggregation(name = "termsAggregation", field = "name.first_name.keyword"))\n\n// type-safe methods\nElasticRequest.aggregate(index = IndexName("index"), aggregation = termsAggregation(name = "termsAggregation", field = User.name / Name.firstName.keyword))\nElasticRequest.search(index = IndexName("index"), query = matchAll, aggregation = termsAggregation(name = "termsAggregation", field = User.name / Name.firstName.keyword))\n\n'})})]})}const d=function(e={}){const{wrapper:a}=Object.assign({},(0,i.ah)(),e.components);return a?(0,n.jsx)(a,Object.assign({},e,{children:(0,n.jsx)(l,e)})):l(e)}},1151:(e,a,t)=>{t.d(a,{Zo:()=>o,ah:()=>s});var n=t(7294);const i=n.createContext({});function s(e){const a=n.useContext(i);return n.useMemo((()=>"function"==typeof e?e(a):{...a,...e}),[a,e])}const r={};function o({components:e,children:a,disableParentContext:t}){let o;return o=t?"function"==typeof e?e({}):e||r:s(e),n.createElement(i.Provider,{value:o},a)}}}]);