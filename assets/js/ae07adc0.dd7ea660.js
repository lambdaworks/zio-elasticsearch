"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8860],{1100:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>d,contentTitle:()=>a,default:()=>l,frontMatter:()=>i,metadata:()=>c,toc:()=>o});var r=s(5893),n=s(1151);const i={id:"elastic_request_update",title:"Update Request"},a=void 0,c={id:"overview/requests/elastic_request_update",title:"Update Request",description:"This request is used for updating a document either with script or with other document as parameter.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_update.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_update",permalink:"/zio-elasticsearch/overview/requests/elastic_request_update",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_update.md",tags:[],version:"current",frontMatter:{id:"elastic_request_update",title:"Update Request"},sidebar:"docs",previous:{title:"Search Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_search"},next:{title:"Update By Query Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_update_by_query"}},d={},o=[];function u(e){const t=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.a)(),e.components);return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.p,{children:"This request is used for updating a document either with script or with other document as parameter."}),"\n",(0,r.jsxs)(t.p,{children:["In order to use the ",(0,r.jsx)(t.code,{children:"Update"})," request import the following:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticRequest.UpdateRequest\nimport zio.elasticsearch.ElasticRequest._\n"})}),"\n",(0,r.jsxs)(t.p,{children:["You can create a ",(0,r.jsx)(t.code,{children:"Update"})," request using the ",(0,r.jsx)(t.code,{children:"update"})," method with specified document this way:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `IndexName` and `DocumentId`\nimport zio.elasticsearch._\nimport zio.schema.Schema\n\n// example of document\nfinal case class User(id: String, username: String)\n\nval user: User = User(id = "1", username = "johndoe")\n\nimplicit val schema: Schema.CaseClass2[String, String, User] = DeriveSchema.gen[GitHubRepo]\n\nval request: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user)\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can create a ",(0,r.jsx)(t.code,{children:"Update"})," request using the ",(0,r.jsx)(t.code,{children:"updateByScript"})," method with specified script this way:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch._\nimport zio.elasticsearch.script.Script\n\nval request: UpdateRequest = updateByScript(index = IndexName("index"), id = DocumentId("documentId"), script = Script("ctx._source.intField += params[\'factor\']").params("factor" -> 2))\n'})}),"\n",(0,r.jsxs)(t.p,{children:["If you want to change the ",(0,r.jsx)(t.code,{children:"upsert"}),", you can use the ",(0,r.jsx)(t.code,{children:"orCreate"})," method:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'val newUser: User = User(id = "2", username = "janedoe")\n\nval requestWithUpsert: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).orCreate(newUser)\n'})}),"\n",(0,r.jsxs)(t.p,{children:["If you want to change the ",(0,r.jsx)(t.code,{children:"refresh"}),", you can use ",(0,r.jsx)(t.code,{children:"refresh"}),", ",(0,r.jsx)(t.code,{children:"refreshFalse"})," or ",(0,r.jsx)(t.code,{children:"refreshTrue"})," method:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'val requestWithRefresh: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refresh(true)\nval requestWithRefreshFalse: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshFalse\nval requestWithRefreshTrue: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshTrue\n'})}),"\n",(0,r.jsxs)(t.p,{children:["If you want to change the ",(0,r.jsx)(t.code,{children:"routing"}),", you can use the ",(0,r.jsx)(t.code,{children:"routing"})," method:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'// this import is required for using `Routing` also\nimport zio.elasticsearch._\n\nval requestWithRouting: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).routing(Routing("routing"))\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can find more information about ",(0,r.jsx)(t.code,{children:"Update"})," request ",(0,r.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-update.html",children:"here"}),"."]})]})}const l=function(e={}){const{wrapper:t}=Object.assign({},(0,n.a)(),e.components);return t?(0,r.jsx)(t,Object.assign({},e,{children:(0,r.jsx)(u,e)})):u(e)}},1151:(e,t,s)=>{s.d(t,{a:()=>a});var r=s(7294);const n={},i=r.createContext(n);function a(e){const t=r.useContext(i);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}}}]);