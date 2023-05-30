"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[8557],{3905:(e,t,r)=>{r.d(t,{Zo:()=>l,kt:()=>d});var n=r(7294);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function o(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function s(e,t){if(null==e)return{};var r,n,a=function(e,t){if(null==e)return{};var r,n,a={},i=Object.keys(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||(a[r]=e[r]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var c=n.createContext({}),u=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):o(o({},t),e)),r},l=function(e){var t=u(e.components);return n.createElement(c.Provider,{value:t},e.children)},p="mdxType",m={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},y=n.forwardRef((function(e,t){var r=e.components,a=e.mdxType,i=e.originalType,c=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),p=u(r),y=a,d=p["".concat(c,".").concat(y)]||p[y]||m[y]||i;return r?n.createElement(d,o(o({ref:t},l),{},{components:r})):n.createElement(d,o({ref:t},l))}));function d(e,t){var r=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=r.length,o=new Array(i);o[0]=y;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s[p]="string"==typeof e?e:a,o[1]=s;for(var u=2;u<i;u++)o[u]=r[u];return n.createElement.apply(null,o)}return n.createElement.apply(null,r)}y.displayName="MDXCreateElement"},9757:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>o,default:()=>m,frontMatter:()=>i,metadata:()=>s,toc:()=>u});var n=r(7462),a=(r(7294),r(3905));const i={id:"elastic_query_has_parent",title:"Has Parent Query"},o=void 0,s={unversionedId:"overview/queries/elastic_query_has_parent",id:"overview/queries/elastic_query_has_parent",title:"Has Parent Query",description:"The HasParent query returns child documents whose parent document matches a provided query.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_has_parent.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_has_parent",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_parent",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_has_parent.md",tags:[],version:"current",frontMatter:{id:"elastic_query_has_parent",title:"Has Parent Query"},sidebar:"docs",previous:{title:"Has Child Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_child"},next:{title:"Match Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_match"}},c={},u=[],l={toc:u},p="wrapper";function m(e){let{components:t,...r}=e;return(0,a.kt)(p,(0,n.Z)({},l,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"HasParent")," query returns child documents whose parent document matches a provided query."),(0,a.kt)("p",null,"To create a ",(0,a.kt)("inlineCode",{parentName:"p"},"HasParent")," query do the following:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'import zio.elasticsearch.query.HasParentQuery\nimport zio.elasticsearch.ElasticQuery._\n\nval query: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test"))\n')),(0,a.kt)("p",null,"If you want to change the ",(0,a.kt)("inlineCode",{parentName:"p"},"boost"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"boost")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithBoost: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test")).boost(2.0)\n')),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"ignore_unmapped"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"ignoreUnmapped")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithIgnoreUnmapped: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test")).ignoreUnmapped(true)\n')),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"inner_hits"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"innerHits")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'import zio.elasticsearch.query.InnerHits\n\nval queryWithInnerHits: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test")).innerHits(innerHits = InnerHits.from(5))\n')),(0,a.kt)("p",null,"If you want to change ",(0,a.kt)("inlineCode",{parentName:"p"},"score"),", you can use ",(0,a.kt)("inlineCode",{parentName:"p"},"withScore"),", ",(0,a.kt)("inlineCode",{parentName:"p"},"withScoreFalse")," or ",(0,a.kt)("inlineCode",{parentName:"p"},"withScoreTrue")," method:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithScore: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.intField, "test")).withScore(true)\nval queryWithScoreFalse: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.intField, "test")).withScoreFalse\nval queryWithScoreTrue: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.intField, "test")).withScoreTrue\n')),(0,a.kt)("p",null,"You can find more information about ",(0,a.kt)("inlineCode",{parentName:"p"},"HasParent")," query ",(0,a.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-has-parent-query.html"},"here"),"."))}m.isMDXComponent=!0}}]);