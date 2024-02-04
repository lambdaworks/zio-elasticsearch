"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[2240],{860:(e,t,c)=>{c.r(t),c.d(t,{assets:()=>a,contentTitle:()=>n,default:()=>h,frontMatter:()=>s,metadata:()=>o,toc:()=>l});var i=c(7624),r=c(2172);const s={id:"overview_elastic_executor",title:"Executing Requests"},n=void 0,o={id:"overview/overview_elastic_executor",title:"Executing Requests",description:"In order to get the functional effect of executing a specified Elasticsearch request, you should call the execute method defined in the Elasticsearch, which returns a ZIO that requires an Elasticsearch, fails with a Throwable and returns the relevant value A for that request.",source:"@site/../modules/docs/target/mdoc/overview/elastic_executor.md",sourceDirName:"overview",slug:"/overview/overview_elastic_executor",permalink:"/zio-elasticsearch/overview/overview_elastic_executor",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/elastic_executor.md",tags:[],version:"current",frontMatter:{id:"overview_elastic_executor",title:"Executing Requests"},sidebar:"docs",previous:{title:"Use of ZIO Prelude and Schema",permalink:"/zio-elasticsearch/overview/overview_zio_prelude_schema"},next:{title:"Fluent API",permalink:"/zio-elasticsearch/overview/overview_fluent_api"}},a={},l=[];function d(e){const t={code:"code",li:"li",p:"p",pre:"pre",ul:"ul",...(0,r.M)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(t.p,{children:["In order to get the functional effect of executing a specified Elasticsearch request, you should call the ",(0,i.jsx)(t.code,{children:"execute"})," method defined in the ",(0,i.jsx)(t.code,{children:"Elasticsearch"}),", which returns a ",(0,i.jsx)(t.code,{children:"ZIO"})," that requires an ",(0,i.jsx)(t.code,{children:"Elasticsearch"}),", fails with a ",(0,i.jsx)(t.code,{children:"Throwable"})," and returns the relevant value ",(0,i.jsx)(t.code,{children:"A"})," for that request.\nThe ",(0,i.jsx)(t.code,{children:"Elasticsearch.layer"})," can be provided using the following import:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.Elasticsearch\n"})}),"\n",(0,i.jsxs)(t.p,{children:["However, ",(0,i.jsx)(t.code,{children:"Elasticsearch.layer"})," requires a dependency on the ",(0,i.jsx)(t.code,{children:"ElasticExector"}),".\nTo provide the dependency on ",(0,i.jsx)(t.code,{children:"ElasticExecutor"}),", you must pass one of the ",(0,i.jsx)(t.code,{children:"ZLayer"}),"s from the following import:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:"import zio.elasticsearch.ElasticExecutor\n"})}),"\n",(0,i.jsxs)(t.p,{children:["For example, if you want to execute requests on an Elasticsearch server running on ",(0,i.jsx)(t.code,{children:"localhost"})," and port ",(0,i.jsx)(t.code,{children:"9200"}),", you can achieve that in two ways:"]}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsxs)(t.li,{children:["provide the ",(0,i.jsx)(t.code,{children:"live"})," ",(0,i.jsx)(t.code,{children:"ZLayer"})," to your effect, along with a ",(0,i.jsx)(t.code,{children:"SttpBackend"})," and an ",(0,i.jsx)(t.code,{children:"ElasticConfig"})," layer,"]}),"\n",(0,i.jsxs)(t.li,{children:["or provide ",(0,i.jsx)(t.code,{children:"ElasticExecutor.local"})," layer along with a ",(0,i.jsx)(t.code,{children:"SttpBackend"}),"."]}),"\n"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-scala",children:'import zio._\nimport zio.elasticsearch._\nimport sttp.client3.httpclient.zio.HttpClientZioBackend\n\nval result: RIO[Elasticsearch, Boolean] =\n  Elasticsearch.execute(ElasticRequest.exists(IndexName("index"), DocumentId("documentId")))\n\n// Executing Elasticsearch requests with provided ElasticConfig layer explicitly\nresult.provide(\n  ZLayer.succeed(ElasticConfig("localhost", 9200)) >>> ElasticExecutor.live,\n  Elasticsearch.layer,\n  HttpClientZioBackend.layer()\n)\n\n// Executing Elasticsearch requests with local ElasticExecutor\nresult.provide(\n  ElasticExecutor.local,\n  Elasticsearch.layer,\n  HttpClientZioBackend.layer()\n)\n'})})]})}function h(e={}){const{wrapper:t}={...(0,r.M)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(d,{...e})}):d(e)}},2172:(e,t,c)=>{c.d(t,{I:()=>o,M:()=>n});var i=c(1504);const r={},s=i.createContext(r);function n(e){const t=i.useContext(s);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function o(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:n(e.components),i.createElement(s.Provider,{value:t},e.children)}}}]);