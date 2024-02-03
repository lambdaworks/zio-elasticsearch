"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[4228],{2844:(e,s,i)=>{i.r(s),i.d(s,{assets:()=>d,contentTitle:()=>a,default:()=>o,frontMatter:()=>c,metadata:()=>t,toc:()=>l});var r=i(7624),n=i(2172);const c={id:"elastic_query_has_child",title:"Has Child Query"},a=void 0,t={id:"overview/queries/elastic_query_has_child",title:"Has Child Query",description:"The HasChild query returns parent documents whose child documents match a provided query.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_has_child.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_has_child",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_child",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_has_child.md",tags:[],version:"current",frontMatter:{id:"elastic_query_has_child",title:"Has Child Query"},sidebar:"docs",previous:{title:"Geo-polygon Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_geo_polygon"},next:{title:"Has Parent Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_parent"}},d={},l=[];function h(e){const s=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,n.M)(),e.components);return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsxs)(s.p,{children:["The ",(0,r.jsx)(s.code,{children:"HasChild"})," query returns parent documents whose child documents match a provided query."]}),"\n",(0,r.jsxs)(s.p,{children:["To create a ",(0,r.jsx)(s.code,{children:"HasChild"})," query do the following:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'import zio.elasticsearch.query.HasChildQuery\nimport zio.elasticsearch.ElasticQuery._\n\nval query: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test"))\n'})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change ",(0,r.jsx)(s.code,{children:"ignore_unmapped"}),", you can use ",(0,r.jsx)(s.code,{children:"ignoreUnmapped"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val queryWithIgnoreUnmapped: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).ignoreUnmapped(true)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change ",(0,r.jsx)(s.code,{children:"inner_hits"}),", you can use ",(0,r.jsx)(s.code,{children:"innerHits"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'import zio.elasticsearch.query.InnerHits\n\nval queryWithInnerHits: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).innerHits(innerHits = InnerHits.from(5))\n'})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change ",(0,r.jsx)(s.code,{children:"max_children"}),", you can use ",(0,r.jsx)(s.code,{children:"maxChildren"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val queryWithMaxChildren: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).maxChildren(5)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change ",(0,r.jsx)(s.code,{children:"min_children"}),", you can use ",(0,r.jsx)(s.code,{children:"minChildren"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'val queryWithMinChildren: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).minChildren(2)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["If you want to change ",(0,r.jsx)(s.code,{children:"score_mode"}),", you can use ",(0,r.jsx)(s.code,{children:"scoreMode"})," method:"]}),"\n",(0,r.jsx)(s.pre,{children:(0,r.jsx)(s.code,{className:"language-scala",children:'import zio.elasticsearch.query.ScoreMode\n\nval queryWithScoreMode: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).scoreMode(ScoreMode.Max)\n'})}),"\n",(0,r.jsxs)(s.p,{children:["You can find more information about ",(0,r.jsx)(s.code,{children:"HasChild"})," query ",(0,r.jsx)(s.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-has-child-query.html",children:"here"}),"."]})]})}const o=function(e={}){const{wrapper:s}=Object.assign({},(0,n.M)(),e.components);return s?(0,r.jsx)(s,Object.assign({},e,{children:(0,r.jsx)(h,e)})):h(e)}},2172:(e,s,i)=>{i.d(s,{M:()=>a});var r=i(1504);const n={},c=r.createContext(n);function a(e){const s=r.useContext(c);return r.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}}}]);