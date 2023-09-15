"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[858],{2228:(e,o,n)=>{n.r(o),n.d(o,{assets:()=>i,contentTitle:()=>l,default:()=>d,frontMatter:()=>c,metadata:()=>r,toc:()=>a});var s=n(5893),t=n(1151);const c={id:"elastic_query_bool",title:"Boolean Query"},l=void 0,r={id:"overview/queries/elastic_query_bool",title:"Boolean Query",description:"The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_bool.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_bool",permalink:"/zio-elasticsearch/overview/queries/elastic_query_bool",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_bool.md",tags:[],version:"current",frontMatter:{id:"elastic_query_bool",title:"Boolean Query"},sidebar:"docs",previous:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_query"},next:{title:"Constant Score Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_constant_score"}},i={},a=[];function u(e){const o=Object.assign({p:"p",ul:"ul",li:"li",code:"code",pre:"pre",a:"a"},(0,t.ah)(),e.components);return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(o.p,{children:"The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):"}),"\n",(0,s.jsxs)(o.ul,{children:["\n",(0,s.jsxs)(o.li,{children:[(0,s.jsx)(o.code,{children:"filter"}),": The clause (query) must appear in matching documents. However, unlike ",(0,s.jsx)(o.code,{children:"must"})," the score of the query will be ignored."]}),"\n",(0,s.jsxs)(o.li,{children:[(0,s.jsx)(o.code,{children:"must"}),": the clause (query) must appear in matching documents and will contribute to the score."]}),"\n",(0,s.jsxs)(o.li,{children:[(0,s.jsx)(o.code,{children:"must not"}),": the clause (query) must not appear in the matching documents."]}),"\n",(0,s.jsxs)(o.li,{children:[(0,s.jsx)(o.code,{children:"should"}),": the clause (query) should appear in the matching document."]}),"\n"]}),"\n",(0,s.jsxs)(o.p,{children:["In order to use the ",(0,s.jsx)(o.code,{children:"Bool"})," query import the following:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:"import zio.elasticsearch.query.BoolQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,s.jsxs)(o.p,{children:["The ",(0,s.jsx)(o.code,{children:"Bool"})," query can be created with ",(0,s.jsx)(o.code,{children:"filter"}),", ",(0,s.jsx)(o.code,{children:"must"}),", ",(0,s.jsx)(o.code,{children:"mustNot"})," or ",(0,s.jsx)(o.code,{children:"should"})," method:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))\nval mustQuery: BoolQuery = must(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))\nval mustNotQuery: BoolQuery = mustNot(contains(field = Document.name, value = "a"))\nval shouldQuery: BoolQuery = should(startsWith(field = Document.name, value = "a"))\n'})}),"\n",(0,s.jsxs)(o.p,{children:["Once the ",(0,s.jsx)(o.code,{children:"Bool"})," query is created, you can call ",(0,s.jsx)(o.code,{children:"filter"}),", ",(0,s.jsx)(o.code,{children:"must"}),", ",(0,s.jsx)(o.code,{children:"mustNot"}),", ",(0,s.jsx)(o.code,{children:"should"}),", ",(0,s.jsx)(o.code,{children:"boost"})," and ",(0,s.jsx)(o.code,{children:"minimumShouldMatch"})," methods on it."]}),"\n",(0,s.jsxs)(o.p,{children:["If you want to add ",(0,s.jsx)(o.code,{children:"Filter"})," query to ",(0,s.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,s.jsx)(o.code,{children:"filter"})," method (you can also call ",(0,s.jsx)(o.code,{children:"filter"})," method on ",(0,s.jsx)(o.code,{children:"Bool"})," query that is created with ",(0,s.jsx)(o.code,{children:"filter"})," method):"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).filter(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,s.jsxs)(o.p,{children:["If you want to add ",(0,s.jsx)(o.code,{children:"Must"})," query to the ",(0,s.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,s.jsx)(o.code,{children:"must"})," method:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).must(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,s.jsxs)(o.p,{children:["If you want to add ",(0,s.jsx)(o.code,{children:"MustNot"})," query to the ",(0,s.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,s.jsx)(o.code,{children:"mustNot"})," method:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).mustNot(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,s.jsxs)(o.p,{children:["If you want to add ",(0,s.jsx)(o.code,{children:"Should"})," query to the ",(0,s.jsx)(o.code,{children:"Bool"})," query, you can use ",(0,s.jsx)(o.code,{children:"should"})," method:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).should(contains(field = Document.name, value = "c"))\n'})}),"\n",(0,s.jsxs)(o.p,{children:["If you want to change the ",(0,s.jsx)(o.code,{children:"_score"})," parameter, you can use the ",(0,s.jsx)(o.code,{children:"boost"})," method:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val queryWithBoost: BoolQuery = filter(contains(field = Document.name, value = "a")).boost(1.2)\n'})}),"\n",(0,s.jsxs)(o.p,{children:["If you want to change the ",(0,s.jsx)(o.code,{children:"minimum_should_match"})," parameter, you can use the ",(0,s.jsx)(o.code,{children:"minimumShouldMatch"})," method:"]}),"\n",(0,s.jsx)(o.pre,{children:(0,s.jsx)(o.code,{className:"language-scala",children:'val queryWithMinimumShouldMatch: BoolQuery = should(contains(field = Document.name, value = "a")).minimumShouldMatch(2)\n'})}),"\n",(0,s.jsxs)(o.p,{children:["You can find more information about ",(0,s.jsx)(o.code,{children:"Bool"})," query ",(0,s.jsx)(o.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-bool-query.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:o}=Object.assign({},(0,t.ah)(),e.components);return o?(0,s.jsx)(o,Object.assign({},e,{children:(0,s.jsx)(u,e)})):u(e)}},1151:(e,o,n)=>{n.d(o,{Zo:()=>r,ah:()=>c});var s=n(7294);const t=s.createContext({});function c(e){const o=s.useContext(t);return s.useMemo((()=>"function"==typeof e?e(o):{...o,...e}),[o,e])}const l={};function r({components:e,children:o,disableParentContext:n}){let r;return r=n?"function"==typeof e?e({}):e||l:c(e),s.createElement(t.Provider,{value:r},o)}}}]);