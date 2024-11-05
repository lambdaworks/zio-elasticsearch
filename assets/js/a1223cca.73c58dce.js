"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[5315],{4197:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>d,contentTitle:()=>c,default:()=>l,frontMatter:()=>i,metadata:()=>r,toc:()=>o});const r=JSON.parse('{"id":"overview/requests/elastic_request_create","title":"Create Request, CreateWithId Request and CreateOrUpdate Request","description":"The Create, the CreateWithId and the CreateOrUpdate requests add a JSON document to the specified data stream and make it searchable.","source":"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_create.md","sourceDirName":"overview/requests","slug":"/overview/requests/elastic_request_create","permalink":"/zio-elasticsearch/overview/requests/elastic_request_create","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_create.md","tags":[],"version":"current","frontMatter":{"id":"elastic_request_create","title":"Create Request, CreateWithId Request and CreateOrUpdate Request"},"sidebar":"docs","previous":{"title":"Count Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_count"},"next":{"title":"Create Index Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_create_index"}}');var n=s(4848),a=s(8453);const i={id:"elastic_request_create",title:"Create Request, CreateWithId Request and CreateOrUpdate Request"},c=void 0,d={},o=[];function u(e){const t={a:"a",code:"code",li:"li",ol:"ol",p:"p",pre:"pre",...(0,a.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsxs)(t.p,{children:["The ",(0,n.jsx)(t.code,{children:"Create"}),", the ",(0,n.jsx)(t.code,{children:"CreateWithId"})," and the ",(0,n.jsx)(t.code,{children:"CreateOrUpdate"})," requests add a JSON document to the specified data stream and make it searchable."]}),"\n",(0,n.jsx)(t.p,{children:"There are three ways of adding documents to the Elasticsearch index:"}),"\n",(0,n.jsxs)(t.ol,{children:["\n",(0,n.jsxs)(t.li,{children:["By using ",(0,n.jsx)(t.code,{children:"Create"})," request - creates a JSON document without specifying ID (Elasticsearch creates one)"]}),"\n",(0,n.jsxs)(t.li,{children:["By using ",(0,n.jsx)(t.code,{children:"CreateWithId"})," request - creates a JSON document with specified ID"]}),"\n",(0,n.jsxs)(t.li,{children:["By using ",(0,n.jsx)(t.code,{children:"CreateOrUpdate"})," request - creates JSON document with specified ID, or updates the document (if it already exists)"]}),"\n"]}),"\n",(0,n.jsxs)(t.p,{children:["In order to use the ",(0,n.jsx)(t.code,{children:"Create"})," request import the following:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticRequest.CreateRequest\nimport zio.elasticsearch.ElasticRequest.create\n"})}),"\n",(0,n.jsxs)(t.p,{children:["In order to use the ",(0,n.jsx)(t.code,{children:"CreateWithId"})," request import the following:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticRequest.CreateWithIdRequest\nimport zio.elasticsearch.ElasticRequest.create\n"})}),"\n",(0,n.jsxs)(t.p,{children:["In order to use the ",(0,n.jsx)(t.code,{children:"CreateOrUpdate"})," request import the following:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticRequest.CreateOrUpdateRequest\nimport zio.elasticsearch.ElasticRequest.upsert\n"})}),"\n",(0,n.jsx)(t.p,{children:"Except imports, you must specify a document you want to create, with its implicit schema."}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'import zio.schema.Schema\n// example of document\nfinal case class User(id: String, username: String)\n\nval user: User = User(id = "1", username = "johndoe")\n\nimplicit val schema: Schema.CaseClass2[String, String, User] = DeriveSchema.gen[GitHubRepo]\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.code,{children:"Create"})," request using the ",(0,n.jsx)(t.code,{children:"create"})," method this way:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `IndexName`\nimport zio.elasticsearch._\n\nval request: CreateRequest = create(index = IndexName("index"), doc = user)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.code,{children:"CreateWithId"})," request using the ",(0,n.jsx)(t.code,{children:"create"})," method this way:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `DocumentId` also\nimport zio.elasticsearch._ \n\nval request: CreateWithIdRequest = create(index = IndexName("index"), id = DocumentId("documentId"), doc = user)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can create a ",(0,n.jsx)(t.code,{children:"CreateOrUpdate"})," request using the ",(0,n.jsx)(t.code,{children:"upsert"})," method this way:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch._ \n\nval request: CreateOrUpdateRequest = upsert(index = IndexName("index"), id = DocumentId("documentId"), doc = user)\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"refresh"}),", you can use ",(0,n.jsx)(t.code,{children:"refresh"}),", ",(0,n.jsx)(t.code,{children:"refreshFalse"})," or ",(0,n.jsx)(t.code,{children:"refreshTrue"})," method on any of previously mentioned requests:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'val requestWithRefresh: CreateRequest = create(index = IndexName("index"), doc = user).refresh(true)\nval requestWithRefreshFalse: CreateWithIdRequest = create(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshFalse\nval requestWithRefreshTrue: CreateOrUpdateRequest = upsert(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshTrue\n'})}),"\n",(0,n.jsxs)(t.p,{children:["If you want to change the ",(0,n.jsx)(t.code,{children:"routing"}),", you can use the ",(0,n.jsx)(t.code,{children:"routing"})," method on any of previously mentioned requests:"]}),"\n",(0,n.jsx)(t.pre,{children:(0,n.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `Routing` also\nimport zio.elasticsearch._\n\nval request1WithRouting: CreateRequest = create(index = IndexName("index"), doc = user).routing(Routing("routing"))\nval request2WithRouting: CreateWithIdRequest = create(index = IndexName("index"), id = DocumentId("documentId"), doc = user).routing(Routing("routing"))\nval request3WithRouting: CreateOrUpdateRequest = upsert(index = IndexName("index"), id = DocumentId("documentId"), doc = user).routing(Routing("routing"))\n'})}),"\n",(0,n.jsxs)(t.p,{children:["You can find more information about ",(0,n.jsx)(t.code,{children:"Create"}),", ",(0,n.jsx)(t.code,{children:"CreateWithId"}),", ",(0,n.jsx)(t.code,{children:"CreateOrUpdate"})," requests ",(0,n.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-index_.html",children:"here"}),"."]})]})}function l(e={}){const{wrapper:t}={...(0,a.R)(),...e.components};return t?(0,n.jsx)(t,{...e,children:(0,n.jsx)(u,{...e})}):u(e)}},8453:(e,t,s)=>{s.d(t,{R:()=>i,x:()=>c});var r=s(6540);const n={},a=r.createContext(n);function i(e){const t=r.useContext(a);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function c(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(n):e.components||n:i(e.components),r.createElement(a.Provider,{value:t},e.children)}}}]);