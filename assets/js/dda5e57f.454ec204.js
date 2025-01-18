"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[824],{3123:(e,o,n)=>{n.r(o),n.d(o,{assets:()=>i,contentTitle:()=>r,default:()=>d,frontMatter:()=>l,metadata:()=>s,toc:()=>a});const s=JSON.parse('{"id":"overview/queries/elastic_query_bool","title":"Boolean Query","description":"The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):","source":"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_bool.md","sourceDirName":"overview/queries","slug":"/overview/queries/elastic_query_bool","permalink":"/zio-elasticsearch/overview/queries/elastic_query_bool","draft":false,"unlisted":false,"editUrl":"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_bool.md","tags":[],"version":"current","frontMatter":{"id":"elastic_query_bool","title":"Boolean Query"},"sidebar":"docs","previous":{"title":"Overview","permalink":"/zio-elasticsearch/overview/elastic_query"},"next":{"title":"Boosting Query","permalink":"/zio-elasticsearch/overview/queries/elastic_query_boosting"}}');var t=n(4848),c=n(8453);const l={id:"elastic_query_bool",title:"Boolean Query"},r=void 0,i={},a=[];function u(e){const o={a:"a",code:"code",li:"li",p:"p",pre:"pre",ul:"ul",...(0,c.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(o.p,{children:"The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):"}),"\n",(0,t.jsxs)(o.ul,{children:["\n",(0,t.jsxs)(o.li,{children:[(0,t.jsx)(o.code,{children:"filter"}),": The clause (query) must appear in matching documents. However, unlike ",(0,t.jsx)(o.code,{children:"must"})," the score of the query will be ignored."]}),"\n",(0,t.jsxs)(o.li,{children:[(0,t.jsx)(o.code,{children:"must"}),": the clause (query) must appear in matching documents and will contribute to the score."]}),"\n",(0,t.jsxs)(o.li,{children:[(0,t.jsx)(o.code,{children:"must not"}),": the clause (query) must not appear in the matching documents."]}),"\n",(0,t.jsxs)(o.li,{children:[(0,t.jsx)(o.code,{children:"should"}),": the clause (query) should appear in the matching document."]}),"\n"]}),"\n",(0,t.jsxs)(o.p,{children:["In order to use the ",(0,t.jsx)(o.code,{children:"Bool"})," query import the following:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:"import zio.elasticsearch.query.BoolQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,t.jsxs)(o.p,{children:["The ",(0,t.jsx)(o.code,{children:"Bool"})," query can be created with ",(0,t.jsx)(o.code,{children:"filter"}),", ",(0,t.jsx)(o.code,{children:"must"}),", ",(0,t.jsx)(o.code,{children:"mustNot"})," or ",(0,t.jsx)(o.code,{children:"should"})," method:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))\nval mustQuery: BoolQuery = must(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))\nval mustNotQuery: BoolQuery = mustNot(contains(field = Document.name, value = "a"))\nval shouldQuery: BoolQuery = should(startsWith(field = Document.name, value = "a"))\n'})}),"\n",(0,t.jsxs)(o.p,{children:["Once the ",(0,t.jsx)(o.code,{children:"Bool"})," query is created, you can call ",(0,t.jsx)(o.code,{children:"filter"}),", ",(0,t.jsx)(o.code,{children:"must"}),", ",(0,t.jsx)(o.code,{children:"mustNot"}),", ",(0,t.jsx)(o.code,{children:"should"}),", ",(0,t.jsx)(o.code,{children:"boost"})," and ",(0,t.jsx)(o.code,{children:"minimumShouldMatch"})," methods on it."]}),"\n",(0,t.jsxs)(o.p,{children:["If you want to add ",(0,t.jsx)(o.code,{children:"Filter"})," query to ",(0,t.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,t.jsx)(o.code,{children:"filter"})," method (you can also call ",(0,t.jsx)(o.code,{children:"filter"})," method on ",(0,t.jsx)(o.code,{children:"Bool"})," query that is created with ",(0,t.jsx)(o.code,{children:"filter"})," method):"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).filter(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,t.jsxs)(o.p,{children:["If you want to add ",(0,t.jsx)(o.code,{children:"Must"})," query to the ",(0,t.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,t.jsx)(o.code,{children:"must"})," method:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).must(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,t.jsxs)(o.p,{children:["If you want to add ",(0,t.jsx)(o.code,{children:"MustNot"})," query to the ",(0,t.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,t.jsx)(o.code,{children:"mustNot"})," method:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).mustNot(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,t.jsxs)(o.p,{children:["If you want to add ",(0,t.jsx)(o.code,{children:"Should"})," query to the ",(0,t.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,t.jsx)(o.code,{children:"should"})," method:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).should(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,t.jsxs)(o.p,{children:["If you want to change the ",(0,t.jsx)(o.code,{children:"_score"})," parameter, you can use the ",(0,t.jsx)(o.code,{children:"boost"})," method:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val queryWithBoost: BoolQuery = filter(contains(field = Document.name, value = "a")).boost(1.2)\n'})}),"\n",(0,t.jsxs)(o.p,{children:["If you want to change the ",(0,t.jsx)(o.code,{children:"minimum_should_match"})," parameter, you can use the ",(0,t.jsx)(o.code,{children:"minimumShouldMatch"})," method:"]}),"\n",(0,t.jsx)(o.pre,{children:(0,t.jsx)(o.code,{className:"language-scala",children:'val queryWithMinimumShouldMatch: BoolQuery = should(contains(field = Document.name, value = "a")).minimumShouldMatch(2)\n'})}),"\n",(0,t.jsxs)(o.p,{children:["You can find more information about ",(0,t.jsx)(o.code,{children:"Bool"})," query ",(0,t.jsx)(o.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-bool-query.html",children:"here"}),"."]})]})}function d(e={}){const{wrapper:o}={...(0,c.R)(),...e.components};return o?(0,t.jsx)(o,{...e,children:(0,t.jsx)(u,{...e})}):u(e)}},8453:(e,o,n)=>{n.d(o,{R:()=>l,x:()=>r});var s=n(6540);const t={},c=s.createContext(t);function l(e){const o=s.useContext(c);return s.useMemo((function(){return"function"==typeof e?e(o):{...o,...e}}),[o,e])}function r(e){let o;return o=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:l(e.components),s.createElement(c.Provider,{value:o},e.children)}}}]);