"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[858],{3905:(e,t,n)=>{n.d(t,{Zo:()=>s,kt:()=>y});var a=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,a,o=function(e,t){if(null==e)return{};var n,a,o={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var u=a.createContext({}),c=function(e){var t=a.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},s=function(e){var t=c(e.components);return a.createElement(u.Provider,{value:t},e.children)},m="mdxType",p={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},d=a.forwardRef((function(e,t){var n=e.components,o=e.mdxType,r=e.originalType,u=e.parentName,s=i(e,["components","mdxType","originalType","parentName"]),m=c(n),d=o,y=m["".concat(u,".").concat(d)]||m[d]||p[d]||r;return n?a.createElement(y,l(l({ref:t},s),{},{components:n})):a.createElement(y,l({ref:t},s))}));function y(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var r=n.length,l=new Array(r);l[0]=d;var i={};for(var u in t)hasOwnProperty.call(t,u)&&(i[u]=t[u]);i.originalType=e,i[m]="string"==typeof e?e:o,l[1]=i;for(var c=2;c<r;c++)l[c]=n[c];return a.createElement.apply(null,l)}return a.createElement.apply(null,n)}d.displayName="MDXCreateElement"},3272:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>u,contentTitle:()=>l,default:()=>p,frontMatter:()=>r,metadata:()=>i,toc:()=>c});var a=n(7462),o=(n(7294),n(3905));const r={id:"elastic_query_bool",title:"Boolean Query"},l=void 0,i={unversionedId:"overview/queries/elastic_query_bool",id:"overview/queries/elastic_query_bool",title:"Boolean Query",description:"The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_bool.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_bool",permalink:"/zio-elasticsearch/overview/queries/elastic_query_bool",draft:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_bool.md",tags:[],version:"current",frontMatter:{id:"elastic_query_bool",title:"Boolean Query"},sidebar:"docs",previous:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_query"},next:{title:"Exists Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_exists"}},u={},c=[],s={toc:c},m="wrapper";function p(e){let{components:t,...n}=e;return(0,o.kt)(m,(0,a.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("p",null,"The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"filter"),": The clause (query) must appear in matching documents. However, unlike ",(0,o.kt)("inlineCode",{parentName:"li"},"must")," the score of the query will be ignored."),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"must"),": the clause (query) must appear in matching documents and will contribute to the score."),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"must not"),": the clause (query) must not appear in the matching documents."),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"should"),": the clause (query) should appear in the matching document.")),(0,o.kt)("p",null,"In order to use the ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query import the following:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"import zio.elasticsearch.query.BoolQuery\nimport zio.elasticsearch.ElasticQuery._\n")),(0,o.kt)("p",null,"The ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query can be created with ",(0,o.kt)("inlineCode",{parentName:"p"},"filter"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"must"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"mustNot")," or ",(0,o.kt)("inlineCode",{parentName:"p"},"should")," method:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))\nval mustQuery: BoolQuery = must(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))\nval mustNotQuery: BoolQuery = mustNot(contains(field = Document.name, value = "a"))\nval shouldQuery: BoolQuery = should(startsWith(field = Document.name, value = "a"))\n')),(0,o.kt)("p",null,"Once the ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query is created, you can call ",(0,o.kt)("inlineCode",{parentName:"p"},"filter"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"must"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"mustNot"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"should"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"boost")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"minimumShouldMatch")," methods on it."),(0,o.kt)("p",null,"If you want to add ",(0,o.kt)("inlineCode",{parentName:"p"},"Filter")," query to ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query, you can use ",(0,o.kt)("inlineCode",{parentName:"p"},"filter")," method (you can also call ",(0,o.kt)("inlineCode",{parentName:"p"},"filter")," method on ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query that is created with ",(0,o.kt)("inlineCode",{parentName:"p"},"filter")," method):"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).filter(contains(field = Document.name, value = "c"))\n')),(0,o.kt)("p",null,"If you want to add ",(0,o.kt)("inlineCode",{parentName:"p"},"Must")," query to the ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query, you can use ",(0,o.kt)("inlineCode",{parentName:"p"},"must")," method:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).must(contains(field = Document.name, value = "c"))\n')),(0,o.kt)("p",null,"If you want to add ",(0,o.kt)("inlineCode",{parentName:"p"},"MustNot")," query to the ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query, you can use ",(0,o.kt)("inlineCode",{parentName:"p"},"mustNot")," method:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).mustNot(contains(field = Document.name, value = "c"))\n')),(0,o.kt)("p",null,"If you want to add ",(0,o.kt)("inlineCode",{parentName:"p"},"Should")," query to the ",(0,o.kt)("inlineCode",{parentName:"p"},"Bool")," query, you can use ",(0,o.kt)("inlineCode",{parentName:"p"},"should")," method:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).should(contains(field = Document.name, value = "c"))\n')),(0,o.kt)("p",null,"If you want to change the ",(0,o.kt)("inlineCode",{parentName:"p"},"_score")," parameter, you can use the ",(0,o.kt)("inlineCode",{parentName:"p"},"boost")," method:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithBoost: BoolQuery = filter(contains(field = Document.name, value = "a")).boost(1.2)\n')),(0,o.kt)("p",null,"If you want to change the ",(0,o.kt)("inlineCode",{parentName:"p"},"minimum_should_match")," parameter, you can use the ",(0,o.kt)("inlineCode",{parentName:"p"},"minimumShouldMatch")," method:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val queryWithMinimumShouldMatch: BoolQuery = should(contains(field = Document.name, value = "a")).minimumShouldMatch(2)\n')),(0,o.kt)("p",null,"You can find more information about Boolean Query ",(0,o.kt)("a",{parentName:"p",href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-bool-query.html"},"here"),"."))}p.isMDXComponent=!0}}]);