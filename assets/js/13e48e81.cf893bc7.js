"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[288],{3905:(e,t,n)=>{n.d(t,{Zo:()=>s,kt:()=>g});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var u=r.createContext({}),c=function(e){var t=r.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},s=function(e){var t=c(e.components);return r.createElement(u.Provider,{value:t},e.children)},p="mdxType",m={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,u=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),p=c(n),d=a,g=p["".concat(u,".").concat(d)]||p[d]||m[d]||o;return n?r.createElement(g,i(i({ref:t},s),{},{components:n})):r.createElement(g,i({ref:t},s))}));function g(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=d;var l={};for(var u in t)hasOwnProperty.call(t,u)&&(l[u]=t[u]);l.originalType=e,l[p]="string"==typeof e?e:a,i[1]=l;for(var c=2;c<o;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}d.displayName="MDXCreateElement"},4948:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>u,contentTitle:()=>i,default:()=>m,frontMatter:()=>o,metadata:()=>l,toc:()=>c});var r=n(7462),a=(n(7294),n(3905));const o={id:"elastic_query_range",title:"Range Query"},i=void 0,l={unversionedId:"overview/queries/elastic_query_range",id:"overview/queries/elastic_query_range",title:"Range Query",description:"A query that matches documents that contain terms within a provided range.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_range.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_range",permalink:"/zio-elasticsearch/overview/queries/elastic_query_range",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_range.md",tags:[],version:"current",frontMatter:{id:"elastic_query_range",title:"Range Query"},sidebar:"docs",previous:{title:"Nested Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_nested"},next:{title:"Term Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_term"}},u={},c=[],s={toc:c},p="wrapper";function m(e){let{components:t,...n}=e;return(0,a.kt)(p,(0,r.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"A query that matches documents that contain terms within a provided range."),(0,a.kt)("p",null,"In order to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"Range")," query import the following:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.RangeQuery\nimport zio.elasticsearch.ElasticQuery._\n")),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("inlineCode",{parentName:"p"},"Range")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"range")," method in the following manner:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val query: RangeQuery = range(field = "intField")\n')),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("a",{parentName:"p",href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema"},"type-safe")," ",(0,a.kt)("inlineCode",{parentName:"p"},"Range")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"range")," method in the following manner:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val query: RangeQuery = range(field = Document.intField)\n")),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"boost"),", you can use the ",(0,a.kt)("inlineCode",{parentName:"p"},"boost")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithBoost: RangeQuery = range(field = Document.intField).boost(2.0)\n")),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"format"),", you can use the ",(0,a.kt)("inlineCode",{parentName:"p"},"format")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithFormat: RangeQuery = range(field = Document.dateField).format("yyyy-MM-dd")\n')),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"gt")," (greater than), you can use the ",(0,a.kt)("inlineCode",{parentName:"p"},"gt")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithGt: RangeQuery = range(field = Document.intField).gt(1)\n")),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"gte")," (greater than or equal to), you can use the ",(0,a.kt)("inlineCode",{parentName:"p"},"gte")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithGte: RangeQuery = range(field = Document.intField).gte(1)\n")),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"lt")," (less than), you can use the ",(0,a.kt)("inlineCode",{parentName:"p"},"lt")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithLt: RangeQuery = range(field = Document.intField).lt(100)\n")),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"lte")," (less than or equal to), you can use the ",(0,a.kt)("inlineCode",{parentName:"p"},"lte")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"val queryWithLte: RangeQuery = range(field = Document.intField).lte(100)\n")),(0,a.kt)("p",null,"You can find more information about Range Query ",(0,a.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-range-query.html"},"here"),"."))}m.isMDXComponent=!0}}]);