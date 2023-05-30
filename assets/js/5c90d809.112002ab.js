"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[2914],{3905:(e,t,r)=>{r.d(t,{Zo:()=>u,kt:()=>y});var a=r(7294);function n(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function l(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,a)}return r}function o(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?l(Object(r),!0).forEach((function(t){n(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):l(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function i(e,t){if(null==e)return{};var r,a,n=function(e,t){if(null==e)return{};var r,a,n={},l=Object.keys(e);for(a=0;a<l.length;a++)r=l[a],t.indexOf(r)>=0||(n[r]=e[r]);return n}(e,t);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);for(a=0;a<l.length;a++)r=l[a],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(n[r]=e[r])}return n}var c=a.createContext({}),s=function(e){var t=a.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):o(o({},t),e)),r},u=function(e){var t=s(e.components);return a.createElement(c.Provider,{value:t},e.children)},p="mdxType",m={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},h=a.forwardRef((function(e,t){var r=e.components,n=e.mdxType,l=e.originalType,c=e.parentName,u=i(e,["components","mdxType","originalType","parentName"]),p=s(r),h=n,y=p["".concat(c,".").concat(h)]||p[h]||m[h]||l;return r?a.createElement(y,o(o({ref:t},u),{},{components:r})):a.createElement(y,o({ref:t},u))}));function y(e,t){var r=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var l=r.length,o=new Array(l);o[0]=h;var i={};for(var c in t)hasOwnProperty.call(t,c)&&(i[c]=t[c]);i.originalType=e,i[p]="string"==typeof e?e:n,o[1]=i;for(var s=2;s<l;s++)o[s]=r[s];return a.createElement.apply(null,o)}return a.createElement.apply(null,r)}h.displayName="MDXCreateElement"},33:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>o,default:()=>m,frontMatter:()=>l,metadata:()=>i,toc:()=>s});var a=r(7462),n=(r(7294),r(3905));const l={id:"elastic_query_match_all",title:"Match All Query"},o=void 0,i={unversionedId:"overview/queries/elastic_query_match_all",id:"overview/queries/elastic_query_match_all",title:"Match All Query",description:"The most simple query, which matches all documents, giving them all a score of 1.0.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match_all.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_match_all",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_all",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match_all.md",tags:[],version:"current",frontMatter:{id:"elastic_query_match_all",title:"Match All Query"},sidebar:"docs",previous:{title:"Match Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match"},next:{title:"Match Phrase Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase"}},c={},s=[],u={toc:s},p="wrapper";function m(e){let{components:t,...r}=e;return(0,n.kt)(p,(0,a.Z)({},u,r,{components:t,mdxType:"MDXLayout"}),(0,n.kt)("p",null,"The most simple query, which matches all documents, giving them all a ",(0,n.kt)("inlineCode",{parentName:"p"},"score")," of ",(0,n.kt)("inlineCode",{parentName:"p"},"1.0"),"."),(0,n.kt)("p",null,"To create a ",(0,n.kt)("inlineCode",{parentName:"p"},"MatchAll")," query do the following:"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.MatchAllQuery\nimport zio.elasticsearch.ElasticQuery._\n\nval query: MatchAllQuery = matchAll\n")),(0,n.kt)("p",null,"If you want to change the ",(0,n.kt)("inlineCode",{parentName:"p"},"boost"),", you can use the ",(0,n.kt)("inlineCode",{parentName:"p"},"boost")," method:"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithBoost: MatchAllQuery = matchAll.boost(1.2)\n")),(0,n.kt)("p",null,"You can find more information about ",(0,n.kt)("inlineCode",{parentName:"p"},"MatchAll")," query ",(0,n.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-all-query.html"},"here"),"."))}m.isMDXComponent=!0}}]);