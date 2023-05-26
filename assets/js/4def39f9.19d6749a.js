"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[870],{3905:(e,t,r)=>{r.d(t,{Zo:()=>u,kt:()=>y});var n=r(7294);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function o(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function i(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?o(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):o(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function s(e,t){if(null==e)return{};var r,n,a=function(e,t){if(null==e)return{};var r,n,a={},o=Object.keys(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||(a[r]=e[r]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var c=n.createContext({}),l=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):i(i({},t),e)),r},u=function(e){var t=l(e.components);return n.createElement(c.Provider,{value:t},e.children)},p="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},m=n.forwardRef((function(e,t){var r=e.components,a=e.mdxType,o=e.originalType,c=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),p=l(r),m=a,y=p["".concat(c,".").concat(m)]||p[m]||d[m]||o;return r?n.createElement(y,i(i({ref:t},u),{},{components:r})):n.createElement(y,i({ref:t},u))}));function y(e,t){var r=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=r.length,i=new Array(o);i[0]=m;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s[p]="string"==typeof e?e:a,i[1]=s;for(var l=2;l<o;l++)i[l]=r[l];return n.createElement.apply(null,i)}return n.createElement.apply(null,r)}m.displayName="MDXCreateElement"},3882:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>i,default:()=>d,frontMatter:()=>o,metadata:()=>s,toc:()=>l});var n=r(7462),a=(r(7294),r(3905));const o={id:"elastic_query_nested",title:"Nested Query"},i=void 0,s={unversionedId:"overview/queries/elastic_query_nested",id:"overview/queries/elastic_query_nested",title:"Nested Query",description:"The Nested query searches nested field objects as if they were indexed as separate documents. If an object matches the search, the Nested query returns the root parent document.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_nested.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_nested",permalink:"/zio-elasticsearch/overview/queries/elastic_query_nested",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_nested.md",tags:[],version:"current",frontMatter:{id:"elastic_query_nested",title:"Nested Query"},sidebar:"docs",previous:{title:"Match Phrase Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match_phrase"},next:{title:"Range Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_range"}},c={},l=[],u={toc:l},p="wrapper";function d(e){let{components:t,...r}=e;return(0,a.kt)(p,(0,n.Z)({},u,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"Nested")," query searches nested field objects as if they were indexed as separate documents. If an object matches the search, the Nested query returns the root parent document."),(0,a.kt)("p",null,"In order to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"Nested")," query import the following:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.NestedQuery\nimport zio.elasticsearch.ElasticQuery._\n")),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("inlineCode",{parentName:"p"},"Nested")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"nested")," method in the following manner:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val query: NestedQuery = nested(path = "testField", query = matchAll)\n')),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("a",{parentName:"p",href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema"},"type-safe")," ",(0,a.kt)("inlineCode",{parentName:"p"},"Nested")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"nested")," method in the following manner:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val query: NestedQuery = nested(path = Document.subDocumentList, query = matchAll)\n")),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"ignore_unmapped"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"ignoreUnmapped")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithIgnoreUnmapped: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).ignoreUnmapped(true)\n")),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"inner_hits"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"innerHits")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.InnerHits\n\nval queryWithInnerHits: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).innerHits(innerHits = InnerHits.from(5))\n")),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"score_mode"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"scoreMode")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.ScoreMode\n\nval queryWithScoreMode: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).scoreMode(ScoreMode.Avg)\n")),(0,a.kt)("p",null,"You can find more information about ",(0,a.kt)("inlineCode",{parentName:"p"},"Nested")," query ",(0,a.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-nested-query.html"},"here"),"."))}d.isMDXComponent=!0}}]);