"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[3976],{446:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>o,contentTitle:()=>l,default:()=>u,frontMatter:()=>c,metadata:()=>i,toc:()=>a});const i=JSON.parse('{"id":"overview/requests/elastic_request_delete_index","title":"Delete Index Request","description":"This request deletes specified Elasticsearch index.","source":"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_delete_index.md","sourceDirName":"overview/requests","slug":"/overview/requests/elastic_request_delete_index","permalink":"/zio-elasticsearch/overview/requests/elastic_request_delete_index","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_delete_index.md","tags":[],"version":"current","frontMatter":{"id":"elastic_request_delete_index","title":"Delete Index Request"},"sidebar":"docs","previous":{"title":"Delete By Query Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_delete_by_query"},"next":{"title":"Exists Request","permalink":"/zio-elasticsearch/overview/requests/elastic_request_exists"}}');var r=s(4848),n=s(8453);const c={id:"elastic_request_delete_index",title:"Delete Index Request"},l=void 0,o={},a=[];function d(e){const t={a:"a",code:"code",p:"p",pre:"pre",...(0,n.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.p,{children:"This request deletes specified Elasticsearch index."}),"\n",(0,r.jsxs)(t.p,{children:["To create a ",(0,r.jsx)(t.code,{children:"DeleteById"})," request do the following:"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-scala",children:'import zio.elasticsearch.ElasticRequest.DeleteIndexRequest\nimport zio.elasticsearch.ElasticRequest.deleteIndex\n// this import is required for using `IndexName`\nimport zio.elasticsearch._\n\nval request: DeleteIndexRequest = deleteIndex(name = IndexName("index"))\n'})}),"\n",(0,r.jsxs)(t.p,{children:["You can find more information about ",(0,r.jsx)(t.code,{children:"DeleteIndex"})," request ",(0,r.jsx)(t.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-delete-index.html",children:"here"}),"."]})]})}function u(e={}){const{wrapper:t}={...(0,n.R)(),...e.components};return t?(0,r.jsx)(t,{...e,children:(0,r.jsx)(d,{...e})}):d(e)}},8453:(e,t,s)=>{s.d(t,{R:()=>c,x:()=>l});var i=s(6540);const r={},n=i.createContext(r);function c(e){const t=i.useContext(n);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function l(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:c(e.components),i.createElement(n.Provider,{value:t},e.children)}}}]);