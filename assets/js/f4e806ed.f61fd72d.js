"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[7557],{3905:(e,t,r)=>{r.d(t,{Zo:()=>u,kt:()=>y});var a=r(7294);function n(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,a)}return r}function o(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){n(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function c(e,t){if(null==e)return{};var r,a,n=function(e,t){if(null==e)return{};var r,a,n={},i=Object.keys(e);for(a=0;a<i.length;a++)r=i[a],t.indexOf(r)>=0||(n[r]=e[r]);return n}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)r=i[a],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(n[r]=e[r])}return n}var l=a.createContext({}),s=function(e){var t=a.useContext(l),r=t;return e&&(r="function"==typeof e?e(t):o(o({},t),e)),r},u=function(e){var t=s(e.components);return a.createElement(l.Provider,{value:t},e.children)},p="mdxType",m={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},h=a.forwardRef((function(e,t){var r=e.components,n=e.mdxType,i=e.originalType,l=e.parentName,u=c(e,["components","mdxType","originalType","parentName"]),p=s(r),h=n,y=p["".concat(l,".").concat(h)]||p[h]||m[h]||i;return r?a.createElement(y,o(o({ref:t},u),{},{components:r})):a.createElement(y,o({ref:t},u))}));function y(e,t){var r=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var i=r.length,o=new Array(i);o[0]=h;var c={};for(var l in t)hasOwnProperty.call(t,l)&&(c[l]=t[l]);c.originalType=e,c[p]="string"==typeof e?e:n,o[1]=c;for(var s=2;s<i;s++)o[s]=r[s];return a.createElement.apply(null,o)}return a.createElement.apply(null,r)}h.displayName="MDXCreateElement"},9151:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>l,contentTitle:()=>o,default:()=>m,frontMatter:()=>i,metadata:()=>c,toc:()=>s});var a=r(7462),n=(r(7294),r(3905));const i={id:"elastic_query_match",title:"Match Query"},o=void 0,c={unversionedId:"overview/queries/elastic_query_match",id:"overview/queries/elastic_query_match",title:"Match Query",description:"The Match query is a type of query that searches for a provided text, number, date or boolean value.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_match.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_match",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_match.md",tags:[],version:"current",frontMatter:{id:"elastic_query_match",title:"Match Query"},sidebar:"docs",previous:{title:"Has Parent Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_parent"},next:{title:"Match All Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_all"}},l={},s=[],u={toc:s},p="wrapper";function m(e){let{components:t,...r}=e;return(0,n.kt)(p,(0,a.Z)({},u,r,{components:t,mdxType:"MDXLayout"}),(0,n.kt)("p",null,"The ",(0,n.kt)("inlineCode",{parentName:"p"},"Match")," query is a type of query that searches for a provided text, number, date or boolean value.\nThis is the standard query for performing a full-text search, including options for fuzzy matching."),(0,n.kt)("p",null,"In order to use the ",(0,n.kt)("inlineCode",{parentName:"p"},"Match")," query import the following:"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.MatchQuery\nimport zio.elasticsearch.ElasticQuery._\n")),(0,n.kt)("p",null,"You can create a ",(0,n.kt)("inlineCode",{parentName:"p"},"Match")," query using the ",(0,n.kt)("inlineCode",{parentName:"p"},"matches")," method in the following manner:"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-scala"},'val query: MatchQuery = matches(field = "message", value = "this is a test")\n')),(0,n.kt)("p",null,"You can create a ",(0,n.kt)("a",{parentName:"p",href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema"},"type-safe")," ",(0,n.kt)("inlineCode",{parentName:"p"},"Match")," query using the ",(0,n.kt)("inlineCode",{parentName:"p"},"matches")," method in the following manner:"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-scala"},'val query: MatchQuery = matches(field = Document.message, value = "this is a test")\n')),(0,n.kt)("p",null,"You can find more information about ",(0,n.kt)("inlineCode",{parentName:"p"},"Match")," query ",(0,n.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html#query-dsl-match-query"},"here"),"."))}m.isMDXComponent=!0}}]);