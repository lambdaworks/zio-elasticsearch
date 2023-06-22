"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[4277],{3905:(e,t,r)=>{r.d(t,{Zo:()=>l,kt:()=>f});var n=r(7294);function i(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function a(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function s(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?a(Object(r),!0).forEach((function(t){i(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):a(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function o(e,t){if(null==e)return{};var r,n,i=function(e,t){if(null==e)return{};var r,n,i={},a=Object.keys(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||(i[r]=e[r]);return i}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(i[r]=e[r])}return i}var c=n.createContext({}),u=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):s(s({},t),e)),r},l=function(e){var t=u(e.components);return n.createElement(c.Provider,{value:t},e.children)},d="mdxType",p={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},m=n.forwardRef((function(e,t){var r=e.components,i=e.mdxType,a=e.originalType,c=e.parentName,l=o(e,["components","mdxType","originalType","parentName"]),d=u(r),m=i,f=d["".concat(c,".").concat(m)]||d[m]||p[m]||a;return r?n.createElement(f,s(s({ref:t},l),{},{components:r})):n.createElement(f,s({ref:t},l))}));function f(e,t){var r=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var a=r.length,s=new Array(a);s[0]=m;var o={};for(var c in t)hasOwnProperty.call(t,c)&&(o[c]=t[c]);o.originalType=e,o[d]="string"==typeof e?e:i,s[1]=o;for(var u=2;u<a;u++)s[u]=r[u];return n.createElement.apply(null,s)}return n.createElement.apply(null,r)}m.displayName="MDXCreateElement"},289:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>s,default:()=>p,frontMatter:()=>a,metadata:()=>o,toc:()=>u});var n=r(7462),i=(r(7294),r(3905));const a={id:"elastic_request_get_by_id",title:"Get By ID Request"},s=void 0,o={unversionedId:"overview/requests/elastic_request_get_by_id",id:"overview/requests/elastic_request_get_by_id",title:"Get By ID Request",description:"The GetById request retrieves the specified JSON document from an Elasticsearch index.",source:"@site/../modules/docs/target/mdoc/overview/requests/elastic_request_get_by_id.md",sourceDirName:"overview/requests",slug:"/overview/requests/elastic_request_get_by_id",permalink:"/zio-elasticsearch/overview/requests/elastic_request_get_by_id",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/requests/elastic_request_get_by_id.md",tags:[],version:"current",frontMatter:{id:"elastic_request_get_by_id",title:"Get By ID Request"},sidebar:"docs",previous:{title:"Exists Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_exists"},next:{title:"Search Request",permalink:"/zio-elasticsearch/overview/requests/elastic_request_search"}},c={},u=[],l={toc:u},d="wrapper";function p(e){let{components:t,...r}=e;return(0,i.kt)(d,(0,n.Z)({},l,r,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"The ",(0,i.kt)("inlineCode",{parentName:"p"},"GetById")," request retrieves the specified JSON document from an Elasticsearch index."),(0,i.kt)("p",null,"To create a ",(0,i.kt)("inlineCode",{parentName:"p"},"GetById")," request do the following:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'import zio.elasticsearch.ElasticRequest.GetByIdRequest\nimport zio.elasticsearch.ElasticRequest.getById\n// this import is required for using `IndexName` and `DocumentId`\nimport zio.elasticsearch._\n\nval request: ExistsRequest = getById(index = IndexName("index"), id = DocumentId("111"))\n')),(0,i.kt)("p",null,"If you want to change the ",(0,i.kt)("inlineCode",{parentName:"p"},"refresh"),", you can use ",(0,i.kt)("inlineCode",{parentName:"p"},"refresh"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"refreshFalse")," or ",(0,i.kt)("inlineCode",{parentName:"p"},"refreshTrue")," method:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'val requestWithRefresh: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).refresh(true)\nval requestWithRefreshFalse: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).refreshFalse\nval requestWithRefreshTrue: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).refreshTrue\n')),(0,i.kt)("p",null,"If you want to change the ",(0,i.kt)("inlineCode",{parentName:"p"},"routing"),", you can use the ",(0,i.kt)("inlineCode",{parentName:"p"},"routing")," method:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'// this import is required for `Routing` also\nimport zio.elasticsearch._\n\nval requestWithRouting: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).routing(Routing("routing"))\n')),(0,i.kt)("p",null,"You can find more information about ",(0,i.kt)("inlineCode",{parentName:"p"},"GetById")," request ",(0,i.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-get.html"},"here"),"."))}p.isMDXComponent=!0}}]);