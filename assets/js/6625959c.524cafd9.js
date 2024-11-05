"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9122],{7966:(e,s,t)=>{t.r(s),t.d(s,{assets:()=>a,contentTitle:()=>o,default:()=>l,frontMatter:()=>c,metadata:()=>i,toc:()=>u});const i=JSON.parse('{"id":"overview/requests/elastic_request_exists","title":"Exists Request","description":"This request is used for checking whether document exists.","source":"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_exists.md","sourceDirName":"overview/requests","slug":"/overview/requests/elastic_request_exists","permalink":"/zio-elasticsearch/overview/requests/elastic_request_exists","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_exists.md","tags":[],"version":"current","frontMatter":{"id":"elastic_request_exists","title":"Exists Request"},"sidebar":"docs","previous":{"title":"Delete Index Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_delete_index"},"next":{"title":"Get By ID Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_get_by_id"}}');var r=t(4848),n=t(8453);const c={id:"elastic_request_exists",title:"Exists Request"},o=void 0,a={},u=[];function d(e){const s={a:"a",code:"code",p:"p",pre:"pre",...(0,n.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(s.p,{children:"This request is used for checking whether document exists."}),"\n",(0,r.jsxs)(s.p,{children:["To create a ",(0,r.jsx)(s.code,{children:"Exists"})," request do the following:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticRequest.ExistsRequest\nimport zio.elasticsearch.ElasticRequest.exists\n// this import is required for using `IndexName` and `DocumentId`\nimport zio.elasticsearch._\n\nval request: ExistsRequest = exists(index = IndexName("index"), id = DocumentId("111"))\n'})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change the ",(0,r.jsx)(s.code,{children:"routing"}),", you can use the ",(0,r.jsx)(s.code,{children:"routing"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'// this import is required for `Routing` also\nimport zio.elasticsearch._\n\nval requestWithRouting: ExistsRequest = exists(index = IndexName("index"), id = DocumentId("111")).routing(Routing("routing"))\n'})}),"\n",(0,r.jsxs)(s.p,{children:["You can find more information about ",(0,r.jsx)(s.code,{children:"Exists"})," request ",(0,r.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-exists.html#indices-exists",children:"here"}),"."]})]})}function l(e={}){const{wrapper:s}={...(0,n.R)(),...e.components};return s?(0,r.jsx)(s,{...e,children:(0,r.jsx)(d,{...e})}):d(e)}},8453:(e,s,t)=>{t.d(s,{R:()=>c,x:()=>o});var i=t(6540);const r={},n=i.createContext(r);function c(e){const s=i.useContext(n);return i.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function o(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:c(e.components),i.createElement(n.Provider,{value:s},e.children)}}}]);