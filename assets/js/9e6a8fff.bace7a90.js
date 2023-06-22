"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9311],{3905:(e,t,r)=>{r.d(t,{Zo:()=>c,kt:()=>y});var n=r(7294);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function o(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function s(e,t){if(null==e)return{};var r,n,a=function(e,t){if(null==e)return{};var r,n,a={},i=Object.keys(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||(a[r]=e[r]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var l=n.createContext({}),u=function(e){var t=n.useContext(l),r=t;return e&&(r="function"==typeof e?e(t):o(o({},t),e)),r},c=function(e){var t=u(e.components);return n.createElement(l.Provider,{value:t},e.children)},m="mdxType",p={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},d=n.forwardRef((function(e,t){var r=e.components,a=e.mdxType,i=e.originalType,l=e.parentName,c=s(e,["components","mdxType","originalType","parentName"]),m=u(r),d=a,y=m["".concat(l,".").concat(d)]||m[d]||p[d]||i;return r?n.createElement(y,o(o({ref:t},c),{},{components:r})):n.createElement(y,o({ref:t},c))}));function y(e,t){var r=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=r.length,o=new Array(i);o[0]=d;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[m]="string"==typeof e?e:a,o[1]=s;for(var u=2;u<i;u++)o[u]=r[u];return n.createElement.apply(null,o)}return n.createElement.apply(null,r)}d.displayName="MDXCreateElement"},1637:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>l,contentTitle:()=>o,default:()=>p,frontMatter:()=>i,metadata:()=>s,toc:()=>u});var n=r(7462),a=(r(7294),r(3905));const i={id:"elastic_query_term",title:"Term Query"},o=void 0,s={unversionedId:"overview/queries/elastic_query_term",id:"overview/queries/elastic_query_term",title:"Term Query",description:"The Term query returns documents that contain an exact term in a provided field.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_term.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_term",permalink:"/zio-elasticsearch/overview/queries/elastic_query_term",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_term.md",tags:[],version:"current",frontMatter:{id:"elastic_query_term",title:"Term Query"},sidebar:"docs",previous:{title:"Range Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_range"},next:{title:"Terms Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms"}},l={},u=[],c={toc:u},m="wrapper";function p(e){let{components:t,...r}=e;return(0,a.kt)(m,(0,n.Z)({},c,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"Term")," query returns documents that contain an exact term in a provided field."),(0,a.kt)("p",null,"In order to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"Term")," query import the following:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.TermQuery\nimport zio.elasticsearch.ElasticQuery._\n")),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("inlineCode",{parentName:"p"},"Term")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"term")," method this way:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val query: TermQuery = term(field = Document.name, value = "test")\n')),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("a",{parentName:"p",href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema"},"type-safe")," ",(0,a.kt)("inlineCode",{parentName:"p"},"Term")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"term")," method this way:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val query: TermQuery = term(field = Document.name, value = "test")\n')),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"boost"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"boost")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithBoost: TermQuery = term(field = Document.name, value = "test").boost(2.0)\n')),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"case_insensitive"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"caseInsensitive"),", ",(0,a.kt)("inlineCode",{parentName:"p"},"caseInsensitiveFalse")," or ",(0,a.kt)("inlineCode",{parentName:"p"},"caseInsensitiveTrue")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithCaseInsensitive: TermQuery = term(field = Document.name, value = "test").caseInsensitive(true)\nval queryWithCaseInsensitiveFalse: TermQuery = term(field = Document.name, value = "test").caseInsensitiveFalse\nval queryWithCaseInsensitiveTrue: TermQuery = term(field = Document.name, value = "test").caseInsensitiveTrue\n')),(0,a.kt)("p",null,"You can find more information about ",(0,a.kt)("inlineCode",{parentName:"p"},"Term")," query ",(0,a.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-term-query.html"},"here"),"."))}p.isMDXComponent=!0}}]);