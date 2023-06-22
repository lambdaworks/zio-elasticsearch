"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[82],{3905:(e,r,t)=>{t.d(r,{Zo:()=>u,kt:()=>y});var n=t(7294);function a(e,r,t){return r in e?Object.defineProperty(e,r,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[r]=t,e}function i(e,r){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);r&&(n=n.filter((function(r){return Object.getOwnPropertyDescriptor(e,r).enumerable}))),t.push.apply(t,n)}return t}function o(e){for(var r=1;r<arguments.length;r++){var t=null!=arguments[r]?arguments[r]:{};r%2?i(Object(t),!0).forEach((function(r){a(e,r,t[r])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(r){Object.defineProperty(e,r,Object.getOwnPropertyDescriptor(t,r))}))}return e}function s(e,r){if(null==e)return{};var t,n,a=function(e,r){if(null==e)return{};var t,n,a={},i=Object.keys(e);for(n=0;n<i.length;n++)t=i[n],r.indexOf(t)>=0||(a[t]=e[t]);return a}(e,r);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)t=i[n],r.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var l=n.createContext({}),c=function(e){var r=n.useContext(l),t=r;return e&&(t="function"==typeof e?e(r):o(o({},r),e)),t},u=function(e){var r=c(e.components);return n.createElement(l.Provider,{value:r},e.children)},m="mdxType",p={inlineCode:"code",wrapper:function(e){var r=e.children;return n.createElement(n.Fragment,{},r)}},d=n.forwardRef((function(e,r){var t=e.components,a=e.mdxType,i=e.originalType,l=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),m=c(t),d=a,y=m["".concat(l,".").concat(d)]||m[d]||p[d]||i;return t?n.createElement(y,o(o({ref:r},u),{},{components:t})):n.createElement(y,o({ref:r},u))}));function y(e,r){var t=arguments,a=r&&r.mdxType;if("string"==typeof e||a){var i=t.length,o=new Array(i);o[0]=d;var s={};for(var l in r)hasOwnProperty.call(r,l)&&(s[l]=r[l]);s.originalType=e,s[m]="string"==typeof e?e:a,o[1]=s;for(var c=2;c<i;c++)o[c]=t[c];return n.createElement.apply(null,o)}return n.createElement.apply(null,t)}d.displayName="MDXCreateElement"},9169:(e,r,t)=>{t.r(r),t.d(r,{assets:()=>l,contentTitle:()=>o,default:()=>p,frontMatter:()=>i,metadata:()=>s,toc:()=>c});var n=t(7462),a=(t(7294),t(3905));const i={id:"elastic_query_terms",title:"Terms Query"},o=void 0,s={unversionedId:"overview/queries/elastic_query_terms",id:"overview/queries/elastic_query_terms",title:"Terms Query",description:"The Terms query returns documents that contain one or more exact terms in a provided field.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_terms.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_terms",permalink:"/zio-elasticsearch/overview/queries/elastic_query_terms",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_terms.md",tags:[],version:"current",frontMatter:{id:"elastic_query_terms",title:"Terms Query"},sidebar:"docs",previous:{title:"Term Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_term"},next:{title:"Wildcard Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_wildcard"}},l={},c=[],u={toc:c},m="wrapper";function p(e){let{components:r,...t}=e;return(0,a.kt)(m,(0,n.Z)({},u,t,{components:r,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"Terms")," query returns documents that contain one or more exact terms in a provided field.\nThis query is the same as the Term query, except you can search for multiple values."),(0,a.kt)("p",null,"In order to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"Terms")," query import the following:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.TermsQuery\nimport zio.elasticsearch.ElasticQuery.terms\n")),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("inlineCode",{parentName:"p"},"Terms")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"terms")," method this way:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val query: TermsQuery = terms(field = "name", "a", "b", "c")\n')),(0,a.kt)("p",null,"You can create a ",(0,a.kt)("a",{parentName:"p",href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema"},"type-safe")," ",(0,a.kt)("inlineCode",{parentName:"p"},"Terms")," query using the ",(0,a.kt)("inlineCode",{parentName:"p"},"terms")," method this way:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val query: TermQuery = terms(field = Document.name, "a", "b", "c")\n')),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"boost"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"boost")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithBoost: TermsQuery = terms(field = "name", "a", "b", "c").boost(2.0)\n')),(0,a.kt)("p",null,"You can find more information about ",(0,a.kt)("inlineCode",{parentName:"p"},"Terms")," query ",(0,a.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-terms-query.html"},"here"),"."))}p.isMDXComponent=!0}}]);